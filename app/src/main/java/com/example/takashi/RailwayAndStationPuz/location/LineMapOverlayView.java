package com.example.takashi.RailwayAndStationPuz.location;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import com.example.takashi.RailwayAndStationPuz.database.Line;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by takashi on 2016/11/12.
 */

public class LineMapOverlayView extends ImageView {

    private static final boolean DEBUG = true;	// TODO for debugging
    private static String TAG = "RailwayLineImageView";
    private static final float EPS = 0.1f;
    private static final int MOVE_LIMIT = 300; //limit value to prevent the image disappearing from the view when moving
    private static final float DEFAULT_MAX_SCALE = 8.f;
    private static final float DEFAULT_MIN_SCALE = 0.1f;

    protected Matrix mImageMatrix = new Matrix();
    private final RectF mImageRect = new RectF();   // 路線図の矩形領域
    private final RectF mLimitRect = new RectF();
    private final LineSegment[] mLimitSegments = new LineSegment[4];

    private float mMaxScale = DEFAULT_MAX_SCALE;
    private float mMinScale = DEFAULT_MIN_SCALE;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private GestureDetector.OnDoubleTapListener mDoubleTapListener;

    private float mScale = 0.5f; // 描画する倍率
    private boolean mScalingMode = false;
    private boolean mScrolling = false;

    private OnLineScrollEndListener listener;

    public LineMapOverlayView(Context context) {
        super(context);
        createListener(context);
        if(DEBUG) Log.d(TAG,"constructor1");
    }

