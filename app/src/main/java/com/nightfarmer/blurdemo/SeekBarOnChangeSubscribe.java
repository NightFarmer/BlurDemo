package com.nightfarmer.blurdemo;

import android.util.Log;
import android.widget.SeekBar;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

/**
 * Created by zhangfan on 16-9-9.
 */
public class SeekBarOnChangeSubscribe implements Observable.OnSubscribe<Float> {

    SeekBar seekBar;

    public SeekBarOnChangeSubscribe(SeekBar seekBar) {
        this.seekBar = seekBar;
    }

    @Override
    public void call(final Subscriber<? super Float> subscriber) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(i * 1f);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                Log.i("xx", "unsubscribe");
                seekBar.setOnSeekBarChangeListener(null);
            }
        });
    }

}
