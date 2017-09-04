package com.example.azure.wdmedia;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by pohan on 2017/6/26.
 */

public class KeyInUsernameFragment extends android.support.v4.app.Fragment{

    private View mContentView = null;
    private TextView keyinline;
    private Button btnOK;
    public static String Username;
    private KeyListener keyListener;
    private FragmentManager Mangr;
    private FragmentTransaction transs;

    public KeyInUsernameFragment(){}

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        mContentView = inflater.inflate(R.layout.fragment_username_keyin,null);
        keyinline = (TextView)mContentView.findViewById(R.id.txtName);
        btnOK = (Button)mContentView.findViewById(R.id.btn_OK);

        btnOK.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                if(keyinline.getText().toString().equals("") || keyinline.getText().toString() == null){
                    Log.d("KeyIn --","empty username");
                }else{

                    Username = keyinline.getText().toString();
                    keyinline.setText("");
                    keyListener.btnOKListener();

                }
            }
        });

        return mContentView;
    }

    public void setOnKeyListener(KeyListener k){
        keyListener = k;
    }

    public interface KeyListener {

        void btnOKListener();

    }
}
