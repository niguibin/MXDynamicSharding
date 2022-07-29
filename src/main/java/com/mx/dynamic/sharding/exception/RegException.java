package com.mx.dynamic.sharding.exception;

/**
 * @author: niguibin
 * @date: 2022/7/29 2:35 下午
 */
public final class RegException extends RuntimeException {

    private static final long serialVersionUID = -6417179023552012152L;

    public RegException(final Exception ex) {
        super(ex);
    }
}
