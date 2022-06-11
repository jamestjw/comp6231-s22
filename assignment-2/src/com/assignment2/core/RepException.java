package com.assignment2.core;

import java.rmi.RemoteException;

public class RepException extends RemoteException {
  public RepException(Exception e) {
      super(e.getMessage());
  }  
}
