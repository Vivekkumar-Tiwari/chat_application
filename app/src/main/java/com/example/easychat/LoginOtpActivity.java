package com.example.easychat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.easychat.utils.AndroidUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LoginOtpActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String phoneNumber;
    Long timeoutSeconds = 60l;
    String verificationCode;
    PhoneAuthProvider.ForceResendingToken resendingToken;


    EditText otpInput;
    Button nextBtn;
    ProgressBar progressBar;
    TextView resendOtpTextView;
    FirebaseAuth mAuth  = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_otp);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            otpInput = findViewById(R.id.login_otp);
            nextBtn = findViewById(R.id.login_next_btn);
            progressBar = findViewById(R.id.login_progress_bar);
            resendOtpTextView = findViewById(R.id.resend_otp_textview);


           phoneNumber = getIntent().getExtras().getString("phone");

           sendOtp(phoneNumber, false);

           nextBtn.setOnClickListener(v1 -> {
               String enteredOtp = otpInput.getText().toString();
              PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode,enteredOtp);
              signin(credential);

           });

            resendOtpTextView.setOnClickListener(v12 -> {
            sendOtp(phoneNumber,true);
            });

            return insets;
        });
    }
    void sendOtp(String phoneNumber,boolean isResend){
        startResendTimer();
        setInpProgress(true);
        PhoneAuthOptions.Builder builder =
                PhoneAuthOptions.newBuilder(mAuth).setPhoneNumber(phoneNumber).setTimeout(timeoutSeconds, TimeUnit.SECONDS).setActivity(this).setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signin(phoneAuthCredential);
                        setInpProgress(false);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        AndroidUtil.showToast(getApplicationContext(),"OTP verification failed");
                        setInpProgress(false);
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationCode = s;
                       resendingToken = forceResendingToken;
                        AndroidUtil.showToast(getApplicationContext(),"OTP sent successfully");
                        setInpProgress(false);
                    }

                });

        if(isResend){
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        }else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
            //verify the phine number
        }

    }
    void setInpProgress(boolean inpProgress){
        if(inpProgress){
            progressBar.setVisibility(View.VISIBLE);
            nextBtn.setVisibility(View.GONE);
        }
        else {
            progressBar.setVisibility(View.GONE);
            nextBtn.setVisibility(View.VISIBLE);
        }
    }
    void signin(PhoneAuthCredential phoneAuthCredential){
     //login and go to next activity

        setInpProgress(true);

        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                setInpProgress(false);
                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginOtpActivity.this,LoginUserNameActivity.class);
                    intent.putExtra("phone",phoneNumber);
                    startActivity(intent);

                }else {
                    AndroidUtil.showToast(getApplicationContext(),"OTP Verification is failed");
                }
            }
        });
    }
    void startResendTimer(){
        resendOtpTextView.setEnabled(false);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeoutSeconds--;
                resendOtpTextView.setText("Resend OTP in "+timeoutSeconds+" seconds ");
                if(timeoutSeconds<=0){
                    timeoutSeconds = 60l;
                    timer.cancel();
                    runOnUiThread(() -> {
                        resendOtpTextView.setEnabled(true);
                    });
                }
            }
        },0,1000);
    }
}