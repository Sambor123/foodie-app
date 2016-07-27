package com.foodie.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.foodie.app.Activity.LoginActivity;
import com.foodie.app.R;
import com.foodie.app.util.PrefUtils;

/**
 * Created by tomchen on 2/27/16.
 */
public class SettingFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

//        SwitchCompat ss = (SwitchCompat) getActivity().findViewById(R.id.switch_compat);
//        ss.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Logger.d("SwitchCompat " + buttonView + " changed to " + isChecked);
//            }
//        });
        if(PrefUtils.get("user","userId",getActivity())==null){
            if (findPreference("logout")!=null){
                getPreferenceScreen().removePreference(findPreference("logout"));//删除登录按钮
            }
        }
        final CheckBoxPreference saveFlow = (CheckBoxPreference) getPreferenceManager()
                .findPreference("save_flow");

        saveFlow.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.valueOf(newValue.toString());
                if (checked==true){
                    Toast.makeText(getActivity(),"开启省流量模式",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity(),"关闭省流量模式",Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        final CheckBoxPreference allowPush = (CheckBoxPreference) getPreferenceManager()
                .findPreference("allow_push");

        allowPush.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.valueOf(newValue.toString());
                if (checked==true){
                    Toast.makeText(getActivity(),"开启消息推送",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity(),"关闭消息推送",Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        if (findPreference("logout")!=null){
            Preference myPref=getPreferenceManager().findPreference("logout");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity(),"退出成功",Toast.LENGTH_LONG).show();
                    PrefUtils.remove("user","userId",getActivity());
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    return true;
                }
            });
        }
    }
}
