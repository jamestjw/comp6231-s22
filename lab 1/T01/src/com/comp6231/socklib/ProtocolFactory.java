
/*
 *  ProtocolFactory.java
 *
 *  This interface is part of socklib core.
 *
 *  (C) 2022 Ali Jannatpour <ali.jannatpour@concordia.ca>
 *
 *  This code is licensed under GPL.
 *
 */

package com.comp6231.socklib;

import java.net.Socket;

public interface ProtocolFactory {
    SocketProtocol create(Socket s, ListenerInfo li);
}
