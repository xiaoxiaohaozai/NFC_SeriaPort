package com.hc.tools.lib_nfc.serialport;

public interface OnSerialPortControllerListener {
    void writeData(byte[] data);

    void onReceiveData(byte[] buffer, int size);


    void writeStop();

    void onReadStop();
}
