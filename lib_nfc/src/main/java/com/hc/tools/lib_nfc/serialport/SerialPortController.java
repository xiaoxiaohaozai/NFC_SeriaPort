package com.hc.tools.lib_nfc.serialport;


import com.hc.tools.lib_nfc.constant.Config;

/**
 * 作者: chenhao
 * 创建日期: 2019-12-29
 * 描述:
 * 串口控制类
 */
public class SerialPortController implements OnReadListener, OnWriteListener {

    private SerialPortCore mSerialPortCore;
    private ReadThead mReadThead;
    private WriteThread mWriteThread;
    private volatile OnSerialPortControllerListener listener;

    public void setOnSerialPortControllerListener(OnSerialPortControllerListener listener) {
        this.listener = listener;
    }

    public void init() {
        mSerialPortCore = new SerialPortCore();
        mSerialPortCore.initPort();
        if (mSerialPortCore.getState() == SerialPortCore.SerialPortState.CLOSE) {
            if (listener != null) {
                listener.onSerialPortError(Config.CODE_OPEN_ERROR);
            }
            return;
        }
        initThread();
    }


    private void initThread() {
        mReadThead = new ReadThead(mSerialPortCore.getInput());
        mReadThead.setReadListener(this);
        mReadThead.start();

        mWriteThread = new WriteThread(mSerialPortCore.getOutput());
        mWriteThread.setOnWriteListener(this);
        mWriteThread.start();
    }


    /**
     * 发送指令
     *
     * @param data
     */
    public void sendCommond(byte[] data) {
        if (mSerialPortCore.getState() == SerialPortCore.SerialPortState.CLOSE) {
            return;
        }
        if (mWriteThread != null && !mWriteThread.isInterrupted()) {
            mWriteThread.sendCommond(data);
        }
    }

    public void releaseThread() {
        if (mReadThead != null) {
            mReadThead.close();
            mReadThead = null;
        }
        if (mWriteThread != null) {
            mWriteThread.close();
            mWriteThread = null;
        }
    }

    private void releaseSerialPortCore() {
        if (mSerialPortCore != null) {
            mSerialPortCore.release();
            mSerialPortCore = null;
        }
    }

    public void release() {
        releaseSerialPortCore();
        releaseThread();
        if (listener != null) {
            listener = null;
        }
    }

    /**
     * 串口写入成功
     *
     * @param data
     */
    @Override
    public void writeData(byte[] data) {
        if (listener != null) {
            listener.onWriteData(data);
        }
    }


    @Override
    public void writeStop() {
        if (listener != null) {
            listener.onSerialPortError(Config.CODE_WRITE_ERROR);
        }
    }

    /**
     * 串口数据读取
     *
     * @param buffer
     * @param size
     */
    @Override
    public void onReceiveData(byte[] buffer, int size) {
        if (listener != null) {
            listener.onReceiveData(buffer, size);
        }
    }

    @Override
    public void onReadStop() {
        if (listener != null) {
            listener.onSerialPortError(Config.CODE_READ_ERROR);
        }
    }
}
