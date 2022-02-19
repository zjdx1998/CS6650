# Report for Homework1 of CS6650


## GitHub Repo



https://github.com/zjdx1998/CS6650/tree/Homework1/Assignment1

## Server

I implemented a full-completed server which can handle every request and return response as https://app.swaggerhub.com/apis/cloud-perf/SkiDataAPI/1.16#/skiers/getSkierResortTotals expected.

![ServerUML](/Users/jeromy/Desktop/CS6650/Assignment1/Server/ServerUML.png)



## Client 1

### Design

![client1](/Users/jeromy/Desktop/CS6650/Assignment1/Clients/client1.png)

Client1 is consist of three parts:

The first part is global arguments processing, where I utilized `args4j` to parse the arguments. I also stored the global variables like `successCount`, `failureCount` which represents the total number of successful and failed requests.

The second part is a runnable class `PhaseThread`, which accepts `int startID, int endID, int startTime, int endTime, int numOfReqs, CountDownLatch latch` as parameter

![image-20220218170256559](README.assets/image-20220218170256559.png)

![image-20220218164547790](README.assets/image-20220218164547790.png)

![image-20220218164806341](README.assets/image-20220218164806341.png)

![image-20220218165053076](README.assets/image-20220218165053076.png)

![image-20220218170309003](README.assets/image-20220218170309003.png)





## Clients 2

![image-20220218171905249](README.assets/image-20220218171905249.png)



![image-20220218172258397](README.assets/image-20220218172258397.png)

![image-20220218172641323](README.assets/image-20220218172641323.png)
