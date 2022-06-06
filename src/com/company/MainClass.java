package com.company;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MainClass {
    static final int CARS_COUNT = 4;
    static final CountDownLatch countDownLatchFinish = new CountDownLatch(CARS_COUNT);
    static final CountDownLatch countDownLatchReady = new CountDownLatch(CARS_COUNT);
    static final CountDownLatch countDownLatchWin = new CountDownLatch(CARS_COUNT);
    static final CyclicBarrier startBarrier = new CyclicBarrier(CARS_COUNT);
    static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    static final ConcurrentLinkedDeque win = new ConcurrentLinkedDeque<>();

    public static void main(String[] args) {
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
        Race race = new Race(new Road(60), new Tunnel(), new Road(40));
        Car[] cars = new Car[CARS_COUNT];

        for (int i = 0; i < cars.length; i++) {
            final int randomSpeed = 20 + (int) (Math.random() * 10);
            cars[i] = new Car(race, randomSpeed);
        }
        for (Car car : cars) {
            new Thread(car).start();
        }

        try {
            countDownLatchReady.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");

        try {
            countDownLatchFinish.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        for (int i = 0; i < cars.length; i++) {
            Integer index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    cars[index].run();
                    countDownLatchWin.countDown(); //
                    lock.writeLock().lock();
                    win.add(cars[index].getName());
                    if (win.getFirst().equals(cars[index].getName())) {
                        System.out.println(cars[index].getName() + " - WIN");
                    }
                    lock.writeLock().unlock();
                }
            }).start();
        }
        try {
            countDownLatchWin.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");
    }
}

