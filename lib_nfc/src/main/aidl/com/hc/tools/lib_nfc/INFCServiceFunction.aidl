// INFCFunction.aidl
package com.hc.tools.lib_nfc;


import com.hc.tools.lib_nfc.INFCServiceListener;

interface INFCServiceFunction {

   void openFindCard();
   void stopFindCard();

   void openFindCardNumber();
   void stopFindCardNumber();

   void setAutoGetNumber(boolean autoGetNumber) ;

   void registerNFCServiceListener(INFCServiceListener listener);
   void unregisterNFCServiceListener(INFCServiceListener listener);

   void showLog(boolean show);

}
