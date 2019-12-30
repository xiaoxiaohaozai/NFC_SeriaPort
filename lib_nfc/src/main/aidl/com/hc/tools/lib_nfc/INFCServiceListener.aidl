
package com.hc.tools.lib_nfc;

interface INFCServiceListener {
   void hasCard(boolean hasCard);
   void getNumber(String number);
}
