package nachos.threads;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.Comparator;

import nachos.machine.*;

public class Alarm {
    private PriorityBlockingQueue<WaitingThread> sleepQueue
            = new PriorityBlockingQueue<>(10, new WaitingThreadComparator());

    public Alarm() {
        Machine.timer().setInterruptHandler(this::timerInterrupt);
    }

    public void timerInterrupt() {
        long machineTime = Machine.timer().getTime();
        System.out.println("--- Timer Interrupt at: " + machineTime + " for " + KThread.currentThread());

        wakeUpThreads(machineTime);
        KThread.yield();
    }

    private void wakeUpThreads(long currentTime) {
        while (hasThreadToWake(currentTime)) {
            WaitingThread thread = sleepQueue.poll();
            thread.wakeUp();
        }
    }

    private boolean hasThreadToWake(long currentTime) {
        WaitingThread nextThread = sleepQueue.peek();
        return nextThread != null && nextThread.getWakeTime() <= currentTime;
    }

    public void waitUntil(long x) {
        if (x <= 0) return;

        long wakeTime = Machine.timer().getTime() + x;
        System.out.println(KThread.currentThread() + " waits until " + wakeTime);

        WaitingThread currentThread = new WaitingThread(KThread.currentThread(), wakeTime);
        sleepQueue.add(currentThread);

        boolean intStatus = Machine.interrupt().disable();
        KThread.sleep();
        Machine.interrupt().restore(intStatus);
    }

    private class WaitingThread {
        private KThread thread;
        private long wakeTime;

        public WaitingThread(KThread thread, long wakeTime) {
            this.thread = thread;
            this.wakeTime = wakeTime;
        }

        public long getWakeTime() {
            return wakeTime;
        }

        public void wakeUp() {
            System.out.println(thread + " wakes up at " + Machine.timer().getTime() + " (scheduled " + wakeTime + ")");
            thread.ready();
        }
    }

    private static class WaitingThreadComparator implements Comparator<WaitingThread> {
        @Override
        public int compare(WaitingThread t1, WaitingThread t2) {
            return Long.compare(t1.getWakeTime(), t2.getWakeTime());
        }
    }
}
