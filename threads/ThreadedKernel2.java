package nachos.threads;

import nachos.machine.*;

/**
 * A multi-threaded OS kernel.
 */
public class ThreadedKernel2 extends Kernel {
    /**
     * Allocate a new multi-threaded kernel.
     */
    public ThreadedKernel2() {
        super();
    }

    /**
     * Initialize this kernel. Creates a scheduler, the first thread, and an
     * alarm, and enables interrupts. Creates a file system if necessary.
     */
    public void initialize(String[] args) {
        // set scheduler
        String schedulerName = Config.getString("ThreadedKernel.scheduler");
        scheduler = (Scheduler) Lib.constructObject(schedulerName);

        // set fileSystem
        String fileSystemName = Config.getString("ThreadedKernel.fileSystem");
        if (fileSystemName != null)
            fileSystem = (FileSystem) Lib.constructObject(fileSystemName);
        else if (Machine.stubFileSystem() != null)
            fileSystem = Machine.stubFileSystem();
        else
            fileSystem = null;

        // start threading
        new KThread(null);

        alarm = new Alarm();

        Machine.interrupt().enable();
    }

    /**
     * Test this kernel. Tests the <tt>KThread</tt>, <tt>Semaphore</tt>,
     * <tt>SynchList</tt>, and <tt>ElevatorBank</tt> classes.
     */
    public void selfTest() {
        // PingTest 스레드 생성 및 시작
        KThread thread0 = new KThread(new PingTest(0));
        thread0.setName("thread 0");
        thread0.fork();

        KThread thread1 = new KThread(new PingTest(1));
        thread1.setName("thread 1");
        thread1.fork();

        // 모든 스레드의 종료를 기다림
        thread0.join();
        thread1.join();
    }

    /**
     * A threaded kernel does not run user programs, so this method does nothing.
     */
    public void run() {
    }

    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
        Machine.halt();
    }

    /** Globally accessible reference to the scheduler. */
    public static Scheduler scheduler = null;
    /** Globally accessible reference to the alarm. */
    public static Alarm alarm = null;
    /** Globally accessible reference to the file system. */
    public static FileSystem fileSystem = null;

    // dummy variables to make javac smarter
    private static RoundRobinScheduler dummy1 = null;
    private static PriorityScheduler dummy2 = null;
    private static LotteryScheduler dummy3 = null;
    private static Condition2 dummy4 = null;
    private static Communicator dummy5 = null;
    private static Rider dummy6 = null;
    private static ElevatorController dummy7 = null;
}

class PingTest implements Runnable {
    int which; // 스레드 식별자
    PingTest(int which) {
        this.which = which;
    }

    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("*** thread " + which + " looped " + i + " times");
            KThread.currentThread().yield();
        }
    }
}
