package com.hc.tools.lib_nfc.serialport;

import android.serialport.SerialPort;

import com.hc.tools.lib_nfc.constant.Config;
import com.hc.tools.lib_nfc.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 串口核心
 */
public class SerialPortCore {


    public enum SerialPortState {
        OPEN, CLOSE
    }

    private SerialPort mSerialPort;
    private SerialPortState state = SerialPortState.CLOSE;

    public SerialPortState getState() {
        return state;
    }

    public void initPort() {
        try {
            mSerialPort = new SerialPort(new File(Config.PATH), Config.BAUDRATE, 0);
            state = SerialPortState.OPEN;
            LogUtils.d(Config.TAG_SERIAL_PORT, "打开成功");
        } catch (IOException e) {
            e.printStackTrace();
            state = SerialPortState.CLOSE;
            LogUtils.d(Config.TAG_SERIAL_PORT, "打开失败");
        }
    }


    public InputStream getInput() {
        if (mSerialPort != null) {
            return mSerialPort.getInputStream();
        }
        return null;
    }

    public OutputStream getOutput() {
        if (mSerialPort != null) {
            return mSerialPort.getOutputStream();
        }
        return null;
    }

    public void closeInput() {
        if (getInput() != null) {
            try {
                getInput().close();
                LogUtils.d(Config.TAG_SERIAL_PORT, "关闭输入");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeOutput() {
        if (getOutput() != null) {
            try {
                getOutput().close();
                LogUtils.d(Config.TAG_SERIAL_PORT, "关闭输出");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        state = SerialPortState.CLOSE;
        closeInput();
        closeOutput();
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
            LogUtils.d(Config.TAG_SERIAL_PORT, "关闭");
        }
    }

}
