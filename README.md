# Recovery for Parallel State Machine Replication (SMR)

This repository is about a new implementation for the recovery step into a parallel state machine replication.
For more information about how to run parallel SMR please visit https://github.com/parallel-SMR/library. This parallel SMR is based on BFT-Smart, so for a detailed explanation about how to configure the system, please check the BFT-SMaRt GitHub page (https://github.com/bft-smart/library).

## Overview

Under construction!

./p_smartrun.sh demo.list.ListServerDefaultSingleRecovery 0 0 10000 false lockFree

Single thread / Single Replica
./p_smartrun.sh demo.list.ListServerDefaultSingleRecovery 0 0 10000 true lockFree


process id = the process identifier
num threads = number of worker threads
initial entries = the initial list size; use 0 for the tradition sequential SMR
late scheduling? = true to use the late scheduling tecnique, false for early scheduling
graph type = the graph synchronization strategy to be used. It can be coarseLock, fineLock and lockFree (see the paper for details)


											  10000
./p_smartrun.sh demo.list.ListClientMO 1 4001 100000 0 10000 false 10 0

./p_smartrun.sh demo.list.ListClientMO 50 4001 100000 0 10000 true 50 10
./p_smartrun.sh demo.list.ListClientMO <num clients> <client id> <number of requests> <interval> <maxIndex> <parallel?> <operations per request> <conflict percent>

num clients = number of threads clients to be created in the process, each thread represents one client
client id = the client identifier
number of requests = the number of requests to be sent during the execution
interval = waiting time between requests
maxIndex = the list size, clients will use in the requests a random value ranging from 0 to maxIndex-1
parallel? = true for parallel execution, false otherwise
operations per request = number of operations contained in a request
conflict percent = the percentage of write requests in the workload