package com.hzx.view_lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class RotatingClockView extends View {

    private int defaultWidth = 100;
    private int defaultHeight = 100;

    private Paint auxPaint;
    private Paint textPaint;

    private Context mContext;


    private ValueAnimator valueAnimator;

    /**
     * 小时距离分的距离
     */
    private int hourTextOffer = 32;

    /**
     * 每次画布旋转的角度，以便时钟旋转
     */
    private float rotatAngle = 0;

    /**
     * 分钟累计，60清零
     */
    private int min = 0;
    /**
     * 小时累计，12清零
     */
    private int hour = 0;

    /**
     * 是否开启
     */
    private boolean isStart = false;

    /**
     * 秒最大文字偏移
     */
    private float millMaxOffset = 0;
    /**
     * 默认计算最大秒偏移文字
     */
    private String millMeasureTextDaflut = "00秒";
    /**
     * 分最大文字偏移
     */
    private float minMaxOffset = 0;
    /**
     * 默认计算最大分偏移文字
     */
    private String minMeasureTextDaflut = "00分";

    public RotatingClockView(Context context) {
        super(context);
        init(context);
    }

    public RotatingClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RotatingClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureFinalWidth(widthMeasureSpec), measureFinalHeight(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        // 绘制颜色
        canvas.drawColor(Color.BLACK);

        // 圆心辅助点
        canvas.drawPoint(width / 2, height / 2, auxPaint);

        auxPaint.setStrokeWidth(4);
        auxPaint.setColor(Color.GREEN);
        canvas.drawLine(width/2,height/2,width,height/2,auxPaint);
        // 绘制秒
        canvas.save();
        canvas.rotate(-rotatAngle,width / 2, height / 2);
        for (int i = 0; i <= 59; i++) {
            if (i != 0) {
                canvas.rotate(360 / 60, width / 2, height / 2);
            }
            drawMill(canvas, width, height, i);
        }
        canvas.restore();

        // 绘制分
        canvas.save();
        float minAngle = (min+rotatAngle/360f)/60f*360f;
        canvas.rotate(-minAngle,width / 2, height / 2);
        Log.e("sun","分偏移"+((min+rotatAngle/360f)/60f*360f));
        for (int i = 0; i <= 59; i++) {
            if (i != 0) {
                canvas.rotate(360 / 60, width / 2, height / 2);
            }
            drawMin(canvas, width, height, i,millMaxOffset);
        }
        canvas.restore();


        // 绘制小时
        canvas.save();
        canvas.rotate(-((hour+minAngle/360f)/12f*360f),width / 2, height / 2);
        for (int i = 0; i <= 59; i++) {
            if (i != 0) {
                canvas.rotate(360 / 60, width / 2, height / 2);
            }
            if (i % 5 == 0) {
                drawHour(canvas, width, height, i / 5, millMaxOffset + minMaxOffset + dpTopx(hourTextOffer));
            }
        }
        canvas.restore();
        if (!isStart) {
            start();
        }
    }

    /**
     * 使用属性动画进行循环进行改动
     */
    public void start() {
        isStart = true;
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f,360f);
        valueAnimator.setDuration(1000 * 6);
        // 设置动画为循环
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        // 设置线性插值器
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                rotatAngle = value;
                // 请求重绘
                invalidate();
            }
        });
        // 监听循环次数
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                min++;
                if (min==60){
                    min=0;
                    hour++;
                }
                if (hour == 12){
                    hour = 0;
                }
            }
        });
        valueAnimator.start();
    }

    private void drawMill(Canvas canvas, int width, int height, int mill) {
        // 辅助线
        auxPaint.setStrokeWidth(4);
        auxPaint.setColor(Color.GREEN);
//        canvas.drawLine(width/2,height/2,width,height/2,auxPaint);
        // 测试秒文字和绘制的文字分开，避免错位
        String drawText = mill + "秒";
        // 获取文字宽度
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        Rect textBounds = new Rect();
        textPaint.getTextBounds(millMeasureTextDaflut, 0, millMeasureTextDaflut.length(), textBounds);
        // 计算文字绘制几点 x,y 坐标
        float textDrawX = width - millMaxOffset;
        float textDrawY = height / 2 + (Math.abs(fontMetrics.ascent) - Math.abs(fontMetrics.descent)) / 2;
        // 绘制辅助点
        canvas.drawPoint(textDrawX, textDrawY, auxPaint);
//        Log.e("sun","坐标"+textDrawX+"-"+textDrawY);
//        Log.e("sun","坐标"+width/2+"-"+width/2);
        // 绘制文字
        canvas.drawText(drawText, textDrawX, textDrawY, textPaint);
    }

    private void drawMin(Canvas canvas, int width, int height, int min, float offsetX) {
        String testString = min + "分";
        // 获取文字宽度
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        Rect textBounds = new Rect();
        textPaint.getTextBounds(testString, 0, testString.length(), textBounds);
        // 计算文字绘制几点 x,y 坐标
        float textDrawX = width - minMaxOffset - offsetX;
        float textDrawY = height / 2 + (Math.abs(fontMetrics.ascent) - Math.abs(fontMetrics.descent)) / 2;
        // 绘制辅助点
        canvas.drawPoint(textDrawX, textDrawY, auxPaint);
        // 绘制文字
        canvas.drawText(testString, textDrawX, textDrawY, textPaint);
    }

    private void drawHour(Canvas canvas, int width, int height, int min, float offsetX) {
        // 辅助线
        auxPaint.setStrokeWidth(4);
        auxPaint.setColor(Color.GREEN);
//        canvas.drawLine(width/2,height/2,width,height/2,auxPaint);
        // 测试秒文字
        String testString = min + "时";
        // 获取文字宽度
        float textWidth = textPaint.measureText(testString);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        Rect textBounds = new Rect();
        textPaint.getTextBounds(testString, 0, testString.length(), textBounds);
        // 计算文字绘制几点 x,y 坐标
        float textDrawX = width - textWidth - offsetX;
        float textDrawY = height / 2 + (Math.abs(fontMetrics.ascent) - Math.abs(fontMetrics.descent)) / 2;
        // 绘制辅助点
        canvas.drawPoint(textDrawX, textDrawY, auxPaint);
//        Log.e("sun","坐标"+textDrawX+"-"+textDrawY);
//        Log.e("sun","坐标"+width/2+"-"+width/2);
        // 绘制文字
        canvas.drawText(testString, textDrawX, textDrawY, textPaint);
    }

    private void init(Context context) {
        mContext = context;

        auxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        auxPaint.setColor(Color.RED);
        auxPaint.setStrokeWidth(10);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(spTopx(14));
        textPaint.setColor(Color.parseColor("#bdbdbd"));

        millMaxOffset = measureTextWidth(millMeasureTextDaflut);

        minMaxOffset = measureTextWidth(minMeasureTextDaflut);
    }

    private float measureTextWidth(String text){
        return textPaint.measureText(text);
    }

    private int measureFinalHeight(int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int finalHeight = height;
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED:
                // 想多大多大
                break;
            case MeasureSpec.AT_MOST:
                // 最大不能超过父级的大小 对于 warp_content
                finalHeight = dpTopx(defaultWidth) + getPaddingTop() + getPaddingBottom();
                break;
            case MeasureSpec.EXACTLY:
                // 精确指定了大小
                break;
        }
        return finalHeight;
    }

    private int measureFinalWidth(int widthMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int finalWidth = width;
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED:
                // 想多大多大
                break;
            case MeasureSpec.AT_MOST:
                // 最大不能超过父级的大小 对于 warp_content
                finalWidth = dpTopx(defaultWidth) + getPaddingLeft() + getPaddingRight();
                break;
            case MeasureSpec.EXACTLY:
                // 精确指定了大小
                break;
        }
        return finalWidth;
    }


    private int dpTopx(int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        return (int) (dp * displayMetrics.density);
    }

    private int spTopx(int sp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        return (int) (sp * displayMetrics.scaledDensity);
    }
}
