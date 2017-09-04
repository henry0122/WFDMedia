package com.example.azure.wdmedia;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.example.azure.wdmedia.MainActivity.myPlayer;
import static com.example.azure.wdmedia.VideoLocalProxy.curTime;

public class VideoPlayerFragment extends Fragment{

    private static final String TAG = MainActivity.class.getSimpleName();

    protected static final String ARG_PARAM1 = "name";
    protected static final String ARG_PARAM2 = "path";
    protected static final String ARG_PARAM3 = "isOther";

    int listNum = 0;
    int curVideo = 0;
    private ArrayList<String> nameList;
    private ArrayList<String> pathList;
    private boolean isOther = false;

    //紀錄重複一段影片變數
    private boolean A = false;
    private boolean B = false;
    private int A_pos = 0;
    private int B_pos = 0;

    private VideoListen videoListen;
    private View mContentView = null;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Button btnPlay;
    private Button btnPause;
    private Button btnNext;
    private Button btnPrev;
    public TextView time1;    //目前進行時間
    public TextView time2;    //影片全部時間
    public SeekBar timeBar;
    public boolean getBar = false;
    private Handler handler = new Handler();

    public VideoPlayerFragment () {}

    public static VideoPlayerFragment newInstance(ArrayList<String> name, ArrayList<String> path) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_PARAM1, name);
        args.putStringArrayList(ARG_PARAM2, path);
        fragment.setArguments(args);
        return fragment;
    }

    public static VideoPlayerFragment newInstance(boolean isOther) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM3, isOther);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nameList = getArguments().getStringArrayList(ARG_PARAM1);
            pathList = getArguments().getStringArrayList(ARG_PARAM2);
            isOther  = getArguments().getBoolean(ARG_PARAM3);
            if(pathList != null) listNum = pathList.size();
            curVideo = 0;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.fragment_video_player,null);

        // set up UI
        btnPlay  = (Button)   mContentView.findViewById(R.id.play);
        btnPause = (Button)   mContentView.findViewById(R.id.pause);
        btnNext  = (Button)   mContentView.findViewById(R.id.next);
        btnPrev  = (Button)   mContentView.findViewById(R.id.prev);
        time1    = (TextView) mContentView.findViewById(R.id.timer1);
        time2    = (TextView) mContentView.findViewById(R.id.timer2);
        timeBar  = (SeekBar)  mContentView.findViewById(R.id.seekBar);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPlayer.start();
                btnPlay.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myPlayer.pause();
                btnPause.setVisibility(View.GONE);
                btnPlay.setVisibility(View.VISIBLE);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (curVideo + 1 <= listNum - 1) curVideo++; // next song
                else curVideo = 0; // restart to play the first song

                try {
                    if (!isOther) { videoListen.pathChange(nameList.get(curVideo), pathList.get(curVideo)); }
                    myPlayer.reset();
                    myPlayer.setDataSource(pathList.get(curVideo));
                    myPlayer.prepareAsync();
                    myPlayer.start();
                    btnPlay.setVisibility(View.GONE);
                    btnPause.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (curVideo - 1 >= 0) curVideo--; // previous song
                else curVideo = listNum-1; // restart to play the last song

                try {
                    if (!isOther) { videoListen.pathChange(nameList.get(curVideo), pathList.get(curVideo)); }
                    myPlayer.reset();
                    myPlayer.setDataSource(pathList.get(curVideo));
                    myPlayer.prepareAsync();
                    myPlayer.start();
                    btnPlay.setVisibility(View.GONE);
                    btnPause.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // set up timer and time bar
        timeHandler();
        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //放開時
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(btnPause.getVisibility() == View.VISIBLE) { myPlayer.start(); }
                getBar = false;
            }
            //按下時
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(myPlayer.isPlaying() == true) { myPlayer.pause(); }
                getBar = true;
            }
            //拖曳時
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if(getBar == true) {
                    int total_mins =  myPlayer.getDuration()/60000;
                    int total_secs = (myPlayer.getDuration()%60000)/1000;
                    Integer cur_mins   =  myPlayer.getCurrentPosition()/60000;
                    Integer cur_secs   = (myPlayer.getCurrentPosition()%60000)/1000;
                    String timeTemp;
                    String timeTotalTemp;
                    timeTemp = timeTransition(cur_mins,cur_secs);
                    timeTotalTemp = timeTransition(total_mins,total_secs);
                    time1.setText(timeTemp);
                    time2.setText(timeTotalTemp);
                    myPlayer.seekTo(timeBar.getProgress());
                }
            }
        });

        // set up video view
        mSurfaceView = (SurfaceView) mContentView.findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "surfaceCreated");

                if (!isOther) {
                    try {
                        videoListen.pathChange(nameList.get(curVideo), pathList.get(curVideo));
                        Log.i(" isLocal path ", pathList.get(curVideo));
                        String path = pathList.get(curVideo);
                        final File file = new File(path);

                        if (file.exists()) {
                            FileInputStream is = new FileInputStream(file);
                            FileDescriptor fd = is.getFD();
                            try {
                                if (myPlayer == null) { myPlayer = new MediaPlayer(); }
                                myPlayer.reset();
                                myPlayer.setDisplay(mSurfaceHolder);
                                myPlayer.setDataSource(fd);
                                myPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                myPlayer.prepareAsync();
                                curTime = 0;
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }
                            is.close();
                        } else {
                            throw new IOException("setDataSource failed.");
                        }
                    } catch (IOException e) {
                        Log.i("Read file", "~~~failed~~~");
                        e.printStackTrace();
                    }
                } else {

                    try {

                        if (myPlayer == null) { myPlayer = new MediaPlayer(); }
                        myPlayer.reset();
                        myPlayer.setDisplay(mSurfaceHolder);
                        myPlayer.setDataSource("http://127.0.0.1:7777");
                        myPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        myPlayer.prepareAsync();

                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }

                    setContentView(View.VISIBLE);
                }

                myPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                        mediaPlayer.seekTo(curTime);
                        btnPlay.setVisibility(View.GONE);
                        btnPause.setVisibility(View.VISIBLE);
                    }
                });

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed");
            }
        });

        // set up content view
        mContentView.setFocusableInTouchMode(true);
        mContentView.requestFocus();
        mContentView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK ){
                    // back to previous fragment by tag
                    ((MainActivity)getActivity()).delService();
                    myPlayer.stop();
                    myPlayer.reset();
                    getActivity().getSupportFragmentManager().popBackStack();
                    Log.d("AudioFG--"," back success");
                    return true;
                }
                return false;
            }
        });

        return mContentView;
    }

    private void timeHandler() {
        handler.removeCallbacks(updateTimer);
        handler.postDelayed(updateTimer, 500);
    }

    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            String timeTemp;
            String timeTotalTemp;

            if(myPlayer.isPlaying()) {
                //重複一段時間判斷
                if(A == true && B == true)
                {
                    //目前時間等於B時間就回到A時間
                    if(myPlayer.getCurrentPosition()/1000 == B_pos/1000)
                    {
                        myPlayer.seekTo(A_pos);
                    }
                }

                //算出全部時間和目前時間
                int total_mins =  myPlayer.getDuration() / 60000;
                int total_secs = (myPlayer.getDuration() % 60000) / 1000;
                timeBar.setMax(myPlayer.getDuration());
                Integer cur_mins   =  myPlayer.getCurrentPosition() / 60000;
                Integer cur_secs   = (myPlayer.getCurrentPosition() % 60000) / 1000;

                //轉成時間格式
                timeTemp = timeTransition(cur_mins,cur_secs);
                timeTotalTemp = timeTransition(total_mins,total_secs);

                //修正最後時間
                if(cur_mins>total_mins && cur_secs>total_secs){
                    cur_mins = total_mins;
                    cur_secs = total_secs;
                }

                time1.setText(timeTemp);//顯示目前時間
                time2.setText(timeTotalTemp); //顯示全部時間
            }

            handler.postDelayed(this, 500); //delay 0.5;
            timeBar.setProgress(myPlayer.getCurrentPosition()); //讓時間bar同步
        }
    };

    private String timeTransition(Integer mins, Integer secs) {
        String temp;
        if(mins<10 && secs<10) {
            temp = "0"+mins.toString()+":"+"0"+secs.toString();
        } else if(mins<10) {
            temp = "0"+mins.toString()+":"+secs.toString();
        } else if(secs<10) {
            temp = mins.toString()+":"+"0"+secs.toString();
        } else {
            temp = mins.toString()+":"+secs.toString();
        }
        return temp;
    }

    private void setContentView(int i) {

        if(!isOther) {
            btnPause.setVisibility(i);
            btnNext.setVisibility(i);
            btnPrev.setVisibility(i);
            time1.setVisibility(i);
            time2.setVisibility(i);
            timeBar.setVisibility(i);
        }

    }

    public interface VideoListen { void pathChange(String name, String path); }
    public void setOnVideoListen(VideoListen vListen) { videoListen = vListen; }

}
