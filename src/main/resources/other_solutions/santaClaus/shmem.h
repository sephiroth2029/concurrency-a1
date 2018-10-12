#include <sys/types.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <pthread.h>
#include <semaphore.h>
#include <sys/shm.h>
#include <sys/stat.h>

#define NUMDEER 9
#define NUMELVES 2000
#define maxWait 1

// global variables
typedef struct 
{
	int deerCnt;		// deer process running
	int elvCnt;		// elves in santa shop
	int deerNum;		// deer ID
	int totalElves;		// total elf processes running
	int waiting;		// elves waiting outside santas shop
	sem_t deerSem;		// used for protecting counters in reindeer
	sem_t sleepSanta;	// used for santa to wait untill elves need help or deers ready to fly
	sem_t hut;		// where reindeers wait untill all arrive at north pole  			
	sem_t lastDeer;		// the last deer to arrive at the north pole
	sem_t hoho; 		// used for all reindeers to wait and santa to indicate time to fly
	sem_t endOfTrip; 	// used for santa to indicate end of one trip
	sem_t elfSem; 		// used for elves waiting in the room
	sem_t solutionSem; 	// used for waiting on solution from santa
	sem_t elfSpeech; 	// used for santa to wait on the elf speech
	sem_t satisfied;  	// used by santa to weight on satisfaction from each elf
	sem_t CounterMutex; 	// protects elf counter
	sem_t elfMutex;  	// used for queing up elves outside if 3 are already in
	sem_t permission; 	// used for elf to wait for permission to speak
} shared_data;

#define SHMEM_SIZE (sizeof(shared_data))
#define SHMEM_KEY 0x6543210
