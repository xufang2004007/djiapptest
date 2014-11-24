package hkust.djitest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIDroneTypeDef.DJIDroneType;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef.CameraPreviewResolustionType;
import dji.sdk.interfaces.DJIGerneralListener;
import dji.sdk.api.Battery.DJIBatteryProperty;
import dji.sdk.interfaces.DJIBattryUpdateInfoCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;
import android.util.Log;
import android.os.Process;
import java.util.Timer;
import java.util.TimerTask;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private TextView mConnectStateTextView;
    private TextView mBatteryInfoTextView;
    private DjiGLSurfaceView mDjiGLSurfaceView;

    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack  = null;
    private DJIBattryUpdateInfoCallBack mBattryUpdateInfoCallBack = null;
    private Timer mTimer;
    private String BatteryInfoString = "";

    class Task extends TimerTask {
        //int times = 1;

        @Override
        public void run()
        {
            //Log.d(TAG ,"==========>Task Run In!");
            checkConnectState();
        }

    };

    private void checkConnectState(){

        MainActivity.this.runOnUiThread(new Runnable(){

            @Override
            public void run()
            {
                boolean bConnectState = DJIDrone.getDjiCamera().getCameraConnectIsOk();
                if(bConnectState){
                    mConnectStateTextView.setText(R.string.camera_connection_ok);
                    Log.e(TAG, "connection good ");

                }
                else{
                    mConnectStateTextView.setText(R.string.camera_connection_break);
                    Log.e(TAG, "wifi sb了");
                }
            }
        });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkConnection();
        onInitSDK();

        mDjiGLSurfaceView = (DjiGLSurfaceView)findViewById(R.id.DjiSurfaceView_battery_info);
        mBatteryInfoTextView = (TextView)findViewById(R.id.BatteryInfoTV);
        mConnectStateTextView = (TextView)findViewById(R.id.ConnectStateBatteryInfoTextView);

        mDjiGLSurfaceView.start();

        mDjiGLSurfaceView.setStreamType(CameraPreviewResolustionType.Resolution_Type_320x240_15fps);

        mReceivedVideoDataCallBack = new DJIReceivedVideoDataCallBack(){

            @Override
            public void onResult(byte[] videoBuffer, int size)
            {
                // TODO Auto-generated method stub
                Log.e(TAG, "xxxxxxxx");
                mDjiGLSurfaceView.setDataToDecoder(videoBuffer, size);
            }


        };

        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(mReceivedVideoDataCallBack);

        //Log.e(TAG,DJIDrone.getDjiCamera().getCameraVersion());


        mBattryUpdateInfoCallBack = new DJIBattryUpdateInfoCallBack(){

            @Override
            public void onResult(DJIBatteryProperty state) {
                // TODO Auto-generated method stub
                StringBuffer sb = new StringBuffer();
                sb.append(getString(R.string.battery_info)).append("\n");
                sb.append("designedVolume=").append(state.designedVolume).append("\n");
                sb.append("fullChargeVolume=").append(state.fullChargeVolume).append("\n");
                sb.append("currentElectricity=").append(state.currentElectricity).append("\n");
                sb.append("currentVoltage=").append(state.currentVoltage).append("\n");
                sb.append("currentCurrent=").append(state.currentCurrent).append("\n");
                sb.append("remainLifePercent=").append(state.remainLifePercent).append("\n");
                sb.append("remainPowerPercent=").append(state.remainPowerPercent).append("\n");
                sb.append("batteryTemperature=").append(state.batteryTemperature).append("\n");
                sb.append("dischargeCount=").append(state.dischargeCount);
                BatteryInfoString = sb.toString();
                Log.e(TAG, sb.toString());

                MainActivity.this.runOnUiThread(new Runnable(){

                    @Override
                    public void run()
                    {
                        mBatteryInfoTextView.setText(BatteryInfoString);
                    }
                });
            }

        };

        DJIDrone.getDjiBattery().setBattryUpdateInfoCallBack(mBattryUpdateInfoCallBack);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onInitSDK(){
        DJIDrone.initWithType(DJIDroneType.DJIDrone_Vision);
        DJIDrone.connectToDrone();
    }

    private void onUnInitSDK(){
        DJIDrone.disconnectToDrone();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        mTimer = new Timer();
        Task task = new Task();
        mTimer.schedule(task, 0, 500);

        DJIDrone.getDjiBattery().startUpdateTimer(2000);
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        if(mTimer!=null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }

        DJIDrone.getDjiBattery().stopUpdateTimer();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        onUnInitSDK();
        mDjiGLSurfaceView.destroy();
        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(null);
        super.onDestroy();
        Process.killProcess(Process.myPid());
    }

    private void checkConnection()
    {
        new Thread(){
            public void run() {
                try {
                    DJIDrone.checkPermission(getApplicationContext(), new DJIGerneralListener() {

                        @Override
                        public void onGetPermissionResult(int result) {
                            // TODO Auto-generated method stub
                            Log.e(TAG, "onGetPermissionResult = "+result);
                        }
                    });
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
