/* @file DrawingActivity.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid main drawing activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Activity;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.PackageManager;

import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Path;
import android.graphics.Path.Direction;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
// import android.view.Menu;
// import android.view.SubMenu;
// import android.view.MenuItem;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.view.ViewGroup;
import android.view.Display;
import android.util.DisplayMetrics;
// import android.view.ContextMenu;
// import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ZoomControls;
import android.widget.ZoomButton;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.RejectedExecutionException;
// import java.util.Deque; // only API-9

// import android.util.SparseArray;
import android.util.Log;

/**
 */
public class DrawingActivity extends ItemDrawer
                             implements View.OnTouchListener
                                      , View.OnClickListener
                                      , View.OnLongClickListener
                                      , OnItemClickListener
                                      , OnItemSelectedListener
                                      , OnZoomListener
                                      , ILabelAdder
                                      , ILister
                                      , IZoomer
                                      , IExporter
{
  private static int izons_ok[] = { 
                        R.drawable.iz_edit_ok, // 0
                        R.drawable.iz_eraser_ok,
                        R.drawable.iz_select_ok };

  private static int IC_DOWNLOAD = 3;
  private static int IC_PLAN     = 7;
  private static int IC_DIAL     = 8;
  private static int IC_JOIN     = 15;
  private static int IC_JOIN_NO  = 20;
  private static int IC_MENU     = 18;
  private static int IC_EXTEND   = 19;
  private static int IC_CONTINUE_NO   = 12;  // index of continue-no icon
  private static int IC_CONTINUE_CONT = 21;     // index of continue icon
  private static int IC_CONTINUE_JOIN = 22;     // index of continue icon
  private static int IC_ADD = 23;

  private static int BTN_DOWNLOAD = 3;  // index of mButton1 download button
  private static int BTN_BLUETOOTH = 4; // index of mButton1 bluetooth button
  private static int BTN_PLOT = 7;      // index of mButton1 plot button
  private static int BTN_DIAL = 8;      // index of mButton1 azimuth button

  private static int BTN_CONTINUE = 6;  // index of mButton2 continue button
  private static int BTN_JOIN = 5;      // index of mButton3 join button
  private static int BTN_REMOVE = 7;    // index of mButton3 remove

  private View mZoomView;

  private static int izons[] = { 
                        R.drawable.iz_edit,          // 0
                        R.drawable.iz_eraser,
                        R.drawable.iz_select,
                        R.drawable.iz_download,      // 3 MOVE Nr 9
                        R.drawable.iz_bt,
                        R.drawable.iz_mode,          // 5
                        R.drawable.iz_note,          // 6
                        R.drawable.iz_plan,          // 7
                        R.drawable.iz_dial,          // 8
                        R.drawable.iz_undo,          // 9 DRAW Nr 7
                        R.drawable.iz_redo,          // 10
                        R.drawable.iz_tools,         // 11
                        R.drawable.iz_continue_no,   // 12
                        R.drawable.iz_back,          // 13 EDIT Nr 8
                        R.drawable.iz_forw,
                        R.drawable.iz_join,
                        R.drawable.iz_note,          
                        R.drawable.iz_delete,        // 17
                        R.drawable.iz_menu,          // 18
                        R.drawable.iz_extended,      // 19
                        R.drawable.iz_join_no,       // 20
                        R.drawable.iz_continue_cont, // 21
                        R.drawable.iz_continue_join, // 22
                        R.drawable.iz_plus,          // 23
                      };
  private static int menus[] = {
                        R.string.menu_export,
                        R.string.menu_stats,
                        R.string.menu_reload,
                        R.string.menu_zoom_fit,
                        R.string.menu_delete,
                        R.string.menu_palette,
                        R.string.menu_overview,
                        R.string.menu_options,
                        R.string.menu_help,
                        R.string.menu_area
                     };

  private static int help_icons[] = { 
                        R.string.help_draw,
                        R.string.help_eraser,
                        R.string.help_edit,
                        R.string.help_download,
                        R.string.help_remote,
                        R.string.help_refs,
                        R.string.help_note,
                        R.string.help_toggle_plot,
                        R.string.help_azimuth,
                        R.string.help_undo,
                        R.string.help_redo,
                        R.string.help_symbol_plot,
                        R.string.help_continue,
                        R.string.help_previous,
                        R.string.help_next,
                        R.string.help_line_point, 
                        R.string.help_note_plot,
                        R.string.help_delete_item
                      };
  private static int help_menus[] = {
                        R.string.help_save_plot,
                        R.string.help_stats,
                        R.string.help_recover,
                        R.string.help_zoom_fit,
                        R.string.help_trash,
                        R.string.help_symbol,
                        R.string.help_overview,
                        R.string.help_prefs,
                        R.string.help_help
                      };
  private TopoDroidApp mApp;
  private DataDownloader mDataDownloader;
  private DataHelper mData;

  long getSID() { return mApp.mSID; }
  String getSurvey() { return mApp.mySurvey; }

  private DistoXNum mNum;
  private float mDecl;

  // 0: no bezier, plain path
  // 1: bezier interpolator

  private String mSectionName;
  private String mMoveTo; // station of highlighted splay

  private static BezierInterpolator mBezierInterpolator = new BezierInterpolator();
  private DrawingSurface  mDrawingSurface;
  private DrawingLinePath mCurrentLinePath;
  private DrawingAreaPath mCurrentAreaPath;
  private DrawingPath mFixedDrawingPath;
  // private Paint mCurrentPaint;
  private DrawingBrush mCurrentBrush;
  private Path  mCurrentPath;

  // LinearLayout popup_layout = null;
  PopupWindow mPopupEdit = null;

  // private boolean canRedo;
  private int mPointCnt; // counter of points in the currently drawing line

  // private boolean mIsNotMultitouch;

  private boolean mEditMove;    // whether moving the selected point
  private boolean mShiftMove;   // whether to move the canvas in point-shift mode
  boolean mShiftDrawing;        // whether to shift the drawing
  EraseCommand mEraseCommand = null;

  int mHotItemType = -1;
  private boolean inLinePoint = false;

  ZoomButtonsController mZoomBtnsCtrl = null;
  boolean mZoomBtnsCtrlOn = false;
  // FIXME ZOOM_CTRL ZoomControls mZoomCtrl = null;
  // ZoomButton mZoomOut;
  // ZoomButton mZoomIn;
  private float oldDist;  // zoom pointer-sapcing

  static final float ZOOM_INC = 1.4f;
  static final float ZOOM_DEC = 1.0f/ZOOM_INC;

  static final int MODE_DRAW  = 1;
  static final int MODE_MOVE  = 2;
  static final int MODE_EDIT  = 3;
  static final int MODE_ZOOM  = 4; // used only for touchMode
  static final int MODE_SHIFT = 5; // change point symbol position
  static final int MODE_ERASE = 6;

  static final int CONT_NO   = 0;  // no continue
  static final int CONT_JOIN = 1;  // continue: join to existing line
  static final int CONT_CONT = 2;  // continue: continue existing line 
  static final int CONT_MAX  = 3; 

  public int mMode       = MODE_MOVE;
  private int mTouchMode = MODE_MOVE;
  private int mContinueLine = CONT_NO;
  private float mDownX;
  private float mDownY;
  private float mSaveX;
  private float mSaveY;
  private float mSave0X;
  private float mSave0Y;
  private float mSave1X;
  private float mSave1Y;
  private float mStartX; // line shift scene start point
  private float mStartY;

  // private boolean mAllSymbols; // whether the library has all the symbols of the plot

  // -------------------------------------------------------------
  // STATUS items

  private String mName;   // current-plot name
  String mName1;          // first name (PLAN)
  String mName2;  // second name (EXTENDED/PROJECTED)
  String mName3;  // third name (SECTION)
  String mFullName1; // accessible by the SaveThread
  String mFullName2;
  String mFullName3;

  String getName() { return (mName != null)? mName : ""; }

  private PlotInfo mPlot1;
  private PlotInfo mPlot2;
  private PlotInfo mPlot3;

  private long mSid;  // survey id
  private long mPid1; // plot id
  private long mPid2;
  private long mPid3;
  private long mPid;  // current plot id
  private long mType; // current plot type
  private String mFrom;
  private String mTo;   // TO station for sections
  private float mAzimuth = 0.0f;
  private float mClino   = 0.0f;
  private PointF mOffset  = new PointF( 0f, 0f );
  private PointF mDisplayCenter;
  private float mZoom  = 1.0f;

  private boolean mModified; // whether the sketch has been modified 

  long getPlotType()   { return mType; }

  private float mBorderRight      = 4096;
  private float mBorderLeft       = 0;
  private float mBorderInnerRight = 4096;
  private float mBorderInnerLeft  = 0;
  private float mBorderBottom     = 4096;
    
  // ----------------------------------------------------------------
  // BUTTONS and MENU

  // private Button mButtonHelp;
  private int mButtonSize;
  private Button[] mButton1; // primary
  private Button[] mButton2; // draw
  private Button[] mButton3; // edit
  private Button[] mButton5; // eraser
  private int mNrButton1 = 9;          // main-primary
  private int mNrButton2 = 7;          // draw
  private int mNrButton3 = 8;          // edit
  private int mNrButton5 = 5;          // erase
  private HorizontalButtonView mButtonView1;
  private HorizontalButtonView mButtonView2;
  private HorizontalButtonView mButtonView3;
  private HorizontalButtonView mButtonView5;

  private BitmapDrawable mBMdownload;
  private BitmapDrawable mBMdownload_on;
  private BitmapDrawable mBMdownload_wait;
  private BitmapDrawable mBMjoin;
  private BitmapDrawable mBMjoin_no;
  private BitmapDrawable mBMplan;
  private BitmapDrawable mBMextend;
  private BitmapDrawable mBMcontinue_no;
  private BitmapDrawable mBMcontinue_cont;
  private BitmapDrawable mBMcontinue_join;
  private BitmapDrawable mBMadd;
  private BitmapDrawable mBMleft;
  private BitmapDrawable mBMright;
  private Bitmap mBMdial;

  HorizontalListView mListView;
  ListView   mMenu;
  Button     mImage;
  // HOVER
  // MyMenuAdapter mMenuAdapter;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;

  private int mNrSaveTh2Task = 0;
  private final int MAX_TASK_NORMAL = 4;
  private final int MAX_TASK_FINAL  = 6;

  // ----------------------------------------------------------------

  public float zoom() { return mZoom; }

  private void modified()
  {
    if ( ! mModified ) {
      mModified = true;
      startSaveTdrTask( mType, PlotSave.MODIFIED, MAX_TASK_NORMAL, 1 );
    // } else {
    //   if ( mSaveTh2File != null ) {
    //     mSaveTh2File.setModified( true );
    //   }
    }
  }

  // -------------------------------------------------------------------
  // ZOOM CONTROLS

  @Override
  public void onVisibilityChanged(boolean visible)
  {
    if ( mZoomBtnsCtrlOn && mZoomBtnsCtrl != null ) {
      mZoomBtnsCtrl.setVisible( visible || ( TDSetting.mZoomCtrl > 1 ) );
    }
  }

  @Override
  public void onZoom( boolean zoomin )
  {
    if ( zoomin ) changeZoom( ZOOM_INC );
    else changeZoom( ZOOM_DEC );
  }

  private void changeZoom( float f ) 
  {
    float zoom = mZoom;
    mZoom     *= f;
    // Log.v( TopoDroidApp.TAG, "zoom " + mZoom );
    mOffset.x -= mDisplayCenter.x*(1/zoom-1/mZoom);
    mOffset.y -= mDisplayCenter.y*(1/zoom-1/mZoom);
    mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
    // mZoomCtrl.hide();
    // if ( mZoomBtnsCtrlOn ) mZoomBtnsCtrl.setVisible( false );
  }

  public void zoomIn()  { changeZoom( ZOOM_INC ); }
  public void zoomOut() { changeZoom( ZOOM_DEC ); }
  // public void zoomOne() { resetZoom( ); }

  // public void zoomView( )
  // {
  //   // TDLog.Log( TDLog.LOG_PLOT, "zoomView ");
  //   DrawingZoomDialog zoom = new DrawingZoomDialog( mDrawingSurface.getContext(), this );
  //   zoom.show();
  // }

  // -----------------------------------------------------------------
  @Override
  public void lineSelected( int k, boolean update_recent )
  {
    super.lineSelected( k, update_recent );
    if ( mCurrentLine == DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
      setButtonContinue( CONT_NO );
    }
  }

  private void resetFixedPaint( )
  {
    mDrawingSurface.resetFixedPaint( DrawingBrushPaths.fixedShotPaint );
  }
  
  private void addFixedSpecial( float x1, float y1, float x2, float y2, float xoff, float yoff )
  {
    DrawingPath dpath = new DrawingPath( DrawingPath.DRAWING_PATH_NORTH, null );
    dpath.setPaint( DrawingBrushPaths.highlightPaint );
    DrawingUtil.makePath( dpath, x1, y1, x2, y2, xoff, yoff );
    mDrawingSurface.setNorthPath( dpath );
  }

  private void addFixedLine( DistoXDBlock blk, float x1, float y1, float x2, float y2, float xoff, float yoff, 
                             boolean splay, boolean selectable )
  {
    DrawingPath dpath = null;
    if ( splay ) {
      dpath = new DrawingPath( DrawingPath.DRAWING_PATH_SPLAY, blk );
      if ( blk.mClino > TDSetting.mVertSplay ) {
        dpath.setPaint( DrawingBrushPaths.fixedSplay4Paint );
      } else if ( blk.mClino < -TDSetting.mVertSplay ) {
        dpath.setPaint( DrawingBrushPaths.fixedSplay3Paint );
      } else {
        dpath.setPaint( DrawingBrushPaths.fixedSplayPaint );
      }
      
      if ( mApp.getHighlightedSplayId() == blk.mId ) { dpath.setPaint( DrawingBrushPaths.errorPaint ); }
    } else {
      dpath = new DrawingPath( DrawingPath.DRAWING_PATH_FIXED, blk );
      if ( blk.isMultiBad() ) {
        dpath.setPaint( DrawingBrushPaths.fixedRedPaint );
      } else if ( TDSetting.mConnectionMode == TDSetting.CONN_MODE_BATCH && blk.isRecent( mApp.mSecondLastShotId ) ) {
        dpath.setPaint( DrawingBrushPaths.fixedBluePaint );
      } else {
        dpath.setPaint( DrawingBrushPaths.fixedShotPaint );
      }
    }
    DrawingUtil.makePath( dpath, x1, y1, x2, y2, xoff, yoff );
    mDrawingSurface.addFixedPath( dpath, splay, selectable );
  }

  private void addFixedSectionSplay( DistoXDBlock blk, float x1, float y1, float x2, float y2, float xoff, float yoff, 
                             boolean blue )
  {
    DrawingPath dpath = new DrawingPath( DrawingPath.DRAWING_PATH_SPLAY, blk );
    dpath.setPaint( blue? DrawingBrushPaths.fixedSplay2Paint : DrawingBrushPaths.fixedSplayPaint );
    DrawingUtil.makePath( dpath, x1, y1, x2, y2, xoff, yoff );
    mDrawingSurface.addFixedPath( dpath, true, false ); // true SPLAY false SELECTABLE
  }

  // --------------------------------------------------------------------------------------

  @Override
  protected void setTheTitle()
  {
    String s1 = mApp.getConnectionStateTitleStr();
    Resources res = getResources();
    if ( mMode == MODE_DRAW ) { 
      if ( mSymbol == SYMBOL_POINT ) {
        setTitle( s1 + String.format( res.getString(R.string.title_draw_point), 
                                 DrawingBrushPaths.mPointLib.getSymbolName(mCurrentPoint) ) );
      } else if ( mSymbol == SYMBOL_LINE ) {
        setTitle( s1 + String.format( res.getString(R.string.title_draw_line),
                                 DrawingBrushPaths.mLineLib.getSymbolName(mCurrentLine) ) );
      } else  {  // if ( mSymbol == SYMBOL_LINE ) 
        setTitle( s1 + String.format( res.getString(R.string.title_draw_area),
                                 DrawingBrushPaths.mAreaLib.getSymbolName(mCurrentArea) ) );
      }
      // boolean visible = ( mSymbol == SYMBOL_LINE && mCurrentLine == DrawingBrushPaths.mLineLib.mLineWallIndex );
      boolean visible = ( mSymbol == SYMBOL_LINE );
      mButton2[ BTN_CONTINUE ].setVisibility( visible? View.VISIBLE : View.GONE );
    } else if ( mMode == MODE_MOVE ) {
      setTitle( s1 + res.getString( R.string.title_move ) );
    } else if ( mMode == MODE_EDIT ) {
      setTitle( s1 + res.getString( R.string.title_edit ) );
    } else if ( mMode == MODE_SHIFT ) {
      setTitle( s1 + res.getString( R.string.title_shift ) );
    } else if ( mMode == MODE_ERASE ) {
      setTitle( s1 + res.getString( R.string.title_erase ) );
    }
    if ( ! mDrawingSurface.isSelectable() ) {
      setTitle( s1 + getTitle() + " [!s]" );
    }
  }

  // --------------------------------------------------------------

  // private void AlertMissingSymbols()
  // {
  //   TopoDroidAlertDialog.makeAlert( this, getResources(), R.string.missing-symbols,
  //     new DialogInterface.OnClickListener() {
  //       @Override
  //       public void onClick( DialogInterface dialog, int btn ) {
  //         mAllSymbols = true;
  //       }
  //     }
  //   );
  // }

  private boolean doubleBack = false;
  private Handler doubleBackHandler = new Handler();
  private Toast   doubleBackToast = null;

  private final Runnable doubleBackRunnable = new Runnable() {
    @Override 
    public void run() {
      doubleBack = false;
      if ( doubleBackToast != null ) doubleBackToast.cancel();
      doubleBackToast = null;
    }
  };

  // doSaveTdr( ) is already called by onPause
  @Override
  public void onBackPressed () // askClose
  {
    if ( dismissPopups() ) return;
    if ( PlotInfo.isAnySection( mType ) ) {
      mModified = true; // force saving
      startSaveTdrTask( mType, PlotSave.SAVE, MAX_TASK_FINAL, TDPath.NR_BACKUP );
      popInfo();
      doStart( false );
    } else {
      if ( doubleBack ) {
        if ( doubleBackToast != null ) doubleBackToast.cancel();
        doubleBackToast = null;
        super.onBackPressed();
      } else {
        doubleBack = true;
        doubleBackToast = Toast.makeText( this, R.string.double_back, Toast.LENGTH_SHORT );
        doubleBackToast.show();
        doubleBackHandler.postDelayed( doubleBackRunnable, 1000 );
      }
    }
  }


  // called by doPause 
  private void doSaveTdr( )
  {
    if ( mDrawingSurface != null ) {
      // Log.v("DistoX", "do save type " + mType );
      mModified = true; // force saving
      startSaveTdrTask( mType, PlotSave.SAVE, MAX_TASK_FINAL, TDPath.NR_BACKUP );

      // if ( not_all_symbols ) AlertMissingSymbols();
      // if ( mAllSymbols ) {
      //   // Toast.makeText( this, R.string.sketch_saving, Toast.LENGTH_SHORT ).show();
      //   startSaveTdrTask( mType, PlotSave.SAVE, MAX_TASK_FINAL, TDPath.NR_BACKUP );
      // } else { // mAllSymbols is false: FIXME what to do ?
      //  Toast.makeText( this,
      //    "NOT SAVING " + mFullName1 + " " + mFullName2, Toast.LENGTH_LONG ).show();
      // }
    }
    mModified = false;
  }

  static Handler saveHandler = null;


  // called by doSaveTdr and saveTh2
  // @param suffix
  // @param maxTasks
  // @param rotate    backup_rotate
  private void startSaveTdrTask( final long type, int suffix, int maxTasks, int rotate )
  {
    // Log.v("DistoX", "start save Th2 task. type " + type + " suffix " + suffix 
    //               + " maxTasks " + maxTasks + " rotate " + rotate ); 
    if ( suffix != PlotSave.EXPORT ) {
      if ( ! mModified ) return;
      if ( mNrSaveTh2Task > maxTasks ) return;

      saveHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
          -- mNrSaveTh2Task;
          if ( mModified ) {
            startSaveTdrTask( type, PlotSave.HANDLER, MAX_TASK_NORMAL, 0 ); 
          } else {
            // mApp.mShotActivity.enableSketchButton( true );
            mApp.mEnableZip = true;
          }
        }
      };
      ++ mNrSaveTh2Task;
      // mApp.mShotActivity.enableSketchButton( false );
      mApp.mEnableZip = false;
      mModified = false;
    } else {
      // Log.v("DISTOX", "exporting plot ...");
      saveHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
          // mApp.mShotActivity.enableSketchButton( true );
          mApp.mEnableZip = true;
        }
      };
    }
    try { Thread.sleep(10); } catch( InterruptedException e ) { }

    String name = null;
    int azimuth = 0;
    long tt     = type;
    if ( type == -1 ) {
      try { 
        (new SavePlotFileTask( this, this, saveHandler, mApp, mDrawingSurface, mFullName2, mPlot2.type,
                              (int)mPlot2.azimuth, suffix, rotate )).execute();
      } catch ( RejectedExecutionException e ) { }
      name = mFullName1;
      tt   = mPlot1.type; // PlotType.PLOT_PLAN
    } else if ( PlotInfo.isProfile( type ) ) {
      name = mFullName2;
      azimuth  = (int)mPlot2.azimuth;
    } else if ( type == PlotInfo.PLOT_PLAN ) {
      name = mFullName1;
    } else {
      name = mFullName3;
    }
    try { 
      (new SavePlotFileTask( this, this, saveHandler, mApp, mDrawingSurface, name, tt, azimuth, suffix, rotate )
      ).execute();
    } catch ( RejectedExecutionException e ) { 
      -- mNrSaveTh2Task;
    }
  }

  // ---------------------------------------------------------------------------------------

  private void moveTo( int type, String move_to )
  {
    // if ( move_to == null ) return;
    NumStation st = mNum.getStation( move_to );
    if ( st != null ) {
      if ( type == PlotInfo.PLOT_PLAN ) {
        mZoom     = mPlot1.zoom;
        mOffset.x = TopoDroidApp.mDisplayWidth/(2 * mZoom)  - DrawingUtil.toSceneX( st.e );
        mOffset.y = TopoDroidApp.mDisplayHeight/(2 * mZoom) - DrawingUtil.toSceneY( st.s );
        saveReference( mPlot1, mPid1 );
        // resetReference( mPlot1 );
        // mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
        return;
      } else if ( type == PlotInfo.PLOT_EXTENDED ) {
        mZoom     = mPlot2.zoom;
        mOffset.x = TopoDroidApp.mDisplayWidth/(2 * mZoom)  - DrawingUtil.toSceneX( st.h );
        mOffset.y = TopoDroidApp.mDisplayHeight/(2 * mZoom) - DrawingUtil.toSceneY( st.v );
        saveReference( mPlot2, mPid2 );
        // resetReference( mPlot2 );
        return;
      } else { // if ( type == PlotInfo.PLOT_PROFILE ) 
        float cosp = TDMath.cosd( mPlot2.azimuth );
        float sinp = TDMath.sind( mPlot2.azimuth );
        mZoom     = mPlot2.zoom;
        mOffset.x = TopoDroidApp.mDisplayWidth/(2 * mZoom)  - DrawingUtil.toSceneX( st.e * cosp + st.s * sinp );
        mOffset.y = TopoDroidApp.mDisplayHeight/(2 * mZoom) - DrawingUtil.toSceneY( st.v );
        saveReference( mPlot2, mPid2 );
        return;
      }
    }
  }

  // this is called only for PLAN / PROFILE
  private void computeReferences( int type, float xoff, float yoff, float zoom, boolean can_toast )
  {
    // Log.v("DistoX", "computeReferences()" );
    if ( ! PlotInfo.isSketch2D( type ) ) return;
    mDrawingSurface.clearReferences( type );

    float cosp = 0;
    float sinp = 0;

    if ( type == PlotInfo.PLOT_PLAN ) {
      mDrawingSurface.setManager( DrawingSurface.DRAWING_PLAN, type );
      DrawingUtil.addGrid( mNum.surveyEmin(), mNum.surveyEmax(), mNum.surveySmin(), mNum.surveySmax(),
                           xoff, yoff, mDrawingSurface );
    } else {
      mDrawingSurface.setManager( DrawingSurface.DRAWING_PROFILE, type );
      DrawingUtil.addGrid( mNum.surveyHmin(), mNum.surveyHmax(), mNum.surveyVmin(), mNum.surveyVmax(),
                           xoff, yoff, mDrawingSurface );
      if ( type == PlotInfo.PLOT_PROFILE ) {
        cosp = TDMath.cosd( mPlot2.azimuth );
        sinp = TDMath.sind( mPlot2.azimuth );
      }
    }

    List< NumStation > stations = mNum.getStations();
    List< NumShot > shots       = mNum.getShots();
    List< NumSplay > splays     = mNum.getSplays();

    if ( type == PlotInfo.PLOT_PLAN ) {
      for ( NumShot sh : shots ) {
        NumStation st1 = sh.from;
        NumStation st2 = sh.to;
        if ( st1.show() && st2.show() ) {
          addFixedLine( sh.getFirstBlock(), (float)(st1.e), (float)(st1.s), (float)(st2.e), (float)(st2.s), 
                        xoff, yoff, false, true );
        }
      }
      for ( NumSplay sp : splays ) {
        if ( Math.abs( sp.getBlock().mClino ) < TDSetting.mSplayVertThrs ) {
          NumStation st = sp.from;
          if ( st.show() ) {
            addFixedLine( sp.getBlock(), (float)(st.e), (float)(st.s), (float)(sp.e), (float)(sp.s), 
                          xoff, yoff, true, true );
          }
        }
      }
      List< PlotInfo > xsections = mData.selectAllPlotsWithType( mApp.mSID, 0, PlotInfo.PLOT_X_SECTION );
      for ( NumStation st : stations ) {
        if ( st.show() ) {
          DrawingStationName dst;
          dst = mDrawingSurface.addDrawingStationName( st, DrawingUtil.toSceneX(st.e) - xoff,
                                                           DrawingUtil.toSceneY(st.s) - yoff, true, xsections );
        }
      }
    } else if ( type == PlotInfo.PLOT_EXTENDED ) {
      for ( NumShot sh : shots ) {
        if  ( ! sh.mIgnoreExtend ) {
          NumStation st1 = sh.from;
          NumStation st2 = sh.to;
          if ( st1.show() && st2.show() ) {
            addFixedLine( sh.getFirstBlock(), (float)(st1.h), (float)(st1.v), (float)(st2.h), (float)(st2.v), 
                          xoff, yoff, false, true );
          }
        }
      } 
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        if ( st.show() ) {
          addFixedLine( sp.getBlock(), (float)(st.h), (float)(st.v), (float)(sp.h), (float)(sp.v), 
                        xoff, yoff, true, true );
        }
      }
      List< PlotInfo > xhsections = mData.selectAllPlotsWithType( mApp.mSID, 0, PlotInfo.PLOT_XH_SECTION );
      for ( NumStation st : stations ) {
        if ( st.show() ) {
          DrawingStationName dst;
          dst = mDrawingSurface.addDrawingStationName( st, DrawingUtil.toSceneX(st.h) - xoff,
                                                           DrawingUtil.toSceneY(st.v) - yoff, true, xhsections );
        }
      }
    } else { // if ( type == PlotInfo.PLOT_PROFILE ) 
      float h1, h2;
      for ( NumShot sh : shots ) {
        if  ( ! sh.mIgnoreExtend ) {
          NumStation st1 = sh.from;
          NumStation st2 = sh.to;
          if ( st1.show() && st2.show() ) {
            h1 = (float)( st1.e * cosp + st1.s * sinp );
            h2 = (float)( st2.e * cosp + st2.s * sinp );
            addFixedLine( sh.getFirstBlock(), h1, (float)(st1.v), h2, (float)(st2.v), xoff, yoff, false, true );
          }
        }
      } 
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        if ( st.show() ) {
          h1 = (float)( st.e * cosp + st.s * sinp );
          h2 = (float)( sp.e * cosp + sp.s * sinp );
          addFixedLine( sp.getBlock(), h1, (float)(st.v), h2, (float)(sp.v), xoff, yoff, true, true );
        }
      }
      List< PlotInfo > xhsections = mData.selectAllPlotsWithType( mApp.mSID, 0, PlotInfo.PLOT_XH_SECTION );
      for ( NumStation st : stations ) {
        if ( st.show() ) {
          DrawingStationName dst;
          h1 = (float)( st.e * cosp + st.s * sinp );
          dst = mDrawingSurface.addDrawingStationName( st, DrawingUtil.toSceneX(h1) - xoff,
                                                           DrawingUtil.toSceneY(st.v) - yoff, true, xhsections );
        }
      }
    }

    if ( (! mNum.surveyAttached) && TDSetting.mCheckAttached && can_toast ) {
      Toast.makeText( this, R.string.survey_not_attached, Toast.LENGTH_SHORT ).show();
    }
  }

  // --------------------------------------------------------------

  /** set the reference azimuth 
   * and the Extend Button image according to the reference azimuth or fixed one
   * @param azimuth       reference azimuth value
   * @param fixed_extend  fixed extend: -1 (left) 1 (right) 0 (use azimuth)
   */
  public void setRefAzimuth( float azimuth, long fixed_extend )
  {
    TDAzimuth.mFixedExtend = fixed_extend;
    TDAzimuth.mRefAzimuth = azimuth;
    if ( TDAzimuth.mFixedExtend == 0 ) {
      android.graphics.Matrix m = new android.graphics.Matrix();
      m.postRotate( azimuth - 90 );
      Bitmap bm1 = Bitmap.createScaledBitmap( mBMdial, mButtonSize, mButtonSize, true );
      Bitmap bm2 = Bitmap.createBitmap( bm1, 0, 0, mButtonSize, mButtonSize, m, true);
      mButton1[BTN_DIAL].setBackgroundDrawable( new BitmapDrawable( getResources(), bm2 ) );
    } else if ( TDAzimuth.mFixedExtend == -1L ) {
      mButton1[BTN_DIAL].setBackgroundDrawable( mBMleft );
    } else {
      mButton1[BTN_DIAL].setBackgroundDrawable( mBMright );
    } 
  }

  // set the button3 by the type of the hot-item
  private void setButton3( int type )
  {
    mHotItemType = type;
    if (    type == DrawingPath.DRAWING_PATH_POINT 
         || type == DrawingPath.DRAWING_PATH_LINE 
         || type == DrawingPath.DRAWING_PATH_AREA 
         || type == DrawingPath.DRAWING_PATH_STATION ) {
      inLinePoint = true;
      // mButton3[ BTN_JOIN ].setBackgroundResource( icons00[ IC_JOIN ] );
      mButton3[ BTN_JOIN ].setBackgroundDrawable( mBMjoin );
    } else {
      inLinePoint = false;
      // mButton3[ BTN_JOIN ].setBackgroundResource( icons00[ IC_JOIN_NO ] );
      mButton3[ BTN_JOIN ].setBackgroundDrawable( mBMjoin_no );
    }
  }

  private void setButtonContinue( int continue_line )
  {
    mContinueLine = continue_line;
    if ( mSymbol == SYMBOL_LINE /* && mCurrentLine == DrawingBrushPaths.mLineLib.mLineWallIndex */ ) {
      mButton2[ BTN_CONTINUE ].setVisibility( View.VISIBLE );
      switch ( mContinueLine ) {
        case CONT_NO:
          mButton2[ BTN_CONTINUE ].setBackgroundDrawable( mBMcontinue_no  );
          break;
        case CONT_JOIN:
          mButton2[ BTN_CONTINUE ].setBackgroundDrawable( mBMcontinue_join  );
          break;
        case CONT_CONT:
          mButton2[ BTN_CONTINUE ].setBackgroundDrawable( mBMcontinue_cont  );
          break;
      }
    } else {
      mButton2[ BTN_CONTINUE ].setVisibility( View.GONE );
    }
  }

  // this method is a callback to let other objects tell the activity to use zooms or not
  private void switchZoomCtrl( int ctrl )
  {
    // Log.v("DistoX", "DEBUG switchZoomCtrl " + ctrl + " ctrl is " + ((mZoomBtnsCtrl == null )? "null" : "not null") );
    if ( mZoomBtnsCtrl == null ) return;
    mZoomBtnsCtrlOn = (ctrl > 0);
    switch ( ctrl ) {
      case 0:
        mZoomBtnsCtrl.setOnZoomListener( null );
        mZoomBtnsCtrl.setVisible( false );
        mZoomBtnsCtrl.setZoomInEnabled( false );
        mZoomBtnsCtrl.setZoomOutEnabled( false );
        mZoomView.setVisibility( View.GONE );
        break;
      case 1:
        mZoomView.setVisibility( View.VISIBLE );
        mZoomBtnsCtrl.setOnZoomListener( this );
        mZoomBtnsCtrl.setVisible( false );
        mZoomBtnsCtrl.setZoomInEnabled( true );
        mZoomBtnsCtrl.setZoomOutEnabled( true );
        break;
      case 2:
        mZoomView.setVisibility( View.VISIBLE );
        mZoomBtnsCtrl.setOnZoomListener( this );
        mZoomBtnsCtrl.setVisible( true );
        mZoomBtnsCtrl.setZoomInEnabled( true );
        mZoomBtnsCtrl.setZoomOutEnabled( true );
        break;
    }
  }

  long mSavedType;

  private void resetStatus()
  {
    mSectionName  = null; 
    mShiftDrawing = false;
    mContinueLine = CONT_NO;
    mModified     = false;
    setMode( MODE_MOVE );
    mTouchMode    = MODE_MOVE;
    setMenuAdapter( getResources(), mType );
  }
  
  private void popInfo()
  {
    PlotInfo plot = ( mSavedType == PlotInfo.PLOT_PLAN )? mPlot1 : mPlot2;
    mType    = plot.type;
    mName    = plot.name;
    mFrom    = plot.start; 
    mTo      = "";
    mAzimuth = plot.azimuth;
    mClino   = plot.clino;
    // Log.v("DistoX", "pop " + mType + " " + mName + " from " + mFrom + " A " + mAzimuth + " C " + mClino );
    resetStatus();
    mButton1[ BTN_DOWNLOAD ].setVisibility( View.VISIBLE );
    mButton1[ BTN_BLUETOOTH ].setVisibility( View.VISIBLE );
    mButton1[ BTN_PLOT ].setVisibility( View.VISIBLE );
    mButton1[ BTN_DIAL ].setVisibility( View.VISIBLE );
  }

  private void pushInfo( long type, String name, String from, String to, float azimuth, float clino )
  {
    // Log.v("DistoX", "push " + type + " " + name + " from " + from + " " + to + " A " + azimuth + " C " + clino );
    mSavedType = mType;
    mName = mName3 = name;
    mFullName3 = mApp.mySurvey + "-" + mName;
    mType    = type;
    mFrom    = from;
    mTo      = to;
    mAzimuth = azimuth;
    mClino   = clino;
    resetStatus();
    doStart( true );
    mButton1[ BTN_DOWNLOAD ].setVisibility( View.GONE );
    mButton1[ BTN_BLUETOOTH ].setVisibility( View.GONE );
    mButton1[ BTN_PLOT ].setVisibility( View.GONE );
    mButton1[ BTN_DIAL ].setVisibility( View.GONE );
  }

  private void makeButtons( )
  {
    Resources res = getResources();
    mButton1 = new Button[ mNrButton1 ]; // MOVE
    int off = 0;
    int ic = 0;
    for ( int k=0; k<mNrButton1; ++k ) {
      ic = ( k <3 )? k : off+k;
      mButton1[k] = MyButton.getButton( this, this, izons[ic] );
      if ( ic == IC_DOWNLOAD )  { mBMdownload = MyButton.getButtonBackground( res, izons[ic] ); }
      else if ( ic == IC_PLAN ) { mBMplan     = MyButton.getButtonBackground( res, izons[ic] ); }
    }
    mBMdial = BitmapFactory.decodeResource( res, izons[IC_DIAL] );
    mBMextend        = MyButton.getButtonBackground( res, izons[IC_EXTEND] ); 
    mBMdownload_on   = MyButton.getButtonBackground( res, R.drawable.iz_download_on );
    mBMdownload_wait = MyButton.getButtonBackground( res, R.drawable.iz_download_wait );
    mBMleft          = MyButton.getButtonBackground( res, R.drawable.iz_left );
    mBMright         = MyButton.getButtonBackground( res, R.drawable.iz_right );
    setRefAzimuth( TDAzimuth.mRefAzimuth, TDAzimuth.mFixedExtend );

    mButton2 = new Button[ mNrButton2 ]; // DRAW
    off = (mNrButton1 - 3); 
    for ( int k=0; k<mNrButton2; ++k ) {
      ic = ( k < 3 )? k : off+k;
      mButton2[k] = MyButton.getButton( this, this, ((k==0)? izons_ok[ic] : izons[ic]) );
      if ( ic == IC_CONTINUE_NO ) mBMcontinue_no = MyButton.getButtonBackground( res, ((k==0)? izons_ok[ic] : izons[ic]));
    }
    mBMcontinue_cont = MyButton.getButtonBackground( res, izons[IC_CONTINUE_CONT] );
    mBMcontinue_join = MyButton.getButtonBackground( res, izons[IC_CONTINUE_JOIN] );

    mButton3 = new Button[ mNrButton3 ];      // EDIT
    off = (mNrButton1-3) + (mNrButton2-3); 
    for ( int k=0; k<mNrButton3; ++k ) {
      ic = ( k < 3 )? k : off+k;
      mButton3[k] = MyButton.getButton( this, this, ((k==2)? izons_ok[ic] : izons[ic]) );
      if ( ic == IC_JOIN ) 
        mBMjoin = MyButton.getButtonBackground( res, ((k==2)? izons_ok[ic] : izons[ic]) );
    }
    mBMjoin_no = MyButton.getButtonBackground( res, izons[IC_JOIN_NO] );
    mBMadd     = MyButton.getButtonBackground( res, izons[IC_ADD] );

    mButton5 = new Button[ mNrButton5 ];    // ERASE
    off = 9 - 3; // (mNrButton1-3) + (mNrButton2-3) + (mNrButton3-3);
    for ( int k=0; k<mNrButton5; ++k ) {
      ic = ( k < 3 )? k : off+k;
      mButton5[k] = MyButton.getButton( this, this, ((k==1)? izons_ok[ic] : izons[ic] ) );
    }

    mButtonView1 = new HorizontalButtonView( mButton1 );
    mButtonView2 = new HorizontalButtonView( mButton2 );
    mButtonView3 = new HorizontalButtonView( mButton3 );
    mButtonView5 = new HorizontalButtonView( mButton5 );
  }

  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.TimeStart();
    // Log.v("DistoX", "onCreate()" );

    mApp  = (TopoDroidApp)getApplication();
    mData = mApp.mData; // new DataHelper( this ); 

    mZoomBtnsCtrlOn = (TDSetting.mZoomCtrl > 1);  // do before setting content
    mPointScale = DrawingPointPath.SCALE_M;

    // Display display = getWindowManager().getDefaultDisplay();
    // DisplayMetrics dm = new DisplayMetrics();
    // display.getMetrics( dm );
    // int width = dm widthPixels;
    int width = getResources().getDisplayMetrics().widthPixels;

    // mIsNotMultitouch = ! getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );

    setContentView(R.layout.drawing_activity);
    mDataDownloader = mApp.mDataDownloader; // new DataDownloader( this, mApp );
    mZoom         = mApp.mScaleFactor;    // canvas zoom
    mBorderRight  = mApp.mDisplayWidth * 15 / 16;
    mBorderLeft   = mApp.mDisplayWidth / 16;
    mBorderInnerRight  = mApp.mDisplayWidth * 3 / 4;
    mBorderInnerLeft   = mApp.mDisplayWidth / 4;
    mBorderBottom = mApp.mDisplayHeight * 7 / 8;

    mDisplayCenter = new PointF(mApp.mDisplayWidth  / 2, mApp.mDisplayHeight / 2);

    // setCurrentPaint();
    mCurrentBrush = new DrawingPenBrush();

    mDrawingSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
    mDrawingSurface.setZoomer( this );
    mDrawingSurface.previewPath = new DrawingPath( DrawingPath.DRAWING_PATH_LINE, null );
    mDrawingSurface.previewPath.mPath = new Path();
    mDrawingSurface.previewPath.setPaint( getPreviewPaint() );
    mDrawingSurface.setOnTouchListener(this);
    // mDrawingSurface.setOnLongClickListener(this);
    // mDrawingSurface.setBuiltInZoomControls(true);

    mZoomView = (View) findViewById(R.id.zoomView );
    mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );
    // FIXME ZOOM_CTRL mZoomCtrl = (ZoomControls) mZoomBtnsCtrl.getZoomControls();
    // ViewGroup vg = mZoomBtnsCtrl.getContainer();
    // switchZoomCtrl( TDSetting.mZoomCtrl );

    mListView = (HorizontalListView) findViewById(R.id.listview);
    mButtonSize = mApp.setListViewHeight( mListView );

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    // mImage.setBackgroundResource( icons00[ IC_MENU ] );
    mImage.setBackgroundDrawable( MyButton.getButtonBackground( getResources(), izons[IC_MENU] ) );
    mMenu = (ListView) findViewById( R.id.menu );
    // HOVER
    mMenu.setOnItemClickListener( this );

    makeButtons( );

    if ( ! TDSetting.mLevelOverNormal ) {
      mButton1[2].setVisibility( View.GONE );
      mButton2[2].setVisibility( View.GONE );
      mButton3[2].setVisibility( View.GONE );
      mButton5[2].setVisibility( View.GONE );
    }
    if ( TDSetting.mLevelOverBasic ) {
      mButton1[BTN_PLOT].setOnLongClickListener( this );
      mButton3[BTN_REMOVE].setOnLongClickListener( this );
    }
 
    setConnectionStatus( mDataDownloader.getStatus() );
    mListView.setAdapter( mButtonView1.mAdapter );
    // mListView.invalidate();

    // redoBtn.setEnabled(false);
    // undoBtn.setEnabled(false); // let undo always be there

    DrawingBrushPaths.makePaths( getResources() );
    setTheTitle();

    // mBezierInterpolator = new BezierInterpolator( );

    Bundle extras = getIntent().getExtras();
    mSid   = extras.getLong( TopoDroidTag.TOPODROID_SURVEY_ID );
    // mDecl = mData.getSurveyDeclination( mSid );
    mDecl = 0; // FIXME do not correct declination in sketches

    mName1 = extras.getString( TopoDroidTag.TOPODROID_PLOT_NAME );
    mName2 = extras.getString( TopoDroidTag.TOPODROID_PLOT_NAME2 );
    mFullName1 = mApp.mySurvey + "-" + mName1;
    mFullName2 = mApp.mySurvey + "-" + mName2;
    mFullName3 = null;
    mType = extras.getLong( TopoDroidTag.TOPODROID_PLOT_TYPE );

    mName    = (mType == PlotInfo.PLOT_PLAN)? mName1 : mName2;
    mFrom    = extras.getString( TopoDroidTag.TOPODROID_PLOT_FROM );
    mTo      = extras.getString( TopoDroidTag.TOPODROID_PLOT_TO );
    mAzimuth = extras.getFloat( TopoDroidTag.TOPODROID_PLOT_AZIMUTH );
    mClino   = extras.getFloat( TopoDroidTag.TOPODROID_PLOT_CLINO );
    mMoveTo  = extras.getString( TopoDroidTag.TOPODROID_PLOT_MOVE_TO );
    if ( mMoveTo.length() == 0 ) mMoveTo = null;
    mSectionName  = null; // resetStatus
    mShiftDrawing = false;
    mContinueLine = CONT_NO;
    mModified     = false;

    // if ( PlotInfo.isSection( mType ) ) { 
    //   mTo      = extras.getString( TopoDroidTag.TOPODROID_PLOT_TO );  // to station ( null for X-section)
    //   mAzimuth = (float)extras.getLong( TopoDroidTag.TOPODROID_PLOT_AZIMUTH );
    //   mClino   = (float)extras.getLong( TopoDroidTag.TOPODROID_PLOT_CLINO );
    //   // Log.v("DistoX", "X-Section " + mFrom + "-" + mTo + " azimuth " + mAzimuth + " clino " + mClino  );
    // } else if ( PlotInfo.isXSection( mType ) ) {
    //   mTo = null;
    //   mAzimuth = (float)extras.getLong( TopoDroidTag.TOPODROID_PLOT_AZIMUTH );
    //   mClino   = (float)extras.getLong( TopoDroidTag.TOPODROID_PLOT_CLINO );
    // }

    // TDLog.TimeEnd( "on create" );

    doStart( true );

    setMenuAdapter( getResources(), mType );
    closeMenu();

    if ( mDataDownloader != null ) {
      mApp.registerLister( this );
    } 
  }

    @Override
    protected synchronized void onResume()
    {
      super.onResume();
      // Log.v("DistoX", "Drawing Activity onResume " + ((mDataDownloader!=null)?"with DataDownloader":"") );
      doResume();
      if ( mDataDownloader != null ) mDataDownloader.onResume();
      setConnectionStatus( mDataDownloader.getStatus() );
      // TDLog.TimeEnd( "drawing activity ready" );
    }

    @Override
    protected synchronized void onPause() 
    { 
      // Log.v("DistoX", "Drawing Activity onPause " + ((mDataDownloader!=null)?"with DataDownloader":"") );
      doPause();
      super.onPause();
    }

    @Override
    protected synchronized void onStart()
    {
      super.onStart();
      // Log.v("DistoX", "Drawing Activity onStart " + ((mDataDownloader!=null)?"with DataDownloader":"") );
      loadRecentSymbols( mApp.mData );
    }

    @Override
    protected synchronized void onStop()
    {
      super.onStop();
      // Log.v("DistoX", "Drawing Activity onStop ");
      saveRecentSymbols( mApp.mData );
      // doStop();
    }

    @Override
    protected synchronized void onDestroy()
    {
      super.onDestroy();
      // Log.v("DistoX", "Drawing activity onDestroy");
      if ( mDataDownloader != null ) {
        mApp.unregisterLister( this );
      }
      // if ( mDataDownloader != null ) { // data-download management is left to ShotActivity
      //   mDataDownloader.onStop();
      //   mApp.disconnectRemoteDevice( false );
      // }
    }

    private void doResume()
    {
      // Log.v("DistoX", "doResume()" );
      PlotInfo info = mApp.mData.getPlotInfo( mSid, mName );
      mOffset.x = info.xoffset;
      mOffset.y = info.yoffset;
      mZoom     = info.zoom;
      mDrawingSurface.isDrawing = true;
      switchZoomCtrl( TDSetting.mZoomCtrl );
      // Log.v("DistoX", "do Resume. Save tasks: " + mNrSaveTh2Task );
      setPlotType( mType );
    }

    private void doPause()
    {
      switchZoomCtrl( 0 );
      mDrawingSurface.isDrawing = false;
      if ( mPid >= 0 ) {
        try {
          mData.updatePlot( mPid, mSid, mOffset.x, mOffset.y, mZoom );
        } catch ( IllegalStateException e ) {
          TDLog.Error("cannot save plot state: " + e.getMessage() );
        }
      }
      doSaveTdr( ); // do not alert-dialog on mAllSymbols
    }

    // private void doStop()
    // {
    //   TDLog.Log( TDLog.LOG_PLOT, "doStop type " + mType + " modified " + mModified );
    // }

