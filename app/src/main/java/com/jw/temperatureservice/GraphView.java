package com.jw.temperatureservice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;


public class GraphView extends androidx.appcompat.widget.AppCompatImageView {

    private float a, b;
    public GraphView(Context context) {
        super(context);
        a = 25;
        b = 1.5f;
    }

    public GraphView(Context context, AttributeSet attrst) {
        super(context, attrst);
        a = 25;
        b = 1.5f;

    }

    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        a = 25;
        b = 1.5f;
    }

    public void setSubtrahend(float aSubtrahend) {
        a = aSubtrahend;
        this.postInvalidate();
    }

    public void setFactor(float aFactor) {
        b = aFactor;
        this.postInvalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
        Canvas tmp_canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        int h = tmp_canvas.getHeight();
        int w = tmp_canvas.getWidth();
        int top = 50;
        int bot = 250;
        int left = 50;
        int end =  250;
        //canvas.drawCircle(50, 50, 10, paint);
        tmp_canvas.drawLine(left, bot, left, top, paint);
        tmp_canvas.drawLine(left, bot, end, bot, paint);
        tmp_canvas.drawText("y = (x - a) * b", 50, 10, paint);
        tmp_canvas.drawText("duty cycle [%]", 50, 40, paint);
        tmp_canvas.drawText("temp [°c]", 220, 240, paint);

        //float a = 35;
        //float b = 1.3f;
        float x1 = 0;
        float y1 = (x1 - a) * b;
        float x2 = 100;
        float y2 = (x2 - a) * b;
        tmp_canvas.drawLine( x1 * 2 + 50 ,250 -y1 * 2, x2 * 2 + 50, 250 - y2 * 2, paint);

        paint.setColor(Color.BLUE);
        x1 = 40;
        y1 = (x1 - a) * b;
        tmp_canvas.drawLine(x1 * 2 + 50, 250 - y1 * 2 , 50, 250 - y1 * 2, paint);
        tmp_canvas.drawLine(x1 * 2 + 50, 250 - y1 * 2 , x1 * 2 + 50, 250, paint);

        x2 = 75;
        y2 = (x2 - a) * b;
        tmp_canvas.drawLine(x2 * 2 + 50, 250 - y2 * 2 , 50, 250 - y2 * 2, paint);
        tmp_canvas.drawLine(x2 * 2 + 50, 250 - y2 * 2 , x2 * 2 + 50, 250, paint);

        paint.setColor(Color.BLACK);//String.format("Value of a: %.2f", a)
        tmp_canvas.drawText(String.format("%.2f", y1), 18, 250 - y1 * 2, paint);
        tmp_canvas.drawText(String.format("%.2f", y2), 18, 250 - y2 * 2, paint);

        tmp_canvas.drawText(String.valueOf(x1), x1 * 2 + 50, 270, paint);
        tmp_canvas.drawText(String.valueOf(x2), x2 * 2 + 50, 270, paint);

        canvas.drawBitmap(bitmap, 0f, 100f, paint);
        super.onDraw(canvas);
        this.postInvalidate();
    }
}
