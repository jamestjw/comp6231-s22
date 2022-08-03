# T10

## How to setup ?
- go in dir /docker/cluster-base and open CLI (Command prompt) , then build docker
image of cluster-base using command : 
**docker image build -t cluster-base .**
- go in dir /docker/spark-base and open CLI (Command prompt) , then build docker image
of spark-base using command : **docker image build -t spark-base .**
- go in dir /docker/spark-master and open CLI (Command prompt) , then build docker
image of spark-master using command : **docker image build -t spark-master .**
- go in dir /docker/spark-worker and open CLI (Command prompt) , then build docker
image of spark-worker using command :**docker image build -t spark-worker .**
- go in dir /docker/jupyterlab and open CLI (Command prompt) , then build docker image
of jupyterlab using command :**docker image build -t jupyterlab .**
- To compose the cluster,go in /docker and  run the Docker compose file using command:**docker-compose up**




## How to run ?
Once you run above all commands you can see the UI of Sparklab by following URLS:
- Jupyterlab : localhost:8888
- Spark master : localhost:8080
- Spark worker I: localhost:8081
 - Spark worker II : localhost:8082
