package malid.datacollector.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.ArrayList;
import java.util.List;

import malid.datacollector.Helpers.historyitem;
import malid.datacollector.Helpers.historyitemadapter;
import malid.datacollector.R;

public class HistoryActivity extends AppCompatActivity {

    String history_url = "http://13.125.101.194:3000/history";
    String ID = null;
    TabHost tabHost1;
    TabHost.TabSpec ts1, ts2, ts3;
    ArrayList<String> class_name;
    ArrayList<Integer> count, hr;

    String Date = null;
    PieChart pieChart;
    LineChart hrChart;
    int sum;

    ///////////////
    private RecyclerView lecyclerView;
    List<historyitem> albumList;
    historyitemadapter realadapter;
    LinearLayoutManager realmanager;
    int insertindex=-1;
    Button searchbutton;
    public boolean running = false;
    int time=0;
    int ccount=99999999;
    DatePicker mDate;
    TextView forsetmessage;
    ////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historyview);
        Intent intent = getIntent();
        ID = intent.getStringExtra("ID");
        sum = 0;
        init_Tap();
        new historyTask().execute(history_url);

        albumList=new ArrayList<historyitem>();
        initLayout();
        mDate = (DatePicker)findViewById(R.id.datepickdaily);
        searchbutton=findViewById(R.id.searchbutton);
        searchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout viewview = (LinearLayout)findViewById(R.id.progrssbarinHistoryActivity);
                RecyclerView viewviewview = (RecyclerView)findViewById(R.id.HistoryActivityRecycle);
                viewview.setVisibility(viewview.VISIBLE);
                viewviewview.setVisibility(viewviewview.GONE);
                albumList.clear();
                insertindex=-1;
                Date = String.valueOf(mDate.getYear()) +String.valueOf(mDate.getMonth()+1) + String.valueOf(mDate.getDayOfMonth());
                forsetmessage= findViewById(R.id.progrssbarmessageinhistoryview);
                forsetmessage.setText("서버로부터\n" + mDate.getYear() + "년 " + (mDate.getMonth()+1) +"월 " + mDate.getDayOfMonth()
                        + "일\n에 대한 데이터를 요청하고 있습니다.\n잠시만 기다려 주세요");
                forsetmessage=findViewById(R.id.dailyhistorydateshow);
                forsetmessage.setText("측정 날짜 : " + mDate.getYear() + "년 " + (mDate.getMonth()+1) +"월 " + mDate.getDayOfMonth()
                        + "일");
                new recyclehistoryTask().execute("http://13.125.101.194:3000/historyview");
            }
        });

    }
    public void initLayout(){

        lecyclerView = (RecyclerView)findViewById(R.id.HistoryActivityRecycle);
        lecyclerView.setAdapter(realadapter=new historyitemadapter(albumList,R.layout.historyitem));
        realmanager = new LinearLayoutManager(this);
        lecyclerView.setLayoutManager(realmanager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(lecyclerView.getContext(),
                realmanager.getOrientation());
        lecyclerView.addItemDecoration(dividerItemDecoration);
        lecyclerView.setItemAnimator(new DefaultItemAnimator());
        realadapter.notifyDataSetChanged();
    }

    private void setData(String idx,String time, String hr, String exercise){

        historyitem album = new historyitem();
        album.setidx(idx);
        album.setTime(time);
        album.setClasss(exercise);
        album.setHeartrate(hr);
        insertindex++;
        albumList.add(insertindex,album);
            /*new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                }
            },1000);*/
        //realadapter.notifyItemInserted(insertindex);
        //lecyclerView.invalidate();
        //lecyclerView.requestLayout();
        //lecyclerView.scrollToPosition(albumList.size() -1);

    }

    void init_Tap(){
        tabHost1 = (TabHost)findViewById(R.id.tabHost1);
        tabHost1.setup();
        ts3 = tabHost1.newTabSpec("all") ;
        ts3.setContent(R.id.all) ;
        ts3.setIndicator("전체") ;
        tabHost1.addTab(ts3) ;
        ts1 = tabHost1.newTabSpec("daily") ;
        ts1.setContent(R.id.daily) ;
        ts1.setIndicator("일일") ;
        tabHost1.addTab(ts1) ;
        ts2 = tabHost1.newTabSpec("weekly") ;
        ts2.setContent(R.id.weekly) ;
        ts2.setIndicator("주간") ;
        tabHost1.addTab(ts2) ;

    }

    void make_Chart(){
        pieChart = (PieChart)findViewById(R.id.piechart);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(true);
        pieChart.setExtraOffsets(5,10,5,5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setCenterText("운동");
        pieChart.setCenterTextSize(35f);
        ArrayList<PieEntry> Values = new ArrayList<PieEntry>();
        for(int i = 0; i < class_name.size();i++)
            Values.add(new PieEntry((float)count.get(i)/sum, class_name.get(i)));
        Description description = new Description();
        description.setText("운동 비율"); //라벨
        description.setTextSize(15);
        pieChart.setDescription(description);
        pieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic); //애니메이션
        PieDataSet dataSet = new PieDataSet(Values,"운동");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData data = new PieData(dataSet);
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.WHITE);
        pieChart.setData(data);

        hrChart = (LineChart)findViewById(R.id.hrchart);

        ArrayList<Entry> entries = new ArrayList<>();
        for(int i = 0;i<hr.size();i++) {
            entries.add(new Entry(i, hr.get(i)));
        }

        LineDataSet dataset = new LineDataSet(entries, "심박수");
        LineData hrdata = new LineData(dataset);
        dataset.setDrawCircles(false);
        dataset.setColor(Color.RED);

        hrChart.setData(hrdata);
        hrChart.animateY(5000);

    }

    public class historyTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            JSONObject jsonObject = new JSONObject();
            HttpURLConnection con = null;
            BufferedReader reader = null;
            try {
                jsonObject.accumulate("ID", ID);
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
                return buffer.toString();

            }catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }finally {
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
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result == null){
                Toast.makeText(getApplicationContext(), "데이터를 받아올 수 없습니다", Toast.LENGTH_LONG).show();
            }
            else {
                try {
                    class_name = new ArrayList<>();
                    count = new ArrayList<>();
                    JSONArray jarray = new JSONArray(result);
                    for(int i=jarray.length()-1; i>=0; i--){
                        JSONObject json_Object = jarray.getJSONObject(i);
                        if(json_Object.optString("class").equals("0"))class_name.add("정지");
                        else if(json_Object.optString("class").equals("1"))class_name.add("걷기");
                        else if(json_Object.optString("class").equals("2"))class_name.add("달리기");
                        else if(json_Object.optString("class").equals("3"))class_name.add("아령");
                        count.add(json_Object.optInt("count"));
                        sum += json_Object.optInt("count");
                    }
                    result = result.substring(result.indexOf("]")+1);
                    jarray = new JSONArray(result);
                    JSONObject jsonObject = jarray.getJSONObject(0);
                    String str = jsonObject.optString("HR");
                    hr = new ArrayList();
                    String temp;
                    int t;
                    while(str.contains(",")) {
                        temp = str.substring(0, str.indexOf(", "));
                        str = str.substring(str.indexOf(", ")+2);
                        Log.v("temp", Integer.toString(temp.length()));
                        Log.v("str", str);
                        t=Integer.parseInt(temp);
                        hr.add(t);
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
                make_Chart();
            }
        }
    }
    public class recyclehistoryTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("ID", ID);
                jsonObject.accumulate("count", ccount);
                jsonObject.accumulate("Date",Date);

                HttpURLConnection con = null;
                BufferedReader reader = null;

                if(running == false){
                    //Thread.sleep(1000); // 3초 대기
                }
                try{
                    URL url = new URL(urls[0]);

                    con = (HttpURLConnection) url.openConnection();
                    Log.v("test", "openConnection");
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

                    return buffer.toString();

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

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.v("test", "onPostExcute");
            if(running == false) searchbutton.setEnabled(true);
            if(result == null){
                Toast.makeText(getApplicationContext(), "서버와 연결에 실패하였습니다.", Toast.LENGTH_LONG).show();
            }
            else {
                Log.v("test", "DB result : "+result);
                try {
                    JSONArray jarray = new JSONArray(result);
                    if(jarray.length()==0){
                        Toast.makeText(getApplicationContext(), "해당 날짜에 대한 데이터가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
                    }
                    for(int i=jarray.length()-1; i>=0; i--){
                        JSONObject jobject = jarray.getJSONObject(i);
                        String Class_name = "";
                        if(jobject.optString("class").equals("0")) Class_name = "정지";
                        else if(jobject.optString("class").equals("1")) Class_name = "걷기";
                        else if(jobject.optString("class").equals("2")) Class_name = "달리기";
                        setData(String.valueOf(jarray.length()-i),jobject.optString("time"),jobject.optString("HR"),Class_name);
                    }
                    LinearLayout viewview = (LinearLayout)findViewById(R.id.progrssbarinHistoryActivity);
                    RecyclerView viewviewview = (RecyclerView)findViewById(R.id.HistoryActivityRecycle);
                    viewview.setVisibility(viewview.GONE);
                    viewviewview.setVisibility(viewviewview.VISIBLE);
                    realadapter.notifyDataSetChanged();
                    viewviewview.postInvalidate();
                } catch (JSONException e){
                    e.printStackTrace();
                }


            }
        }

    }

}
