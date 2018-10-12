# Santa-Claus-Problem
Solution to the Santa Claus problem suggested by John Trono.  

This solution presented in C programming language takes Santa, Elves, and reindeers
as separate processes all forked (created) by Mrs. Claus.  They all run toghether concurrently.

The progam uses concepts of multi processes, forking, shared memory, and semaphores to accomplish the task.

The orginal problem can be found online.

To run: 
	Download all C files.
	Compile all programs using the make file given.
	Run Mrs Claus using 3 arguments (number of years, number of toys, and fail rate)

NOTE:  sometimes the program gets deadlocked ~5%-10%
