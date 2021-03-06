/* @file TD_DistoXComm.java
 *
 * @author marco corvi
 * @date jun 2016
 *
 * @brief TopoDroid - DistoX_server communication 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.nio.ByteBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import android.content.Context;
import android.content.ComponentName;
import android.content.ServiceConnection;

import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.IBinder;

// import android.os.Parcelable;
import android.os.ParcelUuid;
// import android.os.AsyncTask;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import android.database.DataSetObserver;

// import android.widget.Toast;
import android.util.Log;

public class TD_DistoXComm extends TopoDroidComm
{
  private String mAddress;
  private TD_DistoX mServer;

  // private static final byte CALIB_BIT = (byte)0x08;
  // public byte[] mCoeff;

// -----------------------------------------------------------

  TD_DistoXComm( TopoDroidApp app, TD_DistoX server )
  {
    super( app );
    mAddress   = Device.ZERO_ADDRESS;
    mServer    = server;
    // Log.v( "TD_DistoX", "TD Comm cstr. address " + mAddress );
  }

  // public void resume()
  // {
  //   // if ( mRfcommThread != null ) { mRfcommThread.resume(); }
  // }

  // public void suspend()
  // {
  //   // if ( mRfcommThread != null ) { mRfcommThread.suspend(); }
  // }

  // -------------------------------------------------------- 
  
  public void disconnectRemoteDevice( )
  {
    super.disconnectRemoteDevice();
    mServer.unbindServer();
    // Log.v( "TD_DistoX", "TD comm disconnect remote");
  }

  /** Toggle device calibration mode
   * @param address    device address
   * @param type       device type
   * @return 
   */
  public boolean toggleCalibMode( String address, int type )
  {
    Log.v("TD_DistoX", "TD comm toggleCalibMode");
    boolean ret = false;
    if ( createSocket() ) {
      byte[] result = new byte[4];
      if ( mProtocol.read8000( result ) ) { 
        if ( (result[0] & CALIB_BIT) == 0 ) {
          ret = mProtocol.sendCommand( (byte)0x31 );  // TOGGLE CALIB ON
        } else {
          ret = mProtocol.sendCommand( (byte)0x30 );  // TOGGLE CALIB OFF
        }
      }
      destroySocket();
    }
    return ret;
  }

  public boolean writeCoeff( String address, byte[] coeff )
  {
    Log.v("TD_DistoX", "TD comm writeCoeff");
    boolean ret = false;
    if ( createSocket() ) {
      ret = mProtocol.writeCalibration( mCoeff );
      destroySocket();
    }
    return ret;
  }

  // called only by CalibReadTask
  public boolean readCoeff( String address, byte[] coeff )
  {
    Log.v("TD_DistoX", "TD comm readCoeff");
    boolean ret = false;
    if ( createSocket() ) {
      ret = mProtocol.readCalibration( coeff );
      destroySocket();
    }
    return ret;
  }

  // low-level memory read
  // called by TopoDroidApp.readMemory
  byte[] readMemory( String address, int addr )
  {
    Log.v("TD_DistoX", "TD comm readMemory");
    return null;
  }

  // ------------------------------------------------------------------------------------
  // CONTINUOUS DATA DOWNLOAD

  public boolean connectDevice( String address, Handler /* ILister */ lister ) // FIXME LISTER
  {
    // Log.v("TD_DistoX", "TD comm connect device");
    if ( createSocket() ) {
      startRfcommThread( -2, lister );
      return true;
    }
    return false;
  }

  public void disconnect()
  {
    // Log.v("TD_DistoX", "TD comm disconnectDevice");
    cancelRfcommThread();
    destroySocket();
  }


  private boolean createSocket( )
  {
    if ( mServer != null ) {
      mServer.bindServer( );
      DataInputStream in   = mServer.getInputStream();
      DataOutputStream out = mServer.getOutputStream();
      if ( in != null && out != null ) {
        mProtocol = new DistoXProtocol( in, out, mApp.mDevice );
        return true;
      } else {
        clearServer();
        return false;
      }
    } else {
      TDLog.Error("TD_DistoX comm create socket - server null");
    }
    clearServer();
    return false;
  }

  private void destroySocket()
  {    
    // Log.v("TD_DistoX", "TD comm destroy socket");
    clearServer();
  }

  private void clearServer()
  {
    // Log.v("TD_DistoX", "TD comm unbind server");
    if ( mServer != null ) mServer.unbindServer( );
  }


  protected boolean startRfcommThread( int to_read, Handler /* ILister */ lister ) // FIXME LISTER
  {
    // Log.v( "TD_DistoX", "start RFcomm thread: to_read " + to_read );
    if ( mServer != null ) {
      if ( mRfcommThread == null ) {
        mRfcommThread = new RfcommThread( mProtocol, to_read, lister );
        mRfcommThread.start();
        // TDLog.Log( TDLog.LOG_COMM, "startRFcommThread started");
      } else {
        // Log.v("TD_DistoX", "startRFcommThread already running");
      }
      return true;
    } else {
      mRfcommThread = null;
      // Log.e("TD_DistoX", "startRFcommThread: unbound server");
      return false;
    }
  }
  // -------------------------------------------------------------------------------------
  // ON-DEMAND DATA DOWNLOAD

  public int downloadData( String address, Handler /* ILister */ lister ) // FIXME LISTER
  {
    int ret = 0;
    // Log.v("TD_DistoX", "TD comm download data");
    if ( createSocket() ) {
      // download with timeout
      startRfcommThread( -1, lister );
      while ( mRfcommThread != null ) {
        try { Thread.sleep( 100 ); } catch ( InterruptedException e ) { }
      }
      ret = nReadPackets;
      destroySocket();
    }
    return ret;
  }

  // ====================================================================================
};
