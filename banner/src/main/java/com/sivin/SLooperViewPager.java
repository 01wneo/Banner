package com.sivin;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * �����ֲ���ViewPager
 * Created by xiwen on 2016/4/13.
 */
public class SLooperViewPager extends ViewPager {
    private SLooperAdapter mAdapter;
    private List<OnPageChangeListener> mOnPageChangeListeners;
    public SLooperViewPager(Context context) {
        this(context, null);
    }


    public SLooperViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    @Override
    public void setAdapter(PagerAdapter adapter) {
        mAdapter = new SLooperAdapter(adapter);
        super.setAdapter(mAdapter);
        setCurrentItem(0, false);
    }

    @Override
    public PagerAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setCurrentItem(int item) {
        setCurrentItem(item, true);
    }

    @Override
    public void setCurrentItem(int position, boolean smoothScroll) {
        //item�ı������ߴ��ݹ�����λ����û��ԭʼ��λ�ã����л�λ���Ǵ�0��DataSize-1֮���л�
        //���Ƕ������ViewPager���ԣ�����Ҫ��λ�÷�ΧӦ����ӳ����λ���л���������ȥ����ӳ���ҳ��
        //Ӧ���Ǵ�1��ӳ���ĵ����ڶ���λ��

        super.setCurrentItem(mAdapter.toLooperPosition(position), smoothScroll);
    }


    /**
     * ���ViewPager�е�item��ͨ���ڲ�λ��ӳ���ϵ�õ���
     *
     * @return ����ӳ����
     */
    @Override
    public int getCurrentItem() {
        return mAdapter.getInnerAdapterPosition(super.getCurrentItem());
    }




    @Override
    public void clearOnPageChangeListeners() {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners.clear();
        }
    }

    @Override
    public void removeOnPageChangeListener(OnPageChangeListener listener) {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners.remove(listener);
        }
    }

    @Override
    public void addOnPageChangeListener(OnPageChangeListener listener) {
        if (mOnPageChangeListeners == null) {
            mOnPageChangeListeners = new ArrayList<>();
        }
        mOnPageChangeListeners.add(listener);
    }


    private void init(Context context) {
        if (mOnPageChangeListener != null) {
            super.removeOnPageChangeListener(mOnPageChangeListener);
        }
        super.addOnPageChangeListener(mOnPageChangeListener);

    }


    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        //��һ�ε�ƫ����
        private float mPreviousOffset = -1;
        //��һ�ε�λ��
        private float mPreviousPosition = -1;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (mAdapter != null) {
                int innerPosition = mAdapter.getInnerAdapterPosition(position);

                /*
                    positionOffset =0:������ɣ�
                    position =0 :��ʼ�ı߽�
                    position =mAdapter.getCount()-1:�����ı߽�
                 */
                if (positionOffset == 0 && mPreviousOffset == 0 && (position == 0 || position == mAdapter.getCount() - 1)) {
                    //ǿ�ƻص�ӳ��λ��
                    setCurrentItem(innerPosition, false);
                }
                mPreviousOffset = positionOffset;

                if (mOnPageChangeListeners != null) {
                    for (int i = 0; i < mOnPageChangeListeners.size(); i++) {
                        OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                        if (listener != null) {
                            //����ڲ��λ��û�дﵽ���һ�����ڲ������������������
                            if (innerPosition != mAdapter.getInnerCount() - 1) {
                                listener.onPageScrolled(innerPosition, positionOffset, positionOffsetPixels);
                            } else {
                                //����������һ��λ�ã���ƫ�����ﵽ0.5���ϣ�����߼����������ҳ���Ѿ������ڲ�ĵ�һ��λ��
                                //���������һ��λ��
                                if (positionOffset > 0.5) {
                                    listener.onPageScrolled(0, 0, 0);
                                } else {
                                    listener.onPageScrolled(innerPosition, 0, 0);
                                }
                            }
                        }
                    }
                }
            }

        }

        @Override
        public void onPageSelected(int position) {

            int realPosition = mAdapter.getInnerAdapterPosition(position);
            if (mPreviousPosition != realPosition) {
                mPreviousPosition = realPosition;
                if (mOnPageChangeListeners != null) {
                    for (int i = 0; i < mOnPageChangeListeners.size(); i++) {
                        OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                        if (listener != null) {
                            listener.onPageSelected(realPosition);
                        }
                    }
                }
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (mAdapter != null) {
                int position = SLooperViewPager.super.getCurrentItem();
                int realPosition = mAdapter.getInnerAdapterPosition(position);
                if (state == ViewPager.SCROLL_STATE_IDLE && (position == 0 || position == mAdapter.getCount() - 1)) {
                    setCurrentItem(realPosition, false);
                }
            }
            if (mOnPageChangeListeners != null) {
                for (int i = 0; i < mOnPageChangeListeners.size(); i++) {
                    OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        listener.onPageScrollStateChanged(state);
                    }
                }
            }
        }
    };


}
