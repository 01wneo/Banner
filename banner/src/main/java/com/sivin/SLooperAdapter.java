package com.sivin;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * �����ֲ���viewPager������
 * Created by xiwen on 2016/4/13.
 */
public class SLooperAdapter extends PagerAdapter {
    private PagerAdapter mAdapter;

    private int mItemCount = 0;

    public SLooperAdapter(PagerAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public int getCount() {
        //�����ViewPager�����������������ϵ�Item��ʱ����ӳ����߽�Item��������ʾ���ڲ����һ��
        return mAdapter.getCount() <= 1 ? mAdapter.getCount() : mAdapter.getCount() + 2;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return mAdapter.isViewFromObject(view, object);
    }


    @Override
    public void startUpdate(ViewGroup container) {
        mAdapter.startUpdate(container);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        return mAdapter.instantiateItem(container, getInnerAdapterPosition(position));
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        mAdapter.destroyItem(container, getInnerAdapterPosition(position), object);
    }


    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        mAdapter.setPrimaryItem(container, position, object);
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        mAdapter.finishUpdate(container);
    }


    @Override
    public void notifyDataSetChanged() {
        mItemCount = getCount();
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    /**
     * �������position�Ļ�ȡ�ڲ��position
     *
     * @param position ���ViewPager��position
     * @return ���viewPager��ǰ����λ�ö�Ӧ���ڲ�viewPager��Ӧ��λ�á�
     */
    public int getInnerAdapterPosition(int position) {
        //viewPager�����Ŀ��õĸ���
        int realCount = getInnerCount();
        //�ڲ�û�п��õ�Item�򻻻�Ϊ��
        if (realCount == 0)
            return 0;
        int realPosition = (position - 1) % realCount;
        if (realPosition < 0)
            realPosition += realCount;
        return realPosition;
    }

    /**
     * @return �ڲ�ViewPager�п��õ�item����
     */
    public int getInnerCount() {
        return mAdapter.getCount();
    }

    /**
     * �����ڲ�postion��λ�ã�����ӳ������position��λ��
     *
     * @param position �ڲ�position��λ��
     * @return �����ֲ�ViewPager���л�λ��
     */
    public int toLooperPosition(int position) {
        if (getInnerCount() > 1) {
            return position + 1;
        } else return position;
    }


}
