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
import com.hc.tools.lib_nfc.constant.SerialPortConfig;
import com.hc.tools.lib_nfc.serialport.SerialPortController;
import com.hc.tools.lib_nfc.utils.LogUtils;


/**
 * NFC服务
 */
public class NFCService extends Service implements Handler.Callback {

    private SerialPortController mSerialPortController;
    private HandlerThread mSendThread;
    private Handler mSendHandler;

    public static final int MSG_FIND_CARD = 0x01;
    public static final int MSG_FIND_CARD_NUMBER = 0x02;
    public static final int MSG_RESULT = 0x03;



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

    public class NFCBinder extends INFCFunction.Stub {
        @Override
        public void openFindCard() throws RemoteException {
            LogUtils.d("NFC", "开启寻卡");
            NFCService.this.openFindCard();
        }

        @Override
        public void stopFindCard() throws RemoteException {
            LogUtils.d("NFC", "停止寻卡");
            NFCService.this.closeFindCard();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        init();
        LogUtils.d("NFC", "开启NFC服务");
    }

    private void init() {
        mSerialPortController = new SerialPortController();
        mSerialPortController.init();

        //控制指令发送
        mSendThread = new HandlerThread("SEND_THREAD");
        mSendThread.start();
        mSendHandler = new Handler(mSendThread.getLooper(), this);
    }

    private void release() {
        if (mSerialPortController != null) {
            mSerialPortController.release();
            mSerialPortController = null;
        }
        closeSendThread();
    }

    @Override
    public void onDestroy() {

        release();

        LogUtils.d("NFC", "停止NFC服务");
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

    private void repeatFindCard() {
        if (mSerialPortController != null) {
            mSerialPortController.sendCommond(SerialPortConfig.CMD_BYTES_FIND_CARD_SEND);
            mSendHandler.sendEmptyMessageDelayed(MSG_FIND_CARD, 300);
        }
    }

    @Override
    public boolean handleMessage(Message message) {

        switch (message.what) {
            case MSG_FIND_CARD://寻卡指令
                repeatFindCard();
                break;
            case MSG_FIND_CARD_NUMBER:

                break;
        }
        return false;
    }
}
