package com.hc.tools.lib_nfc.nfc;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.hc.tools.lib_nfc.aidl.INFCFunction;
import com.hc.tools.lib_nfc.utils.LogUtils;


public class NFCManager implements ServiceConnection {

    @SuppressLint("StaticFieldLeak")
    private static volatile NFCManager instance;
    private INFCFunction mNfcFunction;
    private Context context;

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
        NFCService.unBindNFCService(context, this);
    }

    /**
     * 开始寻卡
     */
    public void openFindCard() {
        if (mNfcFunction != null) {
            try {
                mNfcFunction.openFindCard();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 结束寻卡
     */
    public void stopFindCard() {
        if (mNfcFunction != null) {
            try {
                mNfcFunction.stopFindCard();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        LogUtils.d("NFC", "子线程服务连接");
        mNfcFunction = INFCFunction.Stub.asInterface(service);

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }


}
