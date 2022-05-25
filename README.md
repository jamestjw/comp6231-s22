# comp6231-s22


## Peer Discovery

```bash
echo -n "DISCOVER" | socat - udp-datagram:255.255.255.255:8888,broadcast
```

Example response:

```
ALIVE R3 65529 ALIVE R4 65530 ALIVE R5 65531 ALIVE R1 65527 ALIVE R2 65528
```