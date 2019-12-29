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
 * 作者: chenhao
 * 创建日期: 2019-12-29
 * 描述:
 * NFC 管理类
 * 1.支持自动读卡
 * 2.监听有卡或无卡状态
 * 3.监听卡号获取
 */
public class NFCManager implements ServiceConnection {

    @SuppressLint("StaticFieldLeak")
    private static volatile NFCManager instance;
    private INFCServiceFunction mNfcServiceFunction;
    private volatile OnNFCManagerListener nfcManagerListener;


    public void setNfcManagerListener(OnNFCManagerListener nfcManagerListener) {
        this.nfcManagerListener = nfcManagerListener;
    }

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

    public NFCManager init(Context context) {
        NFCService.bindNFCService(context, this);
        return this;
    }

    public void release(Context context) {
        if (isAlive()) {
            try {
                mNfcServiceFunction.unregisterNFCServiceListener(nfcServiceCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (nfcManagerListener != null) {
            nfcManagerListener = null;
        }
        NFCService.unBindNFCService(context, this);
    }

    private boolean isAlive() {
        return mNfcServiceFunction != null && mNfcServiceFunction.asBinder().isBinderAlive();
    }

    /**
     * 开始寻卡
     */
    public NFCManager openFindCard() {
        if (isAlive()) {
            try {
                mNfcServiceFunction.openFindCard();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return this;
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
    public NFCManager openFindCardNumber() {
        if (isAlive()) {
            try {
                mNfcServiceFunction.openFindCardNumber();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return this;
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

    /**
     * 是否开启自动寻卡
     *
     * @param show
     */
    public NFCManager showLog(boolean show) {
        if (isAlive()) {
            try {
                mNfcServiceFunction.showLog(show);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    /**
     * 是否开启自动寻卡
     *
     * @param autoGetNumber
     */
    public NFCManager setAutoGetNumber(boolean autoGetNumber) {
        if (isAlive()) {
            try {
                mNfcServiceFunction.setAutoGetNumber(autoGetNumber);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    /**
     * 远程服务连接
     *
     * @param componentName
     * @param service
     */
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
     * 连接断开
     *
     * @param componentName
     */
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mNfcServiceFunction = null;
    }


    //TODO 注意回调不在主线程
    private INFCServiceListener nfcServiceCallback = new INFCServiceListener.Stub() {
        @Override
        public void hasCard(boolean hasCard) throws RemoteException {
            LogUtils.d("NFCManager", hasCard ? "有卡" : "无卡");
            if (nfcManagerListener != null) {
                nfcManagerListener.hasCard(hasCard);
            }
        }

        @Override
        public void getNumber(String number) throws RemoteException {
            LogUtils.d("NFCManager", "卡号---" + number);
            if (nfcManagerListener != null) {
                nfcManagerListener.getCardNumber(number);
            }
        }
    };


}
