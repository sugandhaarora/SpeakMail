package com.speakmail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

public class LoginManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "MyCredentials";
    private static final String IS_LOGIN = "isLoggedIn";
    static final String KEY_EMAIL = "email";
    static final String KEY_PASSWORD = "password";

    LoginManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    void createLoginSession(String email, String password) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }

//    void checkLogin(){
//        if(this.isLoggedIn()){
//            Intent intent = new Intent(_context, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            _context.startActivity(intent);
//        }
//    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));
        return user;
    }

    public String getCurrentUser() {
        try {
            HashMap<String, String> user = this.getUserDetails();
            return user.get(LoginManager.KEY_EMAIL);
        } catch (Exception e) {
            return "";
        }
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();

        Intent intent = new Intent(_context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(intent);
    }

    boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
}
