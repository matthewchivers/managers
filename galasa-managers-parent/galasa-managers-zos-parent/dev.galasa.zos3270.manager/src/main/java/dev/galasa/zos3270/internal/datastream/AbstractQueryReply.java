/*
 * Copyright (c) 2019 IBM Corporation.
 */
package dev.galasa.zos3270.internal.datastream;

public abstract class AbstractQueryReply {

    public static final byte QUERY_REPLY = (byte) 0x81;

    public abstract byte[] toByte();

    public abstract byte getID();

}