package com.hc.tools.lib_nfc.nfc;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.hc.tools.lib_nfc.INFCServiceFunction;
import com.hc.tools.lib_nfc.INFCServiceListener;
import com.hc.tools.lib_nfc.utils.LogUtils;

/**
 * 支持自动读卡
 * 暂不支持手动干预
 * 子进程
 */
public class NFCManager implements ServiceConnection {

    @SuppressLint("StaticFieldLeak")
    private static volatile NFCManager instance;
    private INFCServiceFunction mNfcServiceFunction;


    public static NFCManager get() {
        if (instance == null) {
            synchronized (NFCManager.class) {
                if (instance == null) {
                    instance = new NFCManager();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        NFCService.bindNFCService(context, this);
    }

    public void release(Context context) {
        if (isAlive()) {
            try {
                mNfcServiceFunction.unregisterNFCServiceListener(nfcServiceCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        NFCService.unBindNFCService(context, this);
    }

    private boolean isAlive() {
        return mNfcServiceFunction != null && mNfcServiceFunction.asBinder().isBinderAlive();
    }

    /**
     * 开始寻卡
     */
    public void openFindCard() {
        if (isAlive()) {
            try {
                mNfcServiceFunction.openFindCard();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 结束寻卡
     */
    public void stopFindCard() {
        if (isAlive()) {
            try {
                mNfcServiceFunction.stopFindCard();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开始获取号码
     */
    public void openFindCardNumber() {
        if (isAlive()) {
            try {
                mNfcServiceFunction.openFindCardNumber();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止获取号码
     */
    public void stopFindCardNumber() {
        if (isAlive()) {
            try {
                mNfcServiceFunction.stopFindCardNumber();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        mNfcServiceFunction = INFCServiceFunction.Stub.asInterface(service);
        try {
            mNfcServiceFunction.registerNFCServiceListener(nfcServiceCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注意回调不在主线程
     */
    private INFCServiceListener nfcServiceCallback = new INFCServiceListener.Stub() {
        @Override
        public void hasCard(boolean hasCard) throws RemoteException {
          LogUtils.d("NFCManager",Thread.currentThread().getName()+ (hasCard ? "有卡" : "无卡"));

        }

        @Override
        public void getNumber(String number) throws RemoteException {
            LogUtils.d("NFCManager", "卡号---" + number);
        }
    };

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mNfcServiceFunction = null;
    }


}
