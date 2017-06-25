package com.example.vangle.shake;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.widget.TextView;
import android.hardware.SensorManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity implements SensorEventListener{

    SensorManager mySensorManager;
    private Sensor mySensor;
    TextView tvCount;
    TextView tvValue;

    FileOutputStream outputStream;

    int ShakeCount;
    int lowcount;
    int timeout1;
    int timeout2;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCount = (TextView) findViewById(R.id.tvShakeCount);
        tvValue = (TextView) findViewById(R.id.tvAccelerometer);

        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取Sensor Manager

        mySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);//获取传感器

        //用于文件记录
        try {
            File f = new File("/sdcard/","sensorvalue.txt");
            if (f.exists()) {
                f.delete();
            }
            outputStream = new FileOutputStream(f);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ShakeCount = 0;
        lowcount = 0;
        timeout1 = 0;
        timeout2 = 0;


    }

    protected void onResume() {
        super.onResume();
        mySensorManager.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_GAME);//注册传感器，指定相应的参数
    }

    protected void onPause() {
        super.onPause();
        mySensorManager.unregisterListener(this);//注销传感器
    }

    protected void onStop()
    {
        mySensorManager.unregisterListener(this);//注销传感器

        try{
            outputStream.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        super.onStop();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //传感器精度发生变化时的处理
    }

    public void onSensorChanged(SensorEvent event) {
        //传感器数值发生变化时的处理
        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float xValue = event.values[0];
            float yValue = event.values[1];
            float zValue = event.values[2];
            tvValue.setText(String.format("x: %f", xValue) + "  " + String.format("y: %f", yValue) + "  " + String.format("z: %f", zValue));

            try {
                outputStream.write((String.valueOf(xValue)).getBytes());
                outputStream.write(" ".getBytes());
                outputStream.write((String.valueOf(yValue)).getBytes());
                outputStream.write(" ".getBytes());
                outputStream.write((String.valueOf(zValue)).getBytes());
                outputStream.write("\n".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

            double acc = Math.sqrt((double)(xValue*xValue+yValue*yValue));
            if(acc < 15 && timeout2 == 0 && timeout1 == 0)
            {
                lowcount = lowcount+1;
            }

            if(lowcount > 15 && timeout2 == 0 && timeout1 == 0)
            {
                if(acc > 15) {
                    timeout1 = 15;
                    lowcount = 0;
                }
            }

            if(timeout1 > 0)
            {
                if(acc < 15) {
                    timeout2 = 15;
                    timeout1 = 0;
                }
                else{
                    timeout1--;
                }
            }

            if(timeout2 > 0)
            {
                if(acc > 15){
                    ShakeCount++;
                    timeout2 = 0;
                }
                else{
                    timeout2--;
                }
            }

            tvCount.setText(String.format("Shake Count: %d", ShakeCount));

        }
    }


}
