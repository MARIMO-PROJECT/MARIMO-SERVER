package com.marimo.server.global.circuitbreaker;

public enum CircuitBreakerState {
    CLOSED,
    OPEN,
    HALF_OPEN
}
