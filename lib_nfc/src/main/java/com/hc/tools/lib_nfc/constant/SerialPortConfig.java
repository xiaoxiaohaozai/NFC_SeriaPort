package com.hc.tools.lib_nfc.constant;

public class SerialPortConfig {
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

    public static final String FRAME_HEAD = "JSC";
    public static final int MIN_LENGTH = 11;
    public static final int LENGTH_FRAME_HEAD = 3;
    public static final int LENGTH_FRAME_LENGTH = 4;
    public static final int LENGTH_CMD_CODE = 2;
    public static final int LENGTH_CMD_CHECK_CODE = 2;
    public static final int OFFSET_LENGTH = 3;
    public static final int OFFSET_CMD_CODE = 7;
    public static final int OFFSET_CMD_DATA = 9;
}
