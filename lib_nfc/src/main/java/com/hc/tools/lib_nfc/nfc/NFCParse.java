package com.hc.tools.lib_nfc.nfc;


import android.text.TextUtils;

import com.hc.tools.lib_nfc.utils.LogUtils;
import com.hc.tools.lib_nfc.utils.RegUtils;

import static com.hc.tools.lib_nfc.constant.Config.CMD_FIND_CARD_RESPONSE_FAIL;
import static com.hc.tools.lib_nfc.constant.Config.CMD_FIND_CARD_RESPONSE_SUCCESS;
import static com.hc.tools.lib_nfc.constant.Config.CMD_GET_CARD_NUMBER_SEND;
import static com.hc.tools.lib_nfc.constant.Config.TAG_NFC_PARSE;

/**
 * nfc 数据解析
 * <p>
 * // JSC 0004 00 00
 * // JSC 000C 00 97A4886E D5
 * // xx xxx  0x30 0x30 0x30 0x43
 */
public class NFCParse {

    public static final String FRAME_HEAD = "JSC";
    public static final int MIN_LENGTH = 11;
    public static final int LENGTH_FRAME_HEAD = 3;
    public static final int LENGTH_FRAME_LENGTH = 4;
    public static final int OFFSET_LENGTH = 3;
    public static final int OFFSET_CMD_CODE = 7;
    public static final int OFFSET_CMD_DATA = 9;

    private StringBuilder mResult = new StringBuilder();


    private boolean mHasFoundHead;

    public volatile OnNFCParseListener parseListener;

    public void setParseListener(OnNFCParseListener parseListener) {
        this.parseListener = parseListener;
    }

    /**
     * 解析数据
     *
     * @param buffer
     * @param size
     */
    public void parse(byte[] buffer, int size) {
        //超过最大容量
        if (mResult.length() >= 128) {
            reset(mResult.length());
        }
        //获取输入的字符形式
        for (int i = 0; i < size; i++) {
            char c = (char) buffer[i];
            mResult.append(c);
        }
        //小于最小长继续接收
        if (mResult.length() < MIN_LENGTH) {
            return;
        }
        //LogUtils.d(TAG_NFC_PARSE, "数据帧---待解析数据---" + mResult);
        //找到帧头
        if (!mHasFoundHead) {
            findHead();
        }
        if (!mHasFoundHead) {
            return;
        }
        //获取数据区长度,例如OOC
        String lengthStr = mResult.substring(OFFSET_LENGTH, OFFSET_CMD_CODE);
        if (!RegUtils.isRegEx(lengthStr)) {// 可能帧长不是16进制表示
            LogUtils.d(TAG_NFC_PARSE, "数据帧---长度数据错误");
            reset(mResult.length());
            return;
        }
        //获取数据区长度
        int length = Integer.parseInt(lengthStr, 16);
        //获取帧总长
        int frameLength = LENGTH_FRAME_HEAD + LENGTH_FRAME_LENGTH + length;
        if (mResult.length() >= frameLength) {
            if (length % 2 != 0) {
                LogUtils.d(TAG_NFC_PARSE, "数据帧---帧数据错误");
                reset(frameLength);//删除这一帧
                return;
            }
            //获取完整帧
            String target = mResult.substring(0, frameLength);
            LogUtils.d(TAG_NFC_PARSE, "数据帧---完整---" + target);
            //去掉帧头的剩余部分也需要判断
            if (!RegUtils.isRegEx(target.substring(OFFSET_LENGTH))) {
                reset(frameLength);//删除这一帧
                return;
            }
            // 判断操作码 00
            int checkCode = Integer.parseInt(target.substring(OFFSET_CMD_CODE, OFFSET_CMD_CODE + 2), 16);
            for (int i = OFFSET_CMD_CODE + 2; i < target.length() - 2; i += 2) {
                checkCode = (checkCode ^ Integer.parseInt(target.substring(i, i + 2), 16));
            }
            if (checkCode != Integer.parseInt(target.substring(target.length() - 2), 16)) {
                LogUtils.d(TAG_NFC_PARSE, "数据帧---校验码错误");
                reset(frameLength);
                return;
            }
            reset(frameLength);//删除这一帧
            sort(target);
        }
    }

    /**
     * 帧分类
     *
     * @param target
     */
    private void sort(String target) {
        switch (target) {
            case CMD_FIND_CARD_RESPONSE_SUCCESS://有卡
                if (parseListener != null) {
                    parseListener.hasCard();
                }
                break;
            case CMD_FIND_CARD_RESPONSE_FAIL://无卡
                if (parseListener != null) {
                    parseListener.noCard();
                }
                break;
            default:
                if (parseListener != null) {
                    if (!TextUtils.isEmpty(target) && target.length() > MIN_LENGTH) {
                        parseListener.cardNumber(target.substring(OFFSET_CMD_DATA, target.length() - 2));
                    }
                }
                break;
        }
    }

    /**
     * 获取帧头
     */
    private void findHead() {
        String result = mResult.toString();
        if (result.startsWith(FRAME_HEAD)) {
            mHasFoundHead = true;
            return;
        }
        // 清除帧头前的所有数据
        if (result.contains(FRAME_HEAD)) {
            int index = result.indexOf(FRAME_HEAD);
            mResult.delete(0, index);
        }
    }

    /**
     * 清除缓冲区
     *
     * @param length
     */
    private void reset(int length) {
        mHasFoundHead = false;
        mResult.delete(0, length);
    }

    public void release() {
        if (parseListener != null) {
            parseListener = null;
        }
        mHasFoundHead = false;

    }

    public interface OnNFCParseListener {
        void hasCard();

        void noCard();

        void cardNumber(String number);
    }
}
