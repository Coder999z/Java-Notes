package juc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author ： wzb
 * @Date ： 2019.10.09
 * @Description ：
 * @Version :
 */
public class ConditionDemo {
    static Lock lock = new ReentrantLock();
    static Condition hamburgerCondition = lock.newCondition();
    static Condition chipsCondition = lock.newCondition();

    static int hamburger = 3;
    static int chips = 0;

    static Runnable chef = ()->{
        try {
            while (true) {
                if (hamburger == 0) {
                    try {
                        lock.lock();
                        hamburger++;
                        hamburgerCondition.signalAll();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    static Runnable hamburgerCounter = ()->{
        try {
            while (true) {
                while (hamburger > 0) {
                    System.out.println("HamburgerCounter卖出了第" + hamburger + "个汉堡");
                    hamburger--;
                    TimeUnit.MILLISECONDS.sleep(500);
                }
                try {
                    lock.lock();
                    System.out.println( "汉堡已卖完，进入等待");
                    hamburgerCondition.await();
                    System.out.println("收到通知，继续卖汉堡");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    static Runnable chipsCounter = ()-> {
        try {
            while (true) {
                while (chips > 0) {
                    System.out.println("ChipsCounter卖出了第" + chips + "份薯条");
                    chips--;
                    TimeUnit.MILLISECONDS.sleep(500);
                }
                try {
                    lock.lock();
                    System.out.println("薯条已卖完，进入等待");
                    chipsCondition.await();
                    System.out.println("收到通知，继续卖薯条");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    public static void main(String[] args) {
        new Thread(chef).start();
        new Thread(hamburgerCounter).start();
        new Thread(chipsCounter).start();
    }
}
