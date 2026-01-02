package ru.sinvic.client.perf;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DemoClient {
    public static void main(String[] args) throws InterruptedException {
        doWork();
        AtomicInteger counter = new AtomicInteger();
        doIncrementInNewThread(counter);
        doIncrementInNewThread(counter);
        doDecrementInNewThread(counter);
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                try {
                    if (Math.random() > 0.5) {
                        incrementForever(counter);
                    } else {
                        decrementForever(counter);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        runForeverLoop();
    }

    private static void doIncrementInNewThread(AtomicInteger counter) {
        new Thread(() -> {
            try {
                incrementForever(counter);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private static void doDecrementInNewThread(AtomicInteger counter) {
        new Thread(() -> {
            try {
                decrementForever(counter);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private static void incrementForever(AtomicInteger counter) throws InterruptedException {
        while (true) {
            counter.incrementAndGet();
            Thread.sleep(100);
            System.out.println("Hello from " + Thread.currentThread().getName());
        }
    }

    private static void decrementForever(AtomicInteger counter) throws InterruptedException {
        while (true) {
            counter.decrementAndGet();
            Thread.sleep(100);
            System.out.println("Hello from " + Thread.currentThread().getName());
        }
    }

    private static void runForeverLoop() throws InterruptedException {
        while (true) {
            sleep(100000);
            System.out.println("Running forever");
        }
    }

    private static void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    private static void doWork() {
        Thread thread = new Thread(() -> {
            try {
                doSomeMath();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

    }

    private static void doSomeMath() throws InterruptedException {
        int counter = 1;
        while (true) {
            counter += (int) (Math.random() * 10 / 2);
            if (counter % 2 == 0) {
                Thread.sleep(10000);
                System.out.println("Even number from thread " + Thread.currentThread().getName());
            }
        }
    }
}
