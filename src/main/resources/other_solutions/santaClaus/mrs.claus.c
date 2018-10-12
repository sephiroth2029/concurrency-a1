#include <sys/types.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include "shmem.h"
#include <signal.h>
#include <pthread.h>
#include <semaphore.h>
#include <sys/shm.h>
#include <sys/stat.h>

void initialize();
void createSanta();
void createDeers();
void createElves();

shared_data *p;
pid_t santa;
pid_t elves[NUMELVES];
pid_t reindeers[NUMDEER];
int years, ntoys, fail;

/*------------------------------------------------------------
Purpose:  Drives the Mrs.Claus program. 
-------------------------------------------------------------*/
int main(int argc, char * argv[])
{
	int i;
	int shmid, status;
	key_t shmkey;
	int shmflg;
	
	if (argc != 4) 
	{ 
	    printf("Invalid arguments. Program will terminate\n");
	    exit(0);
	}

	// get the data from the arguments
	years = atoi(argv[1]);
	ntoys = atoi(argv[2]);
	fail = atoi(argv[3]);

	shmkey = SHMEM_KEY;
	shmflg = IPC_CREAT | S_IRUSR | S_IWUSR;
	
	shmid = shmget( shmkey, SHMEM_SIZE, shmflg);
	if(shmid != -1)
	{
		printf("Mrs.Claus\t\t Sucessfully Connected to shared memory\n");
	}
	else
	{
		printf("Error connecting to shared memory\n");
		exit(-1);
	}
	p = (shared_data *) shmat( shmid, NULL, 0);
	if ( p == (shared_data *) -1)
	{
		printf("\n Failed to attach to shared memory\n");
	}
	
	// initialize the intial variables from shared memory
	initialize();

	// create the proper processes
	createSanta();
	createDeers();
	createElves();

	// wait for the reindeers and elves to finish
	for (i = 0; i < NUMDEER; i++)
		waitpid(reindeers[i], &status, 0);

	for (i = 0; i < NUMELVES; i++)
		waitpid(elves[i], &status, 0);

	// kill the santa process
	kill(santa, SIGKILL);

	// destroy the shared memory 
	shmctl(shmid, IPC_RMID, NULL);
	
	return 0;
}
/*------------------------------------------------------------
Purpose:  Creates the reindeer processes
-------------------------------------------------------------*/
void createDeers()
{
	int i;
	char param[50];
	char yearParam[50];
	
	for( i = 0; i < NUMDEER; i++)
	{
		reindeers[i] = fork();
		
		// creation failed
		if (reindeers[i] < 0 )
		{
			printf("Forking Raindeer Failed");
			exit(-1);
		}
		else if (reindeers[i] == 0)
		{
			// prepare the arguments
			sprintf(param, "%d", i + 1);
			sprintf(yearParam, "%d", years);
			printf("Mrs.Claus\t Reindeer %d has been created\n", i + 1);
			
			if( execl("./reindeer","reindeer", &param, &yearParam, (char*)0) < 0)
			{
				printf("Making reindeer %d Failed\n", i);
				exit(-1);
			}
		}
	}

}
/*------------------------------------------------------------
Purpose:  Creates the elves processes
-------------------------------------------------------------*/
void createElves()
{	
	int i;
	char param[50];
	char ntoysParam[50];
	char failParam[50];
	
	for( i = 0; i < NUMELVES; i++)
	{
		elves[i] = fork();
		
		// if creation fails
		if (elves[i] < 0 )
		{
			printf("Forking elves failed.");
			exit(-1);
		}
		else if (elves[i] == 0)
		{
			// prepare the arguments
			sprintf(param, "%d", i + 1);
			sprintf(ntoysParam, "%d", ntoys);
			sprintf(failParam, "%d", fail);
			printf("Mrs.Claus\t Elf Number %d has been created\n", i + 1);
			if( execl("./elves","elves", &param, &ntoysParam, &failParam, (char*)0) < 0)
			{
				printf("Making elf number %d Failed\n", i);
				exit(-1);
			}
		}
	}
}
/*------------------------------------------------------------
Purpose:  Creates the santa process
-------------------------------------------------------------*/
void createSanta()
{
	santa = fork();
	if (santa < 0 )
	{
		printf("Forking Failed");
		exit(-1);
	}
	else if(santa == 0 )
	{
		printf("Mrs.Claus\t Santa Claus has been Created\n");
		if( execl("./santa","santa", NULL) < 0)
		{
			printf("Making Santa Failed\n");
			exit(-1);
		}
	}
}
/*------------------------------------------------------------
Purpose:  Intialize all the shared memory used by all the 
processes
-------------------------------------------------------------*/
void initialize()
{
	sem_init(&p->deerSem, 1, 1);
	sem_init(&p->hut, 1, 0);
	sem_init(&p->lastDeer, 1, 0);
	sem_init(&p->hoho, 1, 0);
	sem_init(&p->endOfTrip, 1, 0);
	sem_init(&p->solutionSem, 1, 0);
	sem_init(&p->elfSem, 1, 0);
	sem_init(&p->elfMutex, 1, maxWait);
	sem_init(&p->satisfied, 1, 0);
	sem_init(&p->CounterMutex, 1, 1);
	sem_init(&p->sleepSanta, 1, 0);
	sem_init(&p->elfSpeech, 1, 0);
	sem_init(&p->permission, 1, 0);
	
	p->elvCnt = 0;
	p->deerCnt = 0;
	p->totalElves = NUMELVES;
	p->waiting = 0;

}
