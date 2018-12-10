package com.speakmail;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private int numberOfClicks;
    private boolean IsInitialVoiceFinshed;
    private TextInputEditText inputEditText_email, inputEditText_password;
    private LoginManager loginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginManager = new LoginManager(getApplicationContext());

        inputEditText_email = findViewById(R.id.editText_email);
        inputEditText_password = findViewById(R.id.editText_password);
        Button button_signup = findViewById(R.id.button_signUp);

        button_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUp();
            }
        });

        /**
         * This will redirect user to MainActivity is he is logged in
         **/
        if (!loginManager.isLoggedIn()) {
            IsInitialVoiceFinshed = false;
            textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = textToSpeech.setLanguage(Locale.US);
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TTS", "This Language is not supported");
                        }
                        speak("Welcome to voice mail. To sign up, please tell me your email address and password");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                IsInitialVoiceFinshed = true;
                            }
                        }, 6000);
                    } else {
                        Log.e("TTS", "Initialization Failed!");
                    }
                }
            });
            numberOfClicks = 0;
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        }
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void speak(String voiceMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(voiceMessage, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            textToSpeech.speak(voiceMessage, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void loginLayoutClicked(View view) {
        if (IsInitialVoiceFinshed) {
            numberOfClicks++;
            listen();
        }
    }

    private void listen() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        try {
            startActivityForResult(i, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(LoginActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }

    private void SignUp() {
        String email = inputEditText_email.getText().toString().trim();
        String password = inputEditText_password.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            numberOfClicks = 0;
            speak("Please tell valid email");
            inputEditText_email.setText("");
        } else if (TextUtils.isEmpty(password)) {
            numberOfClicks = 1;
            speak("Password is empty");
            inputEditText_password.setText("");
        } else {
            loginManager.createLoginSession(email, password);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void exitFromApp() {
        this.finishAffinity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && IsInitialVoiceFinshed) {
            IsInitialVoiceFinshed = false;
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if ("cancel".equals(result.get(0))) {
                    speak("Cancelled!");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exitFromApp();
                        }
                    }, 4000);

                } else if ("reset email".equals(result.get(0))) {
                    inputEditText_email.setText("");
                    numberOfClicks = 0;
                    speak("Email cleared, please tell me your email again");

                } else if ("reset password".equals(result.get(0))) {
                    inputEditText_password.setText("");
                    numberOfClicks = 1;
                    speak("Password cleared, please tell me your password again");

                } else if ("reset all".equals(result.get(0))) {
                    numberOfClicks = 0;
                    inputEditText_email.setText("");
                    inputEditText_password.setText("");
                    speak("Credentials Cleared");

                } else if ("sign up".equals(result.get(0)) || "confirm".equals(result.get(0))) {
                    speak("Signing Up");
                    SignUp();

                } else if ("exit".equals(result.get(0))) {
                    speak("Closing Talk Mail");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exitFromApp();
                        }
                    }, 4000);

                } else {
                    switch (numberOfClicks) {
                        case 1:
                            String myEmail;
                            myEmail = result.get(0).replaceAll("underscore", "_");
                            myEmail = myEmail.replaceAll("dot", ".");
                            myEmail = myEmail.replaceAll("\\s+", "");
                            myEmail = myEmail.toLowerCase();
                            myEmail = myEmail + "@gmail.com";
                            inputEditText_email.setText(myEmail);
                            Log.d("CHECK_LENGTH", "onActivityResult: " + inputEditText_password.length());
                            if (inputEditText_password.length() > 0) {
                                speak("Please Confirm the email\n" +
                                        inputEditText_email.getText().toString() + "\nPassword : "
                                        + inputEditText_password.getText().toString() + "\nSpeak sign up to confirm");
                            } else {
                                speak("Please provide your password");
                            }
                            break;
                        case 2:
                            String pass;
                            pass = result.get(0).replaceAll("underscore", "_");
                            pass = pass.replaceAll("dot", ".");
                            pass = pass.replaceAll("\\s+", "");
                            inputEditText_password.setText(pass);
                            speak("Please Confirm the email\n" +
                                    inputEditText_email.getText().toString() + "\nPassword : "
                                    + inputEditText_password.getText().toString() + "\nSpeak sign up to confirm");
                            break;

                        default:
                            speak("Please try again");
                    }


                }
            } else {
                switch (numberOfClicks) {
                    case 1:
                        speak("whom you want to send mail?");
                        break;
                    case 2:
                        speak("provide ur password");
                        break;
                    default:
                        speak("Please try again");
                        break;
                }
                numberOfClicks--;
            }
        }
        IsInitialVoiceFinshed = true;
    }
}
