package com.hc.tools.lib_nfc.serialport;

public interface OnReadListener {
    void onReceiveData(byte[] buffer, int size);

    void onReadStop();
}
