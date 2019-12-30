package com.hc.tools.nfc_serialport;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hc.tools.lib_nfc.nfc.NFCManager;
import com.hc.tools.lib_nfc.nfc.OnNFCErrorListener;
import com.hc.tools.lib_nfc.nfc.OnNFCManagerListener;

/**
 * 作者: chenhao
 * 创建日期: 2019-12-29
 * 描述:
 * sdk基本使用方式
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bt_1;
    private Button bt_2;
    private Button bt_3;
    private Button bt_4;
    private TextView tv_state;

    private volatile boolean currentHasCard = false;
    private volatile String currentCardNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initData();
    }

    private void initData() {
        tv_state.setTextColor(Color.RED);
        tv_state.setText("请放置卡片");
        currentHasCard = false;
        currentCardNumber = "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        bt_1 = findViewById(R.id.bt_1);
        bt_2 = findViewById(R.id.bt_2);
        bt_3 = findViewById(R.id.bt_3);
        bt_4 = findViewById(R.id.bt_4);
        tv_state = findViewById(R.id.tv_state);
        bt_1.setOnClickListener(this);
        bt_2.setOnClickListener(this);
        bt_3.setOnClickListener(this);
        bt_4.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_1:
                NFCManager.get().init(this);
                NFCManager.get().setNfcManagerListener(new OnNFCManagerListener() {
                    @Override
                    public void hasCard(boolean hasCard) {
                        updateTv(hasCard);
                    }

                    @Override
                    public void getCardNumber(String number) {
                        updateTv(number);
                    }
                });
                NFCManager.get().setNfcErrorListener(new OnNFCErrorListener() {
                    @Override
                    public void OnNFCError(int errorCode) {

                    }
                });
                break;
            case R.id.bt_2:
                NFCManager.get().release(this);
                break;
            case R.id.bt_3:
                NFCManager.get().openFindCard();
                break;
            case R.id.bt_4:
                NFCManager.get().stopFindCard();
                break;
        }
    }

    private void updateTv(boolean hasCard) {
        if (currentHasCard != hasCard) {
            currentHasCard = hasCard;
            if (!hasCard) {
                currentCardNumber = "";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_state.setTextColor(Color.RED);
                        tv_state.setText("请放置卡片");
                    }
                });
            }
        }
    }

    private void updateTv(final String number) {
        if (currentHasCard) {
            if (!TextUtils.equals(currentCardNumber, number)) {
                currentCardNumber = number;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_state.setTextColor(Color.GREEN);
                        tv_state.setText(String.format("识别卡号: %s", number));
                    }
                });
            }
        }
    }


}
