package com.hc.tools.lib_nfc.nfc;


import android.text.TextUtils;

import com.hc.tools.lib_nfc.utils.LogUtils;
import com.hc.tools.lib_nfc.utils.RegUtils;

import static com.hc.tools.lib_nfc.constant.Config.CMD_FIND_CARD_RESPONSE_FAIL;
import static com.hc.tools.lib_nfc.constant.Config.CMD_FIND_CARD_RESPONSE_SUCCESS;
import static com.hc.tools.lib_nfc.constant.Config.FRAME_HEAD;
import static com.hc.tools.lib_nfc.constant.Config.LENGTH_FRAME_HEAD;
import static com.hc.tools.lib_nfc.constant.Config.LENGTH_FRAME_LENGTH;
import static com.hc.tools.lib_nfc.constant.Config.MIN_LENGTH;
import static com.hc.tools.lib_nfc.constant.Config.OFFSET_CMD_CODE;
import static com.hc.tools.lib_nfc.constant.Config.OFFSET_CMD_DATA;
import static com.hc.tools.lib_nfc.constant.Config.OFFSET_LENGTH;
import static com.hc.tools.lib_nfc.constant.Config.TAG_NFC_PARSE;


/**
 * 作者: chenhao
 * 创建日期: 2019-12-29
 * 描述:
 * NFC 数据解析
 * JSC 0004 00 00
 * JSC 000C 00 97A4886E D5
 * xx xxx  0x30 0x30 0x30 0x43
 */
public class NFCParse {


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
            //删除该帧
            reset(frameLength);
            sort(target);
        }
    }

    /**
     * 帧分类
     *
     * @param target
     */
    private void sort(String target) {
        if (TextUtils.equals(CMD_FIND_CARD_RESPONSE_SUCCESS, target)) {
            if (parseListener != null) {
                parseListener.hasCard(true);
            }
        } else if (TextUtils.equals(CMD_FIND_CARD_RESPONSE_FAIL, target)) {
            if (parseListener != null) {
                parseListener.hasCard(false);
            }
        } else {
            if (parseListener != null) {
                if (!TextUtils.isEmpty(target) && target.length() > MIN_LENGTH) {
                    parseListener.getCardNumber(target.substring(OFFSET_CMD_DATA, target.length() - 2));
                }
            }
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
        mHasFoundHead = false;
        mResult.delete(0, mResult.length());
    }

}
