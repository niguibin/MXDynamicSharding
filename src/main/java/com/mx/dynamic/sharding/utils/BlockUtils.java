package com.mx.dynamic.sharding.utils;

/**
 * @author: niguibin
 * @date: 2022/8/10 5:27 下午
 */
public class BlockUtils {

    private BlockUtils() {}

    public static void waitingShortTime() {
        sleep(100L);
    }

    public static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
