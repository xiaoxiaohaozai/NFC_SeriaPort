package com.hc.tools.lib_nfc.nfc;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.hc.tools.lib_nfc.aidl.INFCFunction;
import com.hc.tools.lib_nfc.constant.Config;
import com.hc.tools.lib_nfc.serialport.SerialPortController;
import com.hc.tools.lib_nfc.utils.LogUtils;


/**
 * NFC服务
 */
public class NFCService extends Service implements Handler.Callback, SerialPortController.OnSerialPortControllerListener, NFCParse.OnNFCParseListener {

    private SerialPortController mSerialPortController;
    private HandlerThread mSendThread;
    private Handler mSendHandler;

    public static final int MSG_FIND_CARD = 0x01;
    public static final int MSG_FIND_CARD_NUMBER = 0x02;
    public static final int MSG_RESULT = 0x03;
    private NFCParse mNFCParse;


    @Override
    public IBinder onBind(Intent intent) {
        return new NFCBinder();
    }

    /**
     * 绑定服务
     *
     * @param context
     * @param serviceConnection
     */
    public static void bindNFCService(Context context, ServiceConnection serviceConnection) {
        if (context != null) {
            Intent intent = new Intent(context, NFCService.class);
            context.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    /**
     * 解绑服务
     *
     * @param context
     * @param serviceConnection
     */
    public static void unBindNFCService(Context context, ServiceConnection serviceConnection) {
        if (context != null) {
            context.unbindService(serviceConnection);
        }
    }

    @Override
    public void writeData(byte[] data) {
        //LogUtils.d(Config.TAG_NFC, "写入数据---" + ByteUtils.bytes2AscllString(data));

    }

    @Override
    public void onReceiveData(byte[] buffer, int size) {
        //  LogUtils.d(Config.TAG_NFC, "读取数据---" + ByteUtils.bytes2AscllString(buffer, size));
        if (mNFCParse != null) {
            mNFCParse.parse(buffer, size);
        }
    }

    @Override
    public void writeStop() {

    }

    @Override
    public void onReadStop() {

    }

    /**
     * 有卡
     */
    @Override
    public void hasCard() {
        LogUtils.d(Config.TAG_NFC, "有卡");
        openGetNumber();
    }

    /**
     * 无卡
     */
    @Override
    public void noCard() {
        LogUtils.d(Config.TAG_NFC, "无卡");
        closeGetNumber();
    }

    @Override
    public void cardNumber(String number) {
        LogUtils.d(Config.TAG_NFC, "卡号" + number);
        closeGetNumber();
    }

    public class NFCBinder extends INFCFunction.Stub {
        @Override
        public void openFindCard() throws RemoteException {
            LogUtils.d(Config.TAG_NFC, "开启寻卡");
            NFCService.this.openFindCard();
        }

        @Override
        public void stopFindCard() throws RemoteException {
            LogUtils.d(Config.TAG_NFC, "停止寻卡");
            NFCService.this.closeFindCard();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        init();
        LogUtils.d(Config.TAG_NFC, "开启NFC服务");
    }

    private void init() {
        mSerialPortController = new SerialPortController();
        mSerialPortController.setOnSerialPortControllerListener(this);
        mSerialPortController.init();


        //控制指令发送
        mSendThread = new HandlerThread("SEND_THREAD");
        mSendThread.start();
        mSendHandler = new Handler(mSendThread.getLooper(), this);

        //nfc指令解析
        mNFCParse = new NFCParse();
        mNFCParse.setParseListener(this);
    }

    private void release() {
        closeSendThread();
        if (mSerialPortController != null) {
            mSerialPortController.release();
            mSerialPortController = null;
        }
        if (mNFCParse != null) {
            mNFCParse.release();
        }
    }

    @Override
    public void onDestroy() {
        release();
        LogUtils.d(Config.TAG_NFC, "NFC服务销毁");
        super.onDestroy();

    }

    private void closeSendThread() {
        if (mSendHandler != null) {
            mSendHandler.removeCallbacksAndMessages(null);
            mSendHandler = null;
        }
        if (mSendThread != null) {
            mSendThread.quit();
            mSendThread = null;
        }
    }


    /**
     * 开始寻卡
     */
    private void openFindCard() {
        closeFindCard();
        closeGetNumber();
        if (mSendHandler != null) {
            mSendHandler.sendEmptyMessage(MSG_FIND_CARD);
        }
    }

    /**
     * 结束寻卡
     */
    private void closeFindCard() {
        if (mSendHandler != null) {
            mSendHandler.removeMessages(MSG_FIND_CARD);
        }
    }

    /**
     * 开始获取卡号
     */
    private void openGetNumber() {
        if (mSendHandler != null) {
            mSendHandler.sendEmptyMessage(MSG_FIND_CARD_NUMBER);
        }
    }

    /**
     * 停止获取卡号
     */
    private void closeGetNumber() {
        if (mSendHandler != null) {
            mSendHandler.removeMessages(MSG_FIND_CARD_NUMBER);
        }
    }

    private void repeat(int type, byte[] data, int time) {
        if (mSerialPortController != null) {
            mSerialPortController.sendCommond(data);
            mSendHandler.sendEmptyMessageDelayed(type, time);
        }
    }

    /**
     * 处理消息
     *
     * @param message
     * @return
     */
    @Override
    public boolean handleMessage(Message message) {

        switch (message.what) {
            case MSG_FIND_CARD://寻卡指令
                repeat(MSG_FIND_CARD, Config.CMD_BYTES_FIND_CARD_SEND, 300);
                break;
            case MSG_FIND_CARD_NUMBER://查询号码
                repeat(MSG_FIND_CARD_NUMBER, Config.CMD_BYTES_GET_CARD_NUMBER_SEND, 500);
                break;
        }
        return false;
    }
}
