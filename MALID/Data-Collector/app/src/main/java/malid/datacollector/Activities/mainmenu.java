package malid.datacollector.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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

import malid.datacollector.R;

/**
 * Created by wns11 on 2018-03-17.
 */
public class mainmenu extends AppCompatActivity{

    public String ID = "", joinID = "";    // 사용자 ID
    public String Passwd = "", joinPW = "";    // 사용자 passwd
    public String PW_check = "";    // passwd 확인
    View dialogView;
    EditText temp1;
    CheckBox checkBox = null;
    boolean login = false;
    SharedPreferences sf;
    SharedPreferences.Editor editor;
    String login_url = "http://13.125.101.194:3000/login";
    String join_url = "http://13.125.101.194:3000/join";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);
        init_login();


        Button btn1 = (Button)findViewById(R.id.recordgo); //button이라는 id를 가진 버튼뷰를 가져와서
        Button btn2 = (Button)findViewById(R.id.historygo);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //클릭이 발생했을때.
                if(login == false){
                    Toast.makeText(getApplicationContext(), "로그인 해주세요", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "제작 : MALID", Toast.LENGTH_LONG).show();
                    //토스트로 개발자의 이름을 출력한다.

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class); //인텐트에 mainactivity를 실행할 의도를 담아서
                    intent.putExtra("ID",ID);
                    startActivity(intent); //액티비티를 실행한다.
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(login == false){
                    Toast.makeText(getApplicationContext(), "로그인 해주세요", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "제작 : MALID", Toast.LENGTH_LONG).show();
                    //토스트로 개발자의 이름을 출력한다.
                }
            }
        });
    }

    void init_login(){
        sf = getSharedPreferences("data", MODE_PRIVATE);
        editor = sf.edit();
        ID = sf.getString("ID", "");
        Passwd = sf.getString("Passwd","");
        Log.v("test", "init_login");
        Log.v("test", "ID : "+ID+" Passwd : "+Passwd);
        if(!ID.isEmpty() && !Passwd.isEmpty()) {
            new LoginTask().execute(login_url);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.login:        // login
                if(login == false){
                    dialogView = (View) View.inflate(mainmenu.this, R.layout.dialog_login, null);
                    AlertDialog.Builder dlg = new AlertDialog.Builder(mainmenu.this);
                    //dlg.setTitle("Login");
                    dlg.setView(dialogView);

                    dlg.setPositiveButton("sign in", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            temp1 = dialogView.findViewById(R.id.ID_text);
                            ID = temp1.getText().toString();
                            temp1 = dialogView.findViewById(R.id.Passwd_text);
                            Passwd = temp1.getText().toString();
                            checkBox = dialogView.findViewById(R.id.Auto_login);

                            if(ID.isEmpty() || Passwd.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "ID와 Password를 입력해주세요", Toast.LENGTH_LONG).show();

                            }
                            else{
                                new LoginTask().execute(login_url);
                            }
                        }
                    });

                    dlg.setNegativeButton("cancel", null);
                    dlg.show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "이미 로그인되어 있습니다.", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.logout:       // logout
                if(login) {
                    login = false;
                    editor.remove("ID");
                    editor.remove("Passwd");
                    editor.commit();
                    Toast.makeText(getApplicationContext(), "로그아웃 되었습니다.", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "로그인되어 있지 않습니다.", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.join:     // join
                dialogView = (View) View.inflate(mainmenu.this, R.layout.dialog_join, null);
                AlertDialog.Builder dlg = new AlertDialog.Builder(mainmenu.this);
                //dlg.setTitle("Login");
                dlg.setView(dialogView);
                dlg.setPositiveButton("sign up", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        temp1 = dialogView.findViewById(R.id.ID_text);
                        joinID = temp1.getText().toString();
                        temp1 = dialogView.findViewById(R.id.Passwd_text);
                        joinPW = temp1.getText().toString();
                        temp1 = dialogView.findViewById(R.id.Passwd_text2);
                        PW_check = temp1.getText().toString();

                        if(joinID.isEmpty() || joinPW.isEmpty() || PW_check.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "항목을 모두 입력해주세요", Toast.LENGTH_LONG).show();
                        }
                        else if(!joinPW.equals(PW_check)){
                            Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
                        }
                        else{
                            new LoginTask().execute(join_url);
                        }
                    }
                });

                dlg.setNegativeButton("cancel", null);
                dlg.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class LoginTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("ID", ID);
                jsonObject.accumulate("PASSWD", Passwd);

                HttpURLConnection con = null;
                BufferedReader reader = null;

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
            if(result == null){
                Toast.makeText(getApplicationContext(), "서버와 연결에 실패하였습니다.", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                if (result.equals("로그인 되었습니다.")) {
                    login = true;
                    if (checkBox != null) {
                        if (checkBox.isChecked()) {
                            Log.v("test", "ID & Passwd save");
                            editor.putString("ID", ID);
                            editor.putString("Passwd", Passwd);
                            editor.commit();
                        }
                    }
                }
            }
        }
    }

    public class JoinTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("ID", joinID);
                jsonObject.accumulate("PASSWD", joinPW);

                HttpURLConnection con = null;
                BufferedReader reader = null;

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
            if(result == null){
                Toast.makeText(getApplicationContext(), "서버와 연결에 실패하였습니다.", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
        }

    }

}
