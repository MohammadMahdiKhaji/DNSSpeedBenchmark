package app.dns.model.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool implements Executor {
    private static Logger logger = LogManager.getLogger(ThreadPool.class);
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    public ThreadPool(int numWorkerThread) {
        for (int i = 0; i<numWorkerThread; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    try {
                        Runnable task = queue.take();
                        task.run();
                    } catch (InterruptedException e) {
                        logger.error("Thread failed: {}", e.getMessage());
                        break;
                    }
                }
            });
            thread.start();
        }
    }
    @Override
    public void execute(Runnable task) {
        queue.add(task);
    }
}
