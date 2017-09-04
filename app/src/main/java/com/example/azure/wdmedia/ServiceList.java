package com.example.azure.wdmedia;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.azure.wdmedia.MainActivity.localProxy;
import static com.example.azure.wdmedia.MainActivity.videoLocalProxy;

/**
 * Created by pohan on 2017/7/3.
 */

public class ServiceList extends ListFragment {

    private View mContentView = null;
    private ArrayAdapter<String> adapter;
    private String[] chooseService;
    HashMap<String, ServiceInfo> groupServiceSupply;
    private HashMap<String, ServiceInfo> checklist;
    private FragmentManager fragMgr;
    private FragmentTransaction transaction;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ServiceListListener serviceList;

    public void setOnServiceListListener(ServiceListListener s) {
        serviceList = s;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.servisce_list, container, false);

        return mContentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSwipeRefreshLayout = (SwipeRefreshLayout) mContentView.findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){

            public void onRefresh(){
                renewList();
                Toast.makeText(getActivity(), "scroll to refresh", Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        groupServiceSupply = ((MainActivity)getActivity()).getServiceSupply();
        String[] list = new String[groupServiceSupply.size()];
        chooseService = new String[groupServiceSupply.size()];
        int i = 0;
        for (Map.Entry<String, ServiceInfo> iterator : groupServiceSupply.entrySet()) {
            Log.d("showDialog--", iterator.getValue().getOther());
            try {
                list[i] = iterator.getValue().getOther().subSequence(0, iterator.getValue().getOther().length()) + " Service from " + iterator.getValue().getD_device() ;
                chooseService[i] = iterator.getKey();
                i++;
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_2,
                android.R.id.text1 ,list);
        setListAdapter(adapter);
    }

    public void renewList(){
        ((MainActivity)getActivity()).showserviceList();
        groupServiceSupply = new HashMap<String, ServiceInfo>();
        groupServiceSupply = ((MainActivity)getActivity()).getServiceSupply();
        String[] list = new String[groupServiceSupply.size()];
        chooseService = new String[groupServiceSupply.size()];
        int i = 0;
        for(Map.Entry<String, ServiceInfo> iterator : groupServiceSupply.entrySet()){
            try{
                list[i] = iterator.getValue().getOther().subSequence(0, iterator.getValue().getOther().length()) + " Service from " + iterator.getValue().getD_device();
                chooseService[i] = iterator.getKey();
                i++;
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_2,
                android.R.id.text1, list);
        setListAdapter(adapter);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
//        fragMgr = getActivity().getSupportFragmentManager();
//        transaction = fragMgr.beginTransaction();
//        ((MainActivity)getActivity()).showserviceList();
//        checklist = new HashMap<String, ServiceInfo>();
//        checklist = ((MainActivity)getActivity()).getServiceSupply();
        serviceList.ServiceListener(position,chooseService[position]);
        renewList();
//        if(checklist.containsKey(chooseService[position])) {
//
//            if(checklist.get(chooseService[position]).getOther().equals(groupServiceSupply.get(chooseService[position]).getOther())) {
//                ((MainActivity) getActivity()).sendReq(groupServiceSupply.get(chooseService[position]));
//                //Toast.makeText(getActivity(),"press" + position , Toast.LENGTH_SHORT).show();
//
//                if (groupServiceSupply.get(chooseService[position]).getOther().equals("Video")) {
//
//                    VideoPlayerFragment playerFragment = VideoPlayerFragment.newInstance(true);
//                    playerFragment.setOnVideoListen(videoLocalProxy);
//                    videoLocalProxy.setOtherid(groupServiceSupply.get(chooseService[position]).getD_ID());
//                    Fragment fg = fragMgr.findFragmentByTag("ServiceList");
//                    transaction.remove(fg).add(R.id.frameLayout, playerFragment, "VideoPlayer");
//                    transaction.addToBackStack(null);
//                    transaction.commit();
//
//
//
//                } else if (groupServiceSupply.get(chooseService[position]).getOther().equals("Audio")) {
//                    AudioPlayerFragment playerFragment = AudioPlayerFragment.newInstance(true);
//                    playerFragment.setOnAudioListen(localProxy);
//                    Fragment fg = fragMgr.findFragmentByTag("ServiceList");
//                    transaction.remove(fg).add(R.id.frameLayout, playerFragment, "AudioPlayer");
//                    transaction.addToBackStack(null);
//                    transaction.commit();
//                } else {
//                }
//            }else{
//                Toast.makeText(getActivity(), "service has been remove", Toast.LENGTH_SHORT).show();
//                renewList();
//            }
//        }else{
//            Toast.makeText(getActivity(), "service has been remove", Toast.LENGTH_SHORT).show();
//            renewList();
//        }
    }

    public interface ServiceListListener {

        void ServiceListener(int pos, String choose);

    }


}
