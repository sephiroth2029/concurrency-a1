make: santa.c reindeer.c mrs.claus.c shmem.h
	gcc -o santa santa.c  -pthread
	gcc -o reindeer reindeer.c  -pthread
	gcc -o elves elves.c -pthread
	gcc -o mrs.claus mrs.claus.c -pthread

clean:
	rm elves ; rm santa ; rm mrs.claus ; rm reindeer 
