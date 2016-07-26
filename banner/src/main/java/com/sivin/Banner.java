package com.sivin;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import sivin.com.banner.R;

/**
 * ����ֲ��ؼ�
 * Created by xiwen on 2016/4/12.
 */
public class Banner extends RelativeLayout {
    private static final String TAG = Banner.class.getSimpleName();

    private Context mContext;

    private SparseArray<ImageView> mItemArrays;

    /**
     * ���ֲ���
     */
    private static final int RMP = LayoutParams.MATCH_PARENT;
    private static final int RWC = LayoutParams.WRAP_CONTENT;
    private static final int LWC = LinearLayout.LayoutParams.WRAP_CONTENT;
    /**
     * ѭ���ֲ���Viewpager
     */
    private SLooperViewPager mViewPager;


    //�����������ؼ�����ŵ�һ����Բ����У����ڲ���Ҫ���ó�Ա�������ʴ�ûд
    /**
     * �ֲ��ؼ�����ʾ����
     */
    private TextView mTipTextView;
    /**
     * ��ʾ���ֵĴ�С
     */
    private int mTipTextSize;

    /**
     * ��ʾ���ֵ���ɫ��Ĭ���ǰ�ɫ
     */
    private int mTipTextColor = Color.WHITE;

    /**
     * ��ŵ������
     */
    private LinearLayout mPointContainerLl;
    /**
     * ���drawable��Դid
     */
    private int mPointDrawableResId = R.drawable.selector_banner_point;

    /**
     * ���������е�layout������
     */
    private int mPointGravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
    private int mPointLeftRightMargin;
    private int mPointTopBottomMargin;
    private int mPointContainerLeftRightPadding;

    /**
     * ���TipTextView��mPointContainerLl����Բ��ֵı�����ԴId��
     */
    private Drawable mPointContainerBackgroundDrawable;

    /**
     * ����ֲ���Ϣ�����ݼ���
     */
    protected List mData = new ArrayList<>();

    /**
     * �Զ����ŵļ��
     */
    private int mAutoPlayInterval = 3;

    /**
     * ҳ���л���ʱ�䣨����һҳ��ʼ���֣�����ȫ���ֵ�ʱ�䣩
     */
    private int mPageChangeDuration = 800;
    /**
     * �Ƿ����ڲ���
     */
    private boolean mPlaying = false;

    /**
     * ��ǰ��ҳ���λ��
     */
    protected int currentPosition;

    /**
     * Banner�ؼ���������
     */
    private BannerAdapter mBannerAdapter;

    /**
     * ����ִ����
     */
    protected ScheduledExecutorService mExecutor;


    /**
     * ������һ��ִ����
     */
    private Handler mPlayHandler = new PlayHandler(this);


    public Banner(Context context) {
        this(context, null);
    }

    public Banner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Banner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //��ʼ��Ĭ������
        initDefaultAttrs(context);

        //��ʼ���Զ�������
        initCustomAttrs(context, attrs);

