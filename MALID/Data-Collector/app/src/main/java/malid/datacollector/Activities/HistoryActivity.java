package malid.datacollector.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
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

import malid.datacollector.R;

public class HistoryActivity extends AppCompatActivity {

    String history_url = "http://13.125.101.194:3000/history";
    String ID = null;
    TabHost tabHost1;
    TabHost.TabSpec ts1, ts2, ts3;
    ArrayList<String> class_name;
    ArrayList<Integer> count;
    PieChart pieChart;
    int sum;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historyview);
        Intent intent = getIntent();
        ID = intent.getStringExtra("ID");
        sum = 0;
        init_Tap();
        new historyTask().execute(history_url);
    }

    void init_Tap(){
        tabHost1 = (TabHost)findViewById(R.id.tabHost1);
        tabHost1.setup();
        ts1 = tabHost1.newTabSpec("daily") ;
        ts1.setContent(R.id.daily) ;
        ts1.setIndicator("일일") ;
        tabHost1.addTab(ts1) ;
        ts2 = tabHost1.newTabSpec("weekly") ;
        ts2.setContent(R.id.weekly) ;
        ts2.setIndicator("주간") ;
        tabHost1.addTab(ts2) ;
        ts3 = tabHost1.newTabSpec("all") ;
        ts3.setContent(R.id.all) ;
        ts3.setIndicator("전체") ;
        tabHost1.addTab(ts3) ;
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

        PieData data = new PieData((dataSet));
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.WHITE);

        pieChart.setData(data);
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
                        Log.v("jsontest", json_Object.optString("class"));
                        if(json_Object.optString("class").equals("0"))class_name.add("정지");
                        else if(json_Object.optString("class").equals("1"))class_name.add("걷기");
                        else if(json_Object.optString("class").equals("2"))class_name.add("달리기");
                        else if(json_Object.optString("class").equals("3"))class_name.add("아령");
                        count.add(json_Object.optInt("count"));
                        sum += json_Object.optInt("count");
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
                make_Chart();
            }
        }
    }
}
