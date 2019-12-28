// INFCServiceListener.aidl
package com.hc.tools.lib_nfc;

// Declare any non-default types here with import statements

interface INFCServiceListener {
   void hasCard(boolean hasCard);
   void getNumber(String number);
}
