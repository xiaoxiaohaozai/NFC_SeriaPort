package com.hc.tools.lib_nfc.serialport;

public interface OnSerialPortControllerListener {
    void onWriteData(byte[] data);

    void onReceiveData(byte[] buffer, int size);
}
