#include <sys/types.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include "shmem.h"
#include <pthread.h>
#include <semaphore.h>
#include <sys/shm.h>
#include <sys/stat.h>

void flyingWithSanta(int);
void waitOnHoHo(int);
void travel2Tropic(int,int);
void travel2NorthPole(int);
void reindeertrip();

shared_data *p;
int years, count, reindeerNum, vacationtime;

/*------------------------------------------------------------
Purpose:  Main function that drives the reindeer process
-------------------------------------------------------------*/
int main(int argc, char *argv[])
{
	int shmid;
	key_t shmkey;
	int shmflg;
	
	// gets the arguments passed in from Mrs claus
	reindeerNum = atoi(argv[1]);
	years = atoi(argv[2]);	

	shmkey = SHMEM_KEY;
	shmflg = IPC_CREAT | S_IRUSR | S_IWUSR;
	
	shmid = shmget( shmkey, SHMEM_SIZE, shmflg);
	if(shmid != -1)
	{
		printf("Reindeer %d:\t Sucessfully Connected to shared memory.\n", reindeerNum);
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
	if (argc != 3)
	{
		printf("Must include a reindeer number.\n");
		exit(0);
	}
	srand(time(NULL) - reindeerNum * 2);
	
	// Start the process
	reindeertrip();
	
	return 0;
}
/*------------------------------------------------------------
Purpose:  Simultates one trip for the reindeer as it flies 
with santa
-------------------------------------------------------------*/
void reindeertrip()
{
	int i, ii;
	for (i = 0; i < years; i++ )
	{		
		printf("Current year %d\n", i);
		// enjoy tropics for 3 to 8 seconds
		vacationtime = 0
		travel2Tropic(reindeerNum, vacationtime);
		sleep(vacationtime);
	
		// travel to the north pole
		travel2NorthPole(reindeerNum);
		
		// incrament deer count	
		sem_wait(&p->deerSem);
		p->deerCnt++;
		count = p->deerCnt;
		sem_post(&p->deerSem);
		
		// if it is not the last deer wait in the hut
		if( count < NUMDEER)
		{
			printf("Reindeer %d:\t Not enough deer, going to hut.\n" , reindeerNum);
			sem_wait(&p->hut);
		}
		
		// else get all waiting reindeers and wake up santa
		else
		{
			printf("Reindeer %d:\t I am the last one to arrive. Waking up those in the hut\n" , reindeerNum );
			for(ii = 0; ii < (NUMDEER-1); ii++)
			{
				sem_post(&p->hut);
			}
			printf("Reindeer %d:\t All deer have arrived, we are coming out of the hut\n" , reindeerNum );
			sem_post(&p->sleepSanta);
		}

		// wait for santa hoho 
		waitOnHoHo(reindeerNum);
		sem_wait(&p->hoho);

		// fly with santa
		flyingWithSanta(reindeerNum);

		// wait for the end of trip
		sem_wait(&p->endOfTrip);
		sem_wait(&p->deerSem);
		p->deerCnt--;
		sem_post(&p->deerSem);
	}
	printf("Reindeer %d:\t I am done: Completed %d trips\n", reindeerNum, i);

}
/*------------------------------------------------------------
Purpose:  Helper print function indicating reindeer is flying
-------------------------------------------------------------*/
void flyingWithSanta(int num)
{
	printf("Reindeer %d:\t I am flying with santa.\n",num);
}
/*------------------------------------------------------------
Purpose: Helper print function indicating reindeer is waiting 
for hoho
-------------------------------------------------------------*/
void waitOnHoHo(int num)
{
	printf("Reindeer %d:\t I am waiting for santa's HoHoHo.\n",num);
}
/*------------------------------------------------------------
Purpose:  Helper print function indicating reindeer is flying
to tropics
-------------------------------------------------------------*/
void travel2Tropic(int num, int sleep)
{
	printf("Reindeer %d:\t I am traveling to the tropics for %d seconds\n", num, sleep);
}
/*------------------------------------------------------------
Purpose:  Helper print function indicating reindeer is flying 
to north pole
-------------------------------------------------------------*/
void travel2NorthPole(int num)
{
	printf("Reindeer %d:\t I am traveling to the North Pole.\n", num);
}
