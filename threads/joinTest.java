package nachos.threads;

public class joinTest {
	public static void selfTest() {
		KThread thread1 = new KThread(new Runnable() {
			public void run() {
				System.out.println("Thread 1 starting.");
				for (int i = 0; i < 5; i++) {
					System.out.println("Thread 1 running: " + i);
					KThread.yield();
				}
				System.out.println("Thread 1 finished.");
			}
		}).setName("Thread 1");

		KThread thread2 = new KThread(new Runnable() {
			public void run() {
				System.out.println("Thread 2 starting.");
				thread1.join();
				System.out.println("Thread 2 finished after Thread 1.");
			}
		}).setName("Thread 2");

		thread1.fork();
		thread2.fork();

		thread2.join(); // Main thread waits for thread2 to finish
	}

	public static void main(String[] args) {
		selfTest();
	}
}
