package com.hc.tools.lib_nfc.thread;


import com.hc.tools.lib_nfc.utils.ByteUtils;
import com.hc.tools.lib_nfc.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * 读取线程
 */
public class ReadThead extends Thread {

    private  InputStream input;
    private OnReadListener readListener;

    public ReadThead(InputStream input) {
        this.input = input;
    }

    public void setReadListener(OnReadListener readListener) {
        this.readListener = readListener;
    }

    @Override
    public void run() {

        byte[] buffer = new byte[128];
        int size;
        while (!isInterrupted() && input != null) {
            try {
                LogUtils.d("读线程---等待读取");
                size = input.read(buffer);
                if (size > 0) {
                    notifyData(buffer, size);
                    LogUtils.d("读线程---读取:---" + ByteUtils.bytes2String(buffer, size));
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        LogUtils.d("读线程---停止");
        notifyStop();
        close();

    }

    private void notifyData(byte[] buffer, int size) {
        if (readListener != null) {
            readListener.onReceiveData(buffer, size);
        }
    }

    private void notifyStop() {
        if (readListener != null) {
            readListener.onReadStop();
        }
    }

    public void close() {
        if (!isInterrupted()) {
            interrupt();
        }
        if (readListener != null) {
            readListener = null;
        }
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            input = null;
        }
    }


    public interface OnReadListener {

        void onReceiveData(byte[] buffer, int size);

        void onReadStop();

    }
}
