package io.papermc.paper.niceserver;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Single worker that recounts mob caps for the next tick.
 * Actual spawning still happens on the main thread.
 */
public final class AsyncMobSpawnProcessor {

    private static volatile ExecutorService executor;

    private AsyncMobSpawnProcessor() {
    }

    private static ExecutorService executor() {
        ExecutorService current = executor;
        if (current == null) {
            synchronized (AsyncMobSpawnProcessor.class) {
                current = executor;
                if (current == null) {
                    current = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                        .setNameFormat("NiceServer Async Mob Spawn Thread")
                        .setDaemon(true)
                        .setPriority(Thread.NORM_PRIORITY - 1)
                        .build());
                    executor = current;
                }
            }
        }
        return current;
    }

    public static void submit(final Runnable task) {
        executor().execute(task);
    }

    public static void shutdown() {
        final ExecutorService current = executor;
        if (current != null) {
            current.shutdownNow();
            try {
                current.awaitTermination(5, TimeUnit.SECONDS);
            } catch (final InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
