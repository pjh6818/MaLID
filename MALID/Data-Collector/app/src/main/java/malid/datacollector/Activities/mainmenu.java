package malid.datacollector.Activities;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import malid.datacollector.R;

/**
 * Created by wns11 on 2018-03-17.
 */
public class mainmenu extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);

        Button btn1 = (Button)findViewById(R.id.recordgo); //button이라는 id를 가진 버튼뷰를 가져와서
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //클릭이 발생했을때.
                Toast.makeText(getApplicationContext(), "제작 : MALID", Toast.LENGTH_LONG).show();
                //토스트로 개발자의 이름을 출력한다.

                Intent intent = new Intent(getApplicationContext(), MainActivity.class); //인텐트에 mainactivity를 실행할 의도를 담아서
                startActivity(intent); //액티비티를 실행한다.
            }
        });
    }

    /*Button.OnClickListener historyClickListener = new View.OnClickListener() {
        public void onClick(View v){
            Intent intent = new Intent(mainmenu.this,MainActivity.class);
            startActivity(intent);
        }
    };*/


}
