package juc;





import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @Author ： wzb
 * @Date ： 2019.10.09
 * @Description ：
 * @Version :
 */
public class SemaphoreDemo {
    static Semaphore semaphore = new Semaphore(10);

    public static void main(String[] args) {
        new Worker(5).start();
        new Worker(5).start();
        new Worker(7).start();
    }

    static class Worker extends Thread {
        int count;

        public Worker(int count) {
            this.count = count;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire(count);
                System.out.println(Thread.currentThread().getName() + " acquire" + count);
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                semaphore.release(count);
                System.out.println(Thread.currentThread().getName() + " release" + count);
            }
        }
    }
}
