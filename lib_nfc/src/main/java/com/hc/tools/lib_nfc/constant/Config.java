package com.hc.tools.lib_nfc.constant;

public class Config {

    public static final String TAG_SERIAL_PORT= "串口";
    public static final String TAG_NFC = "NFC";
    public static final String TAG_NFC_PARSE="解析";

    public static final int BAUDRATE = 115200;
    public static final String PATH = "/dev/ttyS1";

    // 寻卡指令
    public static final String CMD_FIND_CARD_SEND = "JSC00041212";
    // 寻卡失败
    public static final String CMD_FIND_CARD_RESPONSE_FAIL = "JSC00040101";
    // 寻卡成功
    public static final String CMD_FIND_CARD_RESPONSE_SUCCESS = "JSC00040000";
    // 获取卡号
    public static final String CMD_GET_CARD_NUMBER_SEND = "JSC00041313";

    public static final byte[] CMD_BYTES_FIND_CARD_SEND = CMD_FIND_CARD_SEND.getBytes();
    public static final byte[] CMD_BYTES_GET_CARD_NUMBER_SEND = CMD_GET_CARD_NUMBER_SEND.getBytes();


}
