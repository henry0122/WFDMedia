package com.example.azure.wdmedia;


import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.file_browser.FileBrowserActivity;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogRecord;

import static com.example.azure.wdmedia.MainActivity.myPlayer;

public class AudioPlayerFragment extends Fragment implements SyncClient.ChangeListener{

    ///////////////////////////////////////////////////

    protected static final String ARG_PARAM1 = "name";
    protected static final String ARG_PARAM2 = "path";
    protected static final String ARG_PARAM3 = "isOther";

    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";

    int listNum = 0;
    int curMusic = 0;

    private View mContentView = null;
    private ImageButton btnPlay;
    private ImageButton btnPause;
    private ImageButton btnNext;
    private ImageButton btnPrex;
    private ImageView imageMain;
    private TextView musicName;

    public TextView time1;    //目前進行時間
    public TextView time2;    //歌曲全部時間
    public SeekBar timeBar;
    public  boolean getBar = false;
    private Handler handler = new Handler();
    private ArrayList<String> pathList;
    private ArrayList<String> nameList;
    private boolean isOther = false;
    //紀錄重複一段歌曲變數
    private boolean A = false;
    private boolean B = false;
    private int A_pos = 0;
    private int B_pos = 0;

    private VideoPlayerFragment.VideoListen audioListen;
    private AudioPlayerFragment.HasChangedListen changedListener;

    public void setOnAudioListen(VideoPlayerFragment.VideoListen aud) {
        audioListen = aud;
    }

    public AudioPlayerFragment() {
        // Required empty public constructor
    }

