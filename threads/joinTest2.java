package nachos.threads;

public class joinTest2 implements Runnable {
    int num;
    int sum;

    public joinTest2(int num) {
        this.num = num;
        sum = 0;
    }

    public void run() {
        System.out.println("*** thread " + num + " is started");
        for (int i = 1; i <= num; i++) {
            sum += i;
            System.out.println("*** thread " + num + " looped " + i + " times");
        }
        System.out.println("*** thread " + num + " is finished: " + sum);
    }
}
