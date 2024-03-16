package com.siyufeng.maker.meta;

/**
 * @author 司雨枫
 * 元信息异常
 */
public class MetaException extends RuntimeException{

    public MetaException(String message) {
        super(message);
    }

    public MetaException(String message, Throwable cause) {
        super(message, cause);
    }
}