    public static AudioPlayerFragment newInstance(ArrayList<String> name, ArrayList<String> path) {
        AudioPlayerFragment fragment = new AudioPlayerFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_PARAM1, name);
        args.putStringArrayList(ARG_PARAM2, path);
        fragment.setArguments(args);
        return fragment;
    }

    public static AudioPlayerFragment newInstance(boolean isOther) {
        AudioPlayerFragment fragment = new AudioPlayerFragment();
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
            pathList= getArguments().getStringArrayList(ARG_PARAM2);
            isOther = getArguments().getBoolean(ARG_PARAM3);
            if(pathList != null)listNum = pathList.size();
            curMusic = 0;
        }
    }

    //每0.5秒執行函式一次
    void timeHandler()
    {
        handler.removeCallbacks(updateTimer);
        handler.postDelayed(updateTimer, 500);
    }

    public String timeTransition(Integer mins, Integer secs)
    {
        String temp;
        if(mins<10 && secs<10){
            temp = "0"+mins.toString()+":"+"0"+secs.toString();
        }
        else if(mins<10){
            temp = "0"+mins.toString()+":"+secs.toString();
        }
        else if(secs<10){
            temp = mins.toString()+":"+"0"+secs.toString();
        }
        else{
            temp = mins.toString()+":"+secs.toString();
        }
        return temp;
    }

    private Runnable updateTimer = new Runnable() {

        public void run() {
            String timeTemp;
            String timeTotalTemp;

            if(myPlayer.isPlaying()==true)
            {
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
                    cur_mins=total_mins;
                    cur_secs=total_secs;
                }

                time1.setText(timeTemp);//顯示目前時間
                time2.setText(timeTotalTemp); //顯示全部時間
            }

            handler.postDelayed(this, 500); //delay 0.5;
            timeBar.setProgress(myPlayer.getCurrentPosition()); //讓時間bar同步
        }
    };

    private SeekBar.OnSeekBarChangeListener BarFunc = new SeekBar.OnSeekBarChangeListener() {
        //放開時
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(btnPause.getVisibility() == View.GONE) {}
            else {myPlayer.start();}
            getBar = false;
        }
        //按下時
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if(myPlayer.isPlaying()==true) {myPlayer.pause();}
            getBar = true;
        }
        //拖曳時
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            if(getBar == true)
            {
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
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContentView = inflater.inflate(R.layout.fragment_audio_player,null);

        imageMain = (ImageView) mContentView.findViewById(R.id.imageView);
        btnPlay = (ImageButton) mContentView.findViewById(R.id.btn_play);
        btnPause = (ImageButton) mContentView.findViewById(R.id.btn_pause);
        btnNext = (ImageButton) mContentView.findViewById(R.id.btn_next);
        btnPrex = (ImageButton) mContentView.findViewById(R.id.btn_prex);
        time1 = (TextView) mContentView.findViewById(R.id.timer1);
        time2 = (TextView) mContentView.findViewById(R.id.timer2);
        timeBar = (SeekBar) mContentView.findViewById(R.id.seekBar);
        musicName = (TextView) mContentView.findViewById(R.id.musicName);
        timeBar.setProgress(0);

        myPlayer.release();
        myPlayer = null;

        if(!isOther) {
            try {
                audioListen.pathChange(nameList.get(curMusic), pathList.get(curMusic));
                Log.i(" isLocal path ", pathList.get(curMusic));
                String path = pathList.get(curMusic);
                final File file = new File(path);
                musicName.setSingleLine(true);
                musicName.setEllipsize(TextUtils.TruncateAt.END);
                musicName.setText(nameList.get(curMusic));

                if (file.exists()) {

                    if (myPlayer == null) {
                        myPlayer = new MediaPlayer();
                    }
                    myPlayer.reset();
                    FileInputStream is = new FileInputStream(file);
                    FileDescriptor fd = is.getFD();
                    myPlayer.setDataSource(fd);
                    myPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            Log.d("MediaPlayer ","---123onPrepared---");
                            setContentView(View.VISIBLE);

                            mediaPlayer.start();
                        }
                    });
                    try {
                        myPlayer.prepareAsync();
                    } catch(IllegalStateException ee)
                    {
                        myPlayer.reset();
                        myPlayer.setDataSource(fd);
                        myPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                Log.d("MediaPlayer ","---123onPrepared---");
                                setContentView(View.VISIBLE);

                                mediaPlayer.start();
                            }
                        });
                        myPlayer.prepareAsync();
                        ee.printStackTrace();
                    }

                    is.close();
                } else {
                    throw new IOException("setDataSource failed.");
                }
            }
            catch (IOException e) {
                Log.i("Read file", "~~~failed~~~");
                e.printStackTrace();
            }
            setContentView(View.VISIBLE);
        }
        else {
            if (myPlayer == null) {
                myPlayer = new MediaPlayer();
            }
            myPlayer.reset();
            try {
                Log.d("1111","11111");
                myPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                myPlayer.setDataSource("http://127.0.0.1:7777");
                myPlayer.prepareAsync();
                myPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        Log.d("MediaPlayer ","---onPrepared---");
                        mediaPlayer.start();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
            setContentView(View.VISIBLE);
        }

        if(!isOther) {
            timeHandler();
            timeBar.setOnSeekBarChangeListener(BarFunc);



        btnPlay.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myPlayer.start();
                        btnPlay.setVisibility(View.GONE);
                        btnPause.setVisibility(View.VISIBLE);
                        musicName.setSingleLine(true);
                        musicName.setEllipsize(TextUtils.TruncateAt.END);
                        musicName.setText(nameList.get(curMusic));
                    }
                });

        btnPause.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myPlayer.pause();
                        btnPause.setVisibility(View.GONE);
                        btnPlay.setVisibility(View.VISIBLE);
                        musicName.setSingleLine(true);
                        musicName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                        musicName.setSelected(true);
                        musicName.setMarqueeRepeatLimit(-1);
                        musicName.setFocusable(true);
                        musicName.setFocusableInTouchMode(true);
                        musicName.setText(nameList.get(curMusic));
                    }
                });

        btnNext.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myPlayer.reset();
                        if(curMusic <= listNum - 2)
                        {
                            // next song
                            curMusic ++;

                            try {

                                if(!isOther)audioListen.pathChange(nameList.get(curMusic), pathList.get(curMusic));
                                musicName.setText(nameList.get(curMusic));
                                myPlayer.setDataSource(pathList.get(curMusic));
                                myPlayer.prepare();
                                if(myPlayer.isPlaying() == false)
                                {
                                    myPlayer.start();
                                    btnPlay.setVisibility(View.GONE);
                                    setContentView(View.VISIBLE);
                                }
                                else{
                                    myPlayer.start();
                                }
                                changedListener.hasChanged(true);
                            }
                            catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            // restart to play the list
                            curMusic = 0;

                            try {
                                if(!isOther)audioListen.pathChange(nameList.get(curMusic), pathList.get(curMusic));
                                musicName.setText(nameList.get(curMusic));
                                myPlayer.setDataSource(pathList.get(curMusic));
                                myPlayer.prepare();
                                if(myPlayer.isPlaying() == false)
                                {
                                    myPlayer.start();
                                    btnPlay.setVisibility(View.GONE);
                                    setContentView(View.VISIBLE);
                                }
                                else{
                                    myPlayer.start();
                                }
                                changedListener.hasChanged(true);
                            }
                            catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        musicName.setSingleLine(true);
                        musicName.setEllipsize(TextUtils.TruncateAt.END);
                        musicName.setText(nameList.get(curMusic));
                    }
                });

        btnPrex.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myPlayer.reset();
                        if(curMusic - 1 >= 0)
                        {
                            // next song
                            curMusic --;

                            try {
                                if(!isOther)audioListen.pathChange(nameList.get(curMusic), pathList.get(curMusic));
                                musicName.setText(nameList.get(curMusic));
                                myPlayer.setDataSource(pathList.get(curMusic));
                                myPlayer.prepare();
                                if(myPlayer.isPlaying() == false)
                                {
                                    myPlayer.start();
                                    btnPlay.setVisibility(View.GONE);
                                    setContentView(View.VISIBLE);
                                }
                                else{
                                    myPlayer.start();
                                }
                                changedListener.hasChanged(true);
                            }
                            catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            // restart to play the list
                            curMusic = 0;

                            try {
                                if(!isOther)audioListen.pathChange(nameList.get(curMusic), pathList.get(curMusic));
                                musicName.setText(nameList.get(curMusic));
                                myPlayer.setDataSource(pathList.get(curMusic));
                                myPlayer.prepare();
                                if(myPlayer.isPlaying() == false)
                                {
                                    myPlayer.start();
                                    btnPlay.setVisibility(View.GONE);
                                    setContentView(View.VISIBLE);
                                }
                                else{
                                    myPlayer.start();
                                }
                                changedListener.hasChanged(true);
                            }
                            catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        musicName.setSingleLine(true);
                        musicName.setEllipsize(TextUtils.TruncateAt.END);
                        musicName.setText(nameList.get(curMusic));
                    }
                });
        myPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            // @Override
            public void onCompletion(MediaPlayer arg0)
            {
                if(!isOther) {
                    myPlayer.reset();
                    if (curMusic <= listNum - 2) {
                        // next song
                        curMusic++;

                        try {
                            if (!isOther)
                                audioListen.pathChange(nameList.get(curMusic), pathList.get(curMusic));
                            if (nameList != null) musicName.setText(nameList.get(curMusic));
                            if (pathList != null) myPlayer.setDataSource(pathList.get(curMusic));
                            myPlayer.prepare();
                            myPlayer.start();
                            changedListener.hasChanged(true);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // restart to play the list
                        curMusic = 0;

                        try {
                            if (!isOther)
                                audioListen.pathChange(nameList.get(curMusic), pathList.get(curMusic));
                            if (nameList != null) musicName.setText(nameList.get(curMusic));
                            if (pathList != null) myPlayer.setDataSource(pathList.get(curMusic));
                            myPlayer.prepare();
                            myPlayer.start();
                            changedListener.hasChanged(true);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        }
        else{

        }

        mContentView.setFocusableInTouchMode(true);
        mContentView.requestFocus();
        mContentView.setOnKeyListener(new View.OnKeyListener() {
              @Override
              public boolean onKey(View v, int keyCode, KeyEvent event) {
                  if( keyCode == KeyEvent.KEYCODE_BACK ) {
                      // back to previous fragment by tag
                      if(!isOther){
                          ((MainActivity)getActivity()).delService();
                      }
                      else {
//                         audioListen.stopLink();
                      }
                      myPlayer.stop();
                      FragmentManager ffg = getActivity().getSupportFragmentManager();
                      FragmentTransaction ft = ffg.beginTransaction();
                      Fragment fg = ffg.findFragmentByTag("AudioPlayer");
                      ft.remove(fg);
                      ffg.popBackStack();
                      ft.commit();


                      Log.d("AudioFG--"," back success");
                      return true;
                  }
                  return false;
              }
        });

        return mContentView;
    }

    void setContentView(int i)
    {
        if(!isOther) {
            imageMain.setVisibility(i);
            btnPause.setVisibility(i);
            btnNext.setVisibility(i);
            btnPrex.setVisibility(i);
            time1.setVisibility(i);
            time2.setVisibility(i);
            timeBar.setVisibility(i);
            musicName.setVisibility(i);
        }
        else {
            imageMain.setVisibility(i);
        }
    }

    @Override
    public void mediaChange()
    {
        System.out.println("FUCK in Sync mediaChange.");
        myPlayer.reset();
        try
        {
            myPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            myPlayer.setDataSource("http://127.0.0.1:7777");
            myPlayer.prepareAsync();
            myPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Log.d("MediaPlayer ","---onPrepared---");
                    mediaPlayer.start();
                }
            });
        }
        catch(Exception e){}

    }

    public interface AudioListen {

        public void pathChange(String name, String path);

        public void stopLink();
    }

    public interface HasChangedListen
    {
        public void hasChanged(boolean b);
    }

    public void setOnChangedListener(AudioPlayerFragment.HasChangedListen c)
    {
        changedListener = c;
    }

}
