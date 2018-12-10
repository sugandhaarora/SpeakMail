package com.speakmail;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.speakmail.database.DatabaseHelper;
import com.speakmail.database.model.Draft;
import com.speakmail.utils.MyDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;


public class DraftsFragment extends Fragment implements TextToSpeech.OnInitListener {

    private LoginManager loginManager;
    private DraftsAdapter mAdapter;
    private List<Draft> draftList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noDraftsView;
    private int numberOfClicks;

    private String TAG = "DraftsFragment";

    private DatabaseHelper db;

    private TextToSpeech textToSpeech;
    private boolean IsInitialVoiceFinshed;

    SendDraft sendDraft;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)


        {
            Log.d(TAG, "setUserVisibleHint: set reclerview");
//            draftList.addAll(db.getAllDrafts());
            setAdapterData();
            IsInitialVoiceFinshed = false;
            textToSpeech = new TextToSpeech(getContext(), this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drafts, container, false);

        Log.d(TAG, "onCreateView: ");
        loginManager = new LoginManager(getContext());

        recyclerView = view.findViewById(R.id.recycler_view);
        noDraftsView = view.findViewById(R.id.empty_drafts_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            sendDraft = (SendDraft) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Error in retrieving data. Please try again");
        }
    }

    private void setAdapterData() {
        draftList.clear();
        getAllDrafts();
        mAdapter = new DraftsAdapter(getContext(), draftList, new OnItemClickListener() {
            @Override
            public void onItemClick() {
                listen();
            }
        });

        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutFrozen(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        setAdapterData();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_drafts, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_drafts:
                deleteAllDrafts();
                return true;
            case R.id.action_logout_drafts:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void speak(String voiceMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(voiceMessage, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            textToSpeech.speak(voiceMessage, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void toggleEmptyDrafts() {
        if (db.getDraftsCount(loginManager.getCurrentUser()) > 0) {
            noDraftsView.setVisibility(View.INVISIBLE);
            speak("Welcome to drafts. You have " + db.getDraftsCount(loginManager.getCurrentUser()) + " drafts.");
        } else {
            noDraftsView.setVisibility(View.VISIBLE);
            speak("Welcome to drafts. Your draft is empty.");
        }
    }

    public void layoutClicked() {
        if (IsInitialVoiceFinshed) {
            listen();
        }
    }

    private void listen() {
        Log.d(TAG, "listen: Called");
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

    private void logOut() {
        speak("Logging Out");
        loginManager.logoutUser();
    }

    private void exitFromApp() {
        Objects.requireNonNull(getActivity()).finishAffinity();
    }

    private void getAllDrafts() {
        db = new DatabaseHelper(getContext());
        draftList.addAll(db.getAllDrafts(loginManager.getCurrentUser()));
    }

    private void deleteAllDrafts() {
        db.deleteAllDrafts(loginManager.getCurrentUser());
        draftList.clear();
        mAdapter.notifyDataSetChanged();
        toggleEmptyDrafts();
        speak("All drafts Deleted.");
    }

    private void search(String keyword) {
        Draft draft = db.searchDraft(keyword);
        if (draft == null) {
            speak("No draft found with that subject.");
        } else {
            speak("Draft found with subject " + draft.getSubject() + ". Opening the draft to compose it again.");
            sendDraft.sendData(draft);
            openComposeMailFragment();
            Log.d(TAG, "search: " + draft.getSubject());
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
            toggleEmptyDrafts();
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

                } else if ("open compose mail".equals(result.get(0))) {
                    openComposeMailFragment();

                } else if ("search".equals(result.get(0)) || "search draft".equals(result.get(0))) {
                    numberOfClicks = 0;
                    speak("Searching ... ");
                    listen();
                } else if ("clear drafts".equals(result.get(0)) || "clear all".equals(result.get(0))) {
                    deleteAllDrafts();
                } else if ("logout".equals(result.get(0))) {
                    logOut();
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
                        case 0:
                            search(result.get(0));
                            break;
                        default:
                            speak("Please try again.");
                    }
                }
            } else {
                switch (numberOfClicks) {
                    case 1:
                        speak("Please tell the subject to search.");
                        break;
                }
                numberOfClicks--;
            }
        }
        IsInitialVoiceFinshed = true;
    }

    interface SendDraft {
        void sendData(Draft draft);
    }

    private void openComposeMailFragment() {
        ((MainActivity) Objects.requireNonNull(getActivity())).setViewPagerItem(0);
    }

    interface OnItemClickListener {
        void onItemClick();
    }

}