// ----------------------------------------------------------------------------

    private void doStart( boolean do_load )
    {
      // Log.v("DistoX", "doStart()" );
      // TDLog.Log( TDLog.LOG_PLOT, "do Start() " + mName1 + " " + mName2 );
      mCurrentPoint = ( DrawingBrushPaths.mPointLib.isSymbolEnabled( "label" ) )? 1 : 0;
      mCurrentLine  = ( DrawingBrushPaths.mLineLib.isSymbolEnabled( "wall" ) )? 1 : 0;
      mCurrentArea  = ( DrawingBrushPaths.mAreaLib.isSymbolEnabled( "water" ) )? 1 : 0;
      setButtonContinue( CONT_NO );

      List<DistoXDBlock> list = null;
      if ( PlotInfo.isSection( mType ) ) {
        list = mData.selectAllShotsAtStations( mSid, mFrom, mTo );
      } else if ( PlotInfo.isXSection( mType ) ) { 
        // N.B. mTo can be null
        list = mData.selectShotsAt( mSid, mFrom, false ); // select only splays
      } else {
        list = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
      }

      // TDLog.TimeEnd( "before load" );

      if ( do_load ) loadFiles( mType, list ); 

      setPlotType( mType );
      // TDLog.TimeEnd( "after load" );

      // There are four types of sections:
      // SECTION and H_SECTION: mFrom != null, mTo != null, splays and leg
      // X_SECTION, XH_SECTION: mFrom != null, mTo == null, splays only 

      if ( PlotInfo.isAnySection( mType ) ) {
        makeSectionReferences( list );
      }
      // TDLog.TimeEnd("do start done");
    }

    private void makeSectionReferences( List<DistoXDBlock> list )
    {
      // Log.v("DistoX", "Section " + mClino + " " + mAzimuth );
 
      DrawingUtil.addGrid( -10, 10, -10, 10, 0.0f, 0.0f, mDrawingSurface );
      float xfrom=0;
      float yfrom=0;
      float xto=0;
      float yto=0;
      // normal, horizontal and cross-product
      float mc = mClino   * TDMath.GRAD2RAD;
      float ma = mAzimuth * TDMath.GRAD2RAD;
      float X0 = (float)Math.cos( mc ) * (float)Math.cos( ma );  // X = North
      float Y0 = (float)Math.cos( mc ) * (float)Math.sin( ma );  // Y = East
      float Z0 = (float)Math.sin( mc );                        // Z = Up
      // canvas X-axis, unit horizontal axis: 90 degrees to the right of the azimuth
      //   azimuth = 0 (north) --> horizontal = ( 0N, 1E)
      //   azimuth = 90 (east) --> horizontal = (-1N, 0E)
      //   etc.
      float X1 = - (float)Math.sin( ma ); // X1 goes to the left in the section plane !!!
      float Y1 =   (float)Math.cos( ma ); 
      float Z1 = 0;
      // float X2 = - (float)Math.sin( mc ) * (float)Math.cos( ma );
      // float Y2 = - (float)Math.sin( mc ) * (float)Math.sin( ma );
      // float Z2 =   (float)Math.cos( ma );
      // canvas UP-axis: this is X0 ^ X1 : it goes up in the section plane 
      // canvas Y-axis = - UP-axis
      float X2 = Y0 * Z1 - Y1 * Z0; 
      float Y2 = Z0 * X1 - Z1 * X0;
      float Z2 = X0 * Y1 - X1 * Y0;

      float dist = 0;
      DistoXDBlock blk = null;
      float xn = 0;  // X-North // Rotate as NORTH is upward
      float yn = -1; // Y-North
      if ( PlotInfo.isSection( mType ) ) {
        if ( mType == PlotInfo.PLOT_H_SECTION ) {
          if ( Math.abs( mClino ) > TDSetting.mHThreshold ) { // north arrow == (1,0,0), 5 m long in the CS plane
            xn =  (float)(X1);
            yn = -(float)(X2);
            float d = 5 / (float)Math.sqrt(xn*xn + yn*yn);
            if ( mClino > 0 ) xn = -xn;
            // FIXME NORTH addFixedSpecial( xn*d, yn*d, 0, 0, 0, 0 ); 
            addFixedSpecial( 0, -d, 0, 0, 0, 0 ); // NORTH is upward
            // Log.v("AZIMUTH", "North " + xn + " " + yn );
          }
        }

        for ( DistoXDBlock b : list ) {
          if ( b.mType == DistoXDBlock.BLOCK_SPLAY ) continue;
          if ( mFrom.equals( b.mFrom ) && mTo.equals( b.mTo ) ) { // FROM --> TO
            dist = b.mLength;
            blk = b;
            break;
          } else if ( mFrom.equals( b.mTo ) && mTo.equals( b.mFrom ) ) { // TO --> FROM
            dist = - b.mLength;
            blk = b;
            break;
          }
        }
        if ( blk != null ) {
          float bc = blk.mClino * TDMath.GRAD2RAD;
          float bb = blk.mBearing * TDMath.GRAD2RAD;
          float X = (float)Math.cos( bc ) * (float)Math.cos( bb );
          float Y = (float)Math.cos( bc ) * (float)Math.sin( bb );
          float Z = (float)Math.sin( bc );
          xfrom = -dist * (float)(X1 * X + Y1 * Y + Z1 * Z); // neg. because it is the FROM point
          yfrom =  dist * (float)(X2 * X + Y2 * Y + Z2 * Z);
          if ( mType == PlotInfo.PLOT_H_SECTION ) { // Rotate as NORTH is upward
            float xx = -yn * xfrom + xn * yfrom;
            yfrom = -xn * xfrom - yn * yfrom;
            xfrom = xx;
          }
          addFixedLine( blk, xfrom, yfrom, xto, yto, 0, 0, false, false ); // not-splay, not-selecteable
          mDrawingSurface.addDrawingStationName( mFrom, DrawingUtil.toSceneX(xfrom), DrawingUtil.toSceneY(yfrom) );
          mDrawingSurface.addDrawingStationName( mTo, DrawingUtil.toSceneX(xto), DrawingUtil.toSceneY(yto) );
        }
      } else { // if ( PlotInfo.isXSection( mType ) ) 
        mDrawingSurface.addDrawingStationName( mFrom, DrawingUtil.toSceneX(xfrom), DrawingUtil.toSceneY(yfrom) );
      }

      for ( DistoXDBlock b : list ) { // repeat for splays
        if ( b.mType != DistoXDBlock.BLOCK_SPLAY ) continue;
   
        int splay_station = 3; // could use a boolean
        if ( b.mFrom.equals( mFrom ) ) {
          splay_station = 1;
          if ( TDSetting.mSectionStations == 2 ) continue;
        } else if ( b.mFrom.equals( mTo ) ) {
          splay_station = 2;
          if ( TDSetting.mSectionStations == 1 ) continue;
        } else {
          continue;
        }

        float d = b.mLength;
        float bc = b.mClino * TDMath.GRAD2RAD;
        float bb = b.mBearing * TDMath.GRAD2RAD;
        float X = (float)Math.cos( bc ) * (float)Math.cos( bb ); // North
        float Y = (float)Math.cos( bc ) * (float)Math.sin( bb ); // East
        float Z = (float)Math.sin( bc );                       // Up
        float x =  d * (float)(X1 * X + Y1 * Y + Z1 * Z);
        float y = -d * (float)(X2 * X + Y2 * Y + Z2 * Z);
        if ( mType == PlotInfo.PLOT_H_SECTION ) { // Rotate as NORTH is upward
          float xx = -yn * x + xn * y;
          y = -xn * x - yn * y;
          x = xx;
        }
        // Log.v("DistoX", "splay " + d + " " + b.mBearing + " " + b.mClino + " coord " + X + " " + Y + " " + Z );
        if ( splay_station == 1 ) {
          // N.B. this must be guaranteed for X_SECTION
          x += xfrom;
          y += yfrom;
          addFixedSectionSplay( b, xfrom, yfrom, x, y, 0, 0, false );
          // Log.v("DistoX", "Splay(F) " + x + " " + y );
        } else { // if ( splay_station == 2
          x += xto;
          y += yto;
          addFixedSectionSplay( b, xto, yto, x, y, 0, 0, true );
          // Log.v("DistoX", "Splay(T) " + x + " " + y );
        }
      }
      // mDrawingSurface.setScaleBar( mCenter.x, mCenter.y ); // (90,160) center of the drawing
    }

    private void loadFiles( long type, List<DistoXDBlock> list )
    {
      // Log.v("DistoX", "load files()" );
      
      String filename1  = null;
      String filename1b = null;
      String filename2  = null;
      String filename2b = null;
      String filename3  = null;
      String filename3b = null;

      if ( PlotInfo.isSketch2D( type ) ) {
        // Log.v( "DistoX", "load files type " + type + " " + mName1 + " " + mName2 );
        mPlot1 = mApp.mData.getPlotInfo( mSid, mName1 );
        mPid1  = mPlot1.id;
        mPlot2 = mApp.mData.getPlotInfo( mSid, mName2 );
        mPid2  = mPlot2.id;
        // Log.v("DistoX", "Plot2 type " + mPlot2.type + " azimuth " + mPlot2.azimuth );
        mPid = mPid1;
        filename1  = TDPath.getTh2FileWithExt( mFullName1 );
        filename1b = TDPath.getTdrFileWithExt( mFullName1 );
        filename2  = TDPath.getTh2FileWithExt( mFullName2 );
        filename2b = TDPath.getTdrFileWithExt( mFullName2 );
      } else {
        // Log.v( "DistoX", "load files type " + type + " " + mName3 );
        mPlot3 = mApp.mData.getPlotInfo( mSid, mName3 );
        mPid3  = mPlot3.id;
        filename3  = TDPath.getTh2FileWithExt( mFullName3 );
        filename3b = TDPath.getTdrFileWithExt( mFullName3 );
      }

      // mAllSymbols  = true; // by default there are all the symbols
      SymbolsPalette missingSymbols = null; // new SymbolsPalette(); 
      // missingSymbols = palette of missing symbols
      // if there are missing symbols mAllSymbols is false and the MissingDialog is shown
      //    (the dialog just warns the user about missing symbols, maybe a Toast would be enough)
      // when the sketch is saved, mAllSymbols is checked ( see doSaveTdr )
      // if there are not all symbols the user is asked if he/she wants to save anyways

      if ( PlotInfo.isSketch2D( type ) ) {
        if ( list.size() == 0 ) {
          Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
          if ( mPid1 >= 0 ) mData.dropPlot( mPid1, mSid );
          if ( mPid2 >= 0 ) mData.dropPlot( mPid2, mSid );
          finish();
        } else {
          mNum = new DistoXNum( list, mPlot1.start, mPlot1.view, mPlot1.hide, mDecl );
        }

        if ( ! mDrawingSurface.resetManager( DrawingSurface.DRAWING_PLAN, mFullName1 ) ) {
          // mAllSymbols =
          mDrawingSurface.modeloadDataStream( filename1b, filename1, missingSymbols );
          mDrawingSurface.addManagerToCache( mFullName1 );
        }
        if ( ! mDrawingSurface.resetManager( DrawingSurface.DRAWING_PROFILE, mFullName2 ) ) {
          // mAllSymbols = mAllSymbols &&
          mDrawingSurface.modeloadDataStream( filename2b, filename2, missingSymbols );
          mDrawingSurface.addManagerToCache( mFullName2 );
        }
        
        List<PlotInfo> xsection_plan = mData.selectAllPlotsWithType( mApp.mSID, 0, PlotInfo.PLOT_X_SECTION );
        List<PlotInfo> xsection_ext  = mData.selectAllPlotsWithType( mApp.mSID, 0, PlotInfo.PLOT_XH_SECTION );

        computeReferences( mPlot2.type, mOffset.x, mOffset.y, mZoom, true );
        computeReferences( mPlot1.type, mOffset.x, mOffset.y, mZoom, true );

        doMoveTo();

        mDrawingSurface.setStationXSections( xsection_plan, xsection_ext, mPlot2.type );
      } else {
        mTo = ( PlotInfo.isSection( type ) )? mPlot3.view : "";
        mDrawingSurface.resetManager( DrawingSurface.DRAWING_SECTION, null );
        // mAllSymbols =
        mDrawingSurface.modeloadDataStream( filename3b, filename3, missingSymbols );
      }

      // if ( ! mAllSymbols ) {
      //   String msg = missingSymbols.getMessage( getResources() );
      //   TDLog.Log( TDLog.LOG_PLOT, "Missing " + msg );
      //   Toast.makeText( this, "Missing symbols \n" + msg, Toast.LENGTH_LONG ).show();
      //   // (new MissingDialog( this, this, msg )).show();
      //   // finish();
      // }
   }

   private void setPlotType( long type )
   {
     if ( PlotInfo.isProfile( type ) ) {
       setPlotType2( false );
     } else if ( type == PlotInfo.PLOT_PLAN ) { 
       setPlotType1( false );
     } else {
       setPlotType3();
     }
   }

   private void updateReference()
   {
     // Log.v("DistoX", "updateReference()" );
     if ( mType == PlotInfo.PLOT_PLAN ) {
       saveReference( mPlot1, mPid1 );
     } else if ( PlotInfo.isProfile( mType ) ) {
       saveReference( mPlot2, mPid2 );
     }
   }

   private void saveReference( PlotInfo plot, long pid )
   {
     // Log.v("DistoX", "saveReference()" );
     // Log.v("DistoX", "save pid " + pid + " ref " + mOffset.x + " " + mOffset.y + " " + mZoom );
     plot.xoffset = mOffset.x;
     plot.yoffset = mOffset.y;
     plot.zoom    = mZoom;
     mData.updatePlot( pid, mSid, mOffset.x, mOffset.y, mZoom );
   }

   private void resetReference( PlotInfo plot )
   {
     mOffset.x = plot.xoffset; 
     mOffset.y = plot.yoffset; 
     mZoom     = plot.zoom;    
     // Log.v("DistoX", "reset ref " + mOffset.x + " " + mOffset.y + " " + mZoom );
     mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
   }

    static private Paint previewPaint = null;
    static private Paint getPreviewPaint()
    {
      if ( previewPaint == null ) {
        previewPaint = new Paint();
        previewPaint.setColor(0xFFC1C1C1);
        previewPaint.setStyle(Paint.Style.STROKE);
        previewPaint.setStrokeJoin(Paint.Join.ROUND);
        previewPaint.setStrokeCap(Paint.Cap.ROUND);
        previewPaint.setStrokeWidth( DrawingBrushPaths.WIDTH_PREVIEW );
      }
      return previewPaint;
    }

    private void doSelectAt( float x_scene, float y_scene )
    {
      if ( mMode == MODE_EDIT ) {
        // float d0 = TopoDroidApp.mCloseCutoff + TopoDroidApp.mCloseness / mZoom;
        SelectionSet selection = mDrawingSurface.getItemsAt( x_scene, y_scene, mZoom );
        // Log.v( TopoDroidApp.TAG, "selection at " + x_scene + " " + y_scene + " items " + selection.size() );
        // Log.v( TopoDroidApp.TAG, " zoom " + mZoom + " radius " + d0 );
        if ( selection.mPoints.size() > 0 ) {
          mMode = MODE_SHIFT;
          setButton3( selection.mHotItem.type() );
        }
      }
    }

    private void doEraseAt( float x_scene, float y_scene )
    {
      int ret = mDrawingSurface.eraseAt( x_scene, y_scene, mZoom, mEraseCommand );
      modified();
    }

    void updateBlockName( DistoXDBlock block, String from, String to )
    {
      // if ( mFullName2 == null ) return; // nothing for PLOT_SECTION or PLOT_H_SECTION
      if ( PlotInfo.isAnySection( mType ) )  return;
      // FIXME if ( from == null || to == null ) return;

      if ( ( ( block.mFrom == null && from == null ) || block.mFrom.equals(from) ) && 
           ( ( block.mTo == null && to == null ) || block.mTo.equals(to) ) ) return;

      block.mFrom = from;
      block.mTo   = to;
      mData.updateShotName( block.mId, mSid, from, to, true );
      doComputeReferences();
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );

      modified();
    }
 
    void updateBlockComment( DistoXDBlock block, String comment ) 
    {
      if ( comment.equals( block.mComment ) ) return;
      block.mComment = comment;
      mData.updateShotComment( block.mId, mSid, comment, true ); // true = forward
    }
    
    void updateBlockFlag( DistoXDBlock block, long flag )
    {
      if ( block.mFlag == flag ) return;
      block.mFlag = flag;
      mData.updateShotFlag( block.mId, mSid, flag, true );
    }

    // called only be DrawingShotDialog
    void updateBlockExtend( DistoXDBlock block, long extend )
    {
      if ( block.mExtend == extend ) return;
      block.mExtend = extend;
      mData.updateShotExtend( block.mId, mSid, extend, true );
      recomputeProfileReference();
    }

    // only PLOT_EXTENDED ( not PLOT_PROFILE )
    // used only when a shot extend is changed
    private void recomputeProfileReference()
    {
      if ( mType == PlotInfo.PLOT_EXTENDED ) { 
        List<DistoXDBlock> list = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
        mNum = new DistoXNum( list, mPlot1.start, mPlot1.view, mPlot1.hide, mDecl ); 
        computeReferences( (int)mType, 0.0f, 0.0f, mApp.mScaleFactor, true );
        mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
        modified();
      } 
    }

    void deletePoint( DrawingPointPath point ) 
    {
      mDrawingSurface.deletePath( point ); 
      modified();
    }

    void splitLine( DrawingLinePath line, LinePoint point )
    {
      mDrawingSurface.splitLine( line, point );
      modified();
    }

    boolean removeLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp ) 
    {
      boolean ret = mDrawingSurface.removeLinePoint(line, point, sp); 
      if ( ret ) {
        modified();
      }
      return ret;
    }


    // @param name  section-line name 
    void deleteLine( DrawingLinePath line ) 
    { 
      if ( line.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
        String name = line.getOption( "-id" );
        String scrap = mApp.mySurvey + "-" + name;
        mDrawingSurface.deleteSectionLine( line, scrap );
        TDPath.deletePlotFileWithBackups( TDPath.getTh2File( scrap + ".th2" ) );
        TDPath.deletePlotFileWithBackups( TDPath.getTdrFile( scrap + ".tdr" ) );
        TDPath.deleteFile( TDPath.getJpgFile( mApp.mySurvey, name + ".jpg" ) );
       
        PlotInfo plot = mData.getPlotInfo( mApp.mSID, name );
        if ( plot != null ) {
          mData.dropPlot( plot.id, mApp.mSID );
        } else {
          TDLog.Error("No plot NAME " + name + " SID " + mApp.mSID + " in database" );
        }
      } else {
        mDrawingSurface.deletePath( line );
      }
      modified();
    }

    void sharpenLine( DrawingLinePath line )
    {
      mDrawingSurface.sharpenLine( line );
      modified();
    }

    void reduceLine( DrawingLinePath line )
    {
      mDrawingSurface.reduceLine( line );
      modified();
    }

    void closeLine( DrawingLinePath line )
    {
      mDrawingSurface.closeLine( line );
      modified();
    }

    void deleteArea( DrawingAreaPath area )
    {
      mDrawingSurface.deletePath( area );
      modified();
    }

    private void dumpEvent( MotionEventWrap ev )
    {
      String name[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "PTR_DOWN", "PTR_UP", "7?", "8?", "9?" };
      StringBuilder sb = new StringBuilder();
      int action = ev.getAction();
      int actionCode = action & MotionEvent.ACTION_MASK;
      sb.append( "Event action_").append( name[actionCode] );
      if ( actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP ) {
        sb.append( "(pid " ).append( action>>MotionEvent.ACTION_POINTER_ID_SHIFT ).append( ")" );
      }
      sb.append( " [" );
      for (int i=0; i<ev.getPointerCount(); ++i ) {
        sb.append( "#" ).append( i );
        sb.append( "(pid " ).append( ev.getPointerId(i) ).append( ")=" ).append( (int)(ev.getX(i)) ).append( "." ).append( (int)(ev.getY(i)) );
        if ( i+1 < ev.getPointerCount() ) sb.append( ":" );
      }
      sb.append( "]" );
      // TDLog.Log(TDLog.LOG_PLOT, sb.toString() );
    }
    

    float spacing( MotionEventWrap ev )
    {
      int np = ev.getPointerCount();
      if ( np < 2 ) return 0.0f;
      float x = ev.getX(1) - ev.getX(0);
      float y = ev.getY(1) - ev.getY(0);
      return (float)Math.sqrt(x*x + y*y);
    }

    void saveEventPoint( MotionEventWrap ev )
    {
      int np = ev.getPointerCount();
      if ( np >= 1 ) {
        mSave0X = ev.getX(0);
        mSave0Y = ev.getY(0);
        if ( np >= 2 ) {
          mSave1X = ev.getX(1);
          mSave1Y = ev.getY(1);
        } else {
          mSave1X = mSave0X;
          mSave1Y = mSave0Y;
        } 
      }
    }

    
    void shiftByEvent( MotionEventWrap ev )
    {
      float x0 = 0.0f;
      float y0 = 0.0f;
      float x1 = 0.0f;
      float y1 = 0.0f;
      int np = ev.getPointerCount();
      if ( np >= 1 ) {
        x0 = ev.getX(0);
        y0 = ev.getY(0);
        if ( np >= 2 ) {
          x1 = ev.getX(1);
          y1 = ev.getY(1);
        } else {
          x1 = x0;
          y1 = y0;
        } 
      }
      float x_shift = ( x0 - mSave0X + x1 - mSave1X ) / 2;
      float y_shift = ( y0 - mSave0Y + y1 - mSave1Y ) / 2;
      mSave0X = x0;
      mSave0Y = y0;
      mSave1X = x1;
      mSave1Y = y1;
    
      if ( Math.abs( x_shift ) < TDSetting.mMinShift && Math.abs( y_shift ) < TDSetting.mMinShift ) {
        mOffset.x += x_shift / mZoom;                // add shift to offset
        mOffset.y += y_shift / mZoom; 
        mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
      }
    }

    private void moveCanvas( float x_shift, float y_shift )
    {
      if ( Math.abs( x_shift ) < TDSetting.mMinShift && Math.abs( y_shift ) < TDSetting.mMinShift ) {
        mOffset.x += x_shift / mZoom;                // add shift to offset
        mOffset.y += y_shift / mZoom; 
        mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
      }
    }

    public void checkZoomBtnsCtrl()
    {
      // if ( mZoomBtnsCtrl == null ) return; // not necessary
      if ( TDSetting.mZoomCtrl == 2 && ! mZoomBtnsCtrl.isVisible() ) {
        mZoomBtnsCtrl.setVisible( true );
      }
    }

    private boolean pointerDown = false;

    public boolean onTouch( View view, MotionEvent rawEvent )
    {
      dismissPopups();
      checkZoomBtnsCtrl();

      float d0 = TDSetting.mCloseCutoff + TDSetting.mCloseness / mZoom;

      MotionEventWrap event = MotionEventWrap.wrap(rawEvent);
      // TDLog.Log( TDLog.LOG_INPUT, "Drawing Activity onTouch() " );
      // dumpEvent( event );

      int act = event.getAction();
      int action = act & MotionEvent.ACTION_MASK;
      int id = 0;

      if (action == MotionEvent.ACTION_POINTER_DOWN) {
        mTouchMode = MODE_ZOOM;
        oldDist = spacing( event );
        saveEventPoint( event );
        pointerDown = true;
        return true;
      } else if ( action == MotionEvent.ACTION_POINTER_UP) {
        int np = event.getPointerCount();
        if ( np > 2 ) return true;
        mTouchMode = MODE_MOVE;
        id = 1 - ((act & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT);
        // int idx = rawEvent.findPointerIndex( id );
        if ( mSymbol != SYMBOL_POINT ) {
          action = MotionEvent.ACTION_DOWN; // force next case
        }
        /* fall through */
      }
      float x_canvas = event.getX(id);
      float y_canvas = event.getY(id);
      float x_scene = x_canvas/mZoom - mOffset.x;
      float y_scene = y_canvas/mZoom - mOffset.y;


      // ---------------------------------------- DOWN
      if (action == MotionEvent.ACTION_DOWN) {
        // TDLog.Log( TDLog.LOG_PLOT, "DOWN at X " + x_canvas + " [" +mBorderInnerLeft + " " + mBorderInnerRight + "] Y " 
        //                                          + y_canvas + " / " + mBorderBottom );
        if ( y_canvas > mBorderBottom ) {
          if ( mZoomBtnsCtrlOn && x_canvas > mBorderInnerLeft && x_canvas < mBorderInnerRight ) {
            mTouchMode = MODE_ZOOM;
            mZoomBtnsCtrl.setVisible( true );
            // mZoomCtrl.show( );
          } else if ( TDSetting.mSideDrag ) {
            mTouchMode = MODE_ZOOM;
          }
        } else if ( TDSetting.mSideDrag && ( x_canvas > mBorderRight || x_canvas < mBorderLeft ) ) {
          mTouchMode = MODE_ZOOM;
        }

        if ( mMode == MODE_DRAW ) {
          // TDLog.Log( TDLog.LOG_PLOT, "onTouch ACTION_DOWN symbol " + mSymbol );
          mPointCnt = 0;
          if ( mSymbol == SYMBOL_LINE ) {
            mCurrentLinePath = new DrawingLinePath( mCurrentLine );
            mCurrentLinePath.addStartPoint( x_scene, y_scene );
            mCurrentBrush.mouseDown( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
          } else if ( mSymbol == SYMBOL_AREA ) {
            // TDLog.Log( TDLog.LOG_PLOT, "onTouch ACTION_DOWN area type " + mCurrentArea );
            mCurrentAreaPath = new DrawingAreaPath( mCurrentArea, mDrawingSurface.getNextAreaIndex(),
              mName+"-a", TDSetting.mAreaBorder );
            mCurrentAreaPath.addStartPoint( x_scene, y_scene );
            // Log.v("DistoX", "start area start " + x_scene + " " + y_scene );
            mCurrentBrush.mouseDown( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
          } else { // SYMBOL_POINT
            // mSaveX = x_canvas; // FIXME-000
            // mSaveY = y_canvas;
          }
          mSaveX = x_canvas; // FIXME-000
          mSaveY = y_canvas;
        } else if ( mMode == MODE_EDIT ) {
          mStartX = x_canvas;
          mStartY = y_canvas;
          mEditMove = true;
          SelectionPoint pt = mDrawingSurface.hotItem();
          if ( pt != null ) {
            mEditMove = ( pt.distance( x_scene, y_scene ) < d0 );
          } 
          // doSelectAt( x_scene, y_scene );
          mSaveX = x_canvas;
          mSaveY = y_canvas;
          // return false;
        } else if ( mMode == MODE_SHIFT ) {
          mShiftMove = true; // whether to move canvas in point-shift mode
          mStartX = x_canvas;
          mStartY = y_canvas;

          SelectionPoint pt = mDrawingSurface.hotItem();
          if ( pt != null ) {
            if ( pt.distance( x_scene, y_scene ) < d0 ) {
              mShiftMove = false;
              mStartX = x_scene;  // save start position
              mStartY = y_scene;
            }
          }
          mSaveX = x_canvas; // FIXME-000
          mSaveY = y_canvas;
          // return false;
        } else if ( mMode == MODE_ERASE ) {
          // Log.v("DistoX", "Erase at " + x_scene + " " + y_scene );
          mEraseCommand =  new EraseCommand();
          doEraseAt( x_scene, y_scene );
        } else if ( mMode == MODE_MOVE ) {
          setTheTitle( );
          mSaveX = x_canvas; // FIXME-000
          mSaveY = y_canvas;
          mDownX = x_canvas;
          mDownY = y_canvas;
          return false;
        }

      // ---------------------------------------- MOVE

      } else if ( action == MotionEvent.ACTION_MOVE ) {
        // Log.v(  TopoDroidApp.TAG, "action MOVE mode " + mMode + " touch-mode " + mTouchMode);
        if ( mTouchMode == MODE_MOVE) {
          float x_shift = x_canvas - mSaveX; // compute shift
          float y_shift = y_canvas - mSaveY;
          boolean save = true; // FIXME-000
          // mSaveX = x_canvas; 
          // mSaveY = y_canvas;
          if ( mMode == MODE_DRAW ) {
            if ( mSymbol == SYMBOL_LINE ) {
              if ( ( x_shift*x_shift + y_shift*y_shift ) > TDSetting.mLineSegment2 ) {
                if ( ++mPointCnt % mLinePointStep == 0 ) {
                  mCurrentLinePath.addPoint( x_scene, y_scene );
                }
                mCurrentBrush.mouseMove( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
              } else {
                save = false;
              }
            } else if ( mSymbol == SYMBOL_AREA ) {
              if ( ( x_shift*x_shift + y_shift*y_shift ) > TDSetting.mLineSegment2 ) {
                if ( ++mPointCnt % mLinePointStep == 0 ) {
                  mCurrentAreaPath.addPoint( x_scene, y_scene );
                  // Log.v("DistoX", "start area add " + x_scene + " " + y_scene );
                }
                mCurrentBrush.mouseMove( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
              } else {
                save = false;
              }
            } else if ( mSymbol == SYMBOL_POINT ) {
              // if ( ( x_shift*x_shift + y_shift*y_shift ) > TDSetting.mLineSegment2 ) {
              //   pointerDown = 0;
              // }
            }
          } else if (  mMode == MODE_MOVE 
                   || (mMode == MODE_EDIT && mEditMove ) 
                   || (mMode == MODE_SHIFT && mShiftMove) ) {
            moveCanvas( x_shift, y_shift );
          } else if ( mMode == MODE_SHIFT ) {
            mDrawingSurface.shiftHotItem( x_scene - mStartX, y_scene - mStartY );
            mStartX = x_scene;
            mStartY = y_scene;
            modified();
          } else if ( mMode == MODE_ERASE ) {
            doEraseAt( x_scene, y_scene );
          }
          if ( save ) { // FIXME-000
            mSaveX = x_canvas; 
            mSaveY = y_canvas;
          }
        } else { // mTouchMode == MODE_ZOOM
          float newDist = spacing( event );
          if ( newDist > 16.0f && oldDist > 16.0f ) {
            float factor = newDist/oldDist;
            if ( factor > 0.05f && factor < 4.0f ) {
              changeZoom( factor );
              oldDist = newDist;
            }
          }
          if ( mMode == MODE_MOVE && mShiftDrawing ) {
            float x_shift = x_canvas - mSaveX; // compute shift
            float y_shift = y_canvas - mSaveY;
            if ( TDSetting.mLevelOverNormal ) {
              if ( Math.abs( x_shift ) < TDSetting.mMinShift && Math.abs( y_shift ) < TDSetting.mMinShift ) {
                mDrawingSurface.shiftDrawing( x_shift/mZoom, y_shift/mZoom );
                modified();
              }
            // } else {
            //   moveCanvas( x_shift, y_shift );
            }
            mSaveX = x_canvas;
            mSaveY = y_canvas;
          } else {
            shiftByEvent( event );
          }
        }

      // ---------------------------------------- UP

      } else if (action == MotionEvent.ACTION_UP) {
        if ( onMenu ) {
          closeMenu();
          return true;
        }

        if ( mTouchMode == MODE_ZOOM ) {
          mTouchMode = MODE_MOVE;
        } else {
          float x_shift = x_canvas - mSaveX; // compute shift
          float y_shift = y_canvas - mSaveY;
          if ( mMode == MODE_DRAW ) {
            if ( mSymbol == SYMBOL_LINE || mSymbol == SYMBOL_AREA ) {

              mCurrentBrush.mouseUp( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
              mDrawingSurface.previewPath.mPath = new Path();

              if ( mSymbol == SYMBOL_LINE ) {
                if (    ( x_shift*x_shift + y_shift*y_shift ) > TDSetting.mLineSegment2
                     || ( mPointCnt % mLinePointStep ) > 0 ) {
                  mCurrentLinePath.addPoint( x_scene, y_scene );
                }
              } else if ( mSymbol == SYMBOL_AREA ) {
                // Log.v("DistoX",
                //       "DX " + (x_scene - mCurrentAreaPath.mFirst.mX) + " DY " + (y_scene - mCurrentAreaPath.mFirst.mY ) );
                if (    PlotInfo.isVertical( mType )
                     && DrawingBrushPaths.mAreaLib.isCloseHorizontal( mCurrentArea ) 
                     && Math.abs( x_scene - mCurrentAreaPath.mFirst.mX ) > 20  // 20 == 1.0 meter
                     && Math.abs( y_scene - mCurrentAreaPath.mFirst.mY ) < 10  // 10 == 0.5 meter
                  ) {
                  LinePoint lp = mCurrentAreaPath.mFirst; 
                  float yy = lp.mY;
                  mCurrentAreaPath.addPoint( x_scene, yy-0.001f );
                  DrawingAreaPath area = new DrawingAreaPath( mCurrentAreaPath.mAreaType,
                                                              mCurrentAreaPath.mAreaCnt, 
                                                              mCurrentAreaPath.mPrefix, 
                                                              TDSetting.mAreaBorder );
                  area.addStartPoint( lp.mX, lp.mY );
                  for ( lp = lp.mNext; lp != null; lp = lp.mNext ) {
                    if ( lp.mY <= yy ) {
                      area.addPoint( lp.mX, yy );
                      break;
                    } else {
                      area.addPoint( lp.mX, lp.mY );
                    }
                  }
                  mCurrentAreaPath = area;
                } else {  
                  if (    ( x_shift*x_shift + y_shift*y_shift ) > TDSetting.mLineSegment2
                       || ( mPointCnt % mLinePointStep ) > 0 ) {
                    mCurrentAreaPath.addPoint( x_scene, y_scene );
                  }
                }
              }
              
              if ( mPointCnt > mLinePointStep || mLinePointStep == POINT_MAX ) {
                if ( ! ( mSymbol == SYMBOL_LINE && mCurrentLinePath.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) 
                     && TDSetting.mLineStyle == TDSetting.LINE_STYLE_BEZIER
                     && ( mSymbol == SYMBOL_AREA || ! DrawingBrushPaths.mLineLib.isStyleStraight( mCurrentLinePath.mLineType ) )
                   ) {
                  int nPts = (mSymbol == SYMBOL_LINE )? mCurrentLinePath.size() : mCurrentAreaPath.size() ;
                  if ( nPts > 1 ) {
                    ArrayList< BezierPoint > pts = new ArrayList< BezierPoint >(); // [ nPts ];
                    // ArrayList< LinePoint > lp = 
                    //   (mSymbol == SYMBOL_LINE )? mCurrentLinePath.mPoints : mCurrentAreaPath.mPoints ;
                    // for (int k=0; k<nPts; ++k ) {
                    //   pts.add( new BezierPoint( lp.get(k).mX, lp.get(k).mY ) );
                    // }
                    LinePoint lp = (mSymbol == SYMBOL_LINE )? mCurrentLinePath.mFirst : mCurrentAreaPath.mFirst;
                    for ( ; lp != null; lp = lp.mNext ) {
                      pts.add( new BezierPoint( lp.mX, lp.mY ) );
                    }

                    mBezierInterpolator.fitCurve( pts, nPts, TDSetting.mLineAccuracy, TDSetting.mLineCorner );
                    ArrayList< BezierCurve > curves = mBezierInterpolator.getCurves();
                    int k0 = curves.size();
                    // TDLog.Log( TDLog.LOG_PLOT, " Bezier size " + k0 );
                    if ( k0 > 0 ) {
                      BezierCurve c = curves.get(0);
                      BezierPoint p0 = c.getPoint(0);
                      if ( mSymbol == SYMBOL_LINE ) {
                        DrawingLinePath lp1 = new DrawingLinePath( mCurrentLine );
                        lp1.addStartPoint( p0.mX, p0.mY );
                        for (int k=0; k<k0; ++k) {
                          c = curves.get(k);
                          BezierPoint p1 = c.getPoint(1);
                          BezierPoint p2 = c.getPoint(2);
                          BezierPoint p3 = c.getPoint(3);
                          lp1.addPoint3(p1.mX, p1.mY, p2.mX, p2.mY, p3.mX, p3.mY );
                        }
                        boolean addline = true;
                        if ( mContinueLine > CONT_NO && mCurrentLine != DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
                          DrawingLinePath line = mDrawingSurface.getLineToContinue( mCurrentLinePath.mFirst, mCurrentLine, mZoom );
                          if ( line != null ) {
                            // Log.v( "DistoX", "[B] continuing line type " + mCurrentLine );
                            if ( mContinueLine == CONT_CONT && mCurrentLine == line.mLineType ) {
                              mDrawingSurface.addLineToLine( mCurrentLinePath, line );
                              addline = false;
                            } else {
                              if ( line.mFirst.distance( mCurrentLinePath.mFirst ) < 20 ) {
                                // line.reversePath();
                                lp1.moveFirstTo( line.mFirst.mX, line.mFirst.mY );
                              } else {
                                lp1.moveFirstTo( line.mLast.mX, line.mLast.mY );
                              }
                            }
                          }
                        } 
                        if ( addline ) {
                          lp1.computeUnitNormal();
                          mDrawingSurface.addDrawingPath( lp1 );
                        }
                      } else { //  mSymbol == SYMBOL_AREA
                        DrawingAreaPath ap = new DrawingAreaPath( mCurrentArea, mDrawingSurface.getNextAreaIndex(),
                          mName+"-a", TDSetting.mAreaBorder ); 
                        ap.addStartPoint( p0.mX, p0.mY );
                        for (int k=0; k<k0; ++k) {
                          c = curves.get(k);
                          BezierPoint p1 = c.getPoint(1);
                          BezierPoint p2 = c.getPoint(2);
                          BezierPoint p3 = c.getPoint(3);
                          ap.addPoint3(p1.mX, p1.mY, p2.mX, p2.mY, p3.mX, p3.mY );
                        }
                        ap.close();
                        ap.shiftShaderBy( mOffset.x, mOffset.y, mZoom );
                        mDrawingSurface.addDrawingPath( ap );
                      }
                    }
                  }
                } else {
                  if ( mSymbol == SYMBOL_LINE ) {
                    // N.B.
                    // section direction is in the direction of the tick
                    // and splay reference are taken from the station the section looks towards
                    // section line points: right-end -- left-end -- tick-end
                    //
                    if ( mCurrentLinePath.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
                      mCurrentLinePath.addOption("-direction both");
                      mCurrentLinePath.makeStraight( true ); // true = with arrow
                      boolean h_section = PlotInfo.isProfile( mType );
                     
                      // NOTE here l1 is the end-point and l2 the start-point (not considering the tick)
                      //         |
                      //         L2 --------- L1
                      //      The azimuth reference is North-East same as bearing
                      //         L1->L2 = atan2( (L2-L1).x, -(L2-l1).y )  Y is point downward North upward
                      //         azimuth = dir(L1->L2) + 90
                      //
                      LinePoint l2 = mCurrentLinePath.mFirst.mNext;
                      LinePoint l1 = l2.mNext;
                      // Log.v("DistoX", "section line L1 " + l1.mX + " " + l1.mY + " L2 " + l2.mX + " " + l2.mY );

                      List< DrawingPath > paths = mDrawingSurface.getIntersectionShot( l1, l2 );
                      if ( paths.size() > 0 ) {
                        mCurrentLinePath.computeUnitNormal();

                        String from = "-1";
                        String to   = "-1";
                        float clino = 0;

                        float azimuth = 90 + (float)(Math.atan2( l2.mX-l1.mX, -l2.mY+l1.mY ) * TDMath.RAD2GRAD );
                        if ( azimuth >= 360.0f ) azimuth -= 360;
                        if ( azimuth < 0.0f ) azimuth += 360;

                        DistoXDBlock blk = null;
                        float intersection = 0;
                        if ( paths.size() > 1 ) {
                          Toast.makeText( this, R.string.too_many_leg_intersection, Toast.LENGTH_SHORT ).show();
                        } else {
                          DrawingPath p = paths.get(0);
                          blk = p.mBlock;
                          Float result = Float.valueOf(0);
                          p.intersect( l1.mX, l1.mY, l2.mX, l2.mY, result );
                          intersection = result.floatValue();
                          // p.log();
                        }

                        if ( blk != null ) {
                          from = blk.mFrom;
                          to   = blk.mTo;
                          if ( h_section ) {
                            int extend = 1;
                            if ( azimuth < 180 ) {
                              clino = 90 - azimuth;
                              // extend = 1;
                            } else {
                              clino = azimuth - 270;
                              extend = -1;
                            }
                            float dc = (extend == blk.mExtend)? clino - blk.mClino : 180 - clino - blk.mClino ;
                            if ( dc < 0 ) dc += 360;
                            if ( dc > 360 ) dc -= 360;
                            if ( dc > 90 && dc <= 270 ) { // exchange FROM-TO 
                              azimuth = blk.mBearing + 180; if ( azimuth >= 360 ) azimuth -= 360;
                              from = blk.mTo;
                              to   = blk.mFrom;
                            } else {
                              azimuth = blk.mBearing;
                            }
                            // if ( extend != blk.mExtend ) {
                            //   azimuth = blk.mBearing + 180; if ( azimuth >= 360 ) azimuth -= 360;
                            // }
                          } else {
                            float da = azimuth - blk.mBearing;
                            if ( da < 0 ) da += 360;
                            if ( da > 360 ) da -= 360;
                            if ( da > 90 && da <= 270 ) { // exchange FROM-TO 
                              from = blk.mTo;
                              to   = blk.mFrom;
                            }
                          }
                        } else { // null block
                          azimuth = 90 + (float)(Math.atan2( l2.mX-l1.mX, -l2.mY+l1.mY ) * TDMath.RAD2GRAD );
                          if ( azimuth >= 360.0f ) azimuth -= 360;
                          if ( azimuth < 0.0f ) azimuth += 360;
                        }
                        // Log.v("DistoX", "new section " + from + " - " + to );
                        // cross-section does not exists yet
                        String section_id = mData.getNextSectionId( mApp.mSID );
                        mCurrentLinePath.addOption( "-id " + section_id );
                        mDrawingSurface.addDrawingPath( mCurrentLinePath );

                        if ( TDSetting.mAutoSectionPt && section_id != null ) {
                          float x5 = mCurrentLinePath.mLast.mX + mCurrentLinePath.mDx * 20; 
                          float y5 = mCurrentLinePath.mLast.mY + mCurrentLinePath.mDy * 20; 
                          String scrap = mApp.mySurvey + "-" + section_id;
                          DrawingPointPath section_pt = new DrawingPointPath( DrawingBrushPaths.mPointLib.mPointSectionIndex,
                                                                            x5, y5, DrawingPointPath.SCALE_M, 
                                                                            "-scrap " + scrap );
                          mDrawingSurface.addDrawingPath( section_pt );
                        }

                        new DrawingLineSectionDialog( this, mApp, h_section, false, section_id, mCurrentLinePath, from, to, azimuth, clino ).show();

                      } else { // empty path list
                        Toast.makeText( this, R.string.no_leg_intersection, Toast.LENGTH_SHORT ).show(); 
                      }
                    } else {
                      boolean addline= true;
                      if ( mContinueLine > CONT_NO && mCurrentLine != DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
                        // Log.v( "DistoX", "[N] try to continue line type " + mCurrentLine );
                        DrawingLinePath line = mDrawingSurface.getLineToContinue( mCurrentLinePath.mFirst, mCurrentLine, mZoom );
                        if ( line != null ) {
                          // Log.v( "DistoX", "[N] continuing line type " + mCurrentLine );
                          if ( mContinueLine == CONT_CONT && mCurrentLine == line.mLineType ) {
                            mDrawingSurface.addLineToLine( mCurrentLinePath, line );
                            addline = false;
                          } else {
                            if ( line.mFirst.distance( mCurrentLinePath.mFirst ) < 20 ) {
                              // line.reversePath();
                              mCurrentLinePath.moveFirstTo( line.mFirst.mX, line.mFirst.mY );
                            } else {
                              mCurrentLinePath.moveFirstTo( line.mLast.mX, line.mLast.mY );
                            }
                          }
                        }
                      }
                      if ( addline ) {
                        mCurrentLinePath.computeUnitNormal();
                        mDrawingSurface.addDrawingPath( mCurrentLinePath );
                      }
                    }
                  } else { //  mSymbol == SYMBOL_AREA
                    mCurrentAreaPath.close();
                    mCurrentAreaPath.shiftShaderBy( mOffset.x, mOffset.y, mZoom );
                    mDrawingSurface.addDrawingPath( mCurrentAreaPath );
                  }
                }
                // undoBtn.setEnabled(true);
                // redoBtn.setEnabled(false);
                // canRedo = false;
              }
              // if ( mSymbol == SYMBOL_LINE ) {
              //   // Log.v( TopoDroidApp.TAG, "line type " + mCurrentLinePath.mLineType );
              //   if ( mCurrentLinePath.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
              //     // keep only first and last point
              //     // remove line points are put the new ones: FIXME delete and add it again
              //     mDrawingSurface.addDrawingPath( mCurrentLinePath );
              //   }
              // }
            } else { // SYMBOL_POINT
              if ( ( ! pointerDown ) && Math.abs( x_shift ) < TDSetting.mPointingRadius 
                                     && Math.abs( y_shift ) < TDSetting.mPointingRadius ) {
                if ( DrawingBrushPaths.mPointLib.pointHasText(mCurrentPoint) ) {
                  DrawingLabelDialog label = new DrawingLabelDialog( mDrawingSurface.getContext(), this, x_scene, y_scene );
                  label.show();
                } else {
                  mDrawingSurface.addDrawingPath( 
                    new DrawingPointPath( mCurrentPoint, x_scene, y_scene, mPointScale, null ) );

                  // undoBtn.setEnabled(true);
                  // redoBtn.setEnabled(false);
                  // canRedo = false;
                }
              }
            }
            pointerDown = false;
            modified();
          } else if ( mMode == MODE_EDIT ) {
            if ( Math.abs(mStartX - x_canvas) < TDSetting.mPointingRadius 
              && Math.abs(mStartY - y_canvas) < TDSetting.mPointingRadius ) {
              doSelectAt( x_scene, y_scene );
            }
            mEditMove = false;
          } else if ( mMode == MODE_SHIFT ) {
            if ( mShiftMove ) {
              if ( Math.abs(mStartX - x_canvas) < TDSetting.mPointingRadius
                && Math.abs(mStartY - y_canvas) < TDSetting.mPointingRadius ) {
                // mEditMove = false;
                mMode = MODE_EDIT;
                mDrawingSurface.clearSelected();
                setButton3( -1 );
              }
            }
            mShiftMove = false;
          } else if ( mMode == MODE_ERASE ) {
            if ( mEraseCommand != null && mEraseCommand.size() > 0 ) {
              mEraseCommand.completeCommand();
              mDrawingSurface.addEraseCommand( mEraseCommand );
              mEraseCommand = null;
            }
          } else { // MODE_MOVE 
/* FIXME for the moment do not create X-Sections
            if ( Math.abs(x_canvas - mDownX) < 10 && Math.abs(y_canvas - mDownY) < 10 ) {
              // check if there is a station: only PLAN and EXTENDED or PROFILE
              if ( PlotInfo.isSketch2D( mType ) ) {
                DrawingStationName sn = mDrawingSurface.getStationAt( x_scene, y_scene );
                if ( sn != null ) {
                  boolean barrier = mNum.isBarrier( sn.mName );
                  boolean hidden  = mNum.isHidden( sn.mName );
                  // new DrawingStationDialog( this, this, sn, barrier, hidden ).show();
                  openXSection( sn, sn.mName, mType );
                }
              }
            }
*/
          }
        }
      }
      return true;
    }



    // add a therion label point (ILabelAdder)
    public void addLabel( String label, float x, float y )
    {
      if ( label != null && label.length() > 0 ) {
        DrawingLabelPath label_path = new DrawingLabelPath( label, x, y, mPointScale, null );
        mDrawingSurface.addDrawingPath( label_path );
        modified();
      } 
    }

    void setCurrentStationName( String name ) { mApp.setCurrentStationName( name ); }

    void deleteXSection( DrawingStationName st, String name, long type ) 
    {
      long xtype = -1;
      String xsname = null;
      if ( type == PlotInfo.PLOT_PLAN ) {
        xsname = "xs-" + name;
        xtype = PlotInfo.PLOT_X_SECTION;
      } else if ( PlotInfo.isProfile( type ) ) {
        xsname = "xh-" + name;
        xtype = PlotInfo.PLOT_XH_SECTION;
      } else {
        return;
      }

      st.resetXSection();
      mData.deletePlotByName( xsname, mApp.mSID );
      // drop the files
      File tdr = new File( TDPath.getSurveyPlotTdrFile( mApp.mySurvey, xsname ) );
      if ( tdr.exists() ) tdr.delete(); 
      File th2 = new File( TDPath.getSurveyPlotTh2File( mApp.mySurvey, xsname ) );
      if ( th2.exists() ) th2.delete(); 
      // TODO delete backup files
    }

    // X-SECTION AT A STATION
    // @param name station name
    void openXSection( DrawingStationName st, String name, long type ) 
    {
      long xtype = -1;
      String xsname = null;
      if ( type == PlotInfo.PLOT_PLAN ) {
        xsname = "xs-" + name;
        xtype = PlotInfo.PLOT_X_SECTION;
      } else if ( PlotInfo.isProfile( type ) ) {
        xsname = "xh-" + name;
        xtype = PlotInfo.PLOT_XH_SECTION;
      } else {
        return;
      }

      PlotInfo plot = mData.getPlotInfo( mApp.mSID, xsname );
      if ( plot == null  ) { // if there does not exist xsection xs-name create it
        float azimuth = 0;
        float clino   = 0;
        List< DistoXDBlock > legs = mData.selectShotsAt( mApp.mSID, name, true ); // select legs
        if ( legs.size() == 1 ) { // XSection always look "away" for terminal station
          DistoXDBlock leg0 = legs.get(0);
          if ( name.equals( leg0.mFrom ) ) {
            azimuth = leg0.mBearing + 180; 
            clino   = - leg0.mClino;
          } else {
            azimuth = leg0.mBearing; 
            clino   = leg0.mClino;
          }
        } else if ( legs.size() == 2 ) {
          DistoXDBlock leg0 = legs.get(0);
          DistoXDBlock leg1 = legs.get(1);
          float b0 = leg0.mBearing;
          float b1 = leg1.mBearing;
          float c0 = leg0.mClino;
          float c1 = leg1.mClino;
          if ( name.equals( leg1.mTo ) ) {
            b1 = -b1;
            c1 = -c1;
          }
          if ( name.equals( leg0.mFrom ) ) {
            b0 = -b0;
            c0 = -c0;
          }
          azimuth = (b1 + b0)/2;
          if ( Math.abs( b1 - b0 ) > 180 ) azimuth += 180;
          clino = ( c0 + c1 ) / 2;
          
        } else {
          // Log.v("DistoX", "X_SECTION Too many legs" );
          Toast.makeText( this, R.string.too_many_legs_xsection, Toast.LENGTH_SHORT ).show();
          return;
        }
        if ( azimuth >= 360 ) azimuth -= 360;

        if ( PlotInfo.isProfile( type ) ) {
          clino = ( clino >  TDSetting.mVertSplay )?  90
                : ( clino < -TDSetting.mVertSplay )? -90 : 0;
        // } else { // type == PlotInfo.PLOT_PLAN
        //   clino = 0;
        }
        // Log.v("DistoX", "new X section azimuth " + azimuth + " clino " + clino );

        long pid = mApp.insert2dSection( mApp.mSID, xsname, xtype, name, "", azimuth, clino );
        plot = mData.getPlotInfo( mApp.mSID, xsname );

        // add x-section to station-name

        st.setXSection( azimuth, clino, type );
      }
      if ( plot != null ) {
        // Log.v("DistoX", "invoke X section " + plot.name + " <" + plot.start + "> " + plot.azimuth + " " + plot.clino );
        // Intent drawIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DrawingActivity.class );
        // drawIntent.putExtra( TopoDroidTag.TOPODROID_SURVEY_ID, mApp.mSID );
        // drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_NAME, plot.name );
        // drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_TYPE, plot.type );
        // drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_FROM, plot.start );
        // drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_TO,   "" );
        // drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_AZIMUTH, plot.azimuth );
        // drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_CLINO,   plot.clino );
        // startActivity( drawIntent );

        pushInfo( plot.type, plot.name, plot.start, "", plot.azimuth, plot.clino );
      }
    }

    void toggleStationSplays( String name )
    {
      mDrawingSurface.toggleStationSplays( name );
    }

    void toggleStationHidden( String name, boolean is_hidden )
    {
      // Log.v("DistoX", "toggle station " + name + " hidden " + is_hidden );

      String hide = mPlot1.hide;
      String new_hide = "";
      boolean add = false;
      boolean drop = false;
      if ( hide == null ) {
        add = true;
        drop = false;
      } else {
        String[] hidden = hide.split( "\\s+" );
        int k = 0;
        for (; k < hidden.length; ++k ) {
          if ( hidden[k].equals( name ) ) { // N.B. hidden[k] != null
            drop = true;
          } else {
            new_hide = new_hide + " " + hidden[k];
          }
        }
        new_hide.trim();
        add = ! drop;
      }
      int h = 0;

      if ( add && ! is_hidden ) {
        if ( hide == null || hide.length() == 0 ) {
          hide = name;
        } else {
          hide = hide + " " + name;
        }
        // Log.v( "DistoX", "addStationHidden " + name + " hide <" + hide + ">" );
        mData.updatePlotHide( mPid1, mSid, hide );
        mData.updatePlotHide( mPid2, mSid, hide );
        mPlot1.hide = hide;
        mPlot2.hide = hide;
        h = 1; // hide
      } else if ( drop && is_hidden ) {
        mData.updatePlotHide( mPid1, mSid, new_hide );
        mData.updatePlotHide( mPid2, mSid, new_hide );
        mPlot1.hide = new_hide;
        mPlot2.hide = new_hide;
        h = -1; // un-hide
        // Log.v( "DistoX", "dropStationHidden " + name + " hide <" + new_hide + ">" );
      }
      // Log.v("DistoX", "toggle station hidden: hide " + hide + " H " + h );

      if ( h != 0 ) {
        mNum.setStationHidden( name, h );
        computeReferences( (int)mType, 0, 0, mZoom, false );
      }
    }
    //  mNum.setStationHidden( name, (hidden? -1 : +1) ); // if hidden un-hide(-1), else hide(+1)

    void toggleStationBarrier( String name, boolean is_barrier ) 
    {
      // Log.v("DistoX", "toggle station " + name + " barrier " + is_barrier );
      String view = mPlot1.view;
      String new_view = "";
      boolean add = false;
      boolean drop = false;
      if ( view == null ) {
        add = true;
        drop = false;
      } else {
        String[] barrier = view.split( " " );
        int k = 0;
        for (; k < barrier.length; ++k ) {
          if ( barrier[k].equals( name ) ) { // N.B. barrier[k] != null
            drop = true;
          } else {
            new_view = new_view + " " + barrier[k];
          }
        }
        new_view.trim();
        add = ! drop;
      }
      int h = 0;

      if ( add && ! is_barrier ) {
        if ( view == null || view.length() == 0 ) {
          view = name;
        } else {
          view = view + " " + name;
        }
        // Log.v( TopoDroidApp.TAG, "addStationBarrier " + name + " view <" + view + ">" );
        mData.updatePlotView( mPid1, mSid, view );
        mData.updatePlotView( mPid2, mSid, view );
        mPlot1.view = view;
        mPlot2.view = view;
        h = 1;
      } else if ( drop && is_barrier ) {
        mData.updatePlotView( mPid1, mSid, new_view );
        mData.updatePlotView( mPid2, mSid, new_view );
        mPlot1.view = new_view;
        mPlot2.view = new_view;
        h = -1;
      }
      // Log.v("DistoX", "toggle station barrier: view " + view + " H " + h );

      if ( h != 0 ) {
        mNum.setStationBarrier( name, h );
        computeReferences( (int)mType, 0, 0, mZoom, false );
      }
    }
   
    // add a therion station point
    public void addStationPoint( DrawingStationName st )
    {
      mDrawingSurface.addDrawingStationPath( new DrawingStationPath( st, DrawingPointPath.SCALE_M ) );
      modified();
    }

    public void removeStationPoint( DrawingStationName st, DrawingStationPath path )
    {
      mDrawingSurface.removeDrawingStationPath( path );
      modified();
    }


    void doDelete()
    {
      mData.deletePlot( mPid1, mSid );
      if ( mPid2 >= 0 ) mData.deletePlot( mPid2, mSid );
      finish();
    }

    private void askDelete()
    {
      TopoDroidAlertDialog.makeAlert( this, getResources(), R.string.plot_delete,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            doDelete();
          }
        }
      );
    }

    private void setMode( int mode )
    {
      mMode = mode;
      switch ( mMode ) {
        case MODE_MOVE:
          setTheTitle();
          mDrawingSurface.setDisplayPoints( false );
          mListView.setAdapter( mButtonView1.mAdapter );
          mListView.invalidate();
          break;
        case MODE_DRAW:
          setTheTitle();
          mDrawingSurface.setDisplayPoints( false );
          mListView.setAdapter( mButtonView2.mAdapter );
          mListView.invalidate();
          break;
        case MODE_ERASE:
          setTheTitle();
          mDrawingSurface.setDisplayPoints( false );
          mListView.setAdapter( mButtonView5.mAdapter );
          mListView.invalidate();
          break;
        case MODE_EDIT:
          setTheTitle();
          mDrawingSurface.setDisplayPoints( true );
          mListView.setAdapter( mButtonView3.mAdapter );
          mListView.invalidate();
          break;
        default:
          break;
      }
    }

    /** line/area editing
     * @param b button
     */
    private void makePopupEdit( View b )
    {
      if ( mPopupEdit != null ) return;

      final Context context = this;
      LinearLayout popup_layout = new LinearLayout(this);
      popup_layout.setOrientation(LinearLayout.VERTICAL);
      int lHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
      int lWidth = LinearLayout.LayoutParams.WRAP_CONTENT;

      // ----- MOVE POINT TO THE NEAREST CLOSE POINT
      //
      String text = getString(R.string.popup_join_pt);
      int len = text.length();
      Button myTextView0 = CutNPaste.makePopupButton( this, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_POINT ||
                 mHotItemType == DrawingPath.DRAWING_PATH_LINE ||
                 mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // move to nearest point POINT/LINE/AREA
              if ( mDrawingSurface.moveHotItemToNearestPoint() ) {
                modified();
              } else {
                Toast.makeText( context, R.string.failed_snap_to_point, Toast.LENGTH_SHORT ).show();
              }
            }
            dismissPopupEdit();
          }
        } );
  
      // ----- SNAP AREA BORDER TO CLOSE LINE
      //
      text = getString(R.string.popup_snap_ln);
      if ( len < text.length() ) len = text.length();
      Button myTextView1 = CutNPaste.makePopupButton( this, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // snap to nearest line
              switch ( mDrawingSurface.snapHotItemToNearestLine() ) {
                case 1:  // single point copy
                case 0:  // normal
                case -1: // no hot point
                case -2: // not snapping area border
                  modified();
                  break;
                case -3: // no line close enough
                  Toast.makeText( context, R.string.failed_snap_to_line, Toast.LENGTH_SHORT ).show();
                  break;
                default:
                  break;
              }
            }
            dismissPopupEdit();
          }
        } );
  
      // ----- SPLIT LINE/AREA POINT IN TWO
      //
      text = getString(R.string.popup_split_pt);
      if ( len > text.length() ) len = text.length();
      Button myTextView2 = CutNPaste.makePopupButton( this, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // split point LINE/AREA
              mDrawingSurface.splitHotItem();
              modified();
            }
            dismissPopupEdit();
          }
        } );

      // ----- CUT LINE AT SELECTED POINT AND SPLIT IT IN TWO LINES
      //
      text = getString(R.string.popup_split_ln);
      if ( len < text.length() ) len = text.length();
      Button myTextView3 = CutNPaste.makePopupButton( this, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE ) { // split-line LINE
              SelectionPoint sp = mDrawingSurface.hotItem();
              if ( sp != null && sp.type() == DrawingPath.DRAWING_PATH_LINE ) {
                splitLine( (DrawingLinePath)(sp.mItem), sp.mPoint );
                modified();
              }
            }
            dismissPopupEdit();
          }
        } );

      // ----- MAKE LINE SEGMENT STRAIGHT
      //
      text = getString(R.string.popup_sharp_pt);
      if ( len < text.length() ) len = text.length();
      Button myTextView4 = CutNPaste.makePopupButton( this, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) {
              // make segment straight LINE/AREA
              SelectionPoint sp = mDrawingSurface.hotItem();
              if ( sp != null && ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) ) {
                sp.mPoint.has_cp = false;
                DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
                line.retracePath();
                modified();
              }
            }
            dismissPopupEdit();
          }
        } );

      // ----- MAKE LINE SEGMENT SMOOTH (CURVED, WITH CONTROL POINTS)
      //
      text = getString(R.string.popup_curve_pt);
      if ( len < text.length() ) len = text.length();
      Button myTextView5 = CutNPaste.makePopupButton( this, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) {
              // make segment curved LINE/AREA
              SelectionPoint sp = mDrawingSurface.hotItem();
              if ( sp != null && ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) ) {
                LinePoint lp0 = sp.mPoint;
                LinePoint lp2 = lp0.mPrev; 
                if ( ! lp0.has_cp && lp2 != null ) {
                  float dx = (lp0.mX - lp2.mX)/3;
                  float dy = (lp0.mY - lp2.mY)/3;
                  if ( Math.abs(dx) > 0.01 || Math.abs(dy) > 0.01 ) {
                    lp0.mX1 = lp2.mX + dx;
                    lp0.mY1 = lp2.mY + dy;
                    lp0.mX2 = lp0.mX - dx;
                    lp0.mY2 = lp0.mY - dy;
                    lp0.has_cp = true;
                    DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
                    line.retracePath();
                  }
                }
                modified();
              }
            }
            dismissPopupEdit();
          }
        } );

      // ----- REMOVE LINE/AREA POINT
      //
      text = getString(R.string.popup_remove_pt);
      if ( len < text.length() ) len = text.length();
      Button myTextView6 = CutNPaste.makePopupButton( this, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // remove pt
              SelectionPoint sp = mDrawingSurface.hotItem();
              if ( sp != null && ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) ) {
                DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
                if ( line.size() > 2 ) {
                  removeLinePoint( line, sp.mPoint, sp );
                  line.retracePath();
                  modified();
                }
              }
            }
            dismissPopupEdit();
          }
        } );

      FontMetrics fm = myTextView0.getPaint().getFontMetrics();
      // Log.v("DistoX", "font metrics TOP " + fm.top + " ASC. " + fm.ascent + " BOT " + fm.bottom + " LEAD " + fm.leading ); 
      int w = (int)( Math.abs( ( len + 1 ) * fm.ascent ) * 0.7);
      int h = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 1.70);
      // int h1 = (int)( myTextView0.getHeight() * 7 * 1.1 ); this is 0
      myTextView0.setWidth( w );
      myTextView1.setWidth( w );
      myTextView2.setWidth( w );
      myTextView3.setWidth( w );
      myTextView4.setWidth( w );
      myTextView5.setWidth( w );
      myTextView6.setWidth( w );
      // Log.v( TopoDroidApp.TAG, "popup width " + w );
      mPopupEdit = new PopupWindow( popup_layout, w, h ); 
      // mPopupEdit = new PopupWindow( popup_layout, popup_layout.getHeight(), popup_layout.getWidth() );
      mPopupEdit.showAsDropDown(b); 
    }

    private boolean dismissPopupEdit()
    {
      if ( mPopupEdit != null ) {
        mPopupEdit.dismiss();
        mPopupEdit = null;
        return true;
      }
      return false;
    }

    private boolean dismissPopups() 
    {
      return dismissPopupEdit() || CutNPaste.dismissPopupBT();
    }

    // -----------------------------------------------------------------------------------------

    private void switchPlotType()
    {
      doSaveTdr(); // this sets mModified = false
      updateReference();
      if ( mType == PlotInfo.PLOT_PLAN ) {
        setPlotType2( false );
      } else if ( PlotInfo.isProfile( mType ) ) {
        setPlotType1( false );
      }
    }

    private void setPlotType3( )
    {
      // Log.v("DistoX", "set plot type 2 mType " + mType );
      mPid  = mPid3;
      mName = mName3;
      mType = mPlot3.type;
      // mButton1[ BTN_PLOT ].setBackgroundDrawable( mBMextend );
      mDrawingSurface.setManager( DrawingSurface.DRAWING_SECTION, (int)mType );
      resetReference( mPlot3 );
    } 

    private void setPlotType2( boolean compute )
    {
      // Log.v("DistoX", "set plot type 2 mType " + mType );
      mPid  = mPid2;
      mName = mName2;
      mType = mPlot2.type;
      mButton1[ BTN_PLOT ].setBackgroundDrawable( mBMextend );
      mDrawingSurface.setManager( DrawingSurface.DRAWING_PROFILE, (int)mType );
      if ( compute ) {
        computeReferences( mPlot2.type, 0.0f, 0.0f, mApp.mScaleFactor, true );
      }
      resetReference( mPlot2 );
      if ( mApp.mShotActivity != null ) {
        mApp.mShotActivity.mRecentPlotType = mType;
      } else {
        TDLog.Error("Null app mShotActivity on recent plot type2");
      }
    } 

    private void setPlotType1( boolean compute )
    {
      // Log.v("DistoX", "set plot type 1 mType " + mType );
      mPid  = mPid1;
      mName = mName1;
      mType = mPlot1.type;
      mButton1[ BTN_PLOT ].setBackgroundDrawable( mBMplan );
      mDrawingSurface.setManager( DrawingSurface.DRAWING_PLAN, (int)mType );
      if ( compute ) {
        computeReferences( mPlot1.type, 0.0f, 0.0f, mApp.mScaleFactor, true );
      }
      resetReference( mPlot1 );
      if ( mApp.mShotActivity != null ) {
        mApp.mShotActivity.mRecentPlotType = mType;
      } else {
        TDLog.Error("Null app mShotActivity on recent plot type1");
      }
    }

    private void flipBlock( DistoXDBlock blk )
    {
      if ( blk != null ) {
        if ( blk.mExtend == -1 ) {
          blk.mExtend = 1;
          mData.updateShotExtend( blk.mId, mSid, blk.mExtend, true );
        } else if ( blk.mExtend == 1 ) {
          blk.mExtend = -1;
          mData.updateShotExtend( blk.mId, mSid, blk.mExtend, true );
        }
      }
    }

    // flip the profile sketch left/right
    // @param flip_shots whether to flip also the shots extend
    // @note barrier shots are not flipped; hiding shots are flipped
    public void flipProfile( boolean flip_shots )
    {
      mDrawingSurface.flipProfile( );
      if ( flip_shots ) {
        DistoXDBlock blk;
        for ( NumShot sh : mNum.getShots() ) {
          NumStation st1 = sh.from;
          NumStation st2 = sh.to;
          if ( st1.unbarriered() && st1.unbarriered() ) {
            flipBlock( sh.getFirstBlock() );
          }
        }
        for ( NumSplay sp : mNum.getSplays() ) {
          NumStation st = sp.from;
          if ( st.unbarriered() ) {
            flipBlock( sp.getBlock() );
          }
        }
      }
      recomputeProfileReference();
    }

  void doBluetooth( Button b )
  {
    // TDLog.Log( TDLog.LOG_INPUT, "Reset button, mode " + TDSetting.mConnectionMode );
    mDataDownloader.setDownload( false );
    mDataDownloader.stopDownloadData();
    setConnectionStatus( mDataDownloader.getStatus() );
    if ( TDSetting.mConnectionMode == TDSetting.CONN_MODE_BATCH ) {
      switch ( mApp.distoType() ) {
        case Device.DISTO_A3:
          mApp.resetComm();
          Toast.makeText(this, R.string.bt_reset, Toast.LENGTH_SHORT).show();
          break;
        case Device.DISTO_X310:
          CutNPaste.showPopupBT( this, this, mApp, b );
          // (new DeviceRemoteDialog( this, this, mApp )).show();
          break;
      }
    } else {
      mApp.resetComm();
    }
  }

    public boolean onLongClick( View view ) 
    {
      Button b = (Button)view;
      if ( b == mButton1[ BTN_PLOT ] ) {
        if ( mType == PlotInfo.PLOT_EXTENDED ) {
          new DrawingProfileFlipDialog( this, this ).show();
        } else {
          return false; // not consumed
        }
      } else if ( b == mButton3[ BTN_REMOVE ] ) {
        SelectionPoint sp = mDrawingSurface.hotItem();
        if ( sp != null ) {
          int t = sp.type();
          String name = null;
          if ( t == DrawingPath.DRAWING_PATH_POINT ) {
            DrawingPointPath pp = (DrawingPointPath)sp.mItem;
            askDeleteItem( pp, t, DrawingBrushPaths.getPointName( pp.mPointType ) );
          } else if ( t == DrawingPath.DRAWING_PATH_LINE ) {
            DrawingLinePath lp = (DrawingLinePath)sp.mItem;
            if ( lp.size() <= 2 ) {
              askDeleteItem( lp, t, DrawingBrushPaths.mLineLib.getSymbolName( lp.mLineType ) );
            } else {
              removeLinePoint( lp, sp.mPoint, sp );
              lp.retracePath();
              modified();
            }
          } else if ( t == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath ap = (DrawingAreaPath)sp.mItem;
            if ( ap.size() <= 3 ) {
              askDeleteItem( ap, t, DrawingBrushPaths.mAreaLib.getSymbolName( ap.mAreaType ) );
            } else {
              removeLinePoint( ap, sp.mPoint, sp );
              ap.retracePath();
              modified();
            }
          }
        }
      }
      return true;
    }

    public void onClick(View view)
    {
      if ( onMenu ) {
        closeMenu();
        return;
      }
      // TDLog.Log( TDLog.LOG_INPUT, "DrawingActivity onClick() " + view.toString() );
      // TDLog.Log( TDLog.LOG_PLOT, "DrawingActivity onClick() point " + mCurrentPoint + " symbol " + mSymbol );
      dismissPopups();

      Button b = (Button)view;
      if ( b == mImage ) {
        if ( mMenu.getVisibility() == View.VISIBLE ) {
          mMenu.setVisibility( View.GONE );
          onMenu = false;
        } else {
          mMenu.setVisibility( View.VISIBLE );
          onMenu = true;
        }
        return;
      }
      int k1 = 3;
      int k2 = 3;
      int k3 = 3;
      int k5 = 3;
      if ( ( b == mButton2[0] && mMode == MODE_DRAW ) || 
           ( b == mButton5[1] && mMode == MODE_ERASE ) || 
           ( b == mButton3[2] && ( mMode == MODE_EDIT || mMode == MODE_SHIFT ) ) ) { 
        setMode( MODE_MOVE );
      } else if ( b == mButton1[0] || b == mButton3[0] || b == mButton5[0] ) { // 0 --> DRAW
        setMode( MODE_DRAW );
      } else if ( b == mButton1[1] || b == mButton2[1] || b == mButton3[1] ) { // 1--> ERASE
        setMode( MODE_ERASE );
        mListView.invalidate();
      } else if ( b == mButton1[2] || b == mButton2[2] || b == mButton5[2] ) { // 2 --> EDIT
        if ( TDSetting.mLevelOverBasic ) {
          setMode( MODE_EDIT );
        }
      
      // if ( b == mButton1[0] || b == mButton2[0] || b == mButton3[0] || b == mButton5[0] ) {
      //   makeModePopup( b );

      } else if ( b == mButton1[k1++] ) { // DOWNLOAD
        setConnectionStatus( 2 );
        resetFixedPaint();
        updateReference();
        if ( mApp.mDevice == null ) {
          // DistoXDBlock last_blk = null; // mApp.mData.selectLastLegShot( mApp.mSID );
          (new ShotNewDialog( this, mApp, this, null, -1L )).show();
        } else {
          mDataDownloader.toggleDownload();
          setConnectionStatus( mDataDownloader.getStatus() );
          mDataDownloader.doDataDownload( );
        }
      } else if ( b == mButton1[k1++] ) { // BLUETOOTH
        doBluetooth( b );
      } else if ( b == mButton1[k1++] ) { // DISPLAY MODE 
        new DrawingModeDialog( this, this, mDrawingSurface ).show();

      } else if ( b == mButton1[k1++] ) { //  NOTE
        (new DistoXAnnotations( this, mData.getSurveyFromId(mSid) )).show();

      } else if ( b == mButton1[k1++] ) { // TOGGLE PLAN/EXTENDED
        if ( PlotInfo.isSketch2D( mType ) ) { 
          startSaveTdrTask( mType, PlotSave.TOGGLE, MAX_TASK_FINAL, TDPath.NR_BACKUP ); 
          // mDrawingSurface.clearDrawing();
          switchPlotType();
        }
      } else if ( b == mButton1[k1++] ) { //  AZIMUTH
        if ( PlotInfo.isSketch2D( mType ) ) { 
          if ( TDSetting.mAzimuthManual ) {
            setRefAzimuth( 0, - TDAzimuth.mFixedExtend );
          } else {
            (new AzimuthDialDialog( this, this, TDAzimuth.mRefAzimuth, mBMdial )).show();
          }
        }

      } else if ( b == mButton2[k2++] || b == mButton5[k5++] ) { // UNDO
        mDrawingSurface.undo();
        if ( mDrawingSurface.hasMoreUndo() == false ) {
          // undoBtn.setEnabled( false );
        }
        // redoBtn.setEnabled( true );
        // canRedo = true;/
        modified();
      } else if ( b == mButton2[k2++] || b == mButton5[k5++] ) { // REDO
        if ( mDrawingSurface.hasMoreRedo() ) {
          mDrawingSurface.redo();
        }
      } else if ( b == mButton2[k2++] ) { // pointBtn
        if ( TDSetting.mPickerType == TDSetting.PICKER_RECENT ) { 
          new ItemRecentDialog(this, this, mType ).show();
        } else {
          new ItemPickerDialog(this, this, mType, mSymbol ).show();
        }
      } else if ( b == mButton2[k2++] ) { //  continueBtn
        if ( mSymbol == SYMBOL_LINE && mCurrentLine != DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
          setButtonContinue( (mContinueLine+1) % CONT_MAX );
        }

      } else if ( b == mButton3[k3++] ) { // prev
        mMode = MODE_SHIFT;
        SelectionPoint pt = mDrawingSurface.prevHotItem();
        if ( pt != null ) setButton3( pt.type() );
      } else if ( b == mButton3[k3++] ) { // next
        mMode = MODE_SHIFT;
        SelectionPoint pt = mDrawingSurface.nextHotItem();
        if ( pt != null ) setButton3( pt.type() );
      } else if ( b == mButton3[k3++] ) { // item/point editing: move, split, remove, etc.
        // Log.v( TopoDroidApp.TAG, "Button3[5] inLinePoint " + inLinePoint );
        if ( inLinePoint ) {
          makePopupEdit( b );
        } else {
          // SelectionPoint sp = mDrawingSurface.hotItem();
          // if ( sp != null && sp.mItem.mType == DrawingPath.DRAWING_PATH_NAME ) {
          //   DrawingStationName sn = (DrawingStationName)(sp.mItem);
          //   new DrawingBarrierDialog( this, this, sn.mName, mNum.isBarrier( sn.mName ) ).show();
          // }
        }
      } else if ( b == mButton3[k3++] ) { // EDIT ITEM PROPERTIES
        SelectionPoint sp = mDrawingSurface.hotItem();
        if ( sp != null ) {
          switch ( sp.type() ) {
            case DrawingPath.DRAWING_PATH_NAME:
              DrawingStationName sn = (DrawingStationName)(sp.mItem);
              DrawingStationPath path = mDrawingSurface.getStationPath( sn.mName );
              boolean barrier = mNum.isBarrier( sn.mName );
              boolean hidden  = mNum.isHidden( sn.mName );
              new DrawingStationDialog( this, this, sn, path, barrier, hidden ).show();
              break;
            case DrawingPath.DRAWING_PATH_POINT:
              new DrawingPointDialog( this, (DrawingPointPath)(sp.mItem) ).show();
              mModified = true;
              break;
            case DrawingPath.DRAWING_PATH_LINE:
              DrawingLinePath line = (DrawingLinePath)(sp.mItem);
              if ( line.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
                // Log.v("DistoX", "edit section line " ); // default azimuth = 0 clino = 0
                // cross-section exists already
                boolean h_section = PlotInfo.isProfile( mType ); // not really necessary
                String id = line.getOption( "-id" );
                if ( id != null ) {
                  new DrawingLineSectionDialog( this, mApp, h_section, true, id, line, null, null, 0, 0 ).show();
                } else {
                  TDLog.Error("edit section line with null id" );
                }
              } else {
                new DrawingLineDialog( this, line, sp.mPoint ).show();
              }
              mModified = true;
              break;
            case DrawingPath.DRAWING_PATH_AREA:
              new DrawingAreaDialog( this, (DrawingAreaPath)(sp.mItem) ).show();
              mModified = true;
              break;
            case DrawingPath.DRAWING_PATH_FIXED:
            case DrawingPath.DRAWING_PATH_SPLAY:
              new DrawingShotDialog( this, this, (DrawingPath)(sp.mItem) ).show();
              break;
          }
        }
        mDrawingSurface.clearSelected();
        mMode = MODE_EDIT;
      } else if ( b == mButton3[k3++] ) { // edit item delete
        SelectionPoint sp = mDrawingSurface.hotItem();
        if ( sp != null ) {
          int t = sp.type();
          if ( t == DrawingPath.DRAWING_PATH_POINT ||
               t == DrawingPath.DRAWING_PATH_LINE  ||
               t == DrawingPath.DRAWING_PATH_AREA ) {
            String name = "";
            DrawingPath p = sp.mItem;
            switch ( t ) {
              case DrawingPath.DRAWING_PATH_POINT:
                name = DrawingBrushPaths.getPointName( ((DrawingPointPath)p).mPointType );
                break;
              case DrawingPath.DRAWING_PATH_LINE:
                name = DrawingBrushPaths.mLineLib.getSymbolName( ((DrawingLinePath)p).mLineType );
                break;
              case DrawingPath.DRAWING_PATH_AREA:
                name = DrawingBrushPaths.mAreaLib.getSymbolName( ((DrawingAreaPath)p).mAreaType );
                break;
            }
            askDeleteItem( p, t, name );
          }
        }
      }
    }

    private void askDeleteItem( final DrawingPath p, final int t, final String name )
    {
      TopoDroidAlertDialog.makeAlert( this, getResources(), 
                                String.format( getResources().getString( R.string.item_delete ), name ), 
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            switch( t ) {
              case DrawingPath.DRAWING_PATH_POINT:
                deletePoint( (DrawingPointPath)p );
                break;
              case DrawingPath.DRAWING_PATH_LINE:
                deleteLine( (DrawingLinePath)p );
                break;
              case DrawingPath.DRAWING_PATH_AREA:
                deleteArea( (DrawingAreaPath)p );
                break;
              default:
                break;
            }
          }
        }
      );
    }



    void makeSectionPhoto( DrawingLinePath line, String id, long type,
                           String from, String to, float azimuth, float clino )
    {
      mCurrentLine = DrawingBrushPaths.mLineLib.mLineWallIndex;
      if ( ! DrawingBrushPaths.mLineLib.isSymbolEnabled( "wall" ) ) mCurrentLine = 0;
      setTheTitle();

      if ( id == null || id.length() == 0 ) return;
      mSectionName = id;
      long pid = mApp.mData.getPlotId( mApp.mSID, mSectionName );

      if ( pid < 0 ) { 
        pid = mApp.insert2dSection( mApp.mSID, mSectionName, type, from, to, azimuth, clino );
      }
      if ( pid >= 0 ) {
        // imageFile := PHOTO_DIR / surveyId / photoId .jpg
        File imagefile = new File( TDPath.getSurveyJpgFile( mApp.mySurvey, id ) );
        try {
          Uri outfileuri = Uri.fromFile( imagefile );
          Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
          intent.putExtra( MediaStore.EXTRA_OUTPUT, outfileuri );
          intent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );
          // startActivityForResult( intent, ShotActivity.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE );
          startActivity( intent );
        } catch ( ActivityNotFoundException e ) {
          Toast.makeText( this, R.string.no_capture_app, Toast.LENGTH_SHORT ).show();
        }
      }
    }

    // @param line   "section" line
    // @param id     section ID
    // @param type   either PLOT_SECTION or PLOT_H_SECTION
    // @param from   from station
    // @param to     to station
    // @param azimuth
    // @param clino
    void makeSectionDraw( DrawingLinePath line, String id, long type, String from, String to, float azimuth, float clino )
    {
      // Log.v("DistoX", "make section: " + id + " <" + from + "-" + to + "> azimuth " + azimuth + " clino " + clino );

      mCurrentLine = DrawingBrushPaths.mLineLib.mLineWallIndex;
      if ( ! DrawingBrushPaths.mLineLib.isSymbolEnabled( "wall" ) ) mCurrentLine = 0;
      setTheTitle();

      if ( id == null || id.length() == 0 ) return;
      mSectionName = id;
      long pid = mApp.mData.getPlotId( mApp.mSID, mSectionName );

      if ( pid < 0 ) { 
        // pid = mApp.mData.insertPlot( mApp.mSID, -1L, mSectionName, type, 0L, from, to, 
        //                              0, 0, TopoDroidApp.mScaleFactor, azimuth, clino, false ); // forward or not ?
        pid = mApp.insert2dSection( mApp.mSID, mSectionName, type, from, to, azimuth, clino );
      }
      if ( pid >= 0 ) {
        // Intent drawIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DrawingActivity.class );
        // drawIntent.putExtra( TopoDroidTag.TOPODROID_SURVEY_ID, mApp.mSID );
        // drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_NAME, mSectionName );
        // drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_TYPE, type );
        // drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_FROM, from );
        // drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_TO,   to );
        // drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_AZIMUTH, azimuth );
        // drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_CLINO, clino );
        // startActivity( drawIntent );
        pushInfo( type, mSectionName, from, to, azimuth, clino );
      }
    }

    // --------------------------------------------------------------------------

    private void savePng( boolean toast )
    {
      if ( PlotInfo.isAnySection( mType ) ) { 
        doSavePng( mType, mFullName3, toast );
      } else {
        doSavePng( (int)PlotInfo.PLOT_PLAN, mFullName1, toast );
        // FIXME OK PROFILE (to check)
        doSavePng( (int)PlotInfo.PLOT_EXTENDED, mFullName2, toast );
      }
    }

    private void doSavePng( long type, final String filename, boolean toast )
    {
      Bitmap bitmap = mDrawingSurface.getBitmap( type );
      if ( bitmap != null ) {
        float scale = mDrawingSurface.getBitmapScale();
        new ExportBitmapToFile(this, bitmap, scale, filename, toast ).execute();
      } else if ( toast ) {
        Toast.makeText( this, R.string.null_bitmap, Toast.LENGTH_SHORT ).show();
      }
    }

    // used by SavePlotFileTask
    void saveCsx( boolean toast )
    {
      String filename = mApp.exportSurveyAsCsx( this, mPlot1.start );
      if ( toast )
        Toast.makeText( mApp, getString(R.string.saved_file_1) + " " + filename, Toast.LENGTH_SHORT ).show();
    }

    // used to save "dxf", "svg"
    private void saveWithExt( String ext, boolean toast )
    {
      if ( PlotInfo.isAnySection( mType ) ) { 
        if ( "csx".equals( ext ) ) {
          doSavePng( mType, mFullName3, toast );
        } else {
          doSaveWithExt( mType, mFullName3, ext, toast );
        }
      } else {
        doSaveWithExt( mPlot1.type, mFullName1, ext, toast );
        doSaveWithExt( mPlot2.type, mFullName2, ext, toast );
      }
    }

    // ext file extension (--> saving class)
    // ext can be dxf, svg
    // FIXME OK PROFILE
    // used by SavePlotFileTask
    void doSaveWithExt( long type, final String filename, final String ext, boolean toast )
    {
      // Log.v("DistoX", "save with ext: " + filename + " ext " + ext );
      if ( PlotInfo.isProfile( type ) ) {
        new ExportPlotToFile(this, mDrawingSurface.mCommandManager2, mNum, type, filename, ext, toast ).execute();
      } else if ( type == PlotInfo.PLOT_PLAN ) {
        new ExportPlotToFile(this, mDrawingSurface.mCommandManager1, mNum, type, filename, ext, toast ).execute();
      } else {
        new ExportPlotToFile(this, mDrawingSurface.mCommandManager3, mNum, type, filename, ext, toast ).execute();
      }
    }

    // private rotateBackups( String filename )
    // {
    //   String filename2 = filename + "4"; // last backup
    //   File file2 = new File( filename2 );
    //   for ( int i=3; i>=0; --i ) { 
    //     File file1 = new File( filename + Integer.toString(i) );
    //     if ( file1.exists() ) file1.renameTo( file2 );
    //     file2 = file1;
    //   }
    //   File file = new File( filename );
    //   if ( file.exists() ) file.renameTo( file2 );
    // }


  static Handler th2Handler = null;

  // called (indirectly) only by ExportDialog: save as th2 even if there are missing symbols
  // no backup_rotate (rotate = 0)
  private void saveTh2()
  {
    int suffix = PlotSave.EXPORT;
    int azimuth = 0;
    String name = null;

    if ( mType == PlotInfo.PLOT_PLAN ) {
      name = mFullName1;
    } else if ( PlotInfo.isProfile( mType ) ) {
      azimuth = (int)mPlot2.azimuth;
      name = mFullName2;
    } else {
      name = mFullName3;
    }
    final String filename = name;
    th2Handler = new Handler(){
      @Override public void handleMessage(Message msg) {
        if (msg.what == 661 ) {
          Toast.makeText( mApp, getString(R.string.saved_file_1) + " " + filename + ".th2", Toast.LENGTH_SHORT ).show();
        } else {
          Toast.makeText( mApp, R.string.saving_file_failed, Toast.LENGTH_SHORT ).show();
        }
      }
    };
    try { 
      (new SavePlotFileTask( this, this, th2Handler, mApp, mDrawingSurface, name, mType, azimuth, suffix, 0 )).execute();
    } catch ( RejectedExecutionException e ) { }
  }

  
  // @Override
  // public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo info )
  // {
  //   super.onCreateContextMenu( menu, v, info );
  //   getMenuInflater().inflate( R.menu.popup, menu );
  //   menu.setHeaderTitle( "Context Menu" );
  //   Log.v( TopoDroidApp.TAG, "on Create Context Menu view " + v.toString()  );
  // }

  // @Override
  // public boolean onContextItemSelected( MenuItem item )
  // {
  //   switch ( item.getItemId() ) {
  //     // case ...:
  //     //   break;
  //     default:
  //       break;
  //   }
  //   return super.onOptionsItemSelected( item );
  // }

  private void doMoveTo()
  {
    if ( mMoveTo != null ) {
      // Log.v("DistoX", "do move to" );
      moveTo( mPlot1.type, mMoveTo );
      moveTo( mPlot2.type, mMoveTo );
      mMoveTo = null;
    }
  }

  private void doComputeReferences()
  {
    // Log.v("DistoX", "doComputeReferences()" );
    List<DistoXDBlock> list = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
    mNum = new DistoXNum( list, mPlot1.start, mPlot1.view, mPlot1.hide, mDecl );
    // doMoveTo();
    if ( mType == (int)PlotInfo.PLOT_PLAN ) {
      computeReferences( mPlot2.type, 0.0f, 0.0f, mApp.mScaleFactor, true );
      computeReferences( mPlot1.type, 0.0f, 0.0f, mApp.mScaleFactor, true );
      resetReference( mPlot1 );
    } else if ( PlotInfo.isProfile( mType ) ) {
      computeReferences( mPlot1.type, 0.0f, 0.0f, mApp.mScaleFactor, true );
      computeReferences( mPlot2.type, 0.0f, 0.0f, mApp.mScaleFactor, true );
      resetReference( mPlot2 );
    }
  }

  public void refreshDisplay( int nr, boolean toast )
  {
    // Log.v("DistoX", "refreshDisplay()" );
    setTitleColor( TDConst.COLOR_NORMAL );
    if ( nr >= 0 ) {
      if ( nr > 0 ) {
        doComputeReferences();
      }
      if ( toast ) {
        Toast.makeText( this, getResources().getQuantityString(R.plurals.read_data, nr, nr ), Toast.LENGTH_SHORT ).show();
      }
    } else if ( nr < 0 ) {
      if ( toast ) {
        // Toast.makeText( this, getString(R.string.read_fail_with_code) + nr, Toast.LENGTH_SHORT ).show();
        Toast.makeText( this, mApp.DistoXConnectionError[ -nr ], Toast.LENGTH_SHORT ).show();
      }
    }
  }

  private void updateDisplay( /* boolean compute, boolean reference */ ) // always called with true, false
  {
    // Log.v("DistoX", "updateDisplay()" );
    // if ( compute ) {
      List<DistoXDBlock> list = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
      mNum = new DistoXNum( list, mPlot1.start, mPlot1.view, mPlot1.hide, mDecl );
      // doMoveTo();
      // computeReferences( (int)mType, 0.0f, 0.0f, mApp.mScaleFactor, false );
      computeReferences( (int)mPlot1.type, 0.0f, 0.0f, mApp.mScaleFactor, false );
      if ( mPlot2 != null ) {
        computeReferences( (int)mPlot2.type, 0.0f, 0.0f, mApp.mScaleFactor, false );
      }
    // }
    // if ( reference ) {
    //   if ( mType == (int)PlotInfo.PLOT_PLAN ) {
    //     resetReference( mPlot1 );
    //   } else if ( PlotInfo.isProfile( mType ) ) {
    //     resetReference( mPlot2 );
    //   } else {
    //     resetReference( mPlot3 );
    //   }
    // }
  }

  // forward adding data to the ShotActivity
  @Override
  public void updateBlockList( DistoXDBlock blk )
  {
    updateDisplay( /* true, false */ );
  }

  @Override
  public void updateBlockList( long blk_id )
  {
    updateDisplay( /* true, false */ );
  }

  @Override
  public boolean onSearchRequested()
  {
    // TDLog.Error( "search requested" );
    Intent intent = new Intent( this, TopoDroidPreferences.class );
    intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_PLOT );
    startActivity( intent );
    return true;
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        onBackPressed();
        return true;
      case KeyEvent.KEYCODE_SEARCH:
        return onSearchRequested();
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        String help_page = getResources().getString( R.string.DrawingActivity );
        if ( help_page != null ) UserManualActivity.showHelpPage( this, help_page );
        return true;
      case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        TDLog.Error( "key down: code " + code );
    }
    return false;
  }

  // ---------------------------------------------------------
  // MENU

  private void setMenuAdapter( Resources res, long type )
  {
    mMenuAdapter = new ArrayAdapter<String>(this, R.layout.menu );
    // HOVER
    // mMenuAdapter = new MyMenuAdapter( this, this, mMenu, R.layout.menu, new ArrayList< MyMenuItem >() );

    mMenuAdapter.add( res.getString( menus[0] ) );  // EXPORT
    if ( PlotInfo.isAnySection( type ) ) {
      mMenuAdapter.add( res.getString( menus[8] ) );  // AREA
    } else {
      mMenuAdapter.add( res.getString( menus[1] ) );  // INFO
    }
    mMenuAdapter.add( res.getString( menus[2] ) );  // RELOAD
    mMenuAdapter.add( res.getString( menus[3] ) );  // ZOOM_FIT
    if ( TDSetting.mLevelOverBasic && PlotInfo.isSketch2D( type ) ) {
      mMenuAdapter.add( res.getString( menus[4] ) ); // DELETE
    }
    mMenuAdapter.add( res.getString( menus[5] ) ); // PALETTE
    if ( PlotInfo.isSketch2D( type ) ) {
      mMenuAdapter.add( res.getString( menus[6] ) ); // OVERVIEW
    }
    mMenuAdapter.add( res.getString( menus[7] ) ); // OPTIONS
    mMenuAdapter.add( res.getString( menus[8] ) ); // HELP
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    // HOVER
    // mMenuAdapter.resetBgColor();
    onMenu = false;
  }

  private void handleMenu( int pos )
  {
      closeMenu();
      int p = 0;
      if ( p++ == pos ) { // EXPORT
        new ExportDialog( this, this, TDConst.mPlotExportTypes, R.string.title_plot_save ).show();
      } else if ( p++ == pos ) { // INFO
        if ( mNum != null ) {
          new DistoXStatDialog( mDrawingSurface.getContext(), mNum, mPlot1.start, mData.getSurveyStat( mApp.mSID ) ).show();
        } else if ( PlotInfo.isAnySection( mType ) ) {
          float area = mDrawingSurface.computeSectionArea() / (DrawingUtil.SCALE_FIX * DrawingUtil.SCALE_FIX);
          // Log.v("DistoX", "Section area " + area );
          Resources res = getResources();
          String msg = String.format( res.getString( R.string.section_area ), area );
          TopoDroidAlertDialog.makeAlert( this, res, msg, R.string.button_ok, -1, null, null );
        }
      } else if ( p++ == pos ) { // RECOVER RELOAD
        if ( PlotInfo.isProfile( mType ) ) {
          ( new PlotRecoverDialog( this, this, mFullName2, mType ) ).show();
        } else if ( mType == PlotInfo.PLOT_PLAN ) {
          ( new PlotRecoverDialog( this, this, mFullName1, mType ) ).show();
        } else {
          ( new PlotRecoverDialog( this, this, mFullName3, mType ) ).show();
        }
      } else if ( p++ == pos ) { // ZOOM_FIT
        RectF b = mDrawingSurface.getBitmapBounds();
        float w = b.right - b.left;
        float h = b.bottom - b.top;
        mZoom = TopoDroidApp.mDisplayWidth / ( 1 + ((w>h)? w : h ) );
        mOffset.x = TopoDroidApp.mDisplayWidth  / (2*mZoom) - (b.left + b.right) / 2;
        mOffset.y = TopoDroidApp.mDisplayHeight / (2*mZoom) - (b.top + b.bottom) / 2;
        // Log.v("DistoX", "W " + w + " H " + h + " zoom " + mZoom + " X " + mOffset.x + " Y " + mOffset.y );
        mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
      } else if ( TDSetting.mLevelOverBasic && PlotInfo.isSketch2D( mType ) && p++ == pos ) { // DELETE
        askDelete();
      } else if ( p++ == pos ) { // PALETTE
        DrawingBrushPaths.makePaths( getResources() );
        (new SymbolEnableDialog( this, this, mApp )).show();
      } else if ( PlotInfo.isSketch2D( mType ) && p++ == pos ) { // OVERVIEW
        if ( mType == PlotInfo.PLOT_PROFILE ) {
          Toast.makeText( this, R.string.no_profile_overview, Toast.LENGTH_SHORT ).show();
        } else {
          updateReference();
          Intent intent = new Intent( this, OverviewActivity.class );
          intent.putExtra( TopoDroidTag.TOPODROID_SURVEY_ID, mSid );
          intent.putExtra( TopoDroidTag.TOPODROID_PLOT_FROM, mFrom );
          intent.putExtra( TopoDroidTag.TOPODROID_PLOT_ZOOM, mZoom );
          intent.putExtra( TopoDroidTag.TOPODROID_PLOT_TYPE, mType );
          intent.putExtra( TopoDroidTag.TOPODROID_PLOT_XOFF, mOffset.x );
          intent.putExtra( TopoDroidTag.TOPODROID_PLOT_YOFF, mOffset.y );
          startActivity( intent );
        }
      } else if ( p++ == pos ) { // OPTIONS
        updateReference();
        Intent intent = new Intent( this, TopoDroidPreferences.class );
        intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_PLOT );
        startActivity( intent );
      } else if ( p++ == pos ) { // HELP
        int nn = mNrButton1 + mNrButton2 - 3 + mNrButton5 - 5 + ( TDSetting.mLevelOverBasic? mNrButton3 - 3: 0 );
        (new HelpDialog(this, izons, menus, help_icons, help_menus, nn, help_menus.length ) ).show();
      }
  }

  public void doExport( String type )
  {
    int index = TDConst.plotExportIndex( type );
    switch ( index ) {
      case TDConst.DISTOX_EXPORT_TH2: saveTh2(); break;
      case TDConst.DISTOX_EXPORT_CSX: 
        if ( ! PlotInfo.isAnySection( mType ) ) { // FIXME x-sections are saved PNG for CSX
          saveCsx( true );
          break;
        } // else fall-through and savePng
      case TDConst.DISTOX_EXPORT_PNG: savePng( true ); break;
      case TDConst.DISTOX_EXPORT_DXF: saveWithExt( "dxf", true ); break;
      case TDConst.DISTOX_EXPORT_SVG: saveWithExt( "svg", true ); break;
    }
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) { // MENU
      handleMenu( pos );
    }
  }

  void doRecover( String filename, long type )
  {
    float x = mOffset.x;
    float y = mOffset.y;
    float z = mZoom;
    String tdr  = TDPath.getTdrFile( filename );
    String th2  = TDPath.getTh2File( filename );
    // Log.v("DistoX", "recover " + type + " <" + filename + "> TRD " + tdr + " TH2 " + th2 );
    if ( type == PlotInfo.PLOT_PLAN ) {
      mDrawingSurface.resetManager( DrawingSurface.DRAWING_PLAN, null );
      mDrawingSurface.modeloadDataStream( tdr, th2, null ); // no missing symbols
      mDrawingSurface.addManagerToCache( mFullName1 );
      setPlotType1( true );
    } else if ( PlotInfo.isProfile( type ) ) {
      mDrawingSurface.resetManager( DrawingSurface.DRAWING_PROFILE, null );
      mDrawingSurface.modeloadDataStream( tdr, th2, null );
      mDrawingSurface.addManagerToCache( mFullName2 );
      // TODO now switch to extended view FIXME-VIEW
      setPlotType2( true );
    } else {
      mDrawingSurface.resetManager( DrawingSurface.DRAWING_SECTION, null );
      mDrawingSurface.modeloadDataStream( tdr, th2, null );
      mDrawingSurface.addManagerToCache( mFullName2 );
      setPlotType3( );
      makeSectionReferences( mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL ) );
    }
    mOffset.x = x;
    mOffset.y = y;
    mZoom     = z;
    mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
  }

  void exportAsCsx( PrintWriter pw, String cave, String branch )
  {
    // Log.v("DistoX", "export as CSX <<" + cave + ">>" );
    pw.format("  <plan>\n");
    mDrawingSurface.exportAsCsx( pw, PlotInfo.PLOT_PLAN, cave, branch );
    pw.format("    <plot />\n");
    pw.format("  </plan>\n");
    pw.format("  <profile>\n");
    mDrawingSurface.exportAsCsx( pw, PlotInfo.PLOT_EXTENDED, cave, branch ); 
    pw.format("    <plot />\n");
    pw.format("  </profile>\n");
  }

  public void setConnectionStatus( int status )
  { 
    if ( mApp.mDevice == null ) {
      mButton1[ BTN_DOWNLOAD ].setBackgroundDrawable( mBMadd );
    } else {
      switch ( status ) {
        case 1:
          mButton1[ BTN_DOWNLOAD ].setBackgroundDrawable( mBMdownload_on );
          break;
        case 2:
          mButton1[ BTN_DOWNLOAD ].setBackgroundDrawable( mBMdownload_wait );
          break;
        default:
          mButton1[ BTN_DOWNLOAD ].setBackgroundDrawable( mBMdownload );
      }
    }
  }