    public LineMapOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createListener(context);
        if(DEBUG) Log.d(TAG,"constructor2");
    }

    public LineMapOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        createListener(context);
        if(DEBUG) Log.d(TAG,"constructor3");
    }

    public LineMapOverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        createListener(context);
        if(DEBUG) Log.d(TAG,"constructor4");
    }

    public void setOnScrollEndListener(OnLineScrollEndListener listener){
        this.listener = listener;
    }

    public RectF getCurrentImageRect(){

        float values[] = new float[8];
        // calculate the corner coordinates of image applied matrix
        // [(left,top),(right,top),(right,bottom),(left.bottom)]
        values[0] = values[6] = mImageRect.left;
        values[1] = values[3] = mImageRect.top;
        values[5] = values[7] = mImageRect.bottom;
        values[2] = values[4] = mImageRect.right;
        mImageMatrix.mapPoints(values);

        float left = values[0];
        float top = values[1];
        float right = values[2];
        float bottom = values[5];

        return new RectF(left,top,right,bottom);
    }

    // pinch in/out操作のイベントハンドラ
    private class RailwayLineViewScaleGestureDetector
            implements ScaleGestureDetector.OnScaleGestureListener{
        public RailwayLineViewScaleGestureDetector(){

        }
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // ピンチイン・アウト中に継続して呼び出される
            // getScaleFactor()は
            // 『今回の2点タッチの距離/前回の2点タッチの距離』を返す
            if(DEBUG) Log.d(TAG, String.format("onScale factor:%f",detector.getScaleFactor()));
            // 表示倍率の計算
            if(mScalingMode){
                if(processZoom(detector)){
                    invalidate();
                }
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if(DEBUG) Log.d(TAG, "onScaleBegin");
            mScalingMode = true;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if(DEBUG) Log.d(TAG, "onScaleEnd");
            mScalingMode = false;
        }
    }

    // scroll1操作、single tap、double tap、long tap操作のイベントハンドラ
    private class RailwayLineViewGestureListener
            implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener{

        public RailwayLineViewGestureListener(){

        }
        @Override
        public boolean onDown(MotionEvent e) {
//            if(DEBUG) Log.d(TAG, "onDown");
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
//            if(DEBUG) Log.d(TAG, "onShowPress");
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if(DEBUG) Log.d(TAG, "onSingleTapUp");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            if(DEBUG) Log.d(TAG, "onScroll");
            if(!mScalingMode){
                mScrolling = true;
                if(processDrag(distanceX,distanceY)){
                    invalidate();
                }
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
//            if(DEBUG) Log.d(TAG, "onLongPress");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(DEBUG) Log.d(TAG,"onFling");
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            if(DEBUG) Log.d(TAG,"onDoubleTap");
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            if(DEBUG) Log.d(TAG,"onDoubleTapEvent");
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            if(DEBUG) Log.d(TAG,"onSingleTapConfirmed");
            return false;
        }
    }

    private void createListener(Context context){
        setLongClickable(true);
        mScaleDetector = new ScaleGestureDetector(context,new RailwayLineViewScaleGestureDetector());
        mGestureDetector = new GestureDetector(context,new RailwayLineViewGestureListener() );
    }

    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // if view size(width|height) is zero(the view size not decided yet)
        // or no image assigned, skip initialization
        if (getWidth() == 0 || getHeight() == 0 || getDrawable() == null) return;
        if(DEBUG) Log.d(TAG,String.format("onLayout : left %d, top=%d, right=%d, bottom=%d",left,top,right,bottom));
        init();
    }

    private void init(){

        super.setScaleType(ScaleType.CENTER_INSIDE);
        setFrame(getLeft(), getTop(), getRight(), getBottom());

        // 路線図の描画領域の境界を示す矩形データを取得する
        final Drawable dr = getDrawable();
        if (dr != null) {
            mImageRect.set(dr.getBounds());
        } else {
            mImageRect.setEmpty();
        }

        // スケーリングの最大、最小を求める。
        mMaxScale = Math.min(
                Math.abs((getRight()-getLeft()))/Math.abs((mImageRect.right-mImageRect.left)),
                Math.abs((getBottom()-getTop()))/Math.abs((mImageRect.bottom-mImageRect.top)));
        mMinScale = 1.0f;

        // set limit rectangle that the image can move
        final Rect tmp = new Rect();
        getDrawingRect(tmp);
        mLimitRect.set(tmp);
        mLimitRect.inset(MOVE_LIMIT, MOVE_LIMIT);
        mLimitSegments[0] = null;

        // set the scale type to ScaleType.MATRIX
        mImageMatrix = getImageMatrix();
        // set the scale type to ScaleType.MATRIX
        super.setScaleType(ScaleType.MATRIX);
        // apply matrix"
        super.setImageMatrix(mImageMatrix);

        // debug
        float[] values = new float[9];
        mImageMatrix.getValues(values);
        if(DEBUG) Log.v(TAG,String.format("mImageMatrix %f,%f,%f,%f,%f,%f,%f,%f,%f",
                values[0],values[1],values[2],
                values[3],values[4],values[5],
                values[6],values[7],values[8]));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        RectF imageRectF = getCurrentImageRect();

//        if(DEBUG) Log.d(TAG,"onTouchEvent : "
//                + " event.x =" + x + ", event.y =" + y
//                + ", left = " + imageRectF.left + ", top  = " + imageRectF.top + ", bottom = " + imageRectF.bottom  + ", right = " + imageRectF.right);

        if( imageRectF.left < x && x < imageRectF.right && imageRectF.top < y && y < imageRectF.bottom && getDrawable()!=null){
//            if(DEBUG) Log.d(TAG," in the Image");
            super.onTouchEvent(event);
            boolean a = mGestureDetector.onTouchEvent(event);
            boolean b = mScaleDetector.onTouchEvent(event);
//        if(DEBUG) Log.d(TAG,"getWith() = " + getWidth() + ", getHeight() = " + getHeight() + ", getX() = " + getX() + ", getY() = " + getY());
            if(event.getAction()==MotionEvent.ACTION_UP && mScrolling){
                Log.d(TAG,"ピース移動完了");
                if(this.listener!=null) this.listener.onScrollEnd();
                mScrolling = false;
            }
            return a|b;
        }
        else{
//            if(DEBUG) Log.d(TAG," out of the Image");
            mScrolling = false;
            return false;
        }
    }

    private final boolean processDrag(final float distanceX,final float distanceY) {

        float[] mTrans = new float[8];

        float dx = -1.0f*distanceX;
        float dy = -1.0f*distanceY;

        // calculate the corner coordinates of image applied matrix
        // [(left,top),(right,top),(right,bottom),(left.bottom)]
        mTrans[0] = mTrans[6] = mImageRect.left;
        mTrans[1] = mTrans[3] = mImageRect.top;
        mTrans[5] = mTrans[7] = mImageRect.bottom;
        mTrans[2] = mTrans[4] = mImageRect.right;
        mImageMatrix.mapPoints(mTrans);
        for (int i = 0; i < 8; i += 2) {
            mTrans[i] += dx;
            mTrans[i+1] += dy;
        }

        // check whether the image can move
        // if we can ignore rotating, the limit check is more easy...
        boolean canMove
                // check whether at lease one corner of image bounds is in the limitRect
                = mLimitRect.contains(mTrans[0], mTrans[1])
                || mLimitRect.contains(mTrans[2], mTrans[3])
                || mLimitRect.contains(mTrans[4], mTrans[5])
                || mLimitRect.contains(mTrans[6], mTrans[7])
                // check whether at least one corner of limitRect is in the image bounds
                || ptInPoly(mLimitRect.left, mLimitRect.top, mTrans)
                || ptInPoly(mLimitRect.right, mLimitRect.top, mTrans)
                || ptInPoly(mLimitRect.right, mLimitRect.bottom, mTrans)
                || ptInPoly(mLimitRect.left, mLimitRect.bottom, mTrans);
        if (!canMove) {
            // when no corner is in, we need additional check whether at least
            // one side of image bounds intersect with the limit rectangle
            if (mLimitSegments[0] == null) {
                mLimitSegments[0] = new LineSegment(mLimitRect.left, mLimitRect.top, mLimitRect.right, mLimitRect.top);
                mLimitSegments[1] = new LineSegment(mLimitRect.right, mLimitRect.top, mLimitRect.right, mLimitRect.bottom);
                mLimitSegments[2] = new LineSegment(mLimitRect.right, mLimitRect.bottom, mLimitRect.left, mLimitRect.bottom);
                mLimitSegments[3] = new LineSegment(mLimitRect.left, mLimitRect.bottom, mLimitRect.left, mLimitRect.top);
            }
            final LineSegment side = new LineSegment(mTrans[0], mTrans[1], mTrans[2], mTrans[3]);
            canMove = LineSegment.checkIntersect(side, mLimitSegments);
            if (!canMove) {
                side.set(mTrans[2], mTrans[3], mTrans[4], mTrans[5]);
                canMove = LineSegment.checkIntersect(side, mLimitSegments);
                if (!canMove) {
                    side.set(mTrans[4], mTrans[5], mTrans[6], mTrans[7]);
                    canMove = LineSegment.checkIntersect(side, mLimitSegments);
                    if (!canMove) {
                        side.set(mTrans[6], mTrans[7], mTrans[0], mTrans[1]);
                        canMove = LineSegment.checkIntersect(side, mLimitSegments);
                    }
                }
            }
        }
        if (canMove) {
            // TODO we need adjust dx/dy not to penetrate into the limit rectangle
            // otherwise the image can not move when one side is on the border of limit rectangle.
            // only calculate without rotation now because its calculation is to heavy when rotation applied.
//            if (!mIsRotating) {
                final float left = Math.min(Math.min(mTrans[0], mTrans[2]), Math.min(mTrans[4], mTrans[6]));
                final float right = Math.max(Math.max(mTrans[0], mTrans[2]), Math.max(mTrans[4], mTrans[6]));
                final float top = Math.min(Math.min(mTrans[1], mTrans[3]), Math.min(mTrans[5], mTrans[7]));
                final float bottom = Math.max(Math.max(mTrans[1], mTrans[3]), Math.max(mTrans[5], mTrans[7]));

                if (right < mLimitRect.left) {
                    dx = mLimitRect.left - right;
                } else if (left + EPS > mLimitRect.right) {
                    dx = mLimitRect.right - left - EPS;
                }
                if (bottom < mLimitRect.top) {
                    dy = mLimitRect.top - bottom;
                } else if (top + EPS > mLimitRect.bottom) {
                    dy = mLimitRect.bottom - top - EPS;
                }
//            }
            if ((dx != 0) || (dy != 0)) {
//                if(DEBUG) Log.v(TAG, String.format("Applied processDrag:dx=%f,dy=%f", dx, dy));
                // apply move
                if (mImageMatrix.postTranslate(dx, dy)) {
                    // when image is really moved?
//                    mImageMatrixChanged = true;
                    // apply to super class
                    super.setImageMatrix(mImageMatrix);
                }
            }
            else{
//                if(DEBUG) Log.v(TAG, String.format("Not Applied processDrag:dx=%f,dy=%f", dx, dy));
            }
        }
        return canMove;
    }

    private static final Vector sPtInPoly_v1 = new Vector();
    private static final Vector sPtInPoly_v2 = new Vector();
    /**
     * check whether the point is in the clockwise 2D polygon
     * @param x
     * @param y
     * @param poly: the array of polygon coordinates(x,y pairs)
     * @return
     */
    private static final boolean ptInPoly(final float x, final float y, final float[] poly) {

        final int n = poly.length & 0x7fffffff;
        // minimum 3 points(3 pair of x/y coordinates) need to calculate >> length >= 6
        if (n < 6) return false;
        boolean result = true;
        for (int i = 0; i < n; i += 2) {
            sPtInPoly_v1.set(x, y).dec(poly[i], poly[i + 1]);
            if (i + 2 < n) sPtInPoly_v2.set(poly[i + 2], poly[i + 3]);
            else sPtInPoly_v2.set(poly[0], poly[1]);
            sPtInPoly_v2.dec(poly[i], poly[i + 1]);
            if (Vector.crossProduct(sPtInPoly_v1, sPtInPoly_v2) > 0) {
                if(DEBUG) Log.v(TAG, "pt is outside of a polygon:");
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * zooming
     */
    private final boolean processZoom(final ScaleGestureDetector detector) {

        // スケーリングの中心座標を求める。
        // 最初に現在の表示領域の矩形頂点座標を求める
        float[] points = new float[9];
        // calculate the corner coordinates of image applied matrix
        // [(left,top),(right,top),(right,bottom),(left.bottom)]
        points[0] = points[6] = mImageRect.left;
        points[1] = points[3] = mImageRect.top;
        points[5] = points[7] = mImageRect.bottom;
        points[2] = points[4] = mImageRect.right;
        mImageMatrix.mapPoints(points);

        float left = points[0];
        float top = points[1];
        float right = points[2];
        float bottom = points[5];

        //次にX,Yの中心を求め、スケーリングの中心座標とする。
        float pivotX = (left+right)/2.0f;
        float pivotY = (top+bottom)/2.0f;

        // restore the Matrix
        float[] values = new float[9];
        mImageMatrix.getValues(values);
        // get current zooming scale
        final float currentScale = Math.min(values[Matrix.MSCALE_X],values[Matrix.MSCALE_Y]);
        // calculate the zooming scale from the distance between touched positions
        final float scale = detector.getScaleFactor();
        // calculate the applied zooming scale
        final float tmpScale = scale * currentScale*1.1f;
        if (tmpScale < mMinScale) {
            // skip if the applied scale is smaller than minimum scale
            return false;
        } else if (tmpScale > mMaxScale) {
            // skip if the applied scale is bigger than maximum scale
            return false;
        }

        // change scale with scale value and pivot point
        if (mImageMatrix.postScale(scale, scale, pivotX, pivotY)){
            // when Matrix is changed
//            mImageMatrixChanged = true;
            // apply to super class
            super.setImageMatrix(mImageMatrix);
        }
        return true;
    }

    // 正解座標近辺での色変更

    /**
     * ColorMatrix data for reversing image
     */
    private static final float[] REVERSE = {
            -1.0f,   0.0f,   0.0f,  0.0f,  255.0f,
            0.0f,  -1.0f,   0.0f,  0.0f,  255.0f,
            0.0f,   0.0f,  -1.0f,  0.0f,  255.0f,
            0.0f,   0.0f,   0.0f,  1.0f,    0.0f,
    };
    private static final float[] NORMAL = {
            1.0f,   0.0f,   0.0f,  0.0f,  0.0f,
            0.0f,   1.0f,   0.0f,  0.0f,  0.0f,
            0.0f,   0.0f,   1.0f,  0.0f,  0.0f,
            0.0f,   0.0f,   0.0f,  1.0f,  0.0f,
    };
    boolean colorSw = false;
    int colorCount = 0;
    Line line;
    GoogleMap map;
    LatLng point1,point2;
    Point screenPoint1,screenPoint2;

    public void setLine(Line line){
        this.line = line;
    }
    public void setMap(GoogleMap map){
        this.map = map;
    }
    private void computeLocationError(){
        RectF railwayImageRect = getCurrentImageRect();
        Log.d(TAG,String.format("RailwayLine Image : left=%f,top=%f,right=%f,bottom=%f",
                railwayImageRect.left,railwayImageRect.top,railwayImageRect.right,railwayImageRect.bottom));
        screenPoint1 = new Point((int)railwayImageRect.left,(int)railwayImageRect.top);
        screenPoint2 = new Point((int)railwayImageRect.right,(int)railwayImageRect.bottom);
        point1 = map.getProjection().fromScreenLocation(screenPoint1);
        point2 = map.getProjection().fromScreenLocation(screenPoint2);
        Log.d(TAG,String.format("answer = %f,%f, point1 = %f,%f",
                this.line.getCorrectTopLat(),this.line.getCorrectLeftLng(),point1.latitude,point1.longitude));
        Log.d(TAG,String.format("answer = %f,%f, point2 = %f,%f",
                this.line.getCorrectBottomLat(),this.line.getCorrectRightLng(),point2.latitude,point2.longitude));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        computeLocationError();
        if (point2.latitude < 35.0f || 37.0f < point2.latitude) {
            super.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(NORMAL)));
            colorCount++;
            if( colorCount == 100 ){
                colorSw = true;
                colorCount = 0;
            }
        }
        else{
            super.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(REVERSE)));
            colorCount++;
            if( colorCount == 100 ){
                colorSw = false;
                colorCount = 0;
            }
        }
        super.onDraw(canvas);
    }
//
}
