package malid.datacollector.Activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import malid.datacollector.CounterService;
import malid.datacollector.Helpers.CustomBluetoothProfile;
import malid.datacollector.MyCounterService;
import malid.datacollector.R;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;

    Button btnStartConnecting, btnServer;
    EditText txtPhysicalAddress;
    TextView txtState, txtTimer, txtByte;
    HRThread hrthread = new HRThread();
    Thread thread;

    private CounterService binder;
    private boolean running = true;
////////////////////////////////////////
    private TextView tv;
    private SensorManager sm;
    private Sensor s; //옥이 추가
    // ////////////////////////////////////
    boolean startState = false;
    int time=0;
    int prev_step=0, prev_distance=0, prev_cal=0;
    int curr_step=0, curr_distance=0, curr_cal=0;
    ArrayList<Integer> HR_list = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int uiOptions = this.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        boolean isImmersiveModeEnabled = ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i("mode", "Turning immersive mode mode off.");
        } else {
            Log.i("mode", "Turning immersive mode mode on.");
        }
        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        this.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

        initializeObjects();
        initilaizeComponents();
        initializeEvents();

        getBoundedDevice();

        tv = (TextView)findViewById(R.id.textView1);

        // 센서객체를 얻어오기 위해서는 센서메니저를 통해서만 가능하다
        sm = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        s = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION); // 방향센서
    }

    void getBoundedDevice() {   // MI Band 2 MAC address 자동등록
        Set<BluetoothDevice> boundedDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : boundedDevice) {
            if (bd.getName().contains("MI Band 2")) {
                txtPhysicalAddress.setText(bd.getAddress());
            }
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 서비스가 가진 binder를 리턴 받음
            binder = CounterService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    void initializeObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    void initilaizeComponents() {
        btnStartConnecting = (Button) findViewById(R.id.btnStartConnecting);
        btnServer = (Button) findViewById(R.id.btnServer);
        txtPhysicalAddress = (EditText) findViewById(R.id.txtPhysicalAddress);
        txtState = (TextView) findViewById(R.id.txtState);
        txtTimer = (TextView) findViewById(R.id.txtTimer);
        txtByte = (TextView) findViewById(R.id.txtByte);
    }

    void initializeEvents() {
        btnStartConnecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnecting();
            }
        });
        btnServer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(startState == false) {   // 서버전송 시작
                    prev_step=0;
                    prev_distance=0;
                    prev_cal=0;
                    curr_step=0;
                    curr_distance=0;
                    curr_cal=0;
                    HR_list.clear();
                    txtByte.setText("...");
                    txtTimer.setText("0");

                    startState = true;
                    Toast.makeText(getApplicationContext(),"서버 전송을 시작합니다.", Toast.LENGTH_SHORT).show();
                    btnServer.setText("Stop");
                    getInformation();

                    Intent intent = new Intent(MainActivity.this, MyCounterService.class);
                    //startService(intent);
                    bindService(intent, connection, BIND_AUTO_CREATE);
                    running = true;
                    new Thread(new GetCountThread()).start();
                    thread = new Thread(hrthread);
                    thread.start();
                }
                else {      // 서버전송 종료
                    startState = false;
                    Toast.makeText(getApplicationContext(),"서버 전송을 중지합니다.", Toast.LENGTH_SHORT).show();
                    btnServer.setText("Start");
                    getInformation();

                    unbindService(connection);
                    running = false;
                    thread.interrupt();
                }

                return true;
            }
        });
    }

    void sendServer() {

        txtByte.setText("prev info : "+prev_step+"step "+prev_distance+"m "+prev_cal+"cal\ncurr info : " +curr_step+"step "
                +curr_distance+"m "+curr_cal+"cal\ntime : "+time+"s\n"+"Heart Rate : "+HR_list.toString());
    }

    void getInformation() { // 걸음수, 거리, 칼로리 정보
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.Information.service)
                .getCharacteristic(CustomBluetoothProfile.Information.Characteristic);
        if (!bluetoothGatt.readCharacteristic(bchar)) {
            Toast.makeText(this, "Failed get information info", Toast.LENGTH_SHORT).show();
        }
    }

    private class GetCountThread implements Runnable {
        private Handler handler = new Handler();

        @Override
        public void run() {

            while(running) {
                if ( binder == null ) {
                    continue;
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {   // textview 갱신
                            txtTimer.setText(binder.getCount() + "");
                            time = binder.getCount();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class HRThread implements Runnable {

        @Override
        public void run() {

            try{
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                while(running){
                    try {
                        startScanHeartRate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch(Exception e) {
            } finally {
                Log.v("test", "HR-Thread is dead");
            }

        }
    }

    void startConnecting() {    // Mi band 2 연결

        String address = txtPhysicalAddress.getText().toString();
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);

        Log.v("test", "Connecting to " + address);
        Log.v("test", "Device name " + bluetoothDevice.getName());

        bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);
    }


    void stateConnected() {
        bluetoothGatt.discoverServices();
        txtState.setText("Connected");
    }

    void stateDisconnected() {
        bluetoothGatt.disconnect();
        txtState.setText("Disconnected");
    }

    void startScanHeartRate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.controlCharacteristic);
        bchar.setValue(new byte[]{21, 1, 1});       // 대략 10번 측정
        // new byte[]{21, 2, 1} -> 1번 측정
        bluetoothGatt.writeCharacteristic(bchar);
    }

    void listenHeartRate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.measurementCharacteristic);
        bluetoothGatt.setCharacteristicNotification(bchar, true);
        BluetoothGattDescriptor descriptor = bchar.getDescriptor(CustomBluetoothProfile.HeartRate.descriptor);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
    }




    final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.v("test", "onConnectionStateChange");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                stateConnected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                stateDisconnected();
            }

        }

        @Override   // mGatt.discoverServices(); 사용시 호출
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.v("test", "onServicesDiscovered");
            listenHeartRate();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.v("test", "onCharacteristicRead");
            byte[] data = characteristic.getValue();

            if(data.length == 13) {
                int new_step = (data[4]&0xFF) << 24 | (data[3] & 0xFF) << 16 | (data[2] & 0xFF) << 8 | (data[1] & 0xFF);
                int new_distance = (data[8]&0xFF) << 24 | (data[7] & 0xFF) << 16 | (data[6] & 0xFF) << 8 | (data[5] & 0xFF);
                int new_cal = (data[12]&0xFF) << 24 | (data[11] & 0xFF) << 16 | (data[10] & 0xFF) << 8 | (data[9] & 0xFF);

                if(startState == true){
                    prev_step = new_step;
                    prev_distance = new_distance;
                    prev_cal = new_cal;
                }
                else{
                    curr_step = new_step;
                    curr_distance = new_distance;
                    curr_cal = new_cal;
                    sendServer();
                }

                Log.v("test", "step : "+new_step);
                Log.v("test", "distance : "+new_distance+"m");
                Log.v("test", "cal : "+new_cal);

                //txtByte.setText("step : " +step+"\n" + "distance : " + distance + "m\n"+"cal : " + cal);
            }


        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.v("test", "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //Log.v("test", "onCharacteristicChanged");
            byte[] data = characteristic.getValue();
            if(data.length == 2){
                Log.v("test", "HR : "+data[1]);
                if(running) HR_list.add(data[1]&0xFF);
            }
            //txtByte.setText(Arrays.toString(data));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.v("test", "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.v("test", "onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.v("test", "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.v("test", "onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.v("test", "onMtuChanged");
        }

    };
    @Override
    protected void onResume() { // 화면에 보이기 직전에 센서자원 획득
        super.onResume();
        // 센서의 값이 변경되었을 때 콜백 받기위한 리스너를 등록한다
        sm.registerListener(this,        // 콜백 받을 리스너
                s,            // 콜백 원하는 센서
                SensorManager.SENSOR_DELAY_UI); // 지연시간
    }
    @Override
    protected void onPause() { // 화면을 빠져나가면 즉시 센서자원 반납해야함!!
        super.onPause();
        sm.unregisterListener(this); // 반납할 센서
    }

    public void onSensorChanged(SensorEvent event) {
        // 센서값이 변경되었을 때 호출되는 콜백 메서드
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            // 방향센서값이 변경된거라면
            String str = "방향센서값"
                    +"\n방위각: "+event.values[0]
                    +"\n피치 : "+event.values[1]
                    +"\n롤 : "+event.values[2];
            tv.setText(str);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 센서의 정확도가 변경되었을 때 호출되는 콜백 메서드
    }

}

