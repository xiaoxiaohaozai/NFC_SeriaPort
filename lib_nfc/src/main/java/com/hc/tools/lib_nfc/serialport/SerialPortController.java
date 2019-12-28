package com.hc.tools.lib_nfc.serialport;


/**
 * 串口读写控制类
 */
public class SerialPortController implements ReadThead.OnReadListener, WriteThread.OnWriteListener {

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
        if (mSerialPortCore.getState() == SerialPortCore.SerialPortState.OPEN) {
            initThread();
        }
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
        if (mSerialPortCore.getState() == SerialPortCore.SerialPortState.OPEN) {
            if (mWriteThread != null && mWriteThread.isAlive()) {
                mWriteThread.sendCommond(data);
            }
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (listener != null) {
            listener = null;
        }
        if (mSerialPortCore != null) {
            mSerialPortCore.release();
            mSerialPortCore = null;
        }
        if (mReadThead != null) {
            mReadThead.close();
            mReadThead = null;
        }
        if (mWriteThread != null) {
            mWriteThread.close();
            mWriteThread = null;
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
            listener.writeData(data);
        }
    }

    /**
     * 写停止或错误
     */
    @Override
    public void writeStop() {
        if (listener != null) {
            listener.writeStop();
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

    /**
     * 读停止或错误
     */
    @Override
    public void onReadStop() {
        if (listener != null) {
            listener.onReadStop();
        }
    }

    public interface OnSerialPortControllerListener {
        void writeData(byte[] data);

        void onReceiveData(byte[] buffer, int size);


        void writeStop();

        void onReadStop();
    }

}
