package com.cryptandroid.max.drivercar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by MAX on 11.08.2018.
 */

public class ControllerWindow extends Fragment implements SeekBar.OnSeekBarChangeListener, Button.OnTouchListener {
   private TextView spedValue;
   private SeekBar speedBar;
   private SeekBar angleBar;
   private Button go;
   private Button back;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.controls, null);
        spedValue = (TextView)view.findViewById(R.id.speed_value);
        speedBar = (SeekBar)view.findViewById(R.id.speedBar);
        angleBar = (SeekBar)view.findViewById(R.id.angleBar);
        speedBar.setOnSeekBarChangeListener(this);
        angleBar.setOnSeekBarChangeListener(this);
        go = (Button)view.findViewById(R.id.go_btn);
        go.setOnTouchListener(this);
        back = (Button)view.findViewById(R.id.back_btn);
        back.setOnTouchListener(this);
        return view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()) {
            case R.id.angleBar:
                sendData(new byte[] {2, (byte)(10*seekBar.getProgress())});
                break;

            case R.id.speedBar:
                spedValue.setText("" + i);

                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.angleBar:
                seekBar.setProgress(5);
                break;
        }
    }

    private void sendData(byte[] send) {
        BlueToothController.sendData(this, send);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            sendData(new byte[] {3, 0});
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            byte mode = 0;

            switch (view.getId()) {
                case R.id.go_btn:
                    mode = 0;
                    break;

                case R.id.back_btn:
                    mode = 1;
                    break;
            }

            sendData(new byte[] {mode, (byte)speedBar.getProgress()});
        }


        return false;
    }
}
