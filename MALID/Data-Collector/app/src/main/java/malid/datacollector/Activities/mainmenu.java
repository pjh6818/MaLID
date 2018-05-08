package malid.datacollector.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import malid.datacollector.R;

/**
 * Created by wns11 on 2018-03-17.
 */
public class mainmenu extends AppCompatActivity{

    public String ID = null;    // 사용자 ID
    final int RC_SIGN_IN = 1001;
    FirebaseAuth firebaseUser;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);
        initFirebase();

        Button btn1 = (Button)findViewById(R.id.recordgo); //button이라는 id를 가진 버튼뷰를 가져와서
        Button btn2 = (Button)findViewById(R.id.historygo);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //클릭이 발생했을때.
                if(ID == null){
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
                if(ID == null){
                    Toast.makeText(getApplicationContext(), "로그인 해주세요", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "제작 : MALID", Toast.LENGTH_LONG).show();
                    //토스트로 개발자의 이름을 출력한다.
                }
            }
        });
    }

    public void initFirebase(){
        firebaseUser = FirebaseAuth.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        Log.d("test", "test1-------------:");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
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
            case R.id.Login:
                /*
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

                ad.show();*/

                if(firebaseUser.getCurrentUser() == null) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
                else{
                    Toast.makeText(mainmenu.this, "이미 로그인 되어있습니다.",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.Logout:   // 로그아웃 메뉴
                if(firebaseUser.getCurrentUser() != null) {
                    firebaseUser.signOut();
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                    ID = null;
                    Toast.makeText(mainmenu.this, "로그아웃 되었습니다.",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(mainmenu.this, "로그인 되어있지 않습니다.",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    public void onStart() { // 사용자가 현재 로그인되어 있는지 확인
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseUser.getCurrentUser();

        if(currentUser != null){ //
            Toast.makeText(mainmenu.this, currentUser.getEmail()+" 로그인되었습니다.",
                    Toast.LENGTH_SHORT).show();
            ID = currentUser.getEmail();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("test", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseUser.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            ID = firebaseUser.getCurrentUser().getEmail();
                            Log.d("test", "signInWithCredential:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("test", "signInWithCredential:failure", task.getException());
                            Toast.makeText(mainmenu.this, "인증이 실패하였습니다.",
                                    Toast.LENGTH_SHORT).show();
                        }
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
