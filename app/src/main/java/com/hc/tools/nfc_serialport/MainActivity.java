package com.hc.tools.nfc_serialport;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.hc.tools.lib_nfc.nfc.NFCManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bt_1;
    private Button bt_2;

    private Button bt_3;

    private Button bt_4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initData();
    }

    private void initData() {
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
}
