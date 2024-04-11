package com.example.easychat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.easychat.model.UserModel;
import com.example.easychat.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginUserNameActivity extends AppCompatActivity {
    EditText usernameInput;
    Button letmeInBtn;
    ProgressBar progressBar;
    String phonNumber;

    UserModel userModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_user_name);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            usernameInput = findViewById(R.id.login_username);
            letmeInBtn = findViewById(R.id.login_let_me_in);
            progressBar =findViewById(R.id.login_progress_bar);

            phonNumber = getIntent().getExtras().getString("phone");
            getUsername();
            letmeInBtn.setOnClickListener(v1 -> {
                setUsername();
            });
            return insets;

        });
    }

    void setUsername(){

        String username = usernameInput.getText().toString();
        if(username.isEmpty() || username.length()<3){
            usernameInput.setError("Username length should be at least 3 chars");
            return;
        }
        setInpProgress(true);
        if(userModel!=null){
            userModel.setUsername(username);
        }else {
            userModel = new UserModel(phonNumber,username, Timestamp.now());
        }

        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setInpProgress(false);
                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginUserNameActivity.this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });

    }

     void getUsername() {
        setInpProgress(true);
         FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
             @Override
             public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                setInpProgress(false);
                if(task.isSuccessful()){
                  userModel =  task.getResult().toObject(UserModel.class);
                 if(userModel != null){
                     usernameInput.setText(userModel.getUsername());
                 }
                }
             }
         });
    }

    void setInpProgress(boolean inpProgress){
        if(inpProgress){
            progressBar.setVisibility(View.VISIBLE);
            letmeInBtn.setVisibility(View.GONE);
        }
        else {
            progressBar.setVisibility(View.GONE);
            letmeInBtn.setVisibility(View.VISIBLE);
        }
    }
}