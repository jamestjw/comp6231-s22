package com.assignment2.core;

public class RepException extends Exception {
  public RepException(Exception e) {
      super(e.getMessage());
  }  
}
