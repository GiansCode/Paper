package io.papermc.paper.niceserver;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Offloads {@code PathFinder#findPath} from the main thread.
 * Prepared on the main thread; the finished path is applied back on the main thread.
 */
public final class AsyncPathProcessor {

    private static volatile ThreadPoolExecutor executor;

    private AsyncPathProcessor() {
    }

    private static ThreadPoolExecutor executor() {
        ThreadPoolExecutor current = executor;
        if (current == null) {
            synchronized (AsyncPathProcessor.class) {
                current = executor;
                if (current == null) {
                    final int threads = NiceServerConfig.asyncPathfindingThreads > 0
                        ? NiceServerConfig.asyncPathfindingThreads
                        : Math.max(1, Runtime.getRuntime().availableProcessors() / 4);
                    current = new ThreadPoolExecutor(
                        threads, threads,
                        60L, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(Math.max(32, threads * 256)),
                        new ThreadFactoryBuilder()
                            .setNameFormat("NiceServer Async Pathfinding Thread #%d")
                            .setDaemon(true)
                            .setPriority(Thread.NORM_PRIORITY - 2)
                            .build(),
                        new ThreadPoolExecutor.CallerRunsPolicy()
                    );
                    current.allowCoreThreadTimeOut(true);
                    executor = current;
                }
            }
        }
        return current;
    }

    public static void execute(final Runnable task) {
        executor().execute(task);
    }

    public static void shutdown() {
        final ThreadPoolExecutor current = executor;
        if (current != null) {
            current.shutdownNow();
        }
    }
}
