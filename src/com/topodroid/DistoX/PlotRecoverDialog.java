/** @file PlotRecoverDialog.java
 *
 * @author marco corvi
 * @date jan 2015
 *
 * @brief TopoDroid plot recover dialog
 *
 * displays the stack of saved stations and allows to push 
 * a station on it or pop one from it
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;

import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;

import android.content.Context;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.CheckBox;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.AdapterView.OnItemLongClickListener;

import android.widget.Toast;

import android.util.Log;

public class PlotRecoverDialog extends Dialog
                        implements View.OnClickListener
                        , OnItemClickListener
{
  private Context mContext;
  private TopoDroidApp mApp;
  private DrawingActivity mParent;

  private TextView mTVfilename;
  private Button mBtnOK;

  private ListView mList;
  ArrayAdapter<String> mAdapter;
  String mFilename;
  int mType;

  // type is either 1 or 2
  public PlotRecoverDialog( Context context, DrawingActivity parent, String filename, int type )
  {
    super( context );
    mContext = context;
    mParent  = parent;
    mFilename = filename;
    mType = type;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plot_recover_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mList = (ListView) findViewById(R.id.th2_list);
    mList.setDividerHeight( 2 );
    mList.setOnItemClickListener( this );
 
    mTVfilename = (TextView) findViewById( R.id.filename );

    mBtnOK      = (Button) findViewById(R.id.btn_ok );
    mBtnOK.setOnClickListener( this );   // OK-SAVE
    // mBtnCancel.setOnClickListener( this );

    // setTitle( R.string.title_current_station );
    updateList();
  }

  private String getAge( long age )
  {
    age /= 60000;
    if ( age < 120 ) return Long.toString(age) + "\'";
    age /= 60;
    if ( age < 24 ) return Long.toString(age) + "h";
    age /= 24;
    if ( age < 60 ) return Long.toString(age) + "d";
    age /= 30;
    if ( age < 24 ) return Long.toString(age) + "m";
    age /= 12;
    return Long.toString(age) + "y";
  }
    
  private void populateAdapter( String filename, String ext )
  {

    long millis = System.currentTimeMillis();
    File file = new File( filename );
    if ( file.exists() ) {
      String age = getAge( millis - file.lastModified() );
      mAdapter.add( Long.toString(file.length()) + " " + age + " " +  mFilename + ext );
    }
    String filename1 = filename + ".bck";
    file = new File( filename1 );
    if ( file.exists() ) {
      String age = getAge( millis - file.lastModified() );
      mAdapter.add( Long.toString(file.length()) + " " + age + " " +  mFilename + ext + ".bck" );
    }
    for ( int i=0; i<SaveTh2FileTask.NR_BACKUP; ++i ) {
      filename1 = filename + ".bck" + Integer.toString(i);
      file = new File( filename1 );
      if ( file.exists() ) {
        String age = getAge( millis - file.lastModified() );
        mAdapter.add( Long.toString(file.length()) + " " + age + " " +  mFilename + ext + ".bck" + Integer.toString(i) );
      }
    }
  }

  private void updateList()
  {
    mAdapter = new ArrayAdapter<String>( mContext, R.layout.message );

    if ( TopoDroidSetting.mBinaryTh2 ) {
      populateAdapter( TopoDroidPath.getTdrFileWithExt( mFilename ), ".tdr" );
    } else {
      populateAdapter( TopoDroidPath.getTh2FileWithExt( mFilename ), ".th2" );
    }
    mList.setAdapter( mAdapter );
    mTVfilename.setText( mFilename + ".th2" );
  }


  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String[] name = item.toString().split(" ");
    mTVfilename.setText( name[1] );
  }
 
  @Override
  public void onClick(View v) 
  {
    // TopoDroidLog.Log(  TopoDroidLog.LOG_INPUT, "PlotRecoverDialog onClick() " );
    Button b = (Button) v;
    if ( b == mBtnOK ) { // OK
      mParent.doRecover( mTVfilename.getText().toString(), mType );
      dismiss();
    }
  }

}
