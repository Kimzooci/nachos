package nachos.threads;

import nachos.machine.*;

public class KThread {
	private static final int statusFinished = 4; // 상태 Finished를 나타내는 값
	private ThreadQueue waitQueue = ThreadedKernel.scheduler.newThreadQueue(false); // 새로운 쓰레드 대기열 생성
	private KThread joinedThread = null;  // Variable to keep track of the joined thread
	private boolean hasJoined = false;  // Flag to ensure join is called only once

	public static KThread currentThread() {
		Lib.assertTrue(currentThread != null);
		return currentThread;
	}

	public KThread() {
		if (currentThread != null) {
			tcb = new TCB();
		} else {
			readyQueue = ThreadedKernel.scheduler.newThreadQueue(false);
			readyQueue.acquire(this);

			currentThread = this;
			tcb = TCB.currentTCB();
			name = "main";
			restoreState();

			createIdleThread();
		}
	}

	public KThread(Runnable target) {
		this();
		this.target = target;
	}

	public KThread setTarget(Runnable target) {
		Lib.assertTrue(status == statusNew);

		this.target = target;
		return this;
	}

	public KThread setName(String name) {
		this.name = name;
		return this;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return (name + " (#" + id + ")");
	}

	public int compareTo(Object o) {
		KThread thread = (KThread) o;

		if (id < thread.id)
			return -1;
		else if (id > thread.id)
			return 1;
		else
			return 0;
	}

	public void fork() {
		Lib.assertTrue(status == statusNew);
		Lib.assertTrue(target != null);

		Lib.debug(dbgThread, "Forking thread: " + toString() + " Runnable: " + target);

		boolean intStatus = Machine.interrupt().disable();

		tcb.start(new Runnable() {
			public void run() {
				runThread();
			}
		});

		ready();

		Machine.interrupt().restore(intStatus);
	}

	private void runThread() {
		begin();
		target.run();
		finish();
	}

	private void begin() {
		Lib.debug(dbgThread, "Beginning thread: " + toString());

		Lib.assertTrue(this == currentThread);

		restoreState();

		Machine.interrupt().enable();
	}

	public static void finish() {
		Lib.debug(dbgThread, "Finishing thread: " + currentThread.toString());

		Machine.interrupt().disable();

		Machine.autoGrader().finishingCurrentThread();

		Lib.assertTrue(toBeDestroyed == null);
		toBeDestroyed = currentThread;

		currentThread.status = statusFinished;

		KThread toWake = currentThread.joinedThread;
		if (toWake != null) {
			toWake.ready();
		}

		sleep();
	}

	public static void yield() {
		Lib.debug(dbgThread, "Yielding thread: " + currentThread.toString());

		Lib.assertTrue(currentThread.status == statusRunning);

		boolean intStatus = Machine.interrupt().disable();

		currentThread.ready();

		runNextThread();

		Machine.interrupt().restore(intStatus);
	}

	public static void sleep() {
		Lib.debug(dbgThread, "Sleeping thread: " + currentThread.toString());

		Lib.assertTrue(Machine.interrupt().disabled());

		if (currentThread.status != statusFinished)
			currentThread.status = statusBlocked;

		runNextThread();
	}

	public void ready() {
		Lib.debug(dbgThread, "Ready thread: " + toString());

		Lib.assertTrue(Machine.interrupt().disabled());
		Lib.assertTrue(status != statusReady);

		status = statusReady;
		if (this != idleThread)
			readyQueue.waitForAccess(this);

		Machine.autoGrader().readyThread(this);
	}

	public void join() {
		Lib.assertTrue(this != KThread.currentThread(), "Cannot join on itself.");

		boolean intStatus = Machine.interrupt().disable();  // disable interrupts

		if (this.status != statusFinished) {
			if (hasJoined) {
				Lib.assertTrue(false, "This thread has already been joined.");
			}
			hasJoined = true;
			if (joinedThread != null) {
				Lib.assertTrue(false, "Multiple joins on the same thread are not allowed.");
			}
			joinedThread = KThread.currentThread();
			waitQueue.waitForAccess(KThread.currentThread());  // Add the current thread to the wait queue
			KThread.sleep();  // Put the current thread to sleep
		} else {
			hasJoined = true; // Ensure hasJoined is set if the thread has already finished
		}

		Machine.interrupt().restore(intStatus);  // restore interrupts
	}

	// hasJoined 상태를 반환하는 메소드 추가
	public boolean hasJoined() {
		return hasJoined;
	}

	private static void createIdleThread() {
		Lib.assertTrue(idleThread == null);

		idleThread = new KThread(new Runnable() {
			public void run() { while (true) yield(); }
		});
		idleThread.setName("idle");

		Machine.autoGrader().setIdleThread(idleThread);

		idleThread.fork();
	}

	private static void runNextThread() {
		KThread nextThread = readyQueue.nextThread();
		if (nextThread == null)
			nextThread = idleThread;

		nextThread.run();
	}

	private void run() {
		Lib.assertTrue(Machine.interrupt().disabled());

		Machine.yield();

		currentThread.saveState();

		Lib.debug(dbgThread, "Switching from: " + currentThread.toString() + " to: " + toString());

		currentThread = this;

		tcb.contextSwitch();

		currentThread.restoreState();
	}

	protected void restoreState() {
		Lib.debug(dbgThread, "Running thread: " + currentThread.toString());

		Lib.assertTrue(Machine.interrupt().disabled());
		Lib.assertTrue(this == currentThread);
		Lib.assertTrue(tcb == TCB.currentTCB());

		Machine.autoGrader().runningThread(this);

		status = statusRunning;

		if (toBeDestroyed != null) {
			toBeDestroyed.tcb.destroy();
			toBeDestroyed.tcb = null;
			toBeDestroyed = null;
		}
	}

	protected void saveState() {
		Lib.assertTrue(Machine.interrupt().disabled());
		Lib.assertTrue(this == currentThread);
	}

	private static class PingTest implements Runnable {
		PingTest(int which) {
			this.which = which;
		}

		public void run() {
			for (int i = 0; i < 10; i++) {
				System.out.println("*** thread " + which + " looped " + i + " times");
				currentThread.yield();
			}
		}

		private int which;
	}

	public static void selfTest() {
		Lib.debug(dbgThread, "Enter KThread.selfTest");

		new KThread(new PingTest(1)).setName("forked thread").fork();
		new PingTest(0).run();
	}

	private static final char dbgThread = 't';

	public Object schedulingState = null;

	private static final int statusNew = 0;
	private static final int statusReady = 1;
	private static final int statusRunning = 2;
	private static final int statusBlocked = 3;

	private int status = statusNew;
	private String name = "(unnamed thread)";
	private Runnable target;
	private TCB tcb;

	private int id = numCreated++;
	private static int numCreated = 0;

	private static ThreadQueue readyQueue = null;
	private static KThread currentThread = null;
	private static KThread toBeDestroyed = null;
	private static KThread idleThread = null;
}
