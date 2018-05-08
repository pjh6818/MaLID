package malid.datacollector.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import malid.datacollector.R;

/**
 * Created by wns11 on 2018-03-17.
 */
public class mainmenu extends AppCompatActivity{

    public String ID = null;    // 사용자 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);

        Button btn1 = (Button)findViewById(R.id.recordgo); //button이라는 id를 가진 버튼뷰를 가져와서
        Button btn2 = (Button)findViewById(R.id.historygo);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //클릭이 발생했을때.
                if(ID == null){
                    Toast.makeText(getApplicationContext(), "ID를 입력해주세요", Toast.LENGTH_LONG).show();
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
                if(ID == null){
                    Toast.makeText(getApplicationContext(), "ID를 입력해주세요", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "제작 : MALID", Toast.LENGTH_LONG).show();
                    //토스트로 개발자의 이름을 출력한다.
                }
            }
        });
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
            case R.id.input_ID:
                AlertDialog.Builder ad = new AlertDialog.Builder(mainmenu.this);

                ad.setTitle("ID입력");       // 제목 설정
                ad.setMessage("사용할 ID를 입력하세요");   // 내용 설정

// EditText 삽입하기
                final EditText et = new EditText(mainmenu.this);
                ad.setView(et);

// 확인 버튼 설정
                ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ID = et.getText().toString();
                        dialog.dismiss();     //닫기
                    }
                });

// 취소 버튼 설정
                ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                    }
                });

                ad.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*Button.OnClickListener historyClickListener = new View.OnClickListener() {
        public void onClick(View v){
            Intent intent = new Intent(mainmenu.this,MainActivity.class);
            startActivity(intent);
        }
    };*/


}
