/* @file DrawingLabelDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: dialog for the text of a label-point
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.graphics.*;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;


public class DrawingLabelDialog extends MyDialog 
                                implements View.OnClickListener
{
  private EditText mLabel;
  private Button mBtnOK;
  // private Button mBtnCancel;

  private ILabelAdder mActivity;
  private float mX;
  private float mY;

  public DrawingLabelDialog( Context context, ILabelAdder activity, float x, float y )
  {
    super(context, R.string.DrawingLabelDialog );
    mActivity = activity;
    mX = x; 
    mY = y;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.drawing_label_dialog, R.string.label_title );

    mLabel     = (EditText) findViewById(R.id.label_text);
    mBtnOK     = (Button) findViewById(R.id.label_ok);
    // mBtnCancel = (Button) findViewById(R.id.button_cancel);

    mBtnOK.setOnClickListener( this );
    // mBtnCancel.setOnClickListener( this );

    mLabel.setTextSize( TDSetting.mTextSize );

  }

  public void onClick(View view)
  {
    // TDLog.Log( TDLog.LOG_INPUT, "DrawingLabelDialog onClick() " + view.toString() );
    if (view.getId() == R.id.label_ok ) {
      mActivity.addLabel( mLabel.getText().toString(), mX, mY );
    }
    dismiss();
  }
}
        

