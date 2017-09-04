package com.example.azure.wdmedia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.file_browser.FileBrowserActivity;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import static com.example.azure.wdmedia.MainActivity.localProxy;
import static com.example.azure.wdmedia.MainActivity.videoLocalProxy;


public class ChoosingMediaFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button btnAudio;
    private Button btnVideo;
    private Button btnLive;
    private View mContentView = null;
    private FragmentManager manager;
    private FragmentTransaction transact;
    private MediaListener mediaListener;
    private int type = 0; // 1 audio  2 video

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
                        type = 1;
                        openFileBrowser(mContentView);

                    }
                });
        btnVideo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        type = 2;
                        openFileBrowser(mContentView);
                    }
                });
        btnLive.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
        mContentView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK ){
                    // back to previous fragment by tag
                    getActivity().getSupportFragmentManager().popBackStack();
                    Log.d("ChoosingMedia--"," back success");
                    return true;
                }
                return false;
            }
        });


        return mContentView;
    }

    public void openFileBrowser(View v) {
        Intent folderIntent = new Intent(getActivity(), com.example.file_browser.FileBrowserActivity.class);
        folderIntent.setAction(com.example.file_browser.FileBrowserActivity.ACTION_FILEBROWSER);
        if(type == 1) {
            folderIntent.putExtra(com.example.file_browser.FileBrowserActivity.FOLDER, FileBrowserActivity.MUSIC_FOLDER);
        }else if(type == 2) {
            folderIntent.putExtra(com.example.file_browser.FileBrowserActivity.FOLDER, FileBrowserActivity.MOVIE_FOLDER);
        }else{}
        startActivityForResult(folderIntent, CHOOSE_FILE_RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_FILE_RESULT_CODE && resultCode == Activity.RESULT_OK) {

            Bundle bundle = data.getExtras(); // return a map
            int size = bundle.getInt("size");

            ArrayList<String> nameList = bundle.getStringArrayList("name");
            ArrayList<String> pathList = bundle.getStringArrayList("path");

            StringBuffer s = new StringBuffer();
            for (int i = 0; i < size; i++) {
                s.append(nameList.get(i) + ": " + pathList.get(i) + "\n");
            }

            if(type == 2) {
                mediaListener.btnVideoListener(nameList,pathList);
            }else if(type == 1) {
                mediaListener.btnAudioListener(nameList,pathList);
            }else{}

        }
    }

    public void setOnMediaListener(MediaListener m){
        mediaListener = m;
    }

    public interface MediaListener {

        void btnAudioListener(ArrayList<String> name, ArrayList<String> path);

        void btnVideoListener(ArrayList<String> name, ArrayList<String> path);

        void btnLiveListener();

    }


}
