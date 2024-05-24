package nachos.threads;

import nachos.machine.*;

/**
 * A multi-threaded OS kernel.
 */
public class ThreadedKernel extends Kernel {
    public ThreadedKernel() {
        super();
    }

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

    public void selfTest() {
        KThread.selfTest();
        Semaphore.selfTest();
        joinTest.selfTest();  // joinTest 추가
        SynchList.selfTest();
        if (Machine.bank() != null) {
            ElevatorBank.selfTest();
        }

        Alarm.selfTest();  // Alarm 클래스의 selfTest 메서드를 호출
    }

    public void run() {
    }

    public void terminate() {
        Machine.halt();
    }

    public static Scheduler scheduler = null;
    public static Alarm alarm = null;
    public static FileSystem fileSystem = null;

    private static RoundRobinScheduler dummy1 = null;
    private static PriorityScheduler dummy2 = null;
    private static LotteryScheduler dummy3 = null;
    private static Condition2 dummy4 = null;
    private static Communicator dummy5 = null;
    private static Rider dummy6 = null;
    private static ElevatorController dummy7 = null;
}
