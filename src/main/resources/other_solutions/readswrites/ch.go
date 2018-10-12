package main

import (
	"fmt"
	"sync"
	"os"
	"strconv"
)

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
	h := NewChanHolder()
	var wg sync.WaitGroup
	wg.Add(2 * reps)
	
	for i := 0; i < reps; i++ {
		go func() {
			defer wg.Done()
			h.Set(strconv.Itoa(i))
		}()
	
		go func() {
			defer wg.Done()
			fmt.Println(h.Get())
		}()
	}
	wg.Wait()
}
