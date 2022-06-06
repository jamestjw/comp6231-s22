# How to run?
First start up the Java app, after which you should see a list of ports on which the servers are listening on, e.g.
```
Servers are listening on ports [50494, 50495, 50496, 50497, 50498].

Press hit ENTER if you wish to stop the servers. Note that the service may NOT stop immediately.
```

To run some sample commands,
```bash
# Suppose that we connect to the first server
cat single-repo-input.txt | netcat localhost 50494
```

## Peer Discovery

```bash
echo -n "DISCOVER" | socat - udp-datagram:255.255.255.255:8888,broadcast
```

Example response:

```
ALIVE R3 65529 ALIVE R4 65530 ALIVE R5 65531 ALIVE R1 65527 ALIVE R2 65528
```

## Documentation
Detailed documentation of the application can be found in the `documentation.pdf` file.