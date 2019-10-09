package juc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author ： wzb
 * @Date ： 2019.10.09
 * @Description ：
 * @Version :
 */
public class ReadWriteDemo {
    static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    static Runnable read = () -> {
        ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
        try {
            System.out.println("Read Thread-" + Thread.currentThread().getId() + " start");
            readLock.lock();
            System.out.println("Read Thread-" + Thread.currentThread().getId() + " running");
            TimeUnit.SECONDS.sleep(1);
            System.out.println("Read Thread-" + Thread.currentThread().getId() + " stop");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }
    };

    static Runnable write = () -> {
        ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();
        try {
            System.out.println("Write Thread-" + Thread.currentThread().getId() + " start");
            writeLock.lock();
            System.out.println("Write Thread-" + Thread.currentThread().getId() + " running");
            TimeUnit.SECONDS.sleep(1);
            System.out.println("Write Thread-" + Thread.currentThread().getId() + " stop");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    };

    public static void main(String[] args) throws InterruptedException {
        new Thread(read).start();
        new Thread(read).start();
        new Thread(write).start();
        new Thread(write).start();
        new Thread(read).start();
    }
}
