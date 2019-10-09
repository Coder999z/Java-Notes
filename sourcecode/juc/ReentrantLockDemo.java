package juc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author ： wzb
 * @Date ： 2019.10.09
 * @Description ：
 * @Version :
 */
public class ReentrantLockDemo {

    static ReentrantLock lock = new ReentrantLock();
    static int tickets = 100;

    static Runnable cell = () ->{
        try {
            //获得锁
            lock.lock();
            while (tickets > 0) {
                TimeUnit.MILLISECONDS.sleep(10);
                tickets--;
                System.out.println(tickets);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放锁
            lock.unlock();
        }
    };

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(cell).start();
        }
    }
}
