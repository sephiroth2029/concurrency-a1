# **Concurrency (CSC-564)**
## **Assignment 1**

### **Introduction**
This project is meant to be a showcase of the classical concurrency problems, their solutions and compare the features of each solution to these problems. The analysis will be performed in terms of:
1. **Correctness**. The solution will be evaluated in terms of how well it handles common problems, such as deadlocks, starvation and race conditions; and atomicity, considering that new requirements should involve changes in as few places as possible, having a single place to change as the best solution.
2. **Comprehensibility**. Given that the criteria for this may be subjective, the code will be measured by the number of lines of code, assuming that succinct code is preferred by programmers; and required structures or mechanisms unrelated to the actual synchronization schemes (e.g. checked exceptions in Java's concurrent API).
3. **Performance**. This is, arguably, the most interesting part of the analysis. Whenever it was possible, equal conditions were given to the comparable solutions. To measure the average time per batch processed, mostly 1000 operations per batch, the Java Microbenchmarking Harness ([JMH][2]) provided by the JDK9 was used. In all cases, the CPU usage per core will be recorded using [nmon][1], in order to determine if the synchronization mechanism and the language are taking full advantage of the underlying hardware.

#### **Comparison framework**
In this section it will be described how the comparison framework was built and how it integrates JMH and nmon.

Every language provides mechanisms to measure, at different levels, the performance of the programs build with it. However, depending on the maturity of the language and its popularity, the quality, scope and standarization of such tools vary. Due to this constraint, a framework was built, which allows to test and measure programs in different languages seamlessly and obtain results in the same format. The entry point for the test suite is `ca.uvic.concurrency.gmmurguia.execution.Execution`. This class reads the configuration defined in the file `config.yaml` and, using the configuration parameters, invokes each program directly on the operating system, wrapping the invocation in Java code.

The code that executes the final program is annotated with JMH stereotypes, which allows to specify fine-grained behaviors on the benchmarking. As an example, let us consider the following configuration:

```java
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 0, time = 1)
@Measurement(iterations = 3, batchSize = 1)
@State(Scope.Benchmark)
@Fork(value = 5)
public class Execution {
```

In the previous configuration, the average time needed per operation will be the target. The number of iterations corresponds to the number of executions of the program. Each execution will be run inside a fork, to isolate the performance of the executing method. The warmup, which is a defined numer of non-measured executions in order to take advantage of the JVM's optimizations, is disabled to provide a fair environment.

The programs were executed in two ways:
1. A single execution, starting nmon through a shell program and stopping it at the end of the execution.
2. Repetitive executions, in order to obtain average times and provide a wider view of the actual performance.

All programs were executed for 1000 repetitions, with the exception of the 6th which was also executed for 100 and 10000.

#### **Testing environment**

The tests were exeucted in an isolated workstation with the following characteristics:

```
Host bridge: Intel Corporation 2nd Generation Core Processor Family DRAM Controller (rev 09)
	Subsystem: Dell Device 04ad
	Control: I/O- Mem+ BusMaster+ SpecCycle- MemWINV- VGASnoop- ParErr- Stepping- SERR- FastB2B- DisINTx-
	Status: Cap+ 66MHz- UDF- FastB2B+ ParErr- DEVSEL=fast >TAbort- <TAbort- <MAbort+ >SERR- <PERR- INTx-
	Latency: 0
PCI bridge: Intel Corporation Xeon E3-1200/2nd Generation Core Processor Family PCI Express Root Port (rev 09) (prog-if 00 [Normal decode])
	Control: I/O+ Mem+ BusMaster+ SpecCycle- MemWINV- VGASnoop- ParErr- Stepping- SERR- FastB2B- DisINTx-
	Status: Cap+ 66MHz- UDF- FastB2B- ParErr- DEVSEL=fast >TAbort- <TAbort- <MAbort- >SERR- <PERR- INTx-
	Latency: 0, Cache Line Size: 64 bytes
RAM:
    Total Width: 64 bits
	Data Width: 64 bits
	Size: 2x2048 MB
	Form Factor: DIMM
	Set: None
	Locator: ChannelA-DIMM1
	Bank Locator: BANK 1
	Type: DDR3
```

Nmon was executed with a clean system, obtaining the following results:

![Missing graph][39]

### **Problems**

In this section the problems will be discussed, together with the results. It is important to mention that most of the problems were analyzed differently. The detail will be explained at the beginning of each problem.

#### **1. Barbershop**
##### **Description**
```
A barbershop consists of a waiting room with n chairs, and the
barber room containing the barber chair. If there are no customers
to be served, the barber goes to sleep. If a customer enters the
barbershop and all chairs are occupied, then the customer leaves
the shop. If the barber is busy, but chairs are available, then the
customer sits in one of the free chairs. If the barber is asleep, the
customer wakes up the barber (Downey 2014, 121).
```
##### **Analysis performed**
For this problem, a C program found on the [Internet][3] was compared to a Java program developed for this course. The difference lies not only on the languages chosen, but in the primitives used to solve the problem. The C code uses PThreads and mutexes, while the Java code uses green threads and monitor locks.

The modified C program can be found at [this link][4], while the Java code is [here][5].

##### **Relevance to code bases**
This problem may be seen in middle-ware components, where a large number of clients try to access the services of a shared component, for example in SOA architectures. In this case, the services exposed may serve only a limited number of clients and should discard incoming requests when the limit was reached. In this type of architectures is common to create a threadpool when the service starts, and to attend each incoming request in an independent thread.

##### **Raw Results**
There is a log file with the entire contents of the test. However, due to the length of such file, it won't be uploaded to the version control repository. The relevant portions of the log are as follows:

```
2018-10-11 14:06:13,168 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG Shutdown hook enabled. Registering a new one.
2018-10-11 14:06:13,168 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db] started OK.
0.863 s/op
Iteration   2: 0.841 s/op
Iteration   3: 0.863 s/op
Iteration   4: 0.854 s/op
Iteration   5: 0.851 s/op
Iteration   6: 0.859 s/op
Iteration   7: 0.832 s/op
Iteration   8: 0.844 s/op
Iteration   9: 0.847 s/op
Iteration  10: 0.871 s/op
2018-10-11 14:06:30,241 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]
2018-10-11 14:06:30,241 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]...


Result "ca.uvic.concurrency.gmmurguia.execution.Execution.execute":
  0.860 ±(99.9%) 0.010 s/op [Average]
  (min, avg, max) = (0.832, 0.860, 0.928), stdev = 0.019
  CI (99.9%): [0.850, 0.869] (assumes normal distribution)
```

In the previous excerpt it is displayed the execution of the final forks for the C solution, and the metrics obtained.

```
2018-10-11 14:06:31,060 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG Shutdown hook enabled. Registering a new one.
2018-10-11 14:06:31,060 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db] started OK.
0.951 s/op
Iteration   2: 0.891 s/op
Iteration   3: 0.923 s/op
Iteration   4: 0.913 s/op
Iteration   5: 0.927 s/op
Iteration   6: 0.964 s/op
Iteration   7: 0.924 s/op
Iteration   8: 0.937 s/op
Iteration   9: 0.934 s/op
Iteration  10: 0.939 s/op
2018-10-11 14:06:49,686 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]
2018-10-11 14:06:49,687 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]...

# Run progress: 40.00% complete, ETA 00:14:10
# Fork: 2 of 5
```

In the previous excerpt it is displayed the execution of one of the forks for the Java solution, and the partial metrics obtained.

```
# Run complete. Total time: 00:25:17

Benchmark                                                                                                                                                                                                                             (command)  Mode  Cnt  Score   Error  Units
...
Execution.execute                                                                                                                                                                                                     other_solutions/barber     avgt   50  0.860 ± 0.010   s/op
Execution.execute                                                                                                 java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.barbershop.Barbershop false 1000  avgt   50  0.930 ± 0.008   s/op
```

This log excerpt contains the summary data for this problem.

![Missing graph][6]

The previous graph represents the CPU usage per core during the execution of the C program. 

![Missing graph][32]

The previous graph represents the CPU usage per core during the execution of the Java program.

##### **Analysis results**
The results are summarized in the following table:

|  | Correctness | Comprehensibility | Performance |
|------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| C | The solution works as expected and does not seem to run into deadlocks or race conditions. During the several executions, it was able to withstand changes to the sleep timers. Therefore, as far as this analysis went, the solution is correct. | The mechanism to start threads and manage the mutex can be daunting at first. However, after experimenting with the code, the purpose and mechanics become clearer. The author of the code is clearly experienced in the development of C programs. The core problem with the mutex mechanisms are that it is difficult to maintain the code base. For a problem as trivial as this one, it almost 150 lines of code were needed, and it is necessary to invest several minutes to understand how every piece fits. This becomes a problem when new requirements are needed, therefore increasing the risk of introducing bugs when the code is altered. | This solution is clearly faster and, as pthreads are used, it also utilizes the cores available in the system. |
| Java | The code did not expose any problems during its execution. It can be therefore assumed that it is correct under the current circumstances. Several random sleeps were put into place, to try to obtain different scenarios and no problems were found. | One aspect in which the Java code is clearer and easier to maintain lies in the primitive used. A single lock object was needed, whereas the C code requires three mutex instances and two condition variables. The object oriented nature of Java also allows to easily distinguish the roles and responsibilities of each of the actors. Finally, the Java code is constructed with less than 100 lines, despite the several places where randomization was introduced. | It is interesting to notice the enormous difference in performance time between the C and the Java code. The main reason for this difference is that, since the Java code is started as an independent JVM, the initialization time affected severely its performance. |

For this problem, it can be concluded that Java offers a straightforward mechanism to synchonize the customers and the barber. Still, if performance is the main objective, then C and mutexes are a compelling alternative.

#### **2. Producers-consumers**
##### **Description**
```
Producers create items of some kind and add them to a data structure; consumers
remove the items and process them.
• While an item is being added to or removed from the buffer, the buffer is
in an inconsistent state. Therefore, threads must have exclusive access to
the buffer.
• If a consumer thread arrives while the buffer is empty, it blocks until a
producer adds a new item (Downey 2014, 56).
```
##### **Analysis performed**
For this problem, a Go program [shared][7] in Go Language Patterns was compared to a [Java program][8] from Geeks for Geeks, an informal technical website. The purpose was to compare a simple solution of Go using channels against Java's monitor locks.

The modified Go program can be found at [this link][9], while the Java code is [here][10].

##### **Relevance to code bases**
As the Downey described, this can be very relevant in an event-driven environment. Such environments are mostly found in front-end development. For example, in Angular 2 it is very common to consume REST services and propagate the results to the view components, in order to have the data displayed always synchronized with the back-end, to simulate a local interaction. However, if these events were not properly synchronized, then the user might be presented with stale data or worse, the view changes might be lost from the final storage.

##### **Raw Results**
The relevant portions of the log are as follows:

```
2018-10-11 13:58:45,152 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG Shutdown hook enabled. Registering a new one.
2018-10-11 13:58:45,153 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db] started OK.
0.375 s/op
Iteration   2: 0.182 s/op
Iteration   3: 0.178 s/op
Iteration   4: 0.178 s/op
Iteration   5: 0.182 s/op
Iteration   6: 0.181 s/op
Iteration   7: 0.176 s/op
Iteration   8: 0.181 s/op
Iteration   9: 0.181 s/op
Iteration  10: 0.177 s/op
2018-10-11 13:58:56,000 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]
2018-10-11 13:58:56,000 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]...

# Run progress: 6.67% complete, ETA 00:21:48
# Fork: 2 of 5
```

In the previous excerpt it is displayed the execution of one of the forks for the Go solution, and the partial metrics obtained.

```
2018-10-11 13:57:23,402 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG Shutdown hook enabled. Registering a new one.
2018-10-11 13:57:23,403 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db] started OK.
0.772 s/op
Iteration   2: 0.779 s/op
Iteration   3: 0.779 s/op
Iteration   4: 0.781 s/op
Iteration   5: 0.750 s/op
Iteration   6: 0.786 s/op
Iteration   7: 0.777 s/op
Iteration   8: 0.739 s/op
Iteration   9: 0.777 s/op
Iteration  10: 0.789 s/op
2018-10-11 13:57:38,879 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]
2018-10-11 13:57:38,880 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]...

# Run progress: 1.11% complete, ETA 00:24:14
# Fork: 2 of 5
```

In the previous excerpt it is displayed the execution of one of the forks for the Java solution, and the partial metrics obtained.

```
Result "ca.uvic.concurrency.gmmurguia.execution.Execution.execute":
  0.707 ±(99.9%) 0.005 s/op [Average]
  (min, avg, max) = (0.681, 0.707, 0.736), stdev = 0.011
  CI (99.9%): [0.702, 0.713] (assumes normal distribution)


# Run complete. Total time: 00:25:17

Benchmark                                                                                                                                                                                                                             (command)  Mode  Cnt  Score   Error  Units
Execution.execute                                                                                          java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.prodscons.ProducersConsumers false 1000  avgt   50  0.775 ± 0.008   s/op
Execution.execute                                                                                                                                                                                      go run other_solutions/prod_cons.go 1000  avgt   50  0.183 ± 0.014   s/op
```

This log excerpt contains the summary data for this problem.

![Missing graph][11]

The previous graph represents the CPU usage per core during the execution of the Go program. 

![Missing graph][12]

The previous graph represents the CPU usage per core during the execution of the Java program.

##### **Analysis results**
The results are summarized in the following table:

|  | Correctness | Comprehensibility | Performance |
|------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Go | The code did not display any kind of problems during testing. From a step-by-step verification it became clear that under no circumstances the code would fall into a race condition or a deadlock. Even the last instruction of the main is an idiom of Go to wait until the previous go routines finished. | The Go solution is simple and elegant, requiring about 25 lines of code for the main part. The code is simple to follow and the objectives are clear even without background on the problem. Probably, the only obscure part for a person just introduced to the language would be the use of the -> and <- operators, but even that is simple enough to associate with the mechanics of the solution. | The Go code was more than 3 times faster than the Java code. It is also relevant to notice that the CPU usage for the Go program did not change much in terms of user usage. However, the wait usage for the third core was big. Only half of the cores display wait times. |
| Java | The program works fine in terms of synchronization. In this case, the author of the code used wait() and notify() to ensure that both actions remain withing the boundaries of the problem. | While the Java's monitor object can be very powerful, it lacks the clarity that Go's channels provide in this problem. It is not immediately clear why the synchronization object is a reference to the class instance. Also, since both the consumer and producer definition are within the same class, the separation of concerns is not well defined. All of these problems hinder the maintainability of this code. | In this case as well, the JVM initialization had a negative effect in the execution of the program. Additional to that, several instructions are inside the synchronized block, which may be another cause of the poor performance of the Java solution. It is important to notice that the Java code relied heavily on the 8th core, while the Go program used consistently all cores available. |

For this problem, the Go's code is clearly preferrable to the Java's code. It is easier to understand and extend, it less likely to introduce bugs and the performance is superior.

#### **3. Readers-writers**
##### **Description**
```
Readers and writers execute different code before entering the critical section. The synchronization constraints are:
1. Any number of readers can be in the critical section simultaneously.
2. Writers must have exclusive access to the critical section.
In other words, a writer cannot enter the critical section while any other
thread (reader or writer) is there, and while the writer is there, no other thread may enter (Downey 2014, 56).
```
##### **Analysis performed**
In this problem, a [Go program][13] covering several extra cases was compared to a Java program developed for this assignment. This problem may look similar to the producers and consumers, however, after writing the Java solution and exploring the Go code it was clear that the suble changes in the problem domain have a profound impact in the considerations required to build the program.

The modified Go program can be found at [this link][14], while the Java code is [here][15].

##### **Relevance to code bases**
Big data systems are a clear application of this problem. On such systems, data is constantly being updated and retrieved. Due to this, it is essential a mechanism that allows fast and reliable access to the data, and still allowing updates to it. Additionally, since distributed systems are a common approach to handle the vast amount of data used by big data systems, the synchronization between distributed components would be anothe use case for the readers-writers problem.

##### **Raw Results**
The relevant portions of the log are as follows:

```
2018-10-11 14:01:02,327 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG Shutdown hook enabled. Registering a new one.
2018-10-11 14:01:02,328 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db] started OK.
0.176 s/op
Iteration   2: 0.177 s/op
Iteration   3: 0.175 s/op
Iteration   4: 0.182 s/op
Iteration   5: 0.172 s/op
Iteration   6: 0.177 s/op
Iteration   7: 0.175 s/op
Iteration   8: 0.174 s/op
Iteration   9: 0.178 s/op
Iteration  10: 0.174 s/op
2018-10-11 14:01:12,910 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]
2018-10-11 14:01:12,910 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]...

# Run progress: 17.78% complete, ETA 00:17:45
# Fork: 2 of 5
```

In the previous excerpt it is displayed the execution of one of the forks for the Go solution, and the partial metrics obtained.

```
2018-10-11 13:59:43,015 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG Shutdown hook enabled. Registering a new one.
2018-10-11 13:59:43,016 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db] started OK.
0.935 s/op
Iteration   2: 1.047 s/op
Iteration   3: 0.999 s/op
Iteration   4: 1.015 s/op
Iteration   5: 1.035 s/op
Iteration   6: 0.941 s/op
Iteration   7: 1.018 s/op
Iteration   8: 0.977 s/op
Iteration   9: 1.110 s/op
Iteration  10: 1.055 s/op
2018-10-11 13:59:57,021 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]
2018-10-11 13:59:57,021 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]...

# Run progress: 12.22% complete, ETA 00:18:29
# Fork: 2 of 5
```

In the previous excerpt it is displayed the execution of one of the forks for the Java solution, and the partial metrics obtained.

```
Result "ca.uvic.concurrency.gmmurguia.execution.Execution.execute":
  0.707 ±(99.9%) 0.005 s/op [Average]
  (min, avg, max) = (0.681, 0.707, 0.736), stdev = 0.011
  CI (99.9%): [0.702, 0.713] (assumes normal distribution)


# Run complete. Total time: 00:25:17

Benchmark                                                                                                                                                                                                                             (command)  Mode  Cnt  Score   Error  Units
...
Execution.execute                                                                                         java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.readerswriters.ReadersWriters false 1000  avgt   50  1.004 ± 0.029   s/op
Execution.execute  go run other_solutions/readswrites/6713e855b431a0c067afea4b74cbf504/ch.go other_solutions/readswrites/6713e855b431a0c067afea4b74cbf504/chan_holder.go other_solutions/readswrites/6713e855b431a0c067afea4b74cbf504/holder.go  avgt   50  0.177 ± 0.001   s/op
```

This log excerpt contains the summary data for this problem.

![Missing graph][16]

The previous graph represents the CPU usage per core during the execution of the Go program. 

![Missing graph][17]

The previous graph represents the CPU usage per core during the execution of the Java program.

##### **Analysis results**
The results are summarized in the following table:

|  | Correctness | Comprehensibility | Performance |
|------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| Go | The author of the code implemented tests to verify even timeouts. During my tests to understand the mechanics of the code I changed some parts of the use of the code and it remained working without errors. Therefore, it can be conclude that the code is correct as the problem is described. | This code is harder to follow than the code for producers and consumers, as there are more restrictions on the code. The read/write/close cases make it more difficult to read, understand and maintain the code, but they also provide more flexibility to it. It would be worth noting that the author of the code wrote a generic interface to be used and, additional to the channels solution, there is also a mutex solution. The code became so complex that it made sense to split it into files and group them into a package. | The Go code was almost 5 times faster than the Java code. In this program the wait usage was spread across all the cores, showing how the synchronization mechanisms have a direct effect on the usage of the cores. Again, the user usage for the Go program was consistent across all CPUs. |
| Java | The only problem found in this solution was a situation of starvation. Since the readers may enter the critical section concurrently, it may prevent writers from accesing it until all readers finish. | The code for the solution is not very extensive, around 150 lines in total. The solution tries to emulate the lightswitch discussed by Downey. The major problem is that the synchronization blocks are in two different files, which may lead to bugs while modifying the program. | Apart from being slower than the Go program, it is interesting to notice that wait times were much lower than those of the Go implementation. The CPU usage improved in this execution, compared to the previous problem. Still, the user usage is high probably due to the JVM initialization. |

Both solutions cover different areas. The Go solution provides flexibility and the usage of the resulting interface is simple and compelling. However, the code is more difficult to follow and prone to the introduction of new bugs. Java's code is simpler, but it still leaves open the introduction of bugs by having the critical sections in different files.

#### **4. Santa claus**
##### **Description**
```
Stand Claus sleeps in his shop at the North Pole and can only be
awakened by either (1) all nine reindeer being back from their vacation
in the South Pacific, or (2) some of the elves having difficulty
making toys; to allow Santa to get some sleep, the elves can only
wake him when three of them have problems. When three elves are
having their problems solved, any other elves wishing to visit Santa
must wait for those elves to return. If Santa wakes up to find three
elves waiting at his shop’s door, along with the last reindeer having
come back from the tropics, Santa has decided that the elves can
wait until after Christmas, because it is more important to get his
sleigh ready. (It is assumed that the reindeer do not want to leave
the tropics, and therefore they stay there until the last possible moment.)
The last reindeer to arrive must get Santa while the others
wait in a warming hut before being harnessed to the sleigh.
Here are some addition specifications:
• After the ninth reindeer arrives, Santa must invoke prepareSleigh, and
then all nine reindeer must invoke getHitched.
• After the third elf arrives, Santa must invoke helpElves. Concurrently,
all three elves should invoke getHelp.
• All three elves must invoke getHelp before any additional elves enter
(increment the elf counter).
Santa should run in a loop so he can help many sets of elves. We can assume
that there are exactly 9 reindeer, but there may be any number of elves (Downey 2014, 56).
```
##### **Analysis performed**
The two implementations to be analyzed are a Java implementation written for this assignment, and a [C program][18] found in GitHub. This problem was not possible to analyze in the same context as the other problems, due to problems with the code. The C code, as it is described in the README, deadlocks after some iterations. This restriction didn't allow the code to be analysed by using the JMH.

The modified C program can be found at [this link][19], while the Java code is [here][20].

##### **Relevance to code bases**
This is a very particular problem, as the description is modestly simple but the solution is a challenge. Mordechai (1997) published an [article][21] in which he describes the mechanics behind the solution of the problem in ADA 95 using "protected objects" which, according to Mordechai, are similar to monitors. On the Internet there are solutions available in a few languages (e.g., [Haskel][22], [VCL][23], and the C solution used for this comparison), however few of them have the code available for testing. This non-trivial problem may arise in the development of parallel components for industry services. In particular, it may be related to banking applications, where autonomous systems such as mainframe programs, database stored procedures and user applications need to work together to perform financial operations (e.g., back office processing).

##### **Raw Results**
The relevant portions of the log are as follows:

```
2018-10-11 18:34:17,685 main DEBUG Shutdown hook enabled. Registering a new one.
2018-10-11 18:34:17,686 main DEBUG LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@7b9a4292] started OK.
# JMH version: 1.19
# VM version: JDK 1.8.0_181, VM 25.181-b13
# VM invoker: /usr/java/jdk1.8.0_181-amd64/jre/bin/java
# VM options: <none>
# Warmup: <none>
# Measurement: 10 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: ca.uvic.concurrency.gmmurguia.execution.Execution.execute
# Parameters: (command = other_solutions/Santa-Claus-Problem/mrs.claus 100 1000 10)

# Run progress: 0.00% complete, ETA 00:00:50
# Fork: 1 of 5
```

In the previous excerpt it is displayed the execution of first of the forks for the C solution.

![Missing graph][24]

The previous graph represents the CPU usage per core during the execution of the C program. 

Unfortunately the Java program is currently under a deadlock, which prevents the comparison and dispay of raw results.

##### **Analysis results**
The results are summarized in the following table:

|  | Correctness | Comprehensibility | Performance |
|------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| C | The program deadlocks at some point of execution, killing even the terminal process. The cause of the deadlock was not clear during the analysis of the code, and a focused analyses would be required to find and correct the problem. | The code becomes difficult to follow, as the complexity of the Santa Claus problem is higher than the other problems. The structure of the code helps to create a mental figure of the mechanics of the solution. Still, making updates or corrections to the code have a high risk of introducing bugs. | There is no comparation in this problem, since the Santa Claus problem for Java is still under development. However, for the C program it is interesting to note that all CPU cores were used and the percentage between the most used and the least used is less than 10%. |
| Java | During the development of the program, several problems arose. In particular: 1. On ocassions the reindeer would stay deadlocked, despite the other processes staying executing normally. 2. At some point the service process (Santa Claus) didn't finish, despite the reindeers and elves having their execution. 3. Under certain conditions, the reindeers came back from vacations and were hitched again. This created a deadlock where the counters were out of sync. A CountDownLatch was used to prevent this problem.  Due to this, the confidence of the program being correct is not high. | Initially the process concern's were perfectly distinguishable and the synchronization primitives were kept at a minimum. However, after running into runtime problems, the code started to grow making it very difficult to understand and debug. Java offers useful tools to work with the problems discovered while implementing this program, but every redesign should be carefully thought in order to avoid unnecessary additional complexity. | Under development. |

It would be very enlightening to review more solutions for this program, as the problems underlying the simple description are complex and require of careful thought.

#### **5. Unisex bathroom**
##### **Description**
```
There cannot be men and women in the bathroom at the same time.
• There should never be more than three employees squandering company
time in the bathroom.
Of course the solution should avoid deadlock. For now, though, don’t worry
about starvation. You may assume that the bathroom is equipped with all the
semaphores you need (Downey 2014, 56).
```
##### **Analysis performed**
In this case, only implementations in Java are compared. In the [code][26] there are four implementations:

1. A solution where there is no starvation, as groups are restricted from entering all in one batch.
2. A solution where there is a lower possibility of starvation, but it may still happen if the scheduler wakes readers every time.
3. A solution where the code never finishes, since there are no more elements from one group and the other group already consumed the available slots.
4. The simplest solution where starvation is highly likely to occur.

For this comparison only the solutions 1 and 4 will be considered.

##### **Relevance to code bases**
This could be used in the context of data science libraries, as it is desirable to parallelize similar operations and build upon the results of those operations. For example, for matrices multiplication and transpose matrix operation it could be scheduled partially the transpose operation and as it progresses, proceed partially with the multiplication, hence improving the performance. The problem is similar to the readers and writers, and because of it the application field could be shared.

##### **Raw Results**
The relevant portions of the log are as follows:

```
2018-10-11 14:01:59,579 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG Shutdown hook enabled. Registering a new one.
2018-10-11 14:01:59,580 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db] started OK.
0.879 s/op
Iteration   2: 0.849 s/op
Iteration   3: 0.912 s/op
Iteration   4: 0.859 s/op
Iteration   5: 0.857 s/op
Iteration   6: 0.861 s/op
Iteration   7: 0.858 s/op
Iteration   8: 0.886 s/op
Iteration   9: 0.840 s/op
Iteration  10: 0.861 s/op
2018-10-11 14:02:16,925 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]
2018-10-11 14:02:16,925 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]...

# Run progress: 23.33% complete, ETA 00:16:07
# Fork: 2 of 5
```

In the previous excerpt it is displayed the execution of one of the forks for the non-starving solution, and the partial metrics obtained.

```
2018-10-11 14:03:30,635 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG Shutdown hook enabled. Registering a new one.
2018-10-11 14:03:30,635 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db] started OK.
0.826 s/op
Iteration   2: 0.852 s/op
Iteration   3: 0.854 s/op
Iteration   4: 0.894 s/op
Iteration   5: 0.878 s/op
Iteration   6: 0.841 s/op
Iteration   7: 0.885 s/op
Iteration   8: 0.891 s/op
Iteration   9: 0.824 s/op
Iteration  10: 0.840 s/op
2018-10-11 14:03:47,826 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]
2018-10-11 14:03:47,827 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]...

# Run progress: 28.89% complete, ETA 00:15:48
# Fork: 2 of 5
```

In the previous excerpt it is displayed the execution of one of the forks for the solution with starvation, and the partial metrics obtained.

```
Result "ca.uvic.concurrency.gmmurguia.execution.Execution.execute":
  0.707 ±(99.9%) 0.005 s/op [Average]
  (min, avg, max) = (0.681, 0.707, 0.736), stdev = 0.011
  CI (99.9%): [0.702, 0.713] (assumes normal distribution)


# Run complete. Total time: 00:25:17

Benchmark                                                                                                                                                                                                                             (command)  Mode  Cnt  Score   Error  Units
Execution.execute                                                                                         java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.unisexbathroom.UnisexBathroom false 1000  avgt   50  0.868 ± 0.011   s/op
Execution.execute                                                                                 java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.unisexbathroom.UnisexBathroomStarving false 1000  avgt   50  0.860 ± 0.010   s/op
```

This log excerpt contains the summary data for this problem.

![Missing graph][27]

The previous graph represents the CPU usage per core during the execution of the non-starving program. 

![Missing graph][28]

The previous graph represents the CPU usage per core during the execution of the starving program.

##### **Analysis results**
The results are summarized in the following table:

|  | Correctness | Comprehensibility | Performance |
|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------|
| Starvation | Starvation is the biggest problem for this code. It was trivial to find an scenario where a single group gained control of the critial section and prevented the other group from entering. The major problem is that even if the critial section is almost empty, the entrance of a new element of the same group regains control of it. | The code is not trivial and it takes some effort to understand the reasoning behind the synchronization blocks introduced. It is not a clean solution and it is less readable because of the fact that threads in the wait state may me waken up by the scheduler even if the timing is not right, thus having to write extra code to handle this particular case. | The starving program consumes fewer resources than the non-starving one, as can be seen in the CPU usage graphs. This is the result of the mechanism implemented to be fair. However, if there are more writers than readers it may be better to omit the non-starvation mechanism. |
| No starvation | The code was thoroughly tested and bugs were attended. The program was executed with randomized waits and creation of male and female groups without displaying signs of race conditions or deadlocks. | The code is more complex than the solution where starvation is allowed, since extra mechanisms to ensure that both groups were allowed to enter the critical section. Also, it became difficult to ensure high cohesion as the synchronized blocks require from a common lock. | The starving and non-starving times are very similar, only 0.08 of difference in the final score. Which means that handling starvation did not increase the execution time, but attending starvation will improve the overall performance of the execution. |

#### **6. Concurrent Queue**
##### **Description**
Queues are data structures where the first element added (enqueued) should be the first to be removed (dequeued). When multiple threads share a Queue it is important to implement a mechanism which allows consistent operations. For this problem, two strategies to achieve this goal will be analysed: lock-free operations, which optimistically assume that no other thread affected the queue but still compare the state at the end to ensure consistency; and locking operations, which rely on synchonized methods and blocks.

##### **Analysis performed**
A lock-free implementation obtained from [a forum][29] was adapted and compared agains built-in implementations:

1. [ConcurrentLinkedQueue][31], which uses lock-free operations. After exploring the Java's source code it was found that it uses native, lock-free methods such as `public final native boolean compareAndSwapObject(Object var1, long var2, Object var4, Object var5);`.
2. [LinkedBlockingQueue][34], which uses explicit locks to coordinate the operations. One example of the locks is as follows:

```Java
private void signalNotFull() {
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }
```

3. [LinkedList][30], which is an implementation of Queue where no synchronization is done. The idea of comparing against this class is to determine if there was a benefit in operating on the queue through threads.

Finally, the analysis was conducted with results obtained while executing 100, 1000 and 10000 concurrent operations (adding/removing elements and traversing the queue the specified number of times).

##### **Relevance to code bases**
Concurrent data structures are the building pillars of large problems. A queue may be used to keep track of requests performed to a web service and to ensure that each request is serviced without altering the order in which they arrived. For example, this may be useful in submissions to a competing platform for programmers, where the first submission is given a higher mark.

##### **Raw Results**
The relevant portions of the log are as follows (all are for 1000 operations):

```
2018-10-11 14:13:24,285 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG Shutdown hook enabled. Registering a new one.
2018-10-11 14:13:24,286 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db] started OK.
0.795 s/op
Iteration   2: 0.796 s/op
Iteration   3: 0.793 s/op
Iteration   4: 0.794 s/op
Iteration   5: 0.799 s/op
Iteration   6: 0.802 s/op
Iteration   7: 0.807 s/op
Iteration   8: 0.787 s/op
Iteration   9: 0.779 s/op
Iteration  10: 0.818 s/op
2018-10-11 14:13:40,246 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]
2018-10-11 14:13:40,246 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]...

# Run progress: 67.78% complete, ETA 00:07:44
# Fork: 2 of 5
```

In the previous excerpt it is displayed the execution of one of the forks for the lock-free implementation adapted from the online forum, and the partial metrics obtained.

```
2018-10-11 14:14:47,916 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG Shutdown hook enabled. Registering a new one.
2018-10-11 14:14:47,916 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db] started OK.
2.430 s/op
Iteration   2: 2.334 s/op
Iteration   3: 2.225 s/op
Iteration   4: 2.232 s/op
Iteration   5: 2.249 s/op
Iteration   6: 2.217 s/op
Iteration   7: 2.302 s/op
Iteration   8: 2.165 s/op
Iteration   9: 2.246 s/op
Iteration  10: 2.300 s/op
2018-10-11 14:15:10,634 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]
2018-10-11 14:15:10,635 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]...

# Run progress: 73.33% complete, ETA 00:06:28
# Fork: 2 of 5
```

In the previous excerpt it is displayed the execution of one of the forks for the ConcurrentLinkedQueue implementation, and the partial metrics obtained.

```
2018-10-11 14:16:44,603 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG Shutdown hook enabled. Registering a new one.
2018-10-11 14:16:44,604 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db] started OK.
2.206 s/op
Iteration   2: 2.158 s/op
Iteration   3: 2.219 s/op
Iteration   4: 2.290 s/op
Iteration   5: 2.209 s/op
Iteration   6: 2.126 s/op
Iteration   7: 2.246 s/op
Iteration   8: 2.213 s/op
Iteration   9: 2.229 s/op
Iteration  10: 2.252 s/op
2018-10-11 14:17:06,773 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]
2018-10-11 14:17:06,774 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]...

# Run progress: 78.89% complete, ETA 00:05:16
# Fork: 2 of 5
```

In the previous excerpt it is displayed the execution of one of the forks for the LinkedBlockingQueue implementation, and the partial metrics obtained.

```
2018-10-11 14:18:39,201 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG Shutdown hook enabled. Registering a new one.
2018-10-11 14:18:39,202 ca.uvic.concurrency.gmmurguia.execution.Execution.execute-jmh-worker-1 DEBUG LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db] started OK.
0.693 s/op
Iteration   2: 0.693 s/op
Iteration   3: 0.714 s/op
Iteration   4: 0.707 s/op
Iteration   5: 0.713 s/op
Iteration   6: 0.692 s/op
Iteration   7: 0.710 s/op
Iteration   8: 0.692 s/op
Iteration   9: 0.708 s/op
Iteration  10: 0.695 s/op
2018-10-11 14:18:53,258 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]
2018-10-11 14:18:53,258 pool-2-thread-1 DEBUG Stopping LoggerContext[name=5c647e05, org.apache.logging.log4j.core.LoggerContext@6c4af7db]...

# Run progress: 84.44% complete, ETA 00:03:57
# Fork: 2 of 5
```

In the previous excerpt it is displayed the execution of one of the forks for the LinkedList implementation, and the partial metrics obtained.

```
Result "ca.uvic.concurrency.gmmurguia.execution.Execution.execute":
  0.707 ±(99.9%) 0.005 s/op [Average]
  (min, avg, max) = (0.681, 0.707, 0.736), stdev = 0.011
  CI (99.9%): [0.702, 0.713] (assumes normal distribution)


# Run complete. Total time: 00:25:17

Benchmark                                                                                                                                                                                                                             (command)  Mode  Cnt  Score   Error  Units
...
Execution.execute                                                                                          java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.concurrentqueue.QueueTest lock-free 100  avgt   50  0.730 ± 0.006   s/op
Execution.execute                                                                                         java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.concurrentqueue.QueueTest concurrent 100  avgt   50  0.778 ± 0.007   s/op
Execution.execute                                                                                           java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.concurrentqueue.QueueTest blocking 100  avgt   50  0.777 ± 0.008   s/op
Execution.execute                                                                                               java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.concurrentqueue.QueueTest sync 100  avgt   50  0.704 ± 0.005   s/op
Execution.execute                                                                                         java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.concurrentqueue.QueueTest lock-free 1000  avgt   50  0.794 ± 0.006   s/op
Execution.execute                                                                                        java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.concurrentqueue.QueueTest concurrent 1000  avgt   50  2.250 ± 0.031   s/op
Execution.execute                                                                                          java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.concurrentqueue.QueueTest blocking 1000  avgt   50  2.208 ± 0.030   s/op
Execution.execute                                                                                              java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.concurrentqueue.QueueTest sync 1000  avgt   50  0.703 ± 0.006   s/op
Execution.execute                                                                                        java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.concurrentqueue.QueueTest lock-free 10000  avgt   50  0.880 ± 0.010   s/op
Execution.execute                                                                                             java -cp assignment1-1.0-SNAPSHOT-jar-with-dependencies.jar ca.uvic.concurrency.gmmurguia.a1.concurrentqueue.QueueTest sync 10000  avgt   50  0.707 ± 0.005   s/op
```

This log excerpt contains the summary data for this problem for 100, 1000 and 10000 executions.

![Missing graph][35]

The previous graph represents the CPU usage per core during the execution of the non-blocking program. 

![Missing graph][36]

The previous graph represents the CPU usage per core during the execution of the ConcurrentLinkedQueue program.

![Missing graph][37]

The previous graph represents the CPU usage per core during the execution of the LinkedBlockingQueue program. 

![Missing graph][38]

The previous graph represents the CPU usage per core during the execution of the LinkedList program.

##### **Analysis results**
The results are summarized in the following table:

|  | Correctness | Comprehensibility | Performance |
|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------|
| Custom lock-free | The code was able to withstand 10000 operations without displaying errors. At this point the code showed a consistent response, however further testing would be advised before ensuring it is free of undesired effects. | The code is not easy to read. Once the concept is clear, it is simple to understand the code. However, lock-free mechanisms should be crafted carefully. It is not the recommended approach for a novice as there is a high risk of writing code with sensitive bugs. | The lock free solution performs closer to the serial one. Also it is relevant to notice that the wait usage is kept at a minimum compared to the concurrent solution. The first core was used more than the others, but all of the cores were used. |
| ConcurrentLinkedQueue | This code is in the JDK, so the confidence of its correctness should be high. However, during testing, the program executing the operations on this structure apparently deadlocked consistently in the 10000 operations test. Since the testing environment is very limited, it may be a problem unrelated to the implementation. However, it always exposed the same problem on the same batch of operations. | The source code is difficult to read and understand, but the resulting interface is very simple. Probably the native operations obscure to a certain extent the readability of the code. | This implementation has the highest wait usage of all, which is strange since it is a non-blocking solution as well. Its average times were very close to the LinkedBlockingQueue. |
| LinkedBlockingQueue | Similarly to the ConcurrentLinkedQueue, this implementation is provided by the JDK and it exposed the same problem: it deadlocked in the 10000 batch. | The use of locks provide a fine grained control over the behavior of the synchronization, but it becomes difficult to extend until the whole code was read, since there are multiple methods where the locks are signaled and unlocked. | This implementation had the most balanced usage of cores. Despite the fact that it is a blocking solution, the wait usage is not large and the average time is very close to the ConcurrentLinkedQueue. |
| LinkedList | Since there are no threads involved, it is guaranteed that the code works without problems. | It is important to remark how, despite tryig to achieve the same result, the implementation of the non-thread-safe queue is much simpler than the other solutions. As the variations are limited, it is very simple to understand. | This implementation has a low usage of CPU. It used all of the cores, though three of them were used significantly less than the others. For all cases, this implementation was the fastest. But as the number of operations increases, the gap between this and the non-blocking implementation shortens. |

It is important to mention that, as the iterations increased, the performance gap between the sync and async implementions shortened. Which allows to conclude that for queues that will iterate over less than 10000 elements, a serial implementation is to be preferred.

### **Conclussions**

The following table contains the highlights of the analysis of the problems for the languages shown:

|  | Correctness | Comprehensibility | Performance |
|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| C | The code becomes difficult to evaluate in terms of where could race conditions or deadlocks appear and, since it works at a lower level, its effects in the executing environment result in a complicated language for synchronization. | The code is usually dense and not straightforward, requiring complete attention of the programmer. Bugs are simple to introduce and hard to debug. | It is very fast and, since it manages low level control, the underlying resources are fully utilized, such as processor cores. |
| Go | In general, it is easier to write parallel programs that work well. There are some pitfalls as the requirements evolve, such as those exposed in the readers/writers problem. However, the solutions tend to be clear and elegant. | Channels provide a powerful abstraction that is easy to understand and apply. Since the language provides idioms that make code shorter, it is easier to read the code and actually understand why it works and how it could be extended. | Programs in Go run fast. I did not compare C and Go, but it would be relevant to analyse the results. |
| Java | For simple solutions, the mechanisms provided by the language allow to easily notice problems and ensure that the code is working as expected. However, as programs and synchronization problems grow, it becomes difficult to determine if the program will work as expected. The biggest problem I found was that the execution exposed problems, and those problems had to be addressed while trying to ensure the rest of the synchronization was still working.  | The primitives and components offered by the languages make are very helpful for certain problems. However, composing and mixing them increase the risk of unwanted conditions which are difficult to trace and understand. | Java's code suffered greatly from the JVM initialization time. In theory, Java's code should perform closely to C and better than Go, but that was not reflected in this analysis. In order to obtain better results, it would be necessary to isolate the actual execution. |

As a future work consideration, instantiating the programs by using Java's runtime is not the best approach to benchmark across languages. A better approach would be to link that code to native calls in Java and test everything in a leveled ground.

### **Notes**
The graphs obtained with nmon cover much more, the complete graphs can be found [here][33].

### **References**
Downey,  Allen B. 2014. The Little Book of Semaphores: Createspace Independent Pub, 2nd edition.



[1]: http://nmon.sourceforge.net/pmwiki.php
[2]: http://openjdk.java.net/projects/code-tools/jmh/
[3]: https://www.dreamincode.net/forums/topic/47521-barber-shop-problem/
[4]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/other_solutions/barber.c
[5]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/java/ca/uvic/concurrency/gmmurguia/a1/barbershop/Barbershop.java
[6]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/barbershop/c/charts/pantera/CPU_Balance.png?raw=true
[7]: http://www.golangpatterns.info/concurrency/producer-consumer
[8]: https://www.geeksforgeeks.org/producer-consumer-solution-using-threads-java/
[9]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/other_solutions/prod_cons.go
[10]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/java/ca/uvic/concurrency/gmmurguia/a1/prodscons/ProducersConsumers.java
[11]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/prds_cons/go/charts/pantera/CPU_Balance.png?raw=true
[12]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/prds_cons/java/charts/pantera/CPU_Balance.png?raw=true
[13]: https://medium.com/stupid-gopher-tricks/more-powerful-synchronization-in-go-using-channels-f4a1c3242ed0
[14]: https://github.com/sephiroth2029/concurrency-a1/tree/master/src/main/resources/other_solutions/readswrites
[15]: https://github.com/sephiroth2029/concurrency-a1/tree/master/src/main/java/ca/uvic/concurrency/gmmurguia/a1/readerswriters
[16]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/readers_writers/go/charts/pantera/CPU_Balance.png?raw=true
[17]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/readers_writers/java/charts/pantera/CPU_Balance.png?raw=true
[18]: https://github.com/affansheikh95/Santa-Claus-Problem
[19]: https://github.com/sephiroth2029/concurrency-a1/tree/master/src/main/resources/other_solutions/santaClaus
[20]: https://github.com/sephiroth2029/concurrency-a1/tree/master/src/main/java/ca/uvic/concurrency/gmmurguia/a1/santaclaus
[21]: https://pdfs.semanticscholar.org/575b/d506b6531db2133eb50f9256d235f788ea81.pdf
[22]: https://www.schoolofhaskell.com/school/advanced-haskell/beautiful-concurrency/4-the-santa-claus-problem
[23]: http://www.connectivelogic.co.uk/santa.asp
[24]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/santa/c/charts/pantera/CPU_Balance.png?raw=true
[25]: http://www.connectivelogic.co.uk/santa.asp
[26]: https://github.com/sephiroth2029/concurrency-a1/tree/master/src/main/java/ca/uvic/concurrency/gmmurguia/a1/unisexbathroom
[27]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/unisex_bathroom/java/UnisexBathroom/charts/pantera/CPU_Balance.png?raw=true
[28]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/unisex_bathroom/java/Starving/charts/pantera/CPU_Balance.png?raw=true
[29]: https://codereview.stackexchange.com/questions/224/thread-safe-and-lock-free-queue-implementation
[30]: https://docs.oracle.com/javase/8/docs/api/java/util/LinkedList.html
[31]: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentLinkedQueue.html
[32]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/barbershop/java/charts/pantera/CPU_Balance.png?raw=true
[33]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/
[34]: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/LinkedBlockingQueue.html
[35]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/concurrent_queue_1000/java/lock-free/charts/pantera/CPU_Balance.png?raw=true
[36]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/concurrent_queue_1000/java/concurrent/charts/pantera/CPU_Balance.png?raw=true
[37]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/concurrent_queue_1000/java/blocking/charts/pantera/CPU_Balance.png?raw=true
[38]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/concurrent_queue_1000/java/sync/charts/pantera/CPU_Balance.png?raw=true
[39]: https://github.com/sephiroth2029/concurrency-a1/blob/master/src/main/resources/nmon_results/initial/charts/pantera/CPU_Balance.png?raw=true