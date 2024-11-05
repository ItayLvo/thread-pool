# ThreadPool - A custom Thread Pool implementation in Java

## Overview

The `ThreadPool` class is a custom implementation of the `Executor` interface in Java. 
It provides an efficient way to manage and execute tasks concurrently using a blocking priority queue. 
The pool allows dynamic adjustments to the number of worker threads, allows tasks to be paused, resumed, and supports controlled shutdown.

### Key Features
- **Dynamic Thread Management**: The pool can adjust the number of threads based on user demand.
- **Task Prioritization**: Tasks are assigned priorities, allowing higher-priority tasks to be executed first.
- **Pause and Resume Functionality**: Worker threads can be paused and resumed upon demand.
- **Graceful Shutdown**: The pool ensures all tasks are completed before shutting down.
- **Future Interface Support**: Each submitted task returns a `Future` object, allowing for asynchronous result retrieval and cancellation.

## Usage

### Creating a ThreadPool

The thread pool can be created with a default number of threads or a user-defined amount.

```java
// Create a thread pool with default thread count (based on available processors)
ThreadPool pool = new ThreadPool();

// Create a thread pool with a specific number of threads
ThreadPool pool = new ThreadPool(8);
```

### Submitting Tasks

You can submit tasks to the pool using different overloads of the `submit` method. Tasks are prioritized using the `Priority` enum.

```java
// Submit a Runnable task with medium priority
Future<?> future = pool.submit(() -> {
    System.out.println("I am a runnable task");
}, Priority.MEDIUM);

// Submit a Callable task with a return value and high priority
Future<String> future = pool.submit(() -> {
    return "I am a callable task";
}, Priority.HIGH);
```

### Task Prioritization

Tasks are prioritized using the `Priority` enum, which supports values such as `LOW`, `MEDIUM`, and `HIGH`. You can customize priorities when submitting tasks.

```java
public enum Priority {
    LOW, MEDIUM, HIGH;
}
```

### Adjusting Thread Count

The number of worker threads can be dynamically adjusted. If the pool is paused, new threads will enter a waiting state until the pool is resumed.

```java
ThreadPool pool = new ThreadPool(4);
// Do some work...
// Set the number of threads
pool.setNumOfThreads(10);
// Do some work...
pool.setNumOfThreads(1);
```

### Pausing and Resuming the Pool

Tasks can be paused and resumed using the `pause` and `resume` methods. This ensures no tasks are executed during the paused state.

```java
// Pause the pool
pool.pause();

// Resume the pool
pool.resume();
```

### Shutting Down the Pool

You can initiate a graceful shutdown of the thread pool, ensuring all tasks are completed before the pool terminates. The `awaitTermination` method can be used to block until the shutdown is complete. `awaitTermination` method can either accept no arguments, which means no time-out for the termination, or it can accept time-out arguments. 

```java
// Shutdown the pool
pool.shutdown();

// Wait for all tasks to finish
pool.awaitTermination();

// Or, apply a timeout:
// pool.waitTermination(500, TimeUnit.MILLISECONDS);

```

### Handling Task Cancellation

Submitted tasks can be cancelled via the `Future` object. The `cancel` method interrupts the task if it’s currently running or removes it from the queue if it hasn’t started yet.

```java
// Submit a task and get its future
Future<?> future = pool.submit(() -> {
    // Task logic here
});

// Cancel the task
future.cancel(true);  // Interrupt the task if running
```

## How It Works

### Internal Structure

1. **Custom-implemented Waitable (Blocking) Priority Queue (`WaitablePQ`)**: The thread pool uses a custom waitable priority queue to manage tasks. Worker threads continuously wait for tasks to arrive in the queue, and they pick up tasks based on their priority.
   This ensures that threads only proceed when a task is available, preventing busy waiting and reducing CPU usage. The higher the task's priority, the sooner it is executed.
2. **Worker Threads**: The pool uses worker threads that fetch and execute tasks from the queue. Each thread is continuously running unless paused or shut down.
3. **Task Management**: Each task is wrapped in a `Task` object, which contains the task’s priority, execution logic, and a `Future` to manage the result.
4. **Poison Pill**: A "poison pill" pattern is used to safely shut down the thread pool. Special tasks (poison tasks) are enqueued to terminate threads gracefully.

### Custom Task Class

The `Task` class implements the `Comparable` interface to enable priority comparison. It wraps `Callable` objects and provides a `Future` for managing task results.

### Worker Class

Each `Worker` is a thread that continuously retrieves tasks from the queue and executes them. When a worker encounters a "poison pill" task, it terminates.

```java
    private final class Worker extends Thread {
        @Override
        public void run() {
            //each created threads increases the current thread counter
            currentNumberOfThreads.incrementAndGet();
            boolean isTaskPoison = false;
            try {
                while (!isTaskPoison) {
                    //dequeues a task from the queue. waits if no tasks are available to dequeue (blocking)
                    Task<?> task = taskQueue.dequeue();
                    //set the poison flag to true if the task is supposed to kill the thread
                    isTaskPoison = task.isPoison;
                    //run the current task (blocking)
                    task.executeTask();
                }
            } finally {
                //thread was killed: decrease number of current threads, clean up, notify threads waiting for awaitTermination...
```

## Exception Handling

Tasks that encounter exceptions during execution are wrapped in an `ExecutionException`, which is then re-thrown when calling `get()` on the `Future`. The thread that caught the excxeption will then continue to the next task, and does not "die".