// -------------------------------------------------------------
// AUTO WALLS

  void drawWallsAt( DistoXDBlock blk )
  {
    if ( TDSetting.mWallsType == TDSetting.WALLS_NONE ) return;

    String station1 = blk.mFrom;
    String station2 = blk.mTo;
    NumStation st1 = mNum.getStation( station1 );
    NumStation st2 = mNum.getStation( station2 );
    float x0, y0, x1, y1;
    if ( mType == PlotInfo.PLOT_PLAN ) {
      x0 = (float)(st1.e);
      y0 = (float)(st1.s);
      x1 = (float)(st2.e);
      y1 = (float)(st2.s);
    } else {
      x0 = (float)(st1.h);
      y0 = (float)(st1.v);
      x1 = (float)(st2.h);
      y1 = (float)(st2.v);
    }
    float x2 = x1 - x0;
    float y2 = y1 - y0;
    float len = (float)Math.sqrt( x2 * x2 + y2 * y2 );
    PointF uu = new PointF( x2 / len, y2 / len );
    PointF vv = new PointF( -uu.y, uu.x );

    // Log.v("DistoX", "X0 " + x0 + " " + y0 + " X1 " + x1 + " " + y1 );
    // Log.v("DistoX", "U " + uu.x + " " + uu.y + " V " + vv.x + " " + vv.y );

    ArrayList< PointF > pos = new ArrayList< PointF >(); // positive v
    ArrayList< PointF > neg = new ArrayList< PointF >(); // negative v
    List< NumSplay > splays = mNum.getSplays();
    if ( mType == PlotInfo.PLOT_PLAN ) {
      for ( NumSplay sp : splays ) {
        if ( Math.abs( sp.getBlock().mClino ) < TDSetting.mWallsPlanThr ) {
          NumStation st = sp.from;
          if ( st == st1 || st == st2 ) {
            x2 = (float)(sp.e) - x0;
            y2 = (float)(sp.s) - y0;
            float u = x2 * uu.x + y2 * uu.y;
            float v = x2 * vv.x + y2 * vv.y;
            if ( v > 0 ) {
              pos.add( new PointF(u,v) );
            } else {
              neg.add( new PointF(u,v) );
            }
          }
        }
      }
    } else {
      for ( NumSplay sp : splays ) {
        if ( Math.abs( sp.getBlock().mClino ) > TDSetting.mWallsExtendedThr ) { // FIXME
          NumStation st = sp.from;
          if ( st == st1 || st == st2 ) {
            x2 = (float)(sp.h) - x0;
            y2 = (float)(sp.v) - y0;
            float u = x2 * uu.x + y2 * uu.y;
            float v = x2 * vv.x + y2 * vv.y;
            // Log.v("WALL", "Splay " + x2 + " " + y2 + " --> " + u + " " + v);
            if ( v > 0 ) {
              pos.add( new PointF(u,v) );
            } else {
              neg.add( new PointF(u,v) );
            }
          }
        }
      }
    }
    makeWall( pos, x0, y0, x1, y1, len, uu, vv );
    makeWall( neg, x0, y0, x1, y1, len, uu, vv );
    mModified = true;
  }

  void addPointsToLine( DrawingLinePath line, float x0, float y0, float xx, float yy )
  {
    float ll = (float)Math.sqrt( (xx-x0)*(xx-x0) + (yy-y0)*(yy-y0) ) / 20;
    if ( ll > TDSetting.mWallsXStep ) {
      int n = 1 + (int)ll;
      float dx = (xx-x0) / n;
      float dy = (yy-y0) / n;
      for ( int k=1; k<n; ++k ) {
        line.addPoint( x0+k*dx, y0+k*dy );
      }
    }
    line.addPoint( xx, yy );
  }


  void makeWall( ArrayList<PointF> pts, float x0, float y0, float x1, float y1, float len, PointF uu, PointF vv )
  {
    int size = pts.size();
    float xx, yy;
    if ( size == 0 ) { // no wall
      return;
    } else if ( size == 1 ) {
      PointF p = pts.get(0);
      if ( p.x > 0 && p.x < len ) { // wall from--p--to
        xx = DrawingUtil.toSceneX( x0 + uu.x * p.x + vv.x * p.y );
        yy = DrawingUtil.toSceneY( y0 + uu.y * p.x + vv.y * p.y );
        x0 = DrawingUtil.toSceneX( x0 );
        y0 = DrawingUtil.toSceneY( y0 );
        x1 = DrawingUtil.toSceneX( x1 );
        y1 = DrawingUtil.toSceneY( y1 );
        mCurrentLinePath = new DrawingLinePath( DrawingBrushPaths.mLineLib.mLineWallIndex );
        mCurrentLinePath.addStartPoint( x0, y0 );
        addPointsToLine( mCurrentLinePath, x0, y0, xx, yy );
        addPointsToLine( mCurrentLinePath, xx, yy, x1, y1 );
        mCurrentLinePath.computeUnitNormal();
        mDrawingSurface.addDrawingPath( mCurrentLinePath );
      }
    } else {
      sortPointsOnX( pts );
      mCurrentLinePath = new DrawingLinePath( DrawingBrushPaths.mLineLib.mLineWallIndex );
      PointF p1 = pts.get(0);
      xx = DrawingUtil.toSceneX( x0 + uu.x * p1.x + vv.x * p1.y );
      yy = DrawingUtil.toSceneY( y0 + uu.y * p1.x + vv.y * p1.y );
      mCurrentLinePath.addStartPoint( xx, yy );
      for ( int k=1; k<pts.size(); ++k ) {
        p1 = pts.get(k);
        float xx2 = DrawingUtil.toSceneX( x0 + uu.x * p1.x + vv.x * p1.y );
        float yy2 = DrawingUtil.toSceneY( y0 + uu.y * p1.x + vv.y * p1.y );
        addPointsToLine( mCurrentLinePath, xx, yy, xx2, yy2 );
        xx = xx2;
        yy = yy2;
      }
      mCurrentLinePath.computeUnitNormal();
      mDrawingSurface.addDrawingPath( mCurrentLinePath );
    }
  }

  // sort the points on the list by increasing X
  // @param pts list of points
  private void sortPointsOnX( ArrayList<PointF> pts ) 
  {
    int size = pts.size();
    if ( size < 2 ) return;
    boolean repeat = true;
    PointF p1, p2;
    while ( repeat ) {
      repeat = false;
      for ( int k = 1; k < size; ++ k ) {
        p1 = pts.get(k-1);
        p2 = pts.get(k);
        if ( p2.x < p1.x ) {
          float x = p1.x; p1.x = p2.x; p2.x = x;
          float y = p1.y; p1.y = p2.y; p2.y = y;
          repeat = true;
        }
      }
    }

    // remove points with X close to a nearby and smaller Y
    for ( int k = 1; k < pts.size(); ++ k ) {
      p1 = pts.get(k-1);
      p2 = pts.get(k);
      if ( (p2.x - p1.x) < TDSetting.mWallsXClose ) { 
        if ( Math.abs(p2.y) < Math.abs(p1.y) ) { // remove p2
          pts.remove( k );
        } else {
          pts.remove( k-1 ); // no need to move k backward
        }
      } else {
        ++k;
      }
    }
    
    // convex-hull: remove points "inside" (with smaller |Y| )
    if ( size > 2 ) {
      float x0 = pts.get(0).x;
      float y0 = Math.abs( pts.get(0).y );
      for ( int k = 0; k < pts.size()-1; ++k ) {
        int hh = k+1;
        float x1 = pts.get(hh).x;
        float y1 = Math.abs( pts.get(hh).y );
        float s0 = (y1-y0)/(x1-x0); // N.B. x1 >= x0 + 0.1
          for ( int h=hh+1; h<pts.size(); ++h ) {
          x1 = pts.get(h).x;
          y1 = Math.abs( pts.get(h).y );
          float s1 = (y1-y0)/(x1-x0); 
          if ( s1 > s0 + TDSetting.mWallsConcave ) { // allow small concavities
            hh = h;
          }
        }
        for ( int h=hh-1; h>k; --h ) pts.remove(h);
      }
    }
  }

}
