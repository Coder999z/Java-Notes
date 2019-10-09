package juc;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * @Author ： wzb
 * @Date ： 2019.10.09
 * @Description ：
 * @Version :
 */
public class CyclicBarrierDemo {
    static CyclicBarrier cb = new CyclicBarrier(3,()-> System.out.println("CyclicBarrier call back function"));

    static Runnable runner = ()->{
        try {
            System.out.println(Thread.currentThread().getName() + " wait for CyclicBarrier");
            cb.await();
            System.out.println(Thread.currentThread().getName() + " continue");
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 2; i++) {
            new Thread(runner).start();
        }
        TimeUnit.SECONDS.sleep(1);
        new Thread(runner).start();
    }
}
