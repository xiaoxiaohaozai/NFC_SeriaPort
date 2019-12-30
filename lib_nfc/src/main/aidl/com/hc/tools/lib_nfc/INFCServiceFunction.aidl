// INFCFunction.aidl
package com.hc.tools.lib_nfc;

import com.hc.tools.lib_nfc.INFCServiceListener;

import com.hc.tools.lib_nfc.INFCErrorListener;

interface INFCServiceFunction {

   void openFindCard();
   void stopFindCard();

   void openFindCardNumber();
   void stopFindCardNumber();

   void setAutoGetNumber(boolean autoGetNumber) ;

   void registerNFCServiceListener(INFCServiceListener listener);
   void unregisterNFCServiceListener(INFCServiceListener listener);

   void registerNFCErrorListener(INFCErrorListener errorlistener);
   void unregisterNFCErrorListener(INFCErrorListener errorlistener);

   void showLog(boolean show);
}
