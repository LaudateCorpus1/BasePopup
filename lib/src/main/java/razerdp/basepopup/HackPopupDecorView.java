package razerdp.basepopup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import razerdp.blur.BlurImageView;
import razerdp.blur.PopupBlurOption;
import razerdp.util.log.LogTag;
import razerdp.util.log.LogUtil;

/**
 * Created by 大灯泡 on 2017/12/25.
 * <p>
 * 旨在用来拦截keyevent
 */
public class HackPopupDecorView extends ViewGroup {
    private static final String TAG = "HackPopupDecorView";
    private PopupController mPopupController;
    private BlurImageView blurImageView;
    private PopupBlurOption mOption;

    public HackPopupDecorView(Context context) {
        super(context);
    }

    public HackPopupDecorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HackPopupDecorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int childCount = getChildCount();
        if (childCount <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int maxWidth = 0;
            int maxHeight = 0;

            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
            }
            setMeasuredDimension(maxWidth, maxHeight);
        }
        LogUtil.trace(LogTag.d, TAG, "onMeasure");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childCount = getChildCount();
        if (childCount <= 0) {
            return;
        } else {
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                child.layout(l, t, r, b);
            }
        }
        LogUtil.trace(LogTag.d, TAG, "onLayout");

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean intercept = getPopupController() != null && getPopupController().onDispatchKeyEvent(event);
        if (intercept) return true;
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (getKeyDispatcherState() == null) {
                return super.dispatchKeyEvent(event);
            }

            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                final KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    state.startTracking(event, this);
                }
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                final KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null && state.isTracking(event) && !event.isCanceled()) {
                    if (getPopupController() != null) {
                        LogUtil.trace(LogTag.i, TAG, "dispatchKeyEvent: >>> onBackPressed");
                        return getPopupController().onBackPressed();
                    }
                }
            }
            return super.dispatchKeyEvent(event);
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getPopupController() != null) {
            if (getPopupController().onTouchEvent(event)) {
                return true;
            }
        }
        final int x = (int) event.getX();
        final int y = (int) event.getY();

        if ((event.getAction() == MotionEvent.ACTION_DOWN)
                && ((x < 0) || (x >= getWidth()) || (y < 0) || (y >= getHeight()))) {
            if (getPopupController() != null) {
                LogUtil.trace(LogTag.i, TAG, "onTouchEvent:[ACTION_DOWN] >>> onOutSideTouch");
                return getPopupController().onOutSideTouch();
            }
        } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            if (getPopupController() != null) {
                LogUtil.trace(LogTag.i, TAG, "onTouchEvent:[ACTION_OUTSIDE] >>> onOutSideTouch");
                return getPopupController().onOutSideTouch();
            }
        }
        return super.onTouchEvent(event);
    }

    public PopupController getPopupController() {
        return mPopupController;
    }

    public void setPopupController(PopupController popupController) {
        mPopupController = popupController;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (blurImageView != null) {
            blurImageView.attachBlurOption(mOption);
            blurImageView.start(mOption.getDuration());
        }
    }

    public void lazyAttachBlurImageview(PopupBlurOption option) {
        if (blurImageView != null) return;
        mOption = option;
        blurImageView = new BlurImageView(getContext());
        addView(blurImageView, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void startBlurAnima() {
        if (blurImageView == null) return;
        blurImageView.start(mOption == null ? 300 : mOption.getDuration());
    }

    public void dismissBlurAnima() {
        if (blurImageView == null) return;
        blurImageView.dismiss(mOption == null ? 300 : mOption.getDuration());
    }
}
