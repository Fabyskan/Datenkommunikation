package org.example;

public enum Status {
    WAIT_FOR_0((byte) 0x00),
    WAIT_FOR_1((byte) 0xFF),
    INIT_STAT((byte) 0x00);

    public final byte controlByte;

    Status(byte controlByte) {
        this.controlByte = controlByte;
    }
}
