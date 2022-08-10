package com.mx.dynamic.sharding.exception;

import java.io.IOException;

/**
 * @author: niguibin
 * @date: 2022/8/9 4:10 下午
 */
public class HostException extends RuntimeException {
    private static final long serialVersionUID = 3589264847881174997L;

    public HostException(IOException e) {
        super(e);
    }

    public HostException(String message) {
        super(message);
    }
}
