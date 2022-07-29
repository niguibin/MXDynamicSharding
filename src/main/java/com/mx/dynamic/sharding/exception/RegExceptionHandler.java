package com.mx.dynamic.sharding.exception;

import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;

/**
 * @author: niguibin
 * @date: 2022/7/29 2:36 下午
 */
public final class RegExceptionHandler {
    public static void handleException(final Exception ex) {
        if (ex == null || isIgnoredException(ex) || isIgnoredException(ex.getCause())) {
            return;
        }
        if (ex instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        } else {
            throw new RegException(ex);
        }
    }

    public static boolean isIgnoredException(final Throwable throwable) {
        return throwable instanceof ConnectionLossException || throwable instanceof NoNodeException || throwable instanceof NodeExistsException;
    }
}
