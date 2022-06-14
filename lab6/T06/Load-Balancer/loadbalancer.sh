cd src
javac loadbalancer/*.java
echo finish compile.

java loadbalancer/Server 6001 &
java loadbalancer/Server 6002 &
java loadbalancer/Server 6003 &
java loadbalancer/Server 6004 &

java loadbalancer/Loadbalancer &
sleep 2
java loadbalancer/EchoClient &
java loadbalancer/EchoClient &
java loadbalancer/EchoClient &
java loadbalancer/EchoClient &
java loadbalancer/EchoClient &
java loadbalancer/EchoClient
