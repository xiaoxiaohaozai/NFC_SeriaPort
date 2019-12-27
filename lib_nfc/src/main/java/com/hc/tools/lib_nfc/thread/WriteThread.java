package com.hc.tools.lib_nfc.thread;

import com.hc.tools.lib_nfc.utils.ByteUtils;
import com.hc.tools.lib_nfc.utils.LogUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 写入线程
 */
public class WriteThread extends Thread {
    private static final int DEFAULT_QUEUE_SIZE = 30;
    private  OutputStream output;
    private  BlockingQueue<byte[]> mSendQueues;//指令队列

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
        LogUtils.d("写线程---等待写入");
        while (!isInterrupted() && output != null && mSendQueues != null) {
            try {
                byte[] bytes = mSendQueues.take();
                if (bytes.length > 0) {
                    output.write(bytes);
                    LogUtils.d("写线程---写入: " + ByteUtils.bytes2String(bytes));
                    if (onWriteListener != null) {
                        onWriteListener.writeData(bytes);
                    }
                }
                sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        if (onWriteListener != null) {
            onWriteListener.writeStop();
        }
        close();
        LogUtils.d("写线程---停止");
    }

    /**
     * 发送指令
     *
     * @param bytes
     */
    public void sendCommond(byte[] bytes) {
        if (isAlive() && mSendQueues != null) {
            if (mSendQueues.size() < DEFAULT_QUEUE_SIZE) {
                mSendQueues.add(bytes);
            }
        }
    }


    public void close() {
        if (mSendQueues != null) {
            mSendQueues.clear();
            mSendQueues = null;
        }
        if (!isInterrupted()) {
            interrupt();
        }
        if (output != null) {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
