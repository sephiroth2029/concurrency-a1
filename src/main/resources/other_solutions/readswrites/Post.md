# More Powerful Synchronization in Go, using Channels

Here's a common predicament: you have a value, e.g., some string or number or
struct, that can be read and written by many goroutines, and you want to make
sure readers and writers don't collide -- that readers get the most recently
written value, and no other, and that writers don't write the value while a
reader is reading it.

You may have an idea in your head about how you would approach this problem, and
your idea may be completely correct. But I want to try to level up your Go-fu
and see what it looks like to solve this problem using channels, Go's powerful
concurrency primitive.

First, let's imagine a simple interface which we'll implement below. This
example guards reads and writes of a string, but it could easily guard any other
value:

```go
// Holder holds a string value in a concurrency-safe manner.
type Holder interface {
  Get() string
  Set(string)
}
```

The canonical solution is to use a `Mutex`. In Go these are provided by
[`sync.Mutex`](https://golang.org/pkg/sync#Mutex). Here's an example using a
`sync.Mutex` to provide concurrency safety.

```go
type mutexHolder struct {
  val string
  mu sync.Mutex
}

// NewMutexHolder returns a Holder backed by a sync.Mutex.
func NewMutexHolder() Holder {
  return &mutexHolder{}
}

func (h *mutexHolder) Get() string {
  h.mu.Lock()
  defer h.mu.Unlock()
  return h.val
}

func (h *mutexHolder) Set(s string) {
  h.mu.Lock()
  defer h.mu.Unlock()
  h.val = s
}
```

This is very simple, and in many cases it may give you exactly what you want. If
that's the case, you should just use a `Mutex` and get back to solving the real
problem.

But sometimes, you want additional logic and flexibility around your
synchronization, and using a `Mutex`, while easy, doesn't make it easy to add
more synchronization features. Mutexes are simple, but can also be limiting.

--------------------------------------------------------------------------------

Here's an alternative solution, which guards reading from happening concurrently
with writing, using Channels instead of Mutexes.

```go
type chanHolder struct {
  setValCh chan string
  getValCh chan string
}

// NewChanHolder returns a new Holder backed by Channels.
func NewChanHolder() Holder {
  h := chanHolder {
    setValCh: make(chan string),
    getValCh: make(chan string),
  }
  go h.mux()
  return h
}

func (h chanHolder) mux() {
  var value string
  for {
    select {
    case value = <-h.setValCh: // set the current value.
    case h.getValCh <- value: // send the current value.
    }
  }
}

func (h chanHolder) Get() string {
  return <-h.getValCh
}

func (h chanHolder) Set(s string) {
   h.setValCh <- s
}
```

This is a bit more code than the `Mutex`-based solution, but at its heart it's
not magic. The key is in the mux method, which is spawned in a goroutine in
`NewChanHolder`.

`mux` runs an infinite loop with a `select` statement inside it, which will
block until either a value is received from `setValCh` or sent to `getValCh`.
This ensures that either the value is read or written each time through the
`for` loop. This is where the synchronization comes from, the `select` statement
ensures that value is only being written to or read from at a given time.

The implementation of `Set` sends the new value to the setter channel, and `Get`
receives from the getter channel.

The value is local to `mux`'s loop, which means that it's impossible for another
method on `Holder` to modify it. This is a useful contrast to the `Mutex`-based
solution, where a new method on `mutexHolder` can modify the value and forget to
acquire the lock first.

--------------------------------------------------------------------------------

But let's imagine a scenario where you want to guarantee to callers that `Get`
always returns a non-empty string, even if you have to wait for it. The value
it's holding might be an auth token, and callers requesting the token should be
able to guarantee that they've gotten a valid non-empty token before proceeding.
In this case, callers to `Get` should block until a value can be provided by
some other goroutine's call to `Set`.

To do this, we need to handle the empty string as a special value. This is not
so simple to implement if all you're using is a `Mutex`. But with the
channel-backed implementation, this isn't terribly difficult at all, it's just
another special case in mux:

```go
func (h chanHolder) mux() {
  val value string
  for {
    // if the value is empty, only accept setting.
    if value == "" {
      value = <- h.setValCh
      continue
    }
    // once the value is non-empty, it can be set or gotten as normal.
    select {
    case value = <-h.setValCh: // set the current value.
    case h.getValCh <- value: // send the current value.
    }
  }
}
```

With this simple tweak to `mux`, calls to `Get` will block until a value has
been previously `Set`. Callers can call `Set("")` to clear the value and force
callers of `Get` to block until Set is called with a non-empty string.

NB: The `continue` in the above code is important; if a caller calls `Set("")`
while the value is already empty, we want to start over with the for loop, and
not proceed down to the `select` statement, which might provide values to
callers of `Get`.

--------------------------------------------------------------------------------

Some callers of `Get` might not want to their call to block _forever_. In the
case of an auth token, you may want to just fail if you can't acquire the token
after some amount of time. This is also pretty easy to implement with our
channel-backed `Holder`, with another `select` statement and
[`time.After`](https://godoc.org/time#After).

```go
// ErrTimeout is the error returned by GetWithTimeout if the value
// was not provided before the given timeout.
var ErrTimeout = errors.New("timeout waiting for value")

// GetWithTimeout attempts to get the value, or returns ErrTimeout
// if getting it takes too long.
func (h chanHolder) GetWithTimeout(d time.Duration) (string, error) {
  select {
  case <-time.After(d):
    return "", ErrTimeout
  case v := <-h.getValCh:
    return v, nil
  }
}
```

This sort of feature is not so easy to add if you’ve only got a `Mutex` guarding
your value.

--------------------------------------------------------------------------------

But wait! Eagle-eyed readers might notice that this implementation keeps a
goroutine running indefinitely, forever. In doing so, we have committed a
cardinal sin of Go: we [started a goroutine without knowing how it will
stop](https://dave.cheney.net/2016/12/22/never-start-a-goroutine-without-knowing-how-it-will-stop).

If you expect the `Holder` to live for the duration of your program, then the
goroutine will stop when the program stops, and we don't need to do anything
more. On the other hand, if you expect your program to stop setting and getting
values at some point, you will want to clean up the Holder when it's not needed
anymore. Let's add a `Close` method to clean up.

```go
type chanHolder struct {
  setValCh chan string
  getValCh chan string
  closeCh  chan struct{}
}

// NewChanHolder returns a new Holder backed by Channels.
func NewChanHolder() Holder {
  h := chanHolder {
    setValCh: make(chan string),
    getValCh: make(chan string),
    closeCh:  make(chan struct{}),
  }
  go h.mux()
  return h
}

func (h chanHolder) mux() {
  val value string
  for {
    // if the value is empty, only accept setting, or closing.
    if value == "" {
      select {
      case <-h.closeCh: // we also need to handle closing here!
        close(h.setValCh)
        close(h.getValCh)
        return
      case value = <- h.setValCh:
        continue
    }
    select {
    case value = <-h.setValCh: // set the current value.
    case h.getValCh <- value: // send the current value.
    case <-h.closeCh: // closing, time to clean up!
      close(h.setValCh)
      close(h.getValCh)
      return
    }
  }
}

func (h chanHolder) Close() {
  close(h.closeCh)
}
```

This version adds a third channel, which is used to signal to `mux` that it's
time to clean up. This channel doesn't need to pass any information, it just
needs to signal a single event, which we can do just by closing the channel.
Because it doesn't need to carry any information, it can be a `chan struct{}`.

`mux`'s `select` statement receives from `closeCh` in addition to `getValCh` and
`setValCh`, and when it sees the channel is closed, it closes the other two
channels and returns from `mux`.

With this implementation, if any callers call `Set` after `Close`, it will panic
("send on closed channel"). If any callers call `Get` after `Close`, it will
return an empty string, because they're receiving from a closed channel. If any
callers call `Close` multiple times, it will panic ("close of a closed
channel"). With a little more code you can change this behavior however you
want.

--------------------------------------------------------------------------------

Using channels to synchronize data access can unlock a new level of power and
control, but obviously also requires a fair bit of care to make sure you're not
leaking resources. With great power comes great responsibility. Future Work
Using channels for synchronization opens up even more possibilities than those
described here. Consider how you might implement these features:

*   Don't panic if `Set` is called after `Close`, return an error instead, or do
    nothing.
*   Don't return an empty string if `Get` is called after `Close`, return an
    error instead, or return the last value that was set.
*   Don't panic if `Close` is called multiple times, return an error instead, or
    do nothing.
*   Implement `SetWithExpiry(s string, d time.Duration)`, which sets the value
    and clears it after the duration, unless another value is Set in the
    meantime. If the same value is set again before the value expires, just
    update the expiration time. What kinds of cases might such a `Holder` be
    useful?
*   Implement `chanHolder` using only one internal channel. What would the
    channel communicate, and how would `mux` use it? How might this change make
    other future changes easier?
*   Consider the difference in the performance of the `Mutex`-based solution and
    the channel-based solution. Write a benchmark to determine which is faster
    under which use cases. If you plan to use this pattern in a hot code path,
    the flexibility of the channel-based solution may not be worth the
    performance penalty. As usual, you should profile your code to determine
    hotspots before optimizing code, especially if it makes the code harder to
    understand.
