# How to run?
First start up the Java app, after which you should see a list of ports on which the servers are listening on

```bash
# Insert the correct port
cat single-repo-input.txt | netcat localhost 49456 
```

## Peer Discovery

```bash
echo -n "DISCOVER" | socat - udp-datagram:255.255.255.255:8888,broadcast
```

Example response:

```
ALIVE R3 65529 ALIVE R4 65530 ALIVE R5 65531 ALIVE R1 65527 ALIVE R2 65528
```