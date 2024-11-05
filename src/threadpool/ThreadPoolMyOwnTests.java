package threadpool;

import org.junit.jupiter.api.Test;
import waitingqueue.WaitablePQueue;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolMyOwnTests {
    @Test
    void testSubmitBasic() {
        ThreadPool pool = new ThreadPool(2);
        Callable<Long> callable1 = getCallable();
        Callable<Long> callable2 = getCallable();
        Callable<Long> callable3 = getCallable();
        assertEquals(2, pool.getCurrentNumberOfThreads());

        pool.submit(callable1);
        pool.submit(callable2);
        pool.submit(callable3);

        pool.shutdown();
        try {
            pool.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("end of main");
        assertEquals(0, pool.getCurrentNumberOfThreads());
    }

    @Test
    void testAwaitTimeout() {
        ThreadPool pool = new ThreadPool(3);
        Callable<Long> callable1 = getCallable();
        Callable<Long> callable2 = getCallable();
        Callable<Long> callable3 = getCallable();
        Callable<Long> callable4 = getCallable();
        Callable<Long> callable5 = getCallable();
        Callable<Long> callable6 = getCallable();

        pool.submit(callable1);
        pool.submit(callable2);
        pool.submit(callable3);
        pool.submit(callable4);
        pool.submit(callable5);
        pool.submit(callable6);

        pool.shutdown();
        boolean waitResult;
        try {
            waitResult = pool.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertFalse(waitResult);
    }


    @Test
    void testAwaitTimeoutSuccessful() {
        ThreadPool pool = new ThreadPool(3);
        Callable<Long> callable1 = getCallable();
        Callable<Long> callable2 = getCallable();
        Callable<Long> callable3 = getCallable();
        Callable<Long> callable4 = getCallable();
        Callable<Long> callable5 = getCallable();
        Callable<Long> callable6 = getCallable();

        pool.submit(callable1);
        pool.submit(callable2);
        pool.submit(callable3);
        pool.submit(callable4);
        pool.submit(callable5);
        pool.submit(callable6);

        pool.shutdown();
        boolean waitResult;
        try {
            waitResult = pool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertTrue(waitResult);
    }


    @Test
    void testPause() {
        ThreadPool pool = new ThreadPool(2);
        Callable<Long> callable1 = getCallableLongSleep();
        Callable<Long> callable2 = getCallableLongSleep();
        Callable<Long> callable3 = getCallableLongSleep();
        Callable<Long> callable4 = getCallableLongSleep();
        Callable<Long> callable5 = getCallableLongSleep();
        Callable<Long> callable6 = getCallableLongSleep();
        Callable<Long> callable7 = getCallableLongSleep();
        Callable<Long> callable8 = getCallableLongSleep();
        Callable<Long> callable9 = getCallableLongSleep();

        pool.submit(callable1);
        pool.submit(callable2);
        pool.submit(callable3);
        pool.submit(callable4);
        pool.submit(callable5);
        pool.submit(callable6);
        pool.submit(callable7);
        pool.submit(callable8);
        pool.submit(callable9);

        //sleep 1 second to wait for threads to start working on 1st task of each thread
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        pool.pause();


        boolean waitResult;
        try {
            waitResult = pool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertFalse(waitResult);
    }

    @Test
    void testResume() {
        ThreadPool pool = new ThreadPool();
        for (int i = 0; i < 20; ++i) {
            Callable<Long> callable1 = getCallableLongSleep();
            pool.submit(callable1);
        }


        //sleep 1 second to wait for threads to start working on 1st task of each thread
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        pool.pause();

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        pool.resume();


        pool.shutdown();
        try {
            pool.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals(0, pool.getCurrentNumberOfThreads());
    }


    @Test
    void testPriority() {
        ThreadPool pool = new ThreadPool(2);

        Callable<Long> callableHIGH = getCallableHigh();
        Callable<Long> callableMID = getCallableMid();
        Callable<Long> callableLOW = getCallableLow();
        Callable<Long> callableHIGH2 = getCallableHigh();
        Callable<Long> callableMID2 = getCallableMid();
        Callable<Long> callableLOW2 = getCallableLow();


        pool.submit(callableLOW, Priority.LOW);
        pool.submit(callableLOW2, Priority.LOW);
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        pool.submit(callableMID2, Priority.MEDIUM);
        pool.submit(callableMID, Priority.MEDIUM);
        pool.submit(callableHIGH, Priority.HIGH);
        pool.submit(callableHIGH2, Priority.HIGH);

        pool.shutdown();
        try {
            pool.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    @Test
    void testGet() {
        ThreadPool pool = new ThreadPool(2);
        Callable<Long> callable1 = getCallableReturnValue();
        Callable<Long> callable2 = getCallableReturnValue();
        Callable<Long> callable3 = getCallableReturnValue();
        Callable<Long> callable4 = getCallableReturnValue();
        Callable<Long> callable5 = getCallableReturnValue();
        Callable<Long> callable6 = getCallableReturnValue();

        Future<Long> result1 =  pool.submit(callable1);

        try {
            System.out.println(result1.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        System.out.println("is done task1? (after get) " + result1.isDone());
        pool.submit(callable2);
        pool.submit(callable3);
        pool.submit(callable4);
        pool.submit(callable5);

        Future<Long> result6 = pool.submit(callable6, Priority.LOW);

        try {
            System.out.println("is done task6 (before get)? " + result6.isDone());
            System.out.println(result6.get());
            System.out.println("is done task6 (after get)? " + result6.isDone());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        pool.shutdown();
        try {
            pool.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    @Test
    void voidTestCancelAndIsCancelled() {
        ThreadPool pool = new ThreadPool(2);
        Callable<Long> callable1 = getCallableReturnValue();
        Callable<Long> callable2 = getCallableReturnValue();
        Callable<Long> callable3 = getCallableReturnValue();
        Callable<Long> callable4 = getCallableReturnValue();
        Callable<Long> callable5 = getCallableReturnValue();
        Callable<Long> callable6 = getCallableReturnValue();

        Future<Long> result1 =  pool.submit(callable1);
        assertFalse(result1.isCancelled());

        Future<Long> result2 =  pool.submit(callable2);
        Future<Long> result3 =  pool.submit(callable3);
        Future<Long> result4 = pool.submit(callable4);

        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Future<Long> result5 = pool.submit(callable5, Priority.LOW);
        Future<Long> result6 = pool.submit(callable6, Priority.LOW);

        result5.cancel(true);
        result6.cancel(true);

        try {
            result1.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        assertFalse(result1.isCancelled());
        assertTrue(result5.isCancelled());
        assertTrue(result6.isCancelled());

        pool.shutdown();
        try {
            pool.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void testIncreaseNumThreads() {
        ThreadPool pool = new ThreadPool(1);

        Callable<Long> callable1 = getCallableLongSleep();
        Callable<Long> callable2 = getCallableLongSleep();
        Callable<Long> callable3 = getCallableLongSleep();
        Callable<Long> callable4 = getCallableLongSleep();
        Callable<Long> callable5 = getCallableLongSleep();
        Callable<Long> callable6 = getCallableLongSleep();
        Callable<Long> callable7 = getCallableLongSleep();
        Callable<Long> callable8 = getCallableLongSleep();
        Callable<Long> callable9 = getCallableLongSleep();


        pool.submit(callable1);
        pool.submit(callable2);
        pool.submit(callable3);
        pool.submit(callable4);
        pool.submit(callable5);
        pool.submit(callable6);
        pool.submit(callable7);
        pool.submit(callable8);
        pool.submit(callable9);



        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(1, pool.getCurrentNumberOfThreads());

        pool.setNumOfThreads(5);

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals(5, pool.getCurrentNumberOfThreads());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        pool.setNumOfThreads(1);

        pool.shutdown();
        try {
            pool.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(0, pool.getCurrentNumberOfThreads());
    }


    private Callable<Long> getCallableReturnValue() {
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                System.out.println("Executing callable. " + Thread.currentThread().getName());
                Thread.sleep(2000);
                System.out.println("Finished callable. " + Thread.currentThread().getName());
                return 5L;
            }
        };
    }


    private Callable<Long> getCallableHigh() {
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                System.out.println("Executing HIGH. " + Thread.currentThread().getName());
                Thread.sleep(2000);
                System.out.println("Finished HIGH. " + Thread.currentThread().getName());
                return Thread.currentThread().getId();
            }
        };
    }

    private Callable<Long> getCallableMid() {
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                System.out.println("Executing MID. " + Thread.currentThread().getName());
                Thread.sleep(2000);
                System.out.println("Finished MID. " + Thread.currentThread().getName());
                return Thread.currentThread().getId();
            }
        };
    }

    private Callable<Long> getCallableLow() {
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                System.out.println("Executing LOW. " + Thread.currentThread().getName());
                Thread.sleep(2000);
                System.out.println("Finished LOW. " + Thread.currentThread().getName());
                return Thread.currentThread().getId();
            }
        };
    }


    private Callable<Long> getCallable() {
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                System.out.println("Executing Callable. " + Thread.currentThread().getName());
                Thread.sleep(2000);
                System.out.println("Finished Callable. " + Thread.currentThread().getName());
                return Thread.currentThread().getId();
            }
        };
    }

    private Callable<Long> getCallableLongSleep() {
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                System.out.println("Executing Callable. " +Thread.currentThread().getName());
                Thread.sleep(5000);
                System.out.println("Finished Callable. " + Thread.currentThread().getName());
                return Thread.currentThread().getId();
            }
        };
    }
}