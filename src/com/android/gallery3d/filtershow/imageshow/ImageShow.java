
package com.android.gallery3d.filtershow.imageshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.FilterShowActivity;
import com.android.gallery3d.filtershow.HistoryAdapter;
import com.android.gallery3d.filtershow.ImageStateAdapter;
import com.android.gallery3d.filtershow.PanelController;
import com.android.gallery3d.filtershow.cache.ImageLoader;
import com.android.gallery3d.filtershow.filters.ImageFilter;
import com.android.gallery3d.filtershow.presets.ImagePreset;
import com.android.gallery3d.filtershow.ui.SliderController;
import com.android.gallery3d.filtershow.ui.SliderListener;

import java.io.File;

public class ImageShow extends View implements SliderListener, OnSeekBarChangeListener {

    private static final String LOGTAG = "ImageShow";

    protected Paint mPaint = new Paint();
    private static int mTextSize = 24;
    private static int mTextPadding = 20;

    protected ImagePreset mImagePreset = null;
    protected ImageLoader mImageLoader = null;
    private ImageFilter mCurrentFilter = null;

    private Bitmap mBackgroundImage = null;
    protected Bitmap mForegroundImage = null;
    protected Bitmap mFilteredImage = null;

    protected SliderController mSliderController = new SliderController();

    private HistoryAdapter mHistoryAdapter = null;
    private ImageStateAdapter mImageStateAdapter = null;

    protected Rect mImageBounds = null;
    protected float mImageRotation = 0;
    protected float mImageRotationZoomFactor = 0;

    private boolean mShowControls = false;
    private boolean mShowOriginal = false;
    private String mToast = null;
    private boolean mShowToast = false;
    private boolean mImportantToast = false;

    protected float mTouchX = 0;
    protected float mTouchY = 0;

    private SeekBar mSeekBar = null;
    private PanelController mController = null;

    private final Handler mHandler = new Handler();

    public void select() {
        if (getCurrentFilter() != null) {
            int parameter = getCurrentFilter().getParameter();
            updateSeekBar(parameter);
        }
    }

    public void updateSeekBar(int parameter) {
        if (mSeekBar == null) {
            return;
        }
        int progress = parameter + 100;
        mSeekBar.setProgress(progress);
        if (getPanelController() != null) {
            getPanelController().onNewValue(parameter);
        }
    }

    public void unselect() {

    }

    public void resetParameter() {
        onNewValue(0);
        mSliderController.reset();
    }

    public void setPanelController(PanelController controller) {
        mController = controller;
    }

    public PanelController getPanelController() {
        return mController;
    }

    @Override
    public void onNewValue(int value) {
        if (getCurrentFilter() != null) {
            getCurrentFilter().setParameter(value);
        }
        if (getImagePreset() != null) {
            mImageLoader.resetImageForPreset(getImagePreset(), this);
            getImagePreset().fillImageStateAdapter(mImageStateAdapter);
        }
        if (getPanelController() != null) {
            getPanelController().onNewValue(value);
        }
        updateSeekBar(value);
        invalidate();
    }

    @Override
    public void onTouchDown(float x, float y) {
        mTouchX = x;
        mTouchY = y;
        invalidate();
    }

    @Override
    public void onTouchUp() {
    }

    public ImageShow(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSliderController.setListener(this);
        mHistoryAdapter = new HistoryAdapter(context, R.layout.filtershow_history_operation_row,
                R.id.rowTextView);
        mImageStateAdapter = new ImageStateAdapter(context,
                R.layout.filtershow_imagestate_row);
    }

    public ImageShow(Context context) {
        super(context);
        mSliderController.setListener(this);
        mHistoryAdapter = new HistoryAdapter(context, R.layout.filtershow_history_operation_row,
                R.id.rowTextView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(parentWidth, parentHeight);
        mSliderController.setWidth(parentWidth);
        mSliderController.setHeight(parentHeight);
    }

    public void setSeekBar(SeekBar seekBar) {
        mSeekBar = seekBar;
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    public void setCurrentFilter(ImageFilter filter) {
        mCurrentFilter = filter;
    }

    public ImageFilter getCurrentFilter() {
        return mCurrentFilter;
    }

    public void setAdapter(HistoryAdapter adapter) {
        mHistoryAdapter = adapter;
    }

    public void showToast(String text) {
        showToast(text, false);
    }

    public void showToast(String text, boolean important) {
        mToast = text;
        mShowToast = true;
        mImportantToast = important;
        invalidate();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mShowToast = false;
                invalidate();
            }
        }, 400);
    }

    public Rect getImageBounds() {
        return mImageBounds;
    }

    public ImagePreset getImagePreset() {
        return mImagePreset;
    }

    public Bitmap getOriginalFrontBitmap() {
        if (mImageLoader != null) {
            return mImageLoader.getOriginalBitmapLarge();
        }
        return null;
    }

    public void drawToast(Canvas canvas) {
        if (mShowToast && mToast != null) {
            Paint paint = new Paint();
            paint.setTextSize(128);
            float textWidth = paint.measureText(mToast);
            int toastX = (int) ((getWidth() - textWidth) / 2.0f);
            int toastY = (int) (getHeight() / 3.0f);

            paint.setARGB(255, 0, 0, 0);
            canvas.drawText(mToast, toastX - 2, toastY - 2, paint);
            canvas.drawText(mToast, toastX - 2, toastY, paint);
            canvas.drawText(mToast, toastX, toastY - 2, paint);
            canvas.drawText(mToast, toastX + 2, toastY + 2, paint);
            canvas.drawText(mToast, toastX + 2, toastY, paint);
            canvas.drawText(mToast, toastX, toastY + 2, paint);
            if (mImportantToast) {
                paint.setARGB(255, 200, 0, 0);
            } else {
                paint.setARGB(255, 255, 255, 255);
            }
            canvas.drawText(mToast, toastX, toastY, paint);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawBackground(canvas);
        getFilteredImage();
        drawImage(canvas, mFilteredImage);

        if (showTitle() && getImagePreset() != null) {
            mPaint.setARGB(200, 0, 0, 0);
            mPaint.setTextSize(mTextSize);

            Rect textRect = new Rect(0, 0, getWidth(), mTextSize + mTextPadding);
            canvas.drawRect(textRect, mPaint);
            mPaint.setARGB(255, 200, 200, 200);
            canvas.drawText(getImagePreset().name(), mTextPadding,
                    10 + mTextPadding, mPaint);
        }
        mPaint.setARGB(255, 150, 150, 150);
        mPaint.setStrokeWidth(4);
        canvas.drawLine(0, 0, getWidth(), 0, mPaint);

        if (showControls()) {
            mSliderController.onDraw(canvas);
        }

        drawToast(canvas);
    }

    public void getFilteredImage() {
        Bitmap filteredImage = null;
        if (mImageLoader != null) {
            filteredImage = mImageLoader.getImageForPreset(this,
                    getImagePreset(), showHires());
        }

        if (filteredImage == null) {
            // if no image for the current preset, use the previous one
            filteredImage = mFilteredImage;
        } else {
            mFilteredImage = filteredImage;
        }

        if (mShowOriginal || mFilteredImage == null) {
            mFilteredImage = mForegroundImage;
        }
    }

    public void drawImage(Canvas canvas, Bitmap image) {
        if (image != null) {
            Rect s = new Rect(0, 0, image.getWidth(),
                    image.getHeight());
            float ratio = image.getWidth()
                    / (float) image.getHeight();
            float w = getWidth();
            float h = w / ratio;
            float ty = (getHeight() - h) / 2.0f;
            float tx = 0;
            // t = 0;
            if (ratio < 1.0f) { // portrait image
                h = getHeight();
                w = h * ratio;
                tx = (getWidth() - w) / 2.0f;
                ty = 0;
            }
            Rect d = new Rect((int) tx, (int) ty, (int) (w + tx),
                    (int) (h + ty));
            mImageBounds = d;

            canvas.drawBitmap(image, s, d, mPaint);
        }
    }

    public void drawBackground(Canvas canvas) {
        if (mBackgroundImage == null) {
            mBackgroundImage = mImageLoader.getBackgroundBitmap(getResources());
        }
        if (mBackgroundImage != null) {
            Rect s = new Rect(0, 0, mBackgroundImage.getWidth(),
                    mBackgroundImage.getHeight());
            Rect d = new Rect(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(mBackgroundImage, s, d, mPaint);
        }
    }

    public ImageShow setShowControls(boolean value) {
        mShowControls = value;
        if (mShowControls) {
            if (mSeekBar != null) {
                mSeekBar.setVisibility(View.VISIBLE);
            }
        } else {
            if (mSeekBar != null) {
                mSeekBar.setVisibility(View.INVISIBLE);
            }
        }
        return this;
    }

    public boolean showControls() {
        return mShowControls;
    }

    public boolean showHires() {
        return true;
    }

    public boolean showTitle() {
        return false;
    }

    public void setImagePreset(ImagePreset preset) {
        setImagePreset(preset, true);
    }

    public void setImagePreset(ImagePreset preset, boolean addToHistory) {
        mImagePreset = preset;
        if (getImagePreset() != null) {
            if (addToHistory) {
                mHistoryAdapter.insert(getImagePreset(), 0);
            }
            getImagePreset().setEndpoint(this);
            updateImage();
        }
        mImagePreset.fillImageStateAdapter(mImageStateAdapter);
        invalidate();
    }

    public void setImageLoader(ImageLoader loader) {
        mImageLoader = loader;
        if (mImageLoader != null) {
            mImageLoader.addListener(this);
        }
    }

    public void updateImage() {
        mForegroundImage = getOriginalFrontBitmap();
        /*
         * if (mImageLoader != null) {
         * mImageLoader.resetImageForPreset(getImagePreset(), this); }
         */

        /*
         * if (mForegroundImage != null) { Bitmap filteredImage =
         * mForegroundImage.copy(mConfig, true);
         * getImagePreset().apply(filteredImage); invalidate(); }
         */
    }

    public void updateFilteredImage(Bitmap bitmap) {
        mFilteredImage = bitmap;
    }

    public void saveImage(FilterShowActivity filterShowActivity, File file) {
        mImageLoader.saveImage(getImagePreset(), filterShowActivity, file);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        mSliderController.onTouchEvent(event);
        invalidate();
        return true;
    }

    // listview stuff

    public ArrayAdapter getHistoryAdapter() {
        return mHistoryAdapter;
    }

    public ArrayAdapter getImageStateAdapter() {
        return mImageStateAdapter;
    }

    public void onItemClick(int position) {
        setImagePreset(new ImagePreset(mHistoryAdapter.getItem(position)), false);
        // we need a copy from the history
        mHistoryAdapter.setCurrentPreset(position);
    }

    public void showOriginal(boolean show) {
        mShowOriginal = show;
        invalidate();
    }

    public float getImageRotation() {
        return mImageRotation;
    }

    public float getImageRotationZoomFactor() {
        return mImageRotationZoomFactor;
    }

    public void setImageRotation(float imageRotation,
            float imageRotationZoomFactor) {
        if (imageRotation != mImageRotation) {
            invalidate();
        }
        mImageRotation = imageRotation;
        mImageRotationZoomFactor = imageRotationZoomFactor;
    }

    @Override
    public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
        onNewValue(progress - 100);
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub

    }
}
