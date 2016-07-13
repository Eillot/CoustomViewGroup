package eliot.wakfo.com.coustomviewgroup;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by Eillot on 2016/7/12.
 */
public class CoustomViewGroup extends ViewGroup{

    private static final String TAG = "CoustomViewGroup";


    private int mChildSize;
    private int mChildWidth;
    private int mChildIndex;

    //分别记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;

    //分别记录上次滑动的坐标对于(onInterceptTouchEvent)

    private int mLastInterceptX = 0;
    private int mLastInterceptY = 0;

    //Scroller类可以让View平滑滚动的一个Helper类；
    private Scroller mScroller;

    //VelocityTracker主要用跟踪触摸屏事件(flinging事件和其他gestures手势事件)的速率;
    private VelocityTracker mVelocityTracker;

    public CoustomViewGroup(Context context) {
        super(context);
        init();
    }

    public CoustomViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CoustomViewGroup(Context context, AttributeSet attrs,
                            int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {

        if ( mScroller == null){
            mScroller = new Scroller(getContext());
            //获取滑动速率对象
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    /**
     * 为什么要重写ViewGroup的事件分发呢？
     * 若不重写onInterceptTouchEvent（），你会发现即使你的OnTechEvent（）方法返回的是True（即应该处理事件），
     * 然而，它却并没有处理事件，why ？ 因为事件已经被拦截了！所以，要加入自己的逻辑判断，让onInterceptTouchEvent（）
     * 知道什么时候进行拦截，什么时候不进行拦截~~
     *
     * @param event
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercepted = false;
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                intercepted = false;
                //手指落下时若View平滑滚动还未完成，则打断动画，并对Down事件进行拦截
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    intercepted = true;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE:{
                intercepted = false;
                int deltaX = x - mLastInterceptX;//View的滑动后距离的X坐标
                int deltaY = y - mLastInterceptY;//View的滑动后距离的Y坐标

                //用于判断是否正在由左向右进行滑动，若是则intercepted = true对事件进行拦截;
                if ( (Math.abs( deltaX)) > (Math.abs(deltaY)) ){

                    intercepted = true;
                }else {

                    intercepted = false;
                }

                break;
            }

            case MotionEvent.ACTION_UP:{

                intercepted = false;
                break;
            }

            default:
                break;

        }

        Log.d( TAG , "intercepted=  " + intercepted);
        mLastX = x;
        mLastY = y;
        mLastInterceptX = x;
        mLastInterceptY = y;

        //不在使用supper来继续使用父类的拦截方法
        return intercepted;
    }

    /**
     *  重写onTouchEvent
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mVelocityTracker.addMovement(event);//表示追踪当前点击事件的速度；
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            }


            case MotionEvent.ACTION_MOVE: {
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                scrollBy(-deltaX, 0);
                break;
            }

            case MotionEvent.ACTION_UP:{
                int scrollX = getScrollX();
                /**
                 * 表示计算速度，比如：时间间隔为1000 ms ，在1秒内，
                 * 手指在水平方向从左向右滑过100像素，那么水平速度就是100;
                 * 计算速度+获取速度----三步曲
                 * mVelocityTracker.computeCurrentVelocity(1000);
                 float xVelocity = mVelocityTracker.getXVelocity(); //获取水平方向的滑动速度
                 * float yVelocity = mVelocityTracker.getYVelocity();//获取垂直方向的滑动速度
                 * 由于我们需要的是xVelocity，
                 * 这里只是提一下，不计入代码;
                 * 注意：这里的速度指的是一段时间内手指所滑过的像素数！像素数！像素数！重要事说3遍；
                 */

                mVelocityTracker.computeCurrentVelocity(1000);
                float xVelocity = mVelocityTracker.getXVelocity(); //获取水平方向的滑动速度
                /**
                 *当你滑动手机相册中的照片的时候有没有发现，必须滑动到一定距离它才会切到下张图片，
                 * 否则，它就回退回原来的照片了，原来，它是通过“速度”来进行控制的~
                 * 还有就是"速度“可以为负值,很好理解，就像我们规定车前进的方向为正，反向为负；
                 *
                 */

                if (Math.abs(xVelocity) >= 50) {
                    mChildIndex = xVelocity > 0 ? mChildIndex - 1 : mChildIndex + 1;
                } else {
                    mChildIndex = (scrollX + mChildWidth / 2) / mChildWidth;

                }
                mChildIndex = Math.max(0, Math.min(mChildIndex, mChildSize - 1));
                int dx = mChildIndex * mChildIndex - scrollX;//缓慢地滑动到目标的x坐标；
                smoothScrollBy(dx, 0);
                mVelocityTracker.clear();//对速度跟踪进行回收
                break;
            }


            default: {
                break;
            }
        }

        mLastX  = x ;
        mLastY  = y ;
        return true;
    }

