package com.stateofflux.chess.model.player;

import java.util.concurrent.TimeUnit;

public class Timer {
//    private long startTime;
    private final long totalTimeAllocated;
    private long incrementAllocation;
    private long incrementStartTime;

    public static Timer create(long totalTimeInNanos) {
        return new Timer(totalTimeInNanos);
    }

    private Timer(long totalTimeInNanos) {
        // private constructor
        totalTimeAllocated = totalTimeInNanos;
    }

/*
    public void start() {
        startTime = java.lang.System.nanoTime();
    }
*/

    public void startIncrementCountdown(long timeAllocation) {
        incrementStartTime = java.lang.System.nanoTime();
        incrementAllocation = timeAllocation;
    }

    public boolean incrementIsUsed() {
        return (java.lang.System.nanoTime() - incrementStartTime) > incrementAllocation;
    }

    public long incrementTimeUsed() {
        return java.lang.System.nanoTime() - incrementStartTime;
    }

    public long getIncrementAllocation() {
        return incrementAllocation;
    }
}
