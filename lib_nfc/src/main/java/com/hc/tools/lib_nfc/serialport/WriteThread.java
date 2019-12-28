package com.hc.tools.lib_nfc.serialport;

import com.hc.tools.lib_nfc.constant.Config;
import com.hc.tools.lib_nfc.utils.ByteUtils;
import com.hc.tools.lib_nfc.utils.LogUtils;

import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 写入线程
 */
public class WriteThread extends Thread {
    private static final int DEFAULT_QUEUE_SIZE = 30;
    private volatile OutputStream output;
    private BlockingQueue<byte[]> mSendQueues;//指令队列

    private OnWriteListener onWriteListener;

    public void setOnWriteListener(OnWriteListener onWriteListener) {
        this.onWriteListener = onWriteListener;
    }

    public WriteThread(OutputStream output) {
        this.output = output;
        mSendQueues = new ArrayBlockingQueue<>(DEFAULT_QUEUE_SIZE);
    }

    @Override
    public void run() {

        while (!isInterrupted() && output != null && mSendQueues != null) {
            try {

                LogUtils.d(Config.TAG_SERIAL_PORT, "正在等待写入数据");
                byte[] bytes = mSendQueues.take();
                if (bytes.length > 0) {
                    output.write(bytes);
                    LogUtils.d(Config.TAG_SERIAL_PORT, "写入数据---" + ByteUtils.bytes2String(bytes, bytes.length));
                    if (onWriteListener != null) {
                        onWriteListener.writeData(bytes);
                    }
                }
                sleep(100);
            } catch (Exception e) {
                LogUtils.d(Config.TAG_SERIAL_PORT, "写---异常退出");
                break;
            }
        }
        if (onWriteListener != null) {
            onWriteListener.writeStop();
        }
        close();
        LogUtils.d(Config.TAG_SERIAL_PORT, "写---停止");
    }

    /**
     * 发送指令
     *
     * @param bytes
     */
    public void sendCommond(byte[] bytes) {
        if (isAlive() && mSendQueues != null) {
            if (mSendQueues.size() < DEFAULT_QUEUE_SIZE - 1) {//避免发送端数据满了阻塞发送线程
                mSendQueues.add(bytes);
            }
        }
    }


    public void close() {
        if (!isInterrupted()) {
            interrupt();
        }
        if (mSendQueues != null) {
            mSendQueues.clear();
            mSendQueues = null;
        }
        if (output != null) {
            output = null;
        }
        if (onWriteListener != null) {
            onWriteListener = null;
        }
    }

    public interface OnWriteListener {
        void writeData(byte[] data);

        void writeStop();
    }


}
