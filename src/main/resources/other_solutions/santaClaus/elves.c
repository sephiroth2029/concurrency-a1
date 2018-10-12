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

void getHelp();
void enterRoom(int);
void alert(int);
void enter(int);
void presentproblem(int);
void satisfied(int);

int count, total, ii, wait, elfCount;
shared_data *p;
int elfNum, i, waitTime;
int ntoys;
int failrate;
int needHelp;

/*------------------------------------------------------------
Purpose:  Main function that drives the elf process
-------------------------------------------------------------*/
int main(int argc, char *argv[])
{
	int shmid;
	key_t shmkey;
	int shmflg;

	// get arguments
	elfNum = atoi(argv[1]);
	ntoys = atoi(argv[2]);
	failrate = atoi(argv[3]);

	shmkey = SHMEM_KEY;
	shmflg = IPC_CREAT | S_IRUSR | S_IWUSR;
	
	shmid = shmget( shmkey, SHMEM_SIZE, shmflg);
	if(shmid != -1)
	{
		printf("Elf: %d\t\t Sucessfully Connected to shared memory.\n", elfNum);
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
	if (argc != 4)
	{
		printf("Must include a elf number.\n");
		exit(0);
	}
	srand(time(NULL) - elfNum * 2);
	
	// perform iterations for amount of toys needed
	for (i = 0; i < ntoys; i++)
	{
		// make toys for 1 to 4 seconds
		waitTime = (random() % 4) + 1;
		sleep(waitTime);
	
		// generate random number between 1 and 100 to find percentage of error
		needHelp = failrate;
		if (needHelp <= failrate)
		{
			printf("Elf %d:\t\t I need help\n", elfNum);
			getHelp();
		}
	}
	// decrement elf and check to see if after current elves leaves how many in shop
	sem_wait(&p->CounterMutex);
	p->totalElves--;
	total = p->totalElves;
	wait = p->waiting;
	sem_post(&p->CounterMutex);
	
	printf("Elf %d:\t\t I finished and made %d toys.  Elves Remaining: %d\n",elfNum, i, p->totalElves);
	
	if ((total == wait) && (total != 0))
	{
		printf("Elf %d:\t\t I am waking santa up and alerting other elves since %d are waiting and total elves left is %d\n", 
			elfNum, p->elvCnt, p->totalElves);
		sem_post(&p->sleepSanta);
	}
	return 0;
}			
/*------------------------------------------------------------
Purpose:  Function makes elf wait outside shop untill seat 
becombes avaialbe then gets help from santa.
-------------------------------------------------------------*/
void getHelp()
{
	// wait for empty seat outside
	sem_wait(&p->elfMutex);
				
	// say that elf is entering santa room
	enterRoom(elfNum);
			
	// incrament elf count in waiting room
	sem_wait(&p->CounterMutex);
	p->elvCnt++;
	p->waiting++;
	count = p->elvCnt;
	total = p->totalElves;
	wait = p->waiting;
	sem_post(&p->CounterMutex);
		
	// if the weighting room has max holding elves or the amount of elves left
	if ((wait == maxWait) || (wait == p->totalElves))
	{
		// alert other elves to get ready
		alert(elfNum);
		// wake up santa
		sem_post(&p->sleepSanta);
	}

	// enter the shop
	sem_wait(&p->elfSem);
	enter(elfNum);
			
	sem_wait(&p->CounterMutex);
	p->waiting--;
	sem_post(&p->CounterMutex);
			
	// wait for permission to speak and present problem
	sem_wait(&p->permission);

	// present problem and let santa know you are done
	presentproblem(elfNum);
	sem_post(&p->elfSpeech);
			
	// wait for santa solution and then respond with acknowledement
	sem_wait(&p->solutionSem);
	satisfied(elfNum);
	sem_post(&p->satisfied);

	// leave the santa shop
	sem_wait(&p->CounterMutex);
	p->elvCnt--;
	elfCount = p->elvCnt;
	sem_post(&p->CounterMutex);

	// check to see if any more elves are waiting if not delcare seats open
	if (elfCount == 0)
	{
		printf("Elf %d:\t\t I have last elf leaving and santa may sleep\n",elfNum);	
				
		// allow the next group to go in
		for (ii = 0; ii < maxWait ; ii++)
		{
			sem_post(&p->elfMutex);
		}
	}
}
/*------------------------------------------------------------
Purpose:  Helper print function indicating elf is entering 
santas waiting room.
-------------------------------------------------------------*/
void enterRoom(int num)
{
	printf("Elf %d:\t\t Entering santa's waiting room\n", num);
}
/*------------------------------------------------------------
Purpose:  Helper print function indicating elf is altering 
other elfs to get ready.
-------------------------------------------------------------*/
void alert(int num)
{
	printf("Elf %d:\t\t Alerting all other elves to get ready\n", num);
}
/*------------------------------------------------------------
Purpose:  Helper print function indicating elf is entering 
santas shop.
-------------------------------------------------------------*/
void enter(int num)
{
	printf("Elf %d:\t\t Entering the shop\n", num);
}
/*------------------------------------------------------------
Purpose:  Helper print function indicating elf's problem.
-------------------------------------------------------------*/
void presentproblem(int num)
{
	printf("Elf %d:\t\t My problem is blablabla\n", num);	
}
/*------------------------------------------------------------
Purpose:  Helper print function indicating elf is satisfied 
with solution. 
-------------------------------------------------------------*/
void satisfied(int num)
{
	printf("Elf %d:\t\t I am satisfied with the solution\n", num);	
}
