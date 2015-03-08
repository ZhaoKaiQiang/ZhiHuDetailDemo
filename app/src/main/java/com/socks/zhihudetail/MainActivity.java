package com.socks.zhihudetail;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
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
 *         http://blog.csdn.net/zhaokaiqiang1992
 */
public class MainActivity extends ActionBarActivity implements MyScrollView.BottomListener, MyScrollView.onScrollListener {

	private static final String TAG = "TAG";

	//顶部布局隐藏的检测距离
	private static final int TOP_DISTANCE_Y = 120;
	//默认的动画时间
	private static final int TIME_ANIMATION = 300;
	//是否在顶部布局的滑动范围内
	private boolean isInTopDistance = true;

	private ImageView img_bar;
	private TextView tv_title;
	private ImageView img_tools;
	private ImageView img_author;
	private MyScrollView mScroller;
	private FrameLayout fl_top;

	private TextView tv_content;


	private GestureDetector mGestureDetector;

	private float viewSlop;
	//按下的y坐标
	private float lastY;
	//记录手指是否向上滑动
	private boolean isUpSlide;
	//工具栏是否是隐藏状态
	private boolean isToolHide;
	//上部布局是否是隐藏状态
	private boolean isTopHide = false;
	//动画是否结束
	private boolean isAnimationFinish = true;
	//是否已经完成测量
	private boolean isMeasured = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		img_bar = (ImageView) findViewById(R.id.img_bar);
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_content = (TextView) findViewById(R.id.tv_content);
		img_tools = (ImageView) findViewById(R.id.img_tools);
		img_author = (ImageView) findViewById(R.id.img_author);
		mScroller = (MyScrollView) findViewById(R.id.scroller);
		fl_top = (FrameLayout) findViewById(R.id.ll_top);

		viewSlop = ViewConfiguration.get(this).getScaledTouchSlop();

		mGestureDetector = new GestureDetector(this, new DetailGestureListener());

		mScroller.setBottomListener(this);
		mScroller.setScrollListener(this);

		//设置点击事件之后，会消耗DOWN事件，并导致ScrollView的MOVE事件触发不准确
//		tv_content.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Log.d("TAG", "tv_content-----onClick-------------");
//			}
//		});

		mScroller.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {

					case MotionEvent.ACTION_DOWN:
						Log.d("TAG", "mScroller-----ACTION_DOWN------------");
						lastY = event.getY();
						break;
					case MotionEvent.ACTION_MOVE:

						Log.d("TAG", "mScroller-----ACTION_MOVE");

						float disY = event.getY() - lastY;
						//垂直方向滑动
						if (Math.abs(disY) > viewSlop) {
							//设置了TextView的点击事件之后，会导致这里的disY的数值出现跳号现象，最终导致的效果就是
							//下面的tool布局在手指往下滑动的时候，先显示一个，然后再隐藏，这是完全没必要的
							Log.d("TAG", "----------------------disY = " + disY);
							//是否向上滑动
							isUpSlide = disY < 0;
							//实现底部tools的显示与隐藏
							if (isUpSlide) {
								if (!isToolHide)
									hideTool();
							} else {
								if (isToolHide)
									showTool();
							}
						}

						lastY = event.getY();
						break;
				}

				mGestureDetector.onTouchEvent(event);

				return false;
			}


		});

		//获取Bar和Title的高度，完成auther布局的margenTop设置
		ViewTreeObserver viewTreeObserver = fl_top.getViewTreeObserver();
		viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {

				if (!isMeasured) {
					FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout
							.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
					layoutParams.setMargins(0, img_bar.getHeight() + tv_title.getHeight(), 0, 0);
					img_author.setLayoutParams(layoutParams);
					isMeasured = true;
				}
				return true;
			}
		});


	}

	/**
	 * 显示工具栏
	 */
	private void showTool() {

		Log.d("TAG", "------------showTool-----------");

		int startY = getWindow().getDecorView()
				.getHeight() - getStatusHeight(this);
		ObjectAnimator anim = ObjectAnimator.ofFloat(img_tools, "y", startY,
				startY - img_tools.getHeight());
		anim.setDuration(TIME_ANIMATION);
		anim.start();
		isToolHide = false;

	}

	/**
	 * 隐藏工具栏
	 */
	private void hideTool() {

		Log.d("TAG", "------------hideTool-----------");

		int startY = getWindow().getDecorView()
				.getHeight() - getStatusHeight(this);
		ObjectAnimator anim = ObjectAnimator.ofFloat(img_tools, "y", startY - img_tools.getHeight(),
				startY);
		anim.setDuration(TIME_ANIMATION);
		anim.start();
		isToolHide = true;

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
		if (isToolHide) {
			showTool();
		}
	}

	@Override
	public void onScrollChanged(int l, int t, int oldl, int oldt) {

		//判断当前的布局范围是否是在顶部布局的滑动范围内
		if (t <= dp2px(TOP_DISTANCE_Y)) {
			isInTopDistance = true;
		} else {
			isInTopDistance = false;
		}

		if (t <= dp2px(TOP_DISTANCE_Y) && isTopHide) {
			showTop();
		} else if (t > dp2px(TOP_DISTANCE_Y) && !isTopHide) {
			hideTop();
		}
	}

	private int dp2px(int dp) {
		float scale = getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}


	/**
	 * 手势指示器
	 */
	private class DetailGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}


		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {

			//如果都是隐藏状态，那么都显示出来
			if (isTopHide && isToolHide) {
				showTop();
				showTool();
			} else if (!isToolHide && isTopHide) {
				//如果上面隐藏，下面显示，就显示上面
				showTop();
			} else if (!isTopHide && isToolHide) {
				//如果上面显示，下面隐藏，那么就显示下面
				showTool();
			} else {
				//都在显示，那么就都隐藏
				hideTool();
				if (!isInTopDistance) {
					hideTop();
				}
			}

			return super.onSingleTapConfirmed(e);
		}
	}


	public static int getStatusHeight(Activity activity) {
		int statusHeight = 0;
		Rect localRect = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
		statusHeight = localRect.top;
		if (0 == statusHeight) {
			Class<?> localClass;
			try {
				localClass = Class.forName("com.android.internal.R$dimen");
				Object localObject = localClass.newInstance();
				int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
				statusHeight = activity.getResources().getDimensionPixelSize(i5);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusHeight;
	}

}
