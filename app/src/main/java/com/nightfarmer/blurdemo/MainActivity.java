package com.nightfarmer.blurdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    Float radius = 0.0025f;
    Float scale = 0.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final ImageView result = (ImageView) findViewById(R.id.result);


        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.aaa);

        SeekBar seekBar1 = (SeekBar) findViewById(R.id.seekBar1);
        Observable.create(new SeekBarOnChangeSubscribe(seekBar1))
                .onBackpressureBuffer()
                .observeOn(Schedulers.computation())
                .map(new Func1<Float, Bitmap>() {
                    @Override
                    public Bitmap call(Float aFloat) {
                        Bitmap bitmap1 = null;
                        try {
                            if (aFloat == 0) {
                                aFloat = 0.01f;
                            }
                            radius = 25f * aFloat / 100;
                            Log.i("yoo", "" + radius + "--" + scale);
                            bitmap1 = blurBitmap(MainActivity.this, bitmap, radius, scale);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return bitmap1;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap newImage) {
                        result.setImageBitmap(newImage);
                    }
                });

        SeekBar seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
        Observable.create(new SeekBarOnChangeSubscribe(seekBar2))
                .onBackpressureBuffer()
                .observeOn(Schedulers.computation())
                .map(new Func1<Float, Bitmap>() {
                    @Override
                    public Bitmap call(Float aFloat) {
                        Bitmap bitmap1 = null;
                        try {
                            scale = (100 - aFloat) / 100 * 0.4f + 0.1f;
                            Log.i("yoo", "" + radius + "--" + scale);

                            bitmap1 = blurBitmap(MainActivity.this, bitmap, radius, scale);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return bitmap1;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap newImage) {
                        result.setImageBitmap(newImage);
                    }
                });


//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                Log.i("xx", "" + i + "---" + b+"--"+25f*i/100);
//                try {
//                    Bitmap bitmap1 = blurBitmap(MainActivity.this, bitmap, 25f*i/100, 0.4f);
//                    result.setImageBitmap(bitmap1);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
    }

    public void hehe(View vvv) {
        View v = findViewById(R.id.main_content);
        ImageView result = (ImageView) findViewById(R.id.result);
        result.setImageBitmap(getBlurBitmap(v, this));
    }

    public static Bitmap getBlurBitmap(View rootView, Context context) {
        try {
//            Drawable drawable = context.getResources().getDrawable(context.getResources().getDrawable(R.drawable.aaa,null).aaa, null);
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.aaa);
            Bitmap bitmap1 = blurBitmap(context, bitmap, 25f, 0.2f);
            bitmap.recycle();
            return bitmap1;

//            if (rootView == null || context == null) {
//                return null;
//            }
//            rootView.setDrawingCacheEnabled(true);
//            Bitmap drawingCache = rootView.getDrawingCache();
//            Bitmap bgBitmap = Bitmap.createBitmap(drawingCache);
//            return blurBitmap(bgBitmap, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 处理bitmap为高斯模糊图片
     *
     * @param context 上下文
     * @param image   图片源
     * @param radius  模糊程度 0到25之间
     * @param scale   图片缩放比例, 该值越小越节省内存,模糊程度越敏感
     * @return 模糊的图片
     */
    public static Bitmap blurBitmap(Context context, Bitmap image, float radius, float scale) {

        // 计算图片缩小后的长宽
        int width = Math.round(image.getWidth() * scale);
        int height = Math.round(image.getHeight() * scale);

        // 将缩小后的图片做为预渲染的图片。
        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        // 创建一张渲染后的输出图片。
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        // 创建RenderScript内核对象
        RenderScript rs = RenderScript.create(context);
        // 创建一个模糊效果的RenderScript的工具对象
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间。
        // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去。
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

        // 设置渲染的模糊程度, 25f是最大模糊度
        blurScript.setRadius(radius);
        // 设置blurScript对象的输入内存
        blurScript.setInput(tmpIn);
        // 将输出数据保存到输出内存中
        blurScript.forEach(tmpOut);

        // 将数据填充到Allocation中
        tmpOut.copyTo(outputBitmap);

//        blurScript.destroy();

        return outputBitmap;
    }
}
