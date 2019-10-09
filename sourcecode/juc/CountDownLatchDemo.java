package juc;

import java.util.concurrent.CountDownLatch;

/**
 * @Author ： wzb
 * @Date ： 2019.10.09
 * @Description ：
 * @Version :
 */
public class CountDownLatchDemo {
    static CountDownLatch latch = new CountDownLatch(3);

    static Runnable init = ()->{
        latch.countDown();
        System.out.println(Thread.currentThread().getName() + " check off");
    };

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Start init thread");
        for (int i = 0; i < 3; i++) {
            System.out.println("Start " + i + " success");
            new Thread(init).start();
        }
        System.out.println("Waiting init");
        latch.await();
        System.out.println("All thread check off");
        System.out.println("System start success");

    }
}
