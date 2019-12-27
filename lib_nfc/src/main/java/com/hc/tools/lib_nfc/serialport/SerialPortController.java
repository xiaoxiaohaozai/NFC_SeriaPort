package com.hc.tools.lib_nfc.serialport;


import com.hc.tools.lib_nfc.thread.ReadThead;
import com.hc.tools.lib_nfc.thread.WriteThread;

/**
 * 串口读写控制类
 */
public class SerialPortController implements ReadThead.OnReadListener, WriteThread.OnWriteListener {

    private SerialPortCore mSerialPortCore;
    private ReadThead mReadThead;
    private WriteThread mWriteThread;


    private static class Holder {
        private static final SerialPortController INSTANCE = new SerialPortController();
    }

    public static SerialPortController getInstance() {
        return Holder.INSTANCE;
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

    public SerialPortCore.SerialPortState getState() {
        if (mSerialPortCore != null) {
            return mSerialPortCore.getState();
        }
        return SerialPortCore.SerialPortState.CLOSE;
    }

    /**
     * 发送指令
     *
     * @param data
     */
    public void sendCommond(byte[] data) {
        if (mWriteThread != null) {
            mWriteThread.sendCommond(data);
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mReadThead != null) {
            mReadThead.close();
            mReadThead = null;
        }
        if (mWriteThread != null) {
            mWriteThread.close();
            mWriteThread = null;
        }
//        if (mSerialPortCore != null) {
//            mSerialPortCore.release();
//            mSerialPortCore = null;
//        }
    }

    /**
     * 串口写入成功
     *
     * @param data
     */
    @Override
    public void writeData(byte[] data) {

    }

    /**
     * 写停止或错误
     */
    @Override
    public void writeStop() {

    }

    /**
     * 串口数据读取
     *
     * @param buffer
     * @param size
     */
    @Override
    public void onReceiveData(byte[] buffer, int size) {

    }

    /**
     * 读停止或错误
     */
    @Override
    public void onReadStop() {

    }


}
