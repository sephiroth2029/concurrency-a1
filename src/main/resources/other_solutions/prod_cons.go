package main
/* producer-consumer problem in Go */
/* Code adapted from http://www.golangpatterns.info/concurrency/producer-consumer */


import ("fmt"; "os"; "strconv")

var done = make(chan bool)
var msgs = make(chan int)

func produce (reps int) {
    for i := 0; i < reps; i++ {
        msgs <- i
    }
    done <- true
}

func consume (reps int) {
    for {
      msg := <-msgs
      fmt.Println(msg)
   }
}

func main () {
   var reps int = 100
   var err error
   if len(os.Args) == 2 {
      reps, err = strconv.Atoi(os.Args[1])
      if err != nil {
         fmt.Println("Invalid parameter")
         return
      }
   }
   go produce(reps)
   go consume(reps)
   <- done
}
