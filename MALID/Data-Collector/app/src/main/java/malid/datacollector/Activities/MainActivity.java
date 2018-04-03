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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import malid.datacollector.CounterService;
import malid.datacollector.Helpers.CustomBluetoothProfile;
import malid.datacollector.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;

    Button btnServer;
    RadioGroup rg1, rg2;
    TextView txtState, txtTimer, txtByte, txt_heart, serverView;
    HRThread hrthread = new HRThread();
    // GetCountThread getcountThread = new GetCountThread();
    Thread thread;
    String address = null;
    private CounterService binder;
    private boolean running = false;
////////////////////////////////////////
    private TextView tv,tv2;
    private SensorManager sm,sm2;
    private Sensor s,s2; //옥이 추가
    // ////////////////////////////////////
    Intent intent;
    int time=0;
    int prev_step=0, prev_distance=0, prev_cal=0;
    int curr_step=0, curr_distance=0, curr_cal=0;
    int Heart_rate=0, Label=-1;
    int nametag=-1;

    ArrayList<Integer> HR_list = new ArrayList();
    ArrayList<Float> XYZ_list = new ArrayList();
    ArrayList<Float> Gyro_list = new ArrayList<>();
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

        tv = (TextView)findViewById(R.id.orientview);
        tv2=(TextView)findViewById(R.id.accelview);

        // 센서객체를 얻어오기 위해서는 센서메니저를 통해서만 가능하다
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sm2=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        s = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION); // 방향센서
        s2=sm2.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    String getBoundedDevice() {   // MI Band 2 MAC address 자동등록
        Set<BluetoothDevice> boundedDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : boundedDevice) {
            if (bd.getName().contains("MI Band 2")) {
                address=bd.getAddress();
                startConnecting();
            }
        }
        if(address == null)
        {
            Toast.makeText(getApplicationContext(), "페어링 상태를 확인해주세요.", Toast.LENGTH_LONG).show();
        }
        return null;
    }
    void startConnecting() {    // Mi band 2 연결
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);

        Log.v("test", "Connecting to " + address);
        Log.v("test", "Device name " + bluetoothDevice.getName());

        bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);
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
        rg1=(RadioGroup)findViewById(R.id.RG1);
        rg2=(RadioGroup)findViewById(R.id.RG2);
        btnServer = (Button) findViewById(R.id.btnServer);
        txtState = (TextView) findViewById(R.id.txtState);
        txtTimer = (TextView) findViewById(R.id.txtTimer);
        txtByte = (TextView) findViewById(R.id.txtByte);
        txt_heart = (TextView)findViewById(R.id.txt_heart);
        serverView = (TextView) findViewById(R.id.serverView);
    }

    void initializeEvents() {
       rg1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(RadioGroup group, int checkedId) {
               if(checkedId==R.id.radioButton1) Label=0;
               else if(checkedId==R.id.radioButton2) Label=1;
               else if(checkedId==R.id.radioButton3) Label=2;
               else if(checkedId==R.id.radioButton4) Label=3;
               else if(checkedId==R.id.radioButton5) Label=4;
               else if(checkedId==R.id.radioButton6) Label=5;
               else if(checkedId==R.id.radioButton7) Label=6;
               else if(checkedId==R.id.radioButton8) Label=7;
               else if(checkedId==R.id.radioButton9) Label=8;
               else if(checkedId==R.id.radioButton10) Label=9;
               else Label=-1;
           }
       });
        rg2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedname) {
                if(checkedname==R.id.park)nametag=1;
                else if(checkedname==R.id.kim)nametag=2;
                else if(checkedname==R.id.ok)nametag=3;
                else if(checkedname==R.id.song)nametag=4;
                else nametag =-1;
            }
        });

        btnServer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(Label == -1)
                    Toast.makeText(getApplicationContext(),"운동종류를 선택하세요.", Toast.LENGTH_SHORT).show();
                else if(nametag == -1)
                    Toast.makeText(getApplicationContext(),"이름을 선택하세요.", Toast.LENGTH_SHORT).show();
                else if(running == false) {   // 서버전송 시작
                    prev_step=0;
                    prev_distance=0;
                    prev_cal=0;
                    curr_step=0;
                    curr_distance=0;
                    curr_cal=0;
                    Heart_rate = 0;
                    HR_list.clear();
                    XYZ_list.clear();
                    txtByte.setText("prev info : "+prev_step+"step "+prev_distance+"m "+prev_cal+"cal\ncurr info : " +curr_step+"step "
                                    +curr_distance+"m "+curr_cal+"cal\ntime : "+time+"s\n"+"Heart Rate : ");
                    txt_heart.setText("심박수 : ");
                    txtTimer.setText("0");

                    Toast.makeText(getApplicationContext(),"서버 전송을 시작합니다.", Toast.LENGTH_SHORT).show();
                    btnServer.setText("Stop");
                    getInformation();

                    // Intent intent = new Intent(MainActivity.this, MyCounterService.class);
                    //startService(intent);
                    // bindService(intent, connection, BIND_AUTO_CREATE);
                    running = true;
                    // new Thread(new GetCountThread()).start();
                    thread = new Thread(hrthread);
                    thread.start();
                }
                else if(running == true){      // 서버전송 종료
                    Toast.makeText(getApplicationContext(),"서버 전송을 중지합니다.", Toast.LENGTH_SHORT).show();
                    btnServer.setText("Start");
                    getInformation();

                    // unbindService(connection);
                    running = false;
                    thread.interrupt();
                }
                else
                    Toast.makeText(getApplicationContext(),"알 수 없는 오류 발생", Toast.LENGTH_SHORT).show();
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

    public class CountAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            txtTimer = (TextView) findViewById(R.id.txtTimer);
            time = 0;
        }

        @Override
        protected String doInBackground(String... urls) {
            while(running){
                try {
                    Thread.sleep(1000); // 1초 대기
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                time ++;    // time 1증가
                publishProgress();      // onProgressUpdate 호출
                if(time!=0 && time%5==0){
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.accumulate("Name", nametag);
                        jsonObject.accumulate("HeartRate", Heart_rate);
                        jsonObject.accumulate("XYZ_list", XYZ_list);
                        //jsonObject.accumulate("Gyro_list", Gyro_list);
                        jsonObject.accumulate("Label", Label);
                        HttpURLConnection con = null;
                        BufferedReader reader = null;

                        try{
                            //URL url = new URL("http://192.168.25.16:3000/users");
                            URL url = new URL(urls[0]);
                            con = (HttpURLConnection) url.openConnection();
                            con.setRequestMethod("POST");
                            con.setRequestProperty("Cache-Control", "no-cache");
                            con.setRequestProperty("Content-Type", "application/json");
                            con.setRequestProperty("Accept", "text/html");
                            con.setDoOutput(true);
                            con.setDoInput(true);
                            con.connect();

                            OutputStream outStream = con.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                            writer.write(jsonObject.toString());
                            writer.flush();
                            writer.close();

                            InputStream stream = con.getInputStream();

                            reader = new BufferedReader(new InputStreamReader(stream));

                            StringBuffer buffer = new StringBuffer();

                            String line = "";
                            while((line = reader.readLine()) != null){
                                buffer.append(line);
                            }
                            XYZ_list.clear();
                            Gyro_list.clear();
                            serverView.setText(buffer.toString());//서버로 부터 받은 문자 textView에 출력
                            Log.v("test", "receive data from server");
                        } catch (MalformedURLException e){
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if(con != null){
                                con.disconnect();
                            }
                            try {
                                if(reader != null){
                                    reader.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... params) {
            txtTimer.setText(time + "");
            txt_heart.setText("심박수 : " + Heart_rate);
            if(time!=0 && time%5==0) {
                HR_list.add(Heart_rate);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
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

                if(running == true){
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
            Log.v("test", "onCharacteristicChanged");
            byte[] data = characteristic.getValue();
            Log.v("test", Integer.toString(data.length));

            if(data.length == 2){
                Log.v("test", "HR : "+data[1]);
                //if(running) HR_list.add(data[1]&0xFF);
                if(Heart_rate == 0 && ((data[1]&0xFF)!=0)){ // 처음 심박수를 읽은 경우
                    Heart_rate = data[1]&0xFF;

                    CountAsyncTask myAsyncTask = new CountAsyncTask();  // count 스레드생성
                    myAsyncTask.execute("http://13.125.101.194:3000/post");  // count 스레드실행
                }
                else if((data[1]&0xFF)!=0){
                    Heart_rate = data[1]&0xFF;
                }

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
        sm2.registerListener(this,s2,SensorManager.SENSOR_DELAY_UI);
    }
    @Override
    protected void onPause() { // 화면을 빠져나가면 즉시 센서자원 반납해야함!!
        super.onPause();
        sm.unregisterListener(this); // 반납할 센서
        sm2.unregisterListener(this);
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
            /*if(Gyro_list.size() < 90){
                Log.d("gyro", Float.toString(event.values[0]));
                Gyro_list.add(event.values[0]);
                Gyro_list.add(event.values[1]);
                Gyro_list.add(event.values[2]);
            }*/
        }
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            String str = "가속센서값"
                    +"\nX : "+ event.values[0]
                    +"\nY : "+ event.values[1]
                    +"\nZ : "+ event.values[2];
            tv2.setText(str);
            if(XYZ_list.size()<150){
               XYZ_list.add(event.values[0]);
               XYZ_list.add(event.values[1]);
               XYZ_list.add(event.values[2]);
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 센서의 정확도가 변경되었을 때 호출되는 콜백 메서드
    }
}