        //�ؼ���ʼ��
        initView(context);
    }

    private void initDefaultAttrs(Context context) {

        //Ĭ�ϵ�ָʾ��������Margin3dp
        mPointLeftRightMargin = dp2px(context, 3);
        //Ĭ�ϵ�ָʾ��������marginΪ6dp
        mPointTopBottomMargin = dp2px(context, 6);
        //Ĭ�ϵ�����������paddingΪ10dp
        mPointContainerLeftRightPadding = dp2px(context, 10);
        //Ĭ��ָʾ����ʾ���ִ�С8sp
        mTipTextSize = sp2px(context, 8);
        //Ĭ��ָʾ�������ı���ͼƬ
        mPointContainerBackgroundDrawable = new ColorDrawable(Color.parseColor("#33aaaaaa"));
    }

    private int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    private int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    /**
     * ��ʼ���Զ�������
     *
     * @param context context
     * @param attrs   attrs
     */
    private void initCustomAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SivinBanner);
        final int N = typedArray.getIndexCount();
        for (int i = 0; i < N; i++) {
            initCustomAttr(typedArray.getIndex(i), typedArray);
        }
        typedArray.recycle();
    }

    private void initCustomAttr(int attr, TypedArray typedArray) {
        if (attr == R.styleable.SivinBanner_banner_pointDrawable) {
            //ָʾ�������ʽ��Դid
            mPointDrawableResId = typedArray.getResourceId(attr, R.drawable.selector_banner_point);
        } else if (attr == R.styleable.SivinBanner_banner_pointContainerBackground) {
            //ָʾ������������ʽ
            mPointContainerBackgroundDrawable = typedArray.getDrawable(attr);

        } else if (attr == R.styleable.SivinBanner_banner_pointLeftRightMargin) {
            //ָʾ�����ұ߾�
            mPointLeftRightMargin = typedArray.getDimensionPixelSize(attr, mPointLeftRightMargin);
        } else if (attr == R.styleable.SivinBanner_banner_pointContainerLeftRightPadding) {
            //ָʾ������������padding
            mPointContainerLeftRightPadding = typedArray.getDimensionPixelSize(attr, mPointContainerLeftRightPadding);
        } else if (attr == R.styleable.SivinBanner_banner_pointTopBottomMargin) {

            //ָʾ��������margin
            mPointTopBottomMargin = typedArray.getDimensionPixelSize(attr, mPointTopBottomMargin);
        } else if (attr == R.styleable.SivinBanner_banner_pointGravity) {
            //ָʾ���������е�λ������
            mPointGravity = typedArray.getInt(attr, mPointGravity);
        } else if (attr == R.styleable.SivinBanner_banner_pointAutoPlayInterval) {
            //�ֲ��ļ��
            mAutoPlayInterval = typedArray.getInteger(attr, mAutoPlayInterval);
        } else if (attr == R.styleable.SivinBanner_banner_pageChangeDuration) {
            //ҳ���л��ĳ���ʱ��
            mPageChangeDuration = typedArray.getInteger(attr, mPageChangeDuration);
        } else if (attr == R.styleable.SivinBanner_banner_tipTextColor) {
            //��ʾ������ɫ
            mTipTextColor = typedArray.getColor(attr, mTipTextColor);
        } else if (attr == R.styleable.SivinBanner_banner_tipTextSize) {
            //��ʾ���ִ�С
            mTipTextSize = typedArray.getDimensionPixelSize(attr, mTipTextSize);
        }

    }

    /**
     * �ؼ���ʼ��
     *
     * @param context context
     */
    private void initView(Context context) {
        mContext = context;

        mItemArrays = new SparseArray<>();

        //��ʼ��ViewPager
        mViewPager = new SLooperViewPager(context);

        //��matchParent�ķ�ʽ��viewPager��䵽�ؼ�������
        addView(mViewPager, new LayoutParams(RMP, RMP));

        //����ҳ���л��ĳ���ʱ��
        setPageChangeDuration(mPageChangeDuration);


        //����ָʾ����������Բ���
        RelativeLayout indicatorContainerRl = new RelativeLayout(context);
        //����ָʾ�������ı���
        if (Build.VERSION.SDK_INT >= 16) {
            indicatorContainerRl.setBackground(mPointContainerBackgroundDrawable);
        } else {
            indicatorContainerRl.setBackgroundDrawable(mPointContainerBackgroundDrawable);
        }
        //����ָʾ������Padding
        indicatorContainerRl.setPadding(mPointContainerLeftRightPadding, 0, mPointContainerLeftRightPadding, 0);
        //��ʼ��ָʾ�������Ĳ��ֲ���
        LayoutParams indicatorContainerLp = new LayoutParams(RMP, RWC);
        // ����ָʾ�������ڵ���view�Ĳ��ַ�ʽ
        if ((mPointGravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.TOP) {
            indicatorContainerLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        } else {
            indicatorContainerLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        //��ָʾ��������ӵ���View��
        addView(indicatorContainerRl, indicatorContainerLp);


        //��ʼ����ŵ���������Բ���
        mPointContainerLl = new LinearLayout(context);
        //���õ��������ֵ�id
        mPointContainerLl.setId(R.id.banner_pointContainerId);
        //�������Բ��ֵķ���
        mPointContainerLl.setOrientation(LinearLayout.HORIZONTAL);
        //���õ������Ĳ��ֲ���
        LayoutParams pointContainerLp = new LayoutParams(RWC, RWC);
        //����������ŵ�ָʾ��������
        indicatorContainerRl.addView(mPointContainerLl, pointContainerLp);


        //��ʼ��tip��layout�ߴ�������߶Ⱥ͵�ĸ߶�һ��
        LayoutParams tipLp = new LayoutParams(RMP, getResources().getDrawable(mPointDrawableResId).getIntrinsicHeight() + 2 * mPointTopBottomMargin);
        mTipTextView = new TextView(context);
        mTipTextView.setGravity(Gravity.CENTER_VERTICAL);
        mTipTextView.setSingleLine(true);
        mTipTextView.setEllipsize(TextUtils.TruncateAt.END);
        mTipTextView.setTextColor(mTipTextColor);
        mTipTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTipTextSize);
        //��TieTextView�����ָʾ��������
        indicatorContainerRl.addView(mTipTextView, tipLp);
        int horizontalGravity = mPointGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        // ����Բ������λ��ָʾ����������ߡ��ұ߻���ˮƽ����
        if (horizontalGravity == Gravity.LEFT) {
            pointContainerLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            //��ʾ���������ڵ��������ұ�
            tipLp.addRule(RelativeLayout.RIGHT_OF, R.id.banner_pointContainerId);
            mTipTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        } else if (horizontalGravity == Gravity.RIGHT) {
            pointContainerLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            tipLp.addRule(RelativeLayout.LEFT_OF, R.id.banner_pointContainerId);
        } else {
            pointContainerLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            tipLp.addRule(RelativeLayout.LEFT_OF, R.id.banner_pointContainerId);
        }
    }


    /**
     * ��ʼ����
     * ����������������ʹ��ˢ�»����ݵ�ʱ����������
     */
    private void initPoints() {
        //��ȡ������ԭ�е������
        int childCount = mPointContainerLl.getChildCount();
        //��ȡĿ����������
        int dataSize = mData.size();
        //��ȡ���ӻ�ȡɾ���������
        int offset = dataSize - childCount;
        if (offset == 0)
            return;
        if (offset > 0) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LWC, LWC);
            lp.setMargins(mPointLeftRightMargin, mPointTopBottomMargin, mPointLeftRightMargin, mPointTopBottomMargin);
            ImageView imageView;
            for (int i = 0; i < offset; i++) {
                imageView = new ImageView(getContext());
                imageView.setLayoutParams(lp);
                imageView.setImageResource(mPointDrawableResId);
                imageView.setEnabled(false);
                mPointContainerLl.addView(imageView);
            }
            return;
        }
        if (offset < 0) {
            mPointContainerLl.removeViews(dataSize, -offset);
        }
    }


    private final class ChangePointListener extends SLooperViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            currentPosition = position % mData.size();
            switchToPoint(currentPosition);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (mTipTextView != null) {
                if (positionOffset > 0.5) {
                    mBannerAdapter.selectTips(mTipTextView, currentPosition);
                    mTipTextView.setAlpha(positionOffset);
                } else {
                    mTipTextView.setAlpha(1 - positionOffset);
                    mBannerAdapter.selectTips(mTipTextView, currentPosition);
                }
            }
        }
    }

    /**
     * �����л���ָ����λ��
     * ���ǽ�ָ��λ�õĵ����ó�Enable
     *
     * @param newCurrentPoint ��λ��
     */
    private void switchToPoint(int newCurrentPoint) {
        for (int i = 0; i < mPointContainerLl.getChildCount(); i++) {
            mPointContainerLl.getChildAt(i).setEnabled(false);
        }
        mPointContainerLl.getChildAt(newCurrentPoint).setEnabled(true);

        if (mTipTextView != null) {
            mBannerAdapter.selectTips(mTipTextView, currentPosition);
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                pauseScroll();
                break;
            case MotionEvent.ACTION_UP:
                goScroll();
                break;
            case MotionEvent.ACTION_CANCEL:
                goScroll();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    /**
     * ����ҳ���л����̵�ʱ�䳤��
     *
     * @param duration ҳ���л����̵�ʱ�䳤��
     */
    public void setPageChangeDuration(int duration) {
        mPageChangeDuration = duration;
    }

    /**
     * ��������һ����Ŀ
     *
     * @param position
     */
    private void scrollToNextItem(int position) {
        position++;
        mViewPager.setCurrentItem(position, true);
    }


    /**
     * viewPager��������
     */
    private final class InnerPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            ImageView view = createItemView(position);
            mBannerAdapter.setImageViewSource(view, position);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onVpItemClickListener != null) {
                        onVpItemClickListener.onItemClick(position);
                    }
                }
            });

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    /**
     * ����itemView
     *
     * @param position
     * @return
     */
    private ImageView createItemView(int position) {
        ImageView iv = new ImageView(mContext);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mItemArrays.put(position, iv);
        return iv;
    }

    ;


    private OnBannerItemClickListener onVpItemClickListener;

    /**
     * ����viewPage��Item���������
     *
     * @param listener
     */
    public void setOnBannerItemClickListener(OnBannerItemClickListener listener) {
        this.onVpItemClickListener = listener;
    }

    public interface OnBannerItemClickListener {
        void onItemClick(int position);
    }


    /**
     * ����ʹ��״̬ ��viewpager������ͣ��״̬
     * ��ʼ����
     */
    private void goScroll() {
        if (!isValid()) {
            return;
        }
        if (mPlaying) {
            return;
        } else {
            pauseScroll();
            mExecutor = Executors.newSingleThreadScheduledExecutor();
            //command��ִ���߳�
            //initialDelay����ʼ����ʱ
            //period�����ο�ʼִ����С���ʱ��
            //unit����ʱ��λ
            mExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    mPlayHandler.obtainMessage().sendToTarget();
                }
            }, mAutoPlayInterval, mAutoPlayInterval, TimeUnit.SECONDS);
            mPlaying = true;
        }
    }


    /**
     * ��ͣ����
     */
    public void pauseScroll() {
        if (mExecutor != null) {
            mExecutor.shutdown();
            mExecutor = null;
        }
        mPlaying = false;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            goScroll();
        } else if (visibility == INVISIBLE) {
            pauseScroll();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        pauseScroll();
    }

    /**
     * �жϿؼ��Ƿ����
     *
     * @return
     */
    private boolean isValid() {
        if (mViewPager == null) {
            Log.e(TAG, "ViewPager is not exist!");
            return false;
        }
        if (mData == null || mData.size() == 0) {
            Log.e(TAG, "DataList must be not empty!");
            return false;
        }
        return true;
    }

    /**
     * �������ݵļ���
     */
    private void setSource() {
        List list = mBannerAdapter.getDatas();
        if (list == null) {
            Log.d(TAG, "setSource: list==null");
            return;
        }
        this.mData = list;
        setAdapter();
    }

    /**
     * ��viewpager����������
     */
    private void setAdapter() {
        mViewPager.setAdapter(new InnerPagerAdapter());
        mViewPager.addOnPageChangeListener(new ChangePointListener());
    }

    public void setBannerAdapter(BannerAdapter adapter) {
        mBannerAdapter = adapter;
        setSource();
    }


    /**
     * ֪ͨ�����Ѿ������ı�
     */
    public void notifiDataHasChanged() {
        initPoints();
        mViewPager.getAdapter().notifyDataSetChanged();
        mViewPager.setCurrentItem(0, false);
        if (mData.size() > 1)
            goScroll();
    }


    /**
     * ��̬�ڲ��࣬��ֹ�����ڴ�й¶
     */
    static class PlayHandler extends Handler {
        WeakReference<Banner> mWeakBanner;

        public PlayHandler(Banner banner) {
            this.mWeakBanner = new WeakReference<Banner>(banner);
        }

        @Override
        public void handleMessage(Message msg) {
            Banner weakBanner = mWeakBanner.get();
            if (weakBanner != null)
                weakBanner.scrollToNextItem(weakBanner.currentPosition);
        }
    }


}
