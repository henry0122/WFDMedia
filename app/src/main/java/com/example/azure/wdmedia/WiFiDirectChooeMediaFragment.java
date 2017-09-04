package com.example.azure.wdmedia;

import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.file_browser.FileBrowserActivity;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;


public class WiFiDirectChooeMediaFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;

    private com.example.azure.wdmedia.LocalProxy localProxy;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button btnAudio;
    private Button btnVideo;
    private Button btnLive;
    private View mContentView = null;
    private FragmentManager manager;
    private FragmentTransaction transact;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContentView = inflater.inflate(R.layout.fragment_choosing_media,null);

        btnAudio = (Button) mContentView.findViewById(R.id.btn_audio);
        btnVideo = (Button) mContentView.findViewById(R.id.btn_video);
        btnLive = (Button) mContentView.findViewById(R.id.btn_live);

        manager = getActivity().getSupportFragmentManager();
        transact= manager.beginTransaction();

        btnAudio.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChoosingMediaFragment mediaFrag = new ChoosingMediaFragment();
                        manager = getActivity().getSupportFragmentManager();
                        transact = manager.beginTransaction();
                        Fragment fg = manager.findFragmentByTag("SetSource");
                        transact.hide(fg).add(R.id.frameLayout, mediaFrag, "ChooseMedia");
                        transact.addToBackStack("ChooseMedia");
                        transact.commit();
                    }
                });
        btnVideo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        ChoosingMediaFragment mediaFrag = new ChoosingMediaFragment();
//                        manager = getActivity().getSupportFragmentManager();
//                        transact = manager.beginTransaction();
//                        Fragment fg = manager.findFragmentByTag("SetSource");
//                        transact.hide(fg).add(R.id.frameLayout, mediaFrag, "ChooseMedia");
//                        transact.addToBackStack("ChooseMedia");
//                        transact.commit();
                    }
                });
        btnLive.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

        return mContentView;
    }

}
