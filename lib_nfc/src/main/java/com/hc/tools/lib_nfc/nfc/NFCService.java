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

import com.hc.tools.lib_nfc.INFCErrorListener;
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
 * 作者: chenhao
 * 创建日期: 2019-12-29
 * 描述: NFC核心服务,子进程
 */
public class NFCService extends Service implements Handler.Callback, OnNFCParseListener, OnSerialPortControllerListener {

    private SerialPortController mSerialPortController;
    private HandlerThread mSendThread;
    private Handler mSendHandler;
    private NFCParse mNFCParse;
    //远程回调
    private RemoteCallbackList<INFCServiceListener> serviceListenerRemoteCallbackList = new RemoteCallbackList<>();
    private RemoteCallbackList<INFCErrorListener> errorListenerRemoteCallbackList = new RemoteCallbackList<>();

    //感应到卡后是否自动查询号码
    private volatile boolean isAutoGetNumber = true;
    //是否显示日志
    private volatile boolean showLog = true;

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

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new NFCBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtils.d(Config.TAG_NFC, "NFC服务解绑");
        release();
        return super.onUnbind(intent);
    }

    /**
     * 绑定NFC服务
     *
     * @param context
     * @param serviceConnection
     */
    public static void bindNFCService(Context context, ServiceConnection serviceConnection) {
        if (context != null && serviceConnection != null) {
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
        if (context != null && serviceConnection != null) {
            context.unbindService(serviceConnection);
        }
    }

    @Override
    public void onWriteData(byte[] data) {

    }

    /**
     * 监听串口数据
     *
     * @param buffer
     * @param size
     */
    @Override
    public void onReceiveData(byte[] buffer, int size) {
        if (mNFCParse != null) {
            mNFCParse.parse(buffer, size);
        }
    }

    @Override
    public void onSerialPortError(int code) {
        notifyError(code);
    }

    /**
     * 是否显示日志
     *
     * @param showLog
     */
    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    /**
     * 设置自动获取号码
     *
     * @param autoGetNumber
     */
    public void setAutoGetNumber(boolean autoGetNumber) {
        isAutoGetNumber = autoGetNumber;
    }


    @Override
    public void getCardNumber(String number) {
        notifyData(GET_NUMBER, number);
        closeGetNumber();
    }


    /**
     * 回调错误
     *
     * @param errorCode
     */
    private void notifyError(int errorCode) {
        if (errorListenerRemoteCallbackList == null) {
            return;
        }
        int size = errorListenerRemoteCallbackList.beginBroadcast();
        for (int i = 0; i < size; i++) {
            try {
                INFCErrorListener broadcastItem = errorListenerRemoteCallbackList.getBroadcastItem(i);
                broadcastItem.onNFCError(errorCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        errorListenerRemoteCallbackList.finishBroadcast();
    }

    /**
     * 回调结果
     *
     * @param type
     * @param data
     */
    private void notifyData(int type, String data) {
        if (serviceListenerRemoteCallbackList == null) {
            return;
        }
        int size = serviceListenerRemoteCallbackList.beginBroadcast();
        for (int i = 0; i < size; i++) {
            try {
                INFCServiceListener broadcastItem = serviceListenerRemoteCallbackList.getBroadcastItem(i);
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
                e.printStackTrace();
            }
        }
        serviceListenerRemoteCallbackList.finishBroadcast();
    }


    @Override
    public void hasCard(boolean hasCard) {
        if (hasCard) {
            notifyData(HAS_CARD, null);
            openGetNumber();
        } else {
            notifyData(NO_CARD, null);
            closeGetNumber();
        }
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

        @Override
        public void setAutoGetNumber(boolean autoGetNumber) throws RemoteException {
            NFCService.this.setAutoGetNumber(autoGetNumber);
        }


        @Override
        public void registerNFCServiceListener(INFCServiceListener listener) throws RemoteException {
            if (listener != null) {
                serviceListenerRemoteCallbackList.register(listener);
            }
        }

        @Override
        public void unregisterNFCServiceListener(INFCServiceListener listener) throws RemoteException {
            if (listener != null) {
                serviceListenerRemoteCallbackList.unregister(listener);
            }
        }

        @Override
        public void registerNFCErrorListener(INFCErrorListener errorlistener) throws RemoteException {
            if (errorlistener != null) {
                errorListenerRemoteCallbackList.register(errorlistener);
            }
        }

        @Override
        public void unregisterNFCErrorListener(INFCErrorListener errorlistener) throws RemoteException {
            if (errorlistener != null) {
                errorListenerRemoteCallbackList.unregister(errorlistener);
            }
        }

        @Override
        public void showLog(boolean show) throws RemoteException {
            NFCService.this.setShowLog(show);
            LogUtils.enable(showLog);
        }
    }


    private void init() {
        LogUtils.enable(showLog);
        LogUtils.d(Config.TAG_NFC, "NFC服务初始化");
        //串口读写控制
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
        if (!isAutoGetNumber) {
            return;
        }
        LogUtils.d(Config.TAG_NFC, "开始获取号码");
        if (mSendHandler != null) {
            mSendHandler.sendEmptyMessage(MSG_FIND_CARD_NUMBER);
        }
    }

    private void closeGetNumber() {
        if (!isAutoGetNumber) {
            return;
        }
        LogUtils.d(Config.TAG_NFC, "停止获取号码");
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


    private void releaseSerialPortController() {
        if (mSerialPortController != null) {
            mSerialPortController.release();
            mSerialPortController = null;
        }
    }

    private void releaseNFCParse() {
        if (mNFCParse != null) {
            mNFCParse.release();
            mNFCParse = null;
        }
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

    private void release() {
        releaseSerialPortController();
        closeSendThread();
        releaseNFCParse();
        if (serviceListenerRemoteCallbackList != null) {
            serviceListenerRemoteCallbackList.kill();
        }
        if (errorListenerRemoteCallbackList != null) {
            errorListenerRemoteCallbackList.kill();
        }
    }

    @Override
    public void onDestroy() {
        release();
        LogUtils.d(Config.TAG_NFC, "NFC销毁");
        super.onDestroy();
    }
}
