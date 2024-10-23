package com.pbl.virtualcam;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingStorage {
    private final SharedPreferences sharedPreferences;
    public SettingStorage(Context context){
        this.sharedPreferences=context.getSharedPreferences("VirtualCamSetting",Context.MODE_PRIVATE);
    }
    public void SetValue(String key, String value){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(key,value);
        editor.apply();
    }
    public String GetValue(String key){
        return sharedPreferences.getString(key,"");
    }
}
