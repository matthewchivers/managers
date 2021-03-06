/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosfile;

public class ZosVSAMDatasetException extends ZosFileManagerException {
    private static final long serialVersionUID = 1L;

    public ZosVSAMDatasetException() {
    }

    public ZosVSAMDatasetException(String message) {
        super(message);
    }

    public ZosVSAMDatasetException(Throwable cause) {
        super(cause);
    }

    public ZosVSAMDatasetException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZosVSAMDatasetException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
