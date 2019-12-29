package com.hc.tools.lib_nfc.nfc;

public interface OnNFCManagerListener {

    void hasCard(boolean hasCard);

    void getCardNumber(String number);
}
