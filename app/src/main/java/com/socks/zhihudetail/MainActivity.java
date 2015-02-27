package com.socks.zhihudetail;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * 仿知乎回答详情页
 *
 * @author ZhaoKaiQiang
 * http://blog.csdn.net/zhaokaiqiang1992
 */
public class MainActivity extends ActionBarActivity implements MyScrollView.BottomListener,
		MyScrollView.onScrollListener {

	private static final String TAG = "TAG";

	//顶部布局隐藏的检测距离
	private static final int TOP_DISTANCE_Y = 120;
	//默认的动画时间
	private static final int TIME_ANIMATION = 300;

	private ImageView img_bar;
	private TextView tv_title;
	private ImageView img_tools;
	private ImageView img_author;
	private MyScrollView mScroller;
	private FrameLayout fl_top;

	private float viewSlop;
	//按下的y坐标
	private float lastY;
	//记录手指是否向上滑动
	private boolean isUpSlide;
	//工具栏是否是隐藏状态
	private boolean isToolsHide;
	//上部布局是否是隐藏状态
	private boolean isTopHide = false;
	//动画是否结束
	private boolean isAnimationFinish = true;
	//是否已经完成测量
	private boolean hasMeasured = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		img_bar = (ImageView) findViewById(R.id.img_bar);
		tv_title = (TextView) findViewById(R.id.tv_title);
		img_tools = (ImageView) findViewById(R.id.img_tools);
		img_author = (ImageView) findViewById(R.id.img_author);
		mScroller = (MyScrollView) findViewById(R.id.scroller);
		fl_top = (FrameLayout) findViewById(R.id.ll_top);

		viewSlop = ViewConfiguration.get(this).getScaledTouchSlop();

		mScroller.setBottomListener(this);
		mScroller.setScrollListener(this);

		mScroller.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {


				switch (event.getAction()) {

					case MotionEvent.ACTION_DOWN:
						lastY = event.getY();
						break;
					case MotionEvent.ACTION_MOVE:

						float disY = event.getY() - lastY;

						//垂直方向滑动
						if (Math.abs(disY) > viewSlop) {
							//是否向上滑动
							isUpSlide = disY < 0;

							//实现底部tools的显示与隐藏
							if (isUpSlide) {
								if (!isToolsHide)
									hideTools();
							} else {
								if (isToolsHide)
									showTools();
							}
						}

						break;
				}

				return false;
			}
		});

		//获取Bar和Title的高度，完成auther布局的margenTop设置
		ViewTreeObserver viewTreeObserver = fl_top.getViewTreeObserver();
		viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {

				if (!hasMeasured) {
					FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout
							.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
					layoutParams.setMargins(0, img_bar.getHeight() + tv_title.getHeight(), 0, 0);
					img_author.setLayoutParams(layoutParams);
					hasMeasured = true;
				}
				return true;
			}
		});


	}

	/**
	 * 显示工具栏
	 */
	private void showTools() {

		ObjectAnimator anim = ObjectAnimator.ofFloat(img_tools, "y", img_tools.getY(),
				img_tools.getY() - img_tools.getHeight());
		anim.setDuration(TIME_ANIMATION);
		anim.start();

		isToolsHide = false;
	}

	/**
	 * 隐藏工具栏
	 */
	private void hideTools() {

		ObjectAnimator anim = ObjectAnimator.ofFloat(img_tools, "y", img_tools.getY(),
				img_tools.getY() + img_tools.getHeight());
		anim.setDuration(TIME_ANIMATION);
		anim.start();

		isToolsHide = true;

	}

	/**
	 * 显示上部的布局
	 */
	private void showTop() {

		ObjectAnimator anim1 = ObjectAnimator.ofFloat(img_bar, "y", img_bar.getY(),
				0);
		anim1.setDuration(TIME_ANIMATION);
		anim1.start();

		ObjectAnimator anim2 = ObjectAnimator.ofFloat(tv_title, "y", tv_title.getY(),
				img_bar.getHeight());
		anim2.setInterpolator(new DecelerateInterpolator());
		anim2.setDuration(TIME_ANIMATION + 200);
		anim2.start();

		ObjectAnimator anim4 = ObjectAnimator.ofFloat(fl_top, "y", fl_top.getY(),
				0);
		anim4.setDuration(TIME_ANIMATION);
		anim4.start();

		isTopHide = false;
	}


	/**
	 * 隐藏上部的布局
	 */
	private void hideTop() {

		ObjectAnimator anim1 = ObjectAnimator.ofFloat(img_bar, "y", 0,
				-img_bar.getHeight());
		anim1.setDuration(TIME_ANIMATION);
		anim1.start();

		ObjectAnimator anim2 = ObjectAnimator.ofFloat(tv_title, "y", tv_title.getY(),
				-tv_title.getHeight());
		anim2.setDuration(TIME_ANIMATION);
		anim2.start();

		ObjectAnimator anim4 = ObjectAnimator.ofFloat(fl_top, "y", 0,
				-(img_bar.getHeight() + tv_title.getHeight()));
		anim4.setDuration(TIME_ANIMATION);
		anim4.start();

		isTopHide = true;
	}

	@Override
	public void onBottom() {
		if (isToolsHide) {
			showTools();
		}
	}

	@Override
	public void onScrollChanged(int l, int t, int oldl, int oldt) {

		if (t <= dp2px(TOP_DISTANCE_Y) && isTopHide && isAnimationFinish) {
			showTop();
			Log.d(TAG, "显示");
		} else if (t > dp2px(TOP_DISTANCE_Y) && !isTopHide && isAnimationFinish) {
			hideTop();
			Log.d(TAG, "隐藏");
		}
	}

	private int dp2px(int dp) {
		float scale = getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}
}
