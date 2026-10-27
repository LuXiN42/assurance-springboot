package com.assurance.assurance_springboot.service;

/** Exception signalant une règle métier non satisfaite. */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public BusinessException(String message) { super(message); }
    public BusinessException(String message, Throwable cause) { super(message, cause); }
}
