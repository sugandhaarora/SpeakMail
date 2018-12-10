package com.speakmail;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.speakmail.database.DatabaseHelper;
import com.speakmail.database.model.Draft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static android.app.Activity.RESULT_OK;

public class ComposeMailFragment extends Fragment implements
        TextToSpeech.OnInitListener {

    private TextInputEditText et_to;
    private TextInputEditText et_subject;
    private TextInputEditText et_message;
    private String myEmail, password, email, subject, message;
    private TextToSpeech textToSpeech;
    private int numberOfClicks;
    private boolean IsInitialVoiceFinshed, isOnCreateCalled;
    private ProgressDialog progressDialog;
    private LoginManager loginManager;

    private DatabaseHelper db;

    private String TAG = "ComposeMailFragment";

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint: ISONCREATE" + isOnCreateCalled);
        if (isVisibleToUser && isOnCreateCalled) {
            IsInitialVoiceFinshed = false;
            textToSpeech = new TextToSpeech(getContext(), this);
            numberOfClicks = 0;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate: ");

        if (getUserVisibleHint()) {
            IsInitialVoiceFinshed = false;
            textToSpeech = new TextToSpeech(getContext(), this);
            numberOfClicks = 0;
            isOnCreateCalled = true;
        }

        db = new DatabaseHelper(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_compose_mail, container, false);
        Log.d(TAG, "onCreateView: ");
        TextInputEditText et_from = view.findViewById(R.id.editText_from);
        et_to = view.findViewById(R.id.editText_to);
        et_subject = view.findViewById(R.id.editText_subject);
        et_message = view.findViewById(R.id.editText_message);
        numberOfClicks = 0;

        loginManager = new LoginManager(getContext());

        HashMap<String, String> user = loginManager.getUserDetails();
        myEmail = user.get(LoginManager.KEY_EMAIL);
        password = user.get(LoginManager.KEY_PASSWORD);

        et_from.setText(myEmail);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_compose_email, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_in_drafts:
                saveDraft();
                return true;
            case R.id.action_logout:
                logOut();
                return true;
            case R.id.action_send:
                composeEmail();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    public void layoutClicked() {
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
            Toast.makeText(getContext(), "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }

    private void composeEmail() {
        email = et_to.getText().toString().trim();
        subject = et_subject.getText().toString().trim();
        message = et_message.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            numberOfClicks = 0;
            speak("Please tell valid email");
            et_to.setText("");
        } else if (TextUtils.isEmpty(subject)) {
            numberOfClicks = 1;
            speak("Subject is empty");
        } else if (TextUtils.isEmpty(message)) {
            numberOfClicks = 2;
            speak("Message is empty");
        } else {
            Log.d("DATA CHECK", "composeEmail: " + email + subject + message);
            new SendEmail().execute();
        }
    }

    private void saveDraft() {
        email = et_to.getText().toString().trim();
        subject = et_subject.getText().toString().trim();
        message = et_message.getText().toString().trim();

        if (TextUtils.isEmpty(subject)) {
            speak("Subject is required to save a draft. Please tell the subject");
            numberOfClicks = 1;
        } else {
            db.insertDraft(myEmail, email, subject, message);
            speak("Email saved as draft. Compose a new mail now");
            numberOfClicks = 0;
            et_to.setText("");
            et_subject.setText("");
            et_message.setText("");
        }
    }

    protected void displayReceivedData(Draft draft) {
        IsInitialVoiceFinshed = true;
        Log.d(TAG, "displayReceivedData: " + draft.getSubject());
        et_to.setText(draft.getTo());
        et_subject.setText(draft.getSubject());
        et_message.setText(draft.getMessage());

    }

    private void logOut() {
        loginManager.logoutUser();
    }

    private void exitFromApp() {
        Objects.requireNonNull(getActivity()).finishAffinity();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
            speak("Welcome to compose mail. Tell me the mail address to whom you want to send mail?");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    IsInitialVoiceFinshed = true;
                }
            }, 6000);
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SendEmail extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            Log.d("DATA CHECK", "onPreExecute: " + email + subject + message);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");

            Session session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(myEmail, password);
                        }
                    });

            try {
                Log.d("DATA CHECK", "doInBackground: " + email + subject + message);
                MimeMessage mm = new MimeMessage(session);
                mm.setFrom(new InternetAddress(myEmail));
                mm.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                mm.setHeader("X-Priority", "1");
                mm.setSubject(subject);
                mm.setText(message);
                Transport.send(mm);
            } catch (MessagingException e) {
                speak("Email sending failed.");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            speak("Email Sent");
            et_to.setText("");
            et_subject.setText("");
            et_message.setText("");
            numberOfClicks = 0;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && IsInitialVoiceFinshed) {
            IsInitialVoiceFinshed = false;
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Log.d(TAG, "onActivityResult: " + result.get(0));
                if ("cancel".equals(result.get(0))) {
                    speak("Cancelled!");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exitFromApp();
                        }
                    }, 4000);

                } else if ("open drafts".equals(result.get(0)) || "open draft".equals(result.get(0)) || "open draught".equals(result.get(0)) || "open draughts".equals(result.get(0))) {
                    ((MainActivity) Objects.requireNonNull(getActivity())).setViewPagerItem(1);

                } else if ("reset email".equals(result.get(0))) {
                    et_to.setText("");
                    speak("Receiver email cleared, please tell the email again.");
                    numberOfClicks = 0;

                } else if ("reset subject".equals(result.get(0))) {
                    et_subject.setText("");
                    speak("Subject cleared, please tell the subject again.");
                    numberOfClicks = 1;

                } else if ("reset message".equals(result.get(0))) {
                    et_message.setText("");
                    speak("Message cleared, please tell the message again.");
                    numberOfClicks = 2;

                } else if ("save as draft".equals(result.get(0)) || "save as draught".equals(result.get(0))) {
                    saveDraft();

                } else if ("log out".equals(result.get(0)) || ("logout".equals(result.get(0)))) {
                    speak("Logging Out");
                    logOut();

                } else if ("reset all".equals(result.get(0))) {
                    numberOfClicks = 0;
                    et_to.setText("");
                    et_subject.setText("");
                    et_message.setText("");
                    speak("All fields cleared, tell me the mail address to whom you want to send mail.");

                } else if ("send email".equals(result.get(0)) || "send mail".equals(result.get(0)) || "send".equals(result.get(0))) {
                    speak("Sending the mail");
                    composeEmail();

                } else if ("exit".equals(result.get(0))) {
                    speak("Closing Speak Mail");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exitFromApp();
                        }
                    }, 4000);

                } else {
                    switch (numberOfClicks) {
                        case 1:
                            String to;
                            to = result.get(0).replaceAll("underscore", "_");
                            to = to.replaceAll("dot", ".");
                            to = to.replaceAll("\\s+", "");
                            to = to.toLowerCase();
                            to = to + "@gmail.com";
                            et_to.setText(to);
                            speak("What should be the subject?");
                            break;
                        case 2:
                            et_subject.setText(result.get(0));
                            speak("Give me message");
                            break;
                        case 3:
                            et_message.setText(result.get(0));
                            speak("Please Confirm the mail\n To : "
                                    + et_to.getText().toString() + "\nSubject : "
                                    + et_subject.getText().toString() + "\nMessage : "
                                    + et_message.getText().toString() + "\nSpeak send email to confirm");
                            break;
                        default:
                            speak("Please try again.");

                    }
                }
            } else {
                switch (numberOfClicks) {
                    case 1:
                        speak("whom you want to send mail?");
                        break;
                    case 2:
                        speak("What should be the subject?");
                        break;
                    case 3:
                        speak("Give me message");
                        break;
                    case 4:
                        speak("provide ur mail");
                        break;
                    case 5:
                        speak("provide ur password");
                        break;
                    default:
                        speak("say yes or no");
                        break;
                }
                numberOfClicks--;
            }
        }
        IsInitialVoiceFinshed = true;
    }

}
