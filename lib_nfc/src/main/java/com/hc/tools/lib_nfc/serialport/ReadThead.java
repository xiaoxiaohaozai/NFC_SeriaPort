package com.hc.tools.lib_nfc.serialport;


import com.hc.tools.lib_nfc.constant.Config;
import com.hc.tools.lib_nfc.utils.ByteUtils;
import com.hc.tools.lib_nfc.utils.LogUtils;


import java.io.InputStream;

public class ReadThead extends Thread {

    private volatile InputStream input;
    private volatile OnReadListener readListener;

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
                LogUtils.d(Config.TAG_SERIAL_PORT, "正在等待读取数据");
                size = input.read(buffer);
                if (size > 0) {
                    notifyData(buffer, size);
                    LogUtils.d(Config.TAG_SERIAL_PORT, "读数据---" + ByteUtils.bytes2String(buffer, size));
                }
            } catch (Exception e) {
                LogUtils.d(Config.TAG_SERIAL_PORT, "读---异常退出");
                break;
            }
        }
        LogUtils.d(Config.TAG_SERIAL_PORT, "读---停止");
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
        if (input != null) {
            input = null;
        }
    }

}
