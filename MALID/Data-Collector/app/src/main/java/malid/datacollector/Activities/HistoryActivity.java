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
    ArrayList<Integer> count, hr, hrstop, hrwalk, hrrun;

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
    Button allbutton,stopbutton,walkbutton,runbutton;
    int whathrwant=-1;
    public boolean running = false;
    int time=0;
    int ccount=99999999;
    DatePicker mDate;
    TextView forsetmessage;
    int maxx=0,minn=1000,averagee=0,counttt=0;
    JSONObject globaljsonobject;
    JSONArray globaljsonarray;
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

        PieChart view123 = (PieChart)findViewById(R.id.piechartdaily);
        view123.setVisibility(View.INVISIBLE);
        LineChart view456 = (LineChart)findViewById(R.id.hrchartdaily);
        view456.setVisibility(View.INVISIBLE);
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
                PieChart view123 = (PieChart)findViewById(R.id.piechartdaily);
                view123.setVisibility(View.INVISIBLE);
                LineChart view456 = (LineChart)findViewById(R.id.hrchartdaily);
                view456.setVisibility(View.INVISIBLE);
                albumList.clear();
                insertindex=-1;
                Date = String.valueOf(mDate.getYear()) +String.valueOf(mDate.getMonth()+1) + String.valueOf(mDate.getDayOfMonth());
                forsetmessage= findViewById(R.id.progrssbarmessageinhistoryview);
                forsetmessage.setText("서버로부터\n" + mDate.getYear() + "년 " + (mDate.getMonth()+1) +"월 " + mDate.getDayOfMonth()
                        + "일\n에 대한 데이터를 요청하고 있습니다.\n잠시만 기다려 주세요");
                forsetmessage=findViewById(R.id.dailyhistorydateshow);
                forsetmessage.setText("측정 날짜 : " + mDate.getYear() + "년 " + (mDate.getMonth()+1) +"월 " + mDate.getDayOfMonth()
                        + "일");
                TextView one = (TextView)findViewById(R.id.dailyheartgraphnotify);
                one.setText(mDate.getYear() + "년 " + (mDate.getMonth()+1) +"월 " + mDate.getDayOfMonth() + "일");
                one=(TextView)findViewById(R.id.dailypiegraphnotify);
                one.setText(mDate.getYear() + "년 " + (mDate.getMonth()+1) +"월 " + mDate.getDayOfMonth() + "일");
                new recyclehistoryTask().execute("http://13.125.101.194:3000/historyview");
            }
        });

        allbutton = findViewById(R.id.allhrbutton);
        stopbutton = findViewById(R.id.stophrbutton);
        walkbutton = findViewById(R.id.walkhrbutton);
        runbutton = findViewById(R.id.runhrbutton);
        allbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whathrwant=-1;
                allbutton.setBackground(getDrawable(R.drawable.buttonbgred));
                stopbutton.setBackground(getDrawable(R.drawable.buttonbg));
                walkbutton.setBackground(getDrawable(R.drawable.buttonbg));
                runbutton.setBackground(getDrawable(R.drawable.buttonbg));
                make_chart_for_hr(globaljsonobject,globaljsonarray);
            }
        });
        stopbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whathrwant=0;
                allbutton.setBackground(getDrawable(R.drawable.buttonbg));
                stopbutton.setBackground(getDrawable(R.drawable.buttonbgred));
                walkbutton.setBackground(getDrawable(R.drawable.buttonbg));
                runbutton.setBackground(getDrawable(R.drawable.buttonbg));
                make_chart_for_hr(globaljsonobject,globaljsonarray);
            }
        });
        walkbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whathrwant=1;
                allbutton.setBackground(getDrawable(R.drawable.buttonbg));
                stopbutton.setBackground(getDrawable(R.drawable.buttonbg));
                walkbutton.setBackground(getDrawable(R.drawable.buttonbgred));
                runbutton.setBackground(getDrawable(R.drawable.buttonbg));
                make_chart_for_hr(globaljsonobject,globaljsonarray);
            }
        });
        runbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whathrwant=2;
                allbutton.setBackground(getDrawable(R.drawable.buttonbg));
                stopbutton.setBackground(getDrawable(R.drawable.buttonbg));
                walkbutton.setBackground(getDrawable(R.drawable.buttonbg));
                runbutton.setBackground(getDrawable(R.drawable.buttonbgred));
                make_chart_for_hr(globaljsonobject,globaljsonarray);
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

    void make_Chart(PieChart View, LineChart View2){
        pieChart = View;
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(true);
        pieChart.setExtraOffsets(5,10,5,5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setCenterText("운동");
        if(View == findViewById(R.id.piechartdaily) ) {
            pieChart.setCenterTextSize(10f);
            pieChart.setEntryLabelTextSize(10f);
        }
        else {
            pieChart.setCenterTextSize(35f);
        }
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
        data.setValueTextColor(Color.BLACK);
        pieChart.setData(data);

        hrChart = View2;

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

    void make_chart_for_hr(JSONObject jsonObject, JSONArray jarray) {
        maxx=0;minn=1000;averagee=0;counttt=0;
        globaljsonobject = jsonObject;
        globaljsonarray = jarray;
        String str;
        String str2;
        hr = new ArrayList();
        int t, testtest;
        for (int i = jarray.length() - 1; i >= 0; i--) {
            try {
                jsonObject = jarray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            str = jsonObject.optString("cclass");
            str2 = jsonObject.optString("hhr");
            Log.v("temp", Integer.toString(str2.length()));
            Log.v("str", str);
            Log.v("str2", str2);
            t = Integer.parseInt(str2);
            testtest = Integer.parseInt(str);
            if (testtest == 0) {
                Log.v("strr", "Str==0 success");
                if (whathrwant == -1 || whathrwant == 0) {
                    hr.add(t);
                    if (maxx < t) maxx = t;
                    if (minn > t) minn = t;
                    averagee = averagee + t;
                    counttt += 1;
                }
            } else if (testtest == 1) {
                if (whathrwant == -1 || whathrwant == 1) {
                    hr.add(t);
                    if (maxx < t) maxx = t;
                    if (minn > t) minn = t;
                    averagee = averagee + t;
                    counttt += 1;
                }
            } else if (testtest == 2) {
                if (whathrwant == -1 || whathrwant == 2) {
                    hr.add(t);
                    if (maxx < t) maxx = t;
                    if (minn > t) minn = t;
                    averagee = averagee + t;
                    counttt += 1;
                }
            }
        }
        hrChart = (LineChart) findViewById(R.id.hrchart);

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
        TextView yes = findViewById(R.id.allexercisecounttextview);
        yes=findViewById(R.id.allexercisehearttextview);
        averagee/=counttt;
        yes.setText("최고 심박수 : " + maxx + " 최저 심박수 : " + minn + " 평균 심박수 : " + averagee);
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
                    maxx=0;minn=1000;averagee=0;counttt=0;
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

                    JSONObject jsonObject=null;
                    make_chart_for_hr(jsonObject,jarray);

                    } catch (JSONException e){
                    e.printStackTrace();
                }
                make_Chart((PieChart) findViewById(R.id.piechart),(LineChart) findViewById(R.id.hrchart));
                TextView yes = findViewById(R.id.allexercisecounttextview);
                String temptemp = "정지 : " + count.get(0) + "회 걷기 : " + count.get(1) + "회 달리기 : " + count.get(2) + "회";
                yes.setText(temptemp);
                yes=findViewById(R.id.allexercisehearttextview);
                averagee/=counttt;
                yes.setText("최고 심박수 : " + maxx + " 최저 심박수 : " + minn + " 평균 심박수 : " + averagee);
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
                    class_name = new ArrayList<>();
                    class_name.add("정지");
                    class_name.add("걷기");
                    class_name.add("달리기");
                    int pausecount=0,walkcount=0,runcount=0;
                    count = new ArrayList<>();
                    hr = new ArrayList();
                    sum=0;
                    JSONArray jarray = new JSONArray(result);
                    if(jarray.length()==0){
                        Toast.makeText(getApplicationContext(), "해당 날짜에 대한 데이터가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
                    }
                    else{
                        for(int i=jarray.length()-1; i>=0; i--){
                            JSONObject jobject = jarray.getJSONObject(i);
                            String Class_name = "";
                            if(jobject.optString("class").equals("0")) {Class_name = "정지";++pausecount; }
                            else if(jobject.optString("class").equals("1")) {Class_name = "걷기";++walkcount;}
                            else if(jobject.optString("class").equals("2")) {Class_name = "달리기";++runcount;}
                            setData(String.valueOf(jarray.length()-i),jobject.optString("time"),jobject.optString("HR"),Class_name);
                            sum += 1;
                            hr.add(Integer.parseInt(jobject.optString("HR")));
                        }
                        count.add(0,pausecount);
                        count.add(1,walkcount);
                        count.add(2,runcount);
                        make_Chart((PieChart) findViewById(R.id.piechartdaily), (LineChart) findViewById(R.id.hrchartdaily));
                        PieChart view123 = (PieChart)findViewById(R.id.piechartdaily);
                        view123.setVisibility(View.VISIBLE);
                        LineChart view456 = (LineChart)findViewById(R.id.hrchartdaily);
                        view456.setVisibility(View.VISIBLE);
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