    /**
     * view测量原理：
     *      主要是MeasureSpace代表一个int 32位的值，高俩位分别为spaceMode（即测量模式），spaceSize（即测量大小）
     *      那3中测试模式这里就赘述了，自己搜一下，主要针对说下宽高属性为wrap_content的情况,加入任一属性为wrap_content时，
     *      高（宽）需要在onMeasure（）方法中做特殊处理，不复杂，就是给一个默认值比如：200dp ，我在网上看到很多人都喜欢使用这个数字，
     *      不知道为什么？若二者都为wrap_content ，简单那就在OnMeasure（）中都做处理给个默认值呗，就这么简单！
     *
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //定义俩个保存子Viw的测量宽跟高的变量measureWidth和measureHeight
        int measureChildWidth = 0;
        int measureChildheight = 0;
        //获取子view的个数
        final int childCount = getChildCount();
        //测量ziView的宽高;
        measureChildren(widthMeasureSpec ,heightMeasureSpec);

        //下来就是套路了，确定测量的模式跟大小

        int widthSpaceMode = MeasureSpec.getMode(widthMeasureSpec);
        int widSpaceSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpaceMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpaceSize = MeasureSpec.getSize(heightMeasureSpec);

        //下来就是写自己的逻辑判断了，这里必须感谢下大牛----coder任玉刚 ,解决了我许久的困惑，
        // 比如：为什么自定义view时宽高设置为warp_content时，不重写OnMeasure()方法，View的效果等同于math_parent?

        //先来判断下有没有子元素，没有就不用测了直接置0
        if (childCount == 0){
            setMeasuredDimension(0,0);
        }else if ( (widthSpaceMode == MeasureSpec.AT_MOST) && (heightSpaceMode == MeasureSpec.AT_MOST) ){

            //还记得我们自定义View的时候的处理规范吗------setMeasuredDimension(200 , 200);

            //获取第一个子View的对象
            final View childView = getChildAt(0);
            measureChildWidth = childView.getMeasuredWidth() * childCount;
            measureChildheight = childView.getMeasuredHeight() * childCount;
            setMeasuredDimension(measureChildWidth , measureChildheight);

        }else if ( widthSpaceMode == MeasureSpec.AT_MOST ){

            //当宽属性为wrap_content时，需要所有子View的宽之和（记得我们是水平的呀）
            final View childView = getChildAt(0);
            measureChildWidth = childView.getMeasuredWidth() * childCount;
            setMeasuredDimension(measureChildWidth , heightSpaceSize);

        }else  if ( heightMeasureSpec == MeasureSpec.AT_MOST ){

            //注意：当高属性为wrap_content时，仅仅需要任一子View的高即可（记得我们是左右滑动的，高是不变得）
            final View childView = getChildAt(0);
            measureChildheight = childView.getMeasuredHeight() ;
            setMeasuredDimension(widSpaceSize , measureChildheight);
        }
    }

    /**
     * onLayout（）过程主要用于确定view在ViewGroup中的摆放位置，通过确定View的L , T,R ,B四个坐标点;
     * @param b
     * @param i
     * @param i1
     * @param i2
     * @param i3
     */
    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

        int childLeft = 0;
        final int childCount = getChildCount();
        //mChildrenSize = childCount;// 这里没搞懂为什么要把childCount赋值给mChildrenSize ?

        //接下来就是你熟悉的套路了，遍历每个子View并获取它们的位置， 从左向右
        for ( int n = 0 ; n < childCount ; n++){
            final  View childView = getChildAt(n);
            //View可见
            if ( (childView.getVisibility()) != View.GONE){
                final int childWidth = childView.getMeasuredWidth();
                mChildWidth = childWidth;
                childView.layout(childLeft , 0 , childLeft + childWidth , childView.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }


    /**
     * 缓慢滑动到自定位置
     */
    private void smoothScrollBy(int dx, int dy){

        //500ms内滑向dx , 效果就是慢慢地滑动
        mScroller.startScroll(getScrollX() , 0 , dx , 0 , 500 );
        invalidate();
    }

    /**
     * 用于计算出当前滑动的X,Y坐标，即ScrollX ，跟 ScrollY
     */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()){
            scrollTo( mScroller.getCurrX() , mScroller.getCurrY() );
            postInvalidate();
        }
    }


    /**
     * api原话：
     *将视图从窗体上分离的时候调用该方法。这时视图已经不具有可绘制部分。
     * 即我们已经没有需要绘制的View ，可以回收资源了;
     * 很好理解，你画完图了是不是会保存，然后退出软件;
     *
     */
    @Override
    protected void onDetachedFromWindow() {
        mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }
}
