package com.hc.tools.lib_nfc.nfc;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.hc.tools.lib_nfc.INFCServiceFunction;
import com.hc.tools.lib_nfc.INFCServiceListener;
import com.hc.tools.lib_nfc.constant.Config;
import com.hc.tools.lib_nfc.serialport.OnSerialPortControllerListener;
import com.hc.tools.lib_nfc.serialport.SerialPortController;
import com.hc.tools.lib_nfc.utils.LogUtils;

import static com.hc.tools.lib_nfc.constant.Config.GET_NUMBER;
import static com.hc.tools.lib_nfc.constant.Config.HAS_CARD;
import static com.hc.tools.lib_nfc.constant.Config.MSG_FIND_CARD;
import static com.hc.tools.lib_nfc.constant.Config.MSG_FIND_CARD_NUMBER;
import static com.hc.tools.lib_nfc.constant.Config.NO_CARD;


/**
 * NFC服务
 */
public class NFCService extends Service implements Handler.Callback, OnNFCParseListener, OnSerialPortControllerListener {

    private SerialPortController mSerialPortController;
    private HandlerThread mSendThread;
    private Handler mSendHandler;


    private NFCParse mNFCParse;
    private RemoteCallbackList<INFCServiceListener> remoteCallbackList = new RemoteCallbackList<>();

    @Override
    public IBinder onBind(Intent intent) {
        return new NFCBinder();
    }

    /**
     * 绑定NFC服务
     *
     * @param context
     * @param serviceConnection
     */
    public static void bindNFCService(Context context, ServiceConnection serviceConnection) {
        if (context != null) {
            Intent intent = new Intent("com.hc.tools.lib_nfc.nfc.NFCService");
            intent.setPackage(context.getPackageName());
            context.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    /**
     * 解绑NFC服务
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

    }

    @Override
    public void onReceiveData(byte[] buffer, int size) {
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
     * 通知主进程
     *
     * @param type
     * @param data
     */
    private void notifyData(int type, String data) {
        int size = remoteCallbackList.beginBroadcast();
        if (remoteCallbackList != null && size > 0) {
            for (int i = 0; i < size; i++) {
                try {
                    INFCServiceListener broadcastItem = remoteCallbackList.getBroadcastItem(i);
                    switch (type) {
                        case HAS_CARD:
                            broadcastItem.hasCard(true);
                            break;
                        case NO_CARD:
                            broadcastItem.hasCard(false);
                            break;
                        case GET_NUMBER:
                            broadcastItem.getNumber(data);
                            break;
                    }
                } catch (Exception e) {

                }
            }
            remoteCallbackList.finishBroadcast();
        }
    }


    @Override
    public void hasCard(boolean hasCard) {
        if (hasCard) {
            //LogUtils.d(Config.TAG_NFC, "有卡");
            notifyData(HAS_CARD, null);
            openGetNumber();
        } else {
            // LogUtils.d(Config.TAG_NFC, "无卡");
            notifyData(NO_CARD, null);
            closeGetNumber();
        }
    }

    @Override
    public void getCardNumber(String number) {
        // LogUtils.d(Config.TAG_NFC, "卡号" + number);
        notifyData(GET_NUMBER, number);
        closeGetNumber();
    }

    public class NFCBinder extends INFCServiceFunction.Stub {
        @Override
        public void openFindCard() throws RemoteException {
            NFCService.this.openFindCard();
        }

        @Override
        public void stopFindCard() throws RemoteException {
            NFCService.this.closeFindCard();
        }

        @Override
        public void openFindCardNumber() throws RemoteException {
            NFCService.this.openGetNumber();
        }

        @Override
        public void stopFindCardNumber() throws RemoteException {
            NFCService.this.closeGetNumber();
        }

        /**
         * 注册监听
         *
         * @param listener
         * @throws RemoteException
         */
        @Override
        public void registerNFCServiceListener(INFCServiceListener listener) throws RemoteException {
            if (listener != null) {
                remoteCallbackList.register(listener);
            }
        }

        /**
         * 取消监听
         *
         * @param listener
         * @throws RemoteException
         */
        @Override
        public void unregisterNFCServiceListener(INFCServiceListener listener) throws RemoteException {
            if (listener != null) {
                remoteCallbackList.unregister(listener);
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        init();
        LogUtils.d(Config.TAG_NFC, "开启NFC服务");
    }

    private void init() {
        LogUtils.enable(false);
        //串口控制
        mSerialPortController = new SerialPortController();
        mSerialPortController.setOnSerialPortControllerListener(this);
        mSerialPortController.init();

        //控制指令发送
        mSendThread = new HandlerThread("SEND_THREAD");
        mSendThread.start();
        mSendHandler = new Handler(mSendThread.getLooper(), this);

        //NFC指令解析
        mNFCParse = new NFCParse();
        mNFCParse.setParseListener(this);
    }

    private void release() {
        remoteCallbackList.kill();
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


    private void openFindCard() {
        LogUtils.d(Config.TAG_NFC, "开启寻卡");
        closeFindCard();
        closeGetNumber();
        if (mSendHandler != null) {
            mSendHandler.sendEmptyMessage(MSG_FIND_CARD);
        }
    }

    private void closeFindCard() {
        LogUtils.d(Config.TAG_NFC, "停止寻卡");
        if (mSendHandler != null) {
            mSendHandler.removeMessages(MSG_FIND_CARD);
        }
    }

    private void openGetNumber() {
        if (mSendHandler != null) {
            mSendHandler.sendEmptyMessage(MSG_FIND_CARD_NUMBER);
        }
    }


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
