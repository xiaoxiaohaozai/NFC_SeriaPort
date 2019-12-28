package com.hc.tools.lib_nfc.nfc;

public interface OnNFCParseListener {
    void hasCard(boolean hasCard);

    void getCardNumber(String number);
}
