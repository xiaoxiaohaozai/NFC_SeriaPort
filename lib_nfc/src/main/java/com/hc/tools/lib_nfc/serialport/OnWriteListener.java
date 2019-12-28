package com.hc.tools.lib_nfc.serialport;

public interface OnWriteListener {
    void writeData(byte[] data);

    void writeStop();
}
