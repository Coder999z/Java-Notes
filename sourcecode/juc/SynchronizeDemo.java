package juc;

import java.util.concurrent.TimeUnit;

/**
 * @Author ： wzb
 * @Date ： 2019.10.09
 * @Description ：
 * @Version :
 */
public class SynchronizeDemo {
    static final Object obj = new Object();
    static int hamburger = 5;
    static int chips = 0;
    //厨师
    static Runnable chef = () -> {
        try {
            while (true) {
                if (hamburger == 0) {
                    synchronized (obj) {
                        hamburger++;
                        obj.notifyAll();
                    }
                }
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
    //卖汉堡柜台
    static Runnable hamburgerCounter = () -> {
        try {
            while (true) {
                while (hamburger > 0) {
                    System.out.println("HamburgerCounter卖出了第" + hamburger + "个汉堡");
                    hamburger--;
                    TimeUnit.MILLISECONDS.sleep(500);
                }
                synchronized (obj) {
                    System.out.println("汉堡已卖完，进入等待");
                    obj.wait();
                    System.out.println("收到通知，继续卖汉堡");
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };
    //卖薯条柜台
    static Runnable chipsCounter = () -> {
        try {
            while (true) {
                while (chips == 0) {
                    synchronized (obj) {
                        System.out.println("薯条已卖完，进入等待");
                        obj.wait();
                        System.out.println("收到通知，继续卖薯条");
                    }
                }
                chips--;
                System.out.println("ChipsCounter卖出了第" + chips + "包薯条");
                TimeUnit.SECONDS.sleep(1);
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
