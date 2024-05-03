package nachos.threads;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.Comparator;

import nachos.machine.*;

public class Alarm {

    public static void selfTest() {
        alarmTest1();
        // 추가적인 테스트 메서드 호출
    }

    public static void alarmTest1() {
        int durations[] = {1000, 10*1000, 100*1000};
        long t0, t1;

        for (int d : durations) {
            t0 = Machine.timer().getTime();
            ThreadedKernel.alarm.waitUntil(d);
            t1 = Machine.timer().getTime();
            System.out.println("alarmTest1: waited for " + (t1 - t0) + " ticks");
        }
    }

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
        if (x <= 0) return;  // x가 0 또는 음수인 경우 즉시 리턴

        boolean intStatus = Machine.interrupt().disable();  // 인터럽트 비활성화

        long wakeTime = Machine.timer().getTime() + x;  // 쓰레드가 깨어날 시간 계산
        WaitingThread currentThread = new WaitingThread(KThread.currentThread(), wakeTime);

        sleepQueue.add(currentThread);  // 대기 중인 쓰레드 목록에 쓰레드 추가
        KThread.sleep();  // 현재 쓰레드를 블록

        Machine.interrupt().restore(intStatus);  // 인터럽트 원래 상태로 복원
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
