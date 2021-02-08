/* @file BleOpDisconnect.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth LE disconnect operation 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import com.topodroid.utils.TDLog; 

import android.content.Context;

import android.util.Log;

public class BleOpDisconnect extends BleOperation 
{
  public BleOpDisconnect( Context ctx, BleComm pipe )
  {
    super( ctx, pipe );
  }

  // public String name() { return "Disconnect"; }

  @Override 
  public void execute()
  {
    // Log.v("DistoX-BLE", "BleOp exec disconnect");
    if ( mPipe == null ) { 
      TDLog.Error("BleOp disconnect error: null pipe" );
      return;
    }
    if ( mPipe != null ) mPipe.disconnectGatt();
  }
}
