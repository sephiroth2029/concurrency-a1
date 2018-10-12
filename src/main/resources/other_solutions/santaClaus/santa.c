#include <sys/types.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include "shmem.h"
#include <pthread.h>
#include <semaphore.h>
#include <sys/shm.h>
#include <sys/stat.h>
#include <time.h>

void endDeliveries();
void flyingSleigh();
void distributeToys();
void sleepingSanta();
void harness(int);
void helpelves();
void flywithdeer();
void assistelves();
shared_data *p;
int i,flytime;

/*------------------------------------------------------------
Purpose:  Drives the santa process
-------------------------------------------------------------*/
int main()
{
	int shmid;
	key_t shmkey;
	int shmflg;
	srandom(time(NULL));
	int count;

	shmkey = SHMEM_KEY;
	shmflg = IPC_CREAT | S_IRUSR | S_IWUSR;
	
	shmid = shmget( shmkey, SHMEM_SIZE, shmflg);
	if(shmid != -1)
	{
		printf("Santa:\t\t Sucessfully Connected to shared memory\n");
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
	for ( ; ;)
	{
		// santa is sleeping
		sleepingSanta();
		// wait for the santa to wake up (used as a semaphore)
		sem_wait(&p->sleepSanta);
		
		// if last reindeer is arrived
		sem_wait(&p->deerSem);
		count = p->deerCnt;
		sem_post(&p->deerSem);
		
		if(count == NUMDEER)
		{
			flywithdeer();
		}

		// else there are 3 elves who require help
		else
		{	
			assistelves();
		}
	}
	return 0;
}
/*------------------------------------------------------------
Purpose:  Method is responsible for simulating 1 round trip
with reindeers
-------------------------------------------------------------*/
void flywithdeer()
{
	int i, flyTime;
	
	// harness all reindeers
	for( i = 0; i < NUMDEER; i++)
	{
		harness(i);
		//sem_post(&p->harnessed);
	}
			
	// distribute toys
	distributeToys();
	for( i = 0; i < NUMDEER; i++)
	{
		sem_post(&p->hoho);
	}

	// fly sleigh for random time
	flyingSleigh();
	flyTime = 0;
	sleep(flyTime);
	endDeliveries();
		
	// thank and set each reideer free
	for( i = 0; i < NUMDEER; i++)
	{
		sem_post(&p->endOfTrip);
	}
}
/*------------------------------------------------------------
Purpose:  Responsible for helping the group of elves that need
assistance
-------------------------------------------------------------*/
void assistelves()
{
	int i, thinkTime;
	
	// invite all waiting elves into the shop
	sem_wait(&p->CounterMutex);
	int tempCnt = p->elvCnt;
	sem_post(&p->CounterMutex);
	for (i = 0; i < tempCnt; i++)
	{
		sem_post(&p->elfSem);
	}
	
	// interview and suggest solution to each elv
	for (i = 0; i < tempCnt; i++)				
	{
		// give one elf permission to speak
		printf("Santa\t\t I am giving one elf permission to speak\n");
		sem_post(&p->permission);

		// wait for elf speech
		sem_wait(&p->elfSpeech);
				
		// santa is thinking for solution
		thinkTime = 0;
		printf("Santa\t\t I am thinking for %d seconds.\n", thinkTime);
		sleep(thinkTime);
				
		// present solution to the waiting elf
		sem_post(&p->solutionSem);
		sem_wait(&p->satisfied);
	}
	// let us know that santa has helped a elf
	helpelves();
}
/*------------------------------------------------------------
Purpose:  Helper print method
-------------------------------------------------------------*/
void helpelves()
{
	printf("Santa\t\t I have helped all the elves.\n");
}
/*------------------------------------------------------------
Purpose:  Helper print method indicating end of deleveries
-------------------------------------------------------------*/
void endDeliveries()
{
	printf("Santa\t\t We have completed all deliveries.\n");
}
/*------------------------------------------------------------
Purpose:  Helper print method indicating santa is delivering
-------------------------------------------------------------*/
void flyingSleigh()
{
	printf("Santa\t\t I am delivering presents.\n");
}
/*------------------------------------------------------------
Purpose:  Helper print method indicating the hoho
-------------------------------------------------------------*/
void distributeToys()
{
	printf("Santa\t\t HoHo lets distribute toys.\n");
}
/*------------------------------------------------------------
Purpose:  Helper print method indicating santa is sleeping
-------------------------------------------------------------*/
void sleepingSanta()
{
	printf("Santa\t\t I am sleeping until deer arive or elves need help.\n");
}
/*------------------------------------------------------------
Purpose:  Helper print method indicating santa is hanessing
-------------------------------------------------------------*/
void harness(int i)
{
	printf("Santa\t\t Harnessing Deer Number %d\n", i+1);
}
