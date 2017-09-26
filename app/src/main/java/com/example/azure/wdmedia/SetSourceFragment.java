package com.example.azure.wdmedia;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
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

import java.util.HashMap;
import java.util.Map;

import static com.example.azure.wdmedia.MainActivity.myPlayer;
import static com.example.azure.wdmedia.MainActivity.createGC;

public class SetSourceFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button btnOther;
    private Button btnOwn;
    private Button btnQR;
    private View mContentView = null;
    private FragmentManager Mgr;
    private FragmentTransaction trans;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private SourceListener sourceListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setOnSourceListener(SourceListener s) {
        sourceListener = s;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        manager = ((MainActivity)getActivity()).getManager();
        channel = ((MainActivity)getActivity()).getChannel();
        mContentView = inflater.inflate(R.layout.fragment_set_source,null);

        Mgr = getActivity().getSupportFragmentManager();
        trans = Mgr.beginTransaction();

        btnOther = (Button) mContentView.findViewById(R.id.btn_other);
        btnOwn = (Button) mContentView.findViewById(R.id.btn_own);
        btnQR = (Button) mContentView.findViewById(R.id.btn_QR);

        btnOther.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // show group maintain list
//                        if(((MainActivity)getActivity()).lockuntilconnect() == true) {
//                            ((MainActivity) getActivity()).showserviceList();
//                            ServiceList serviceList = new ServiceList();
//                            Fragment fg = Mgr.findFragmentByTag("SetSource");
//                            trans.remove(fg).add(R.id.frameLayout, serviceList, "ServiceList");
//                            trans.addToBackStack(null);
//                            trans.commit();
//                        }
                        sourceListener.btnOtherListener();
                    }
                });

        btnOwn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        if(((MainActivity)getActivity()).lockuntilconnect() == true) {
//                            ChoosingMediaFragment mediaFrag = new ChoosingMediaFragment();
//                            Fragment fg = Mgr.findFragmentByTag("SetSource");
//                            trans.remove(fg).add(R.id.frameLayout, mediaFrag, "ChooseMedia");
//                            trans.addToBackStack("ChooseMedia");
//                            trans.commit();
//                        }
                        sourceListener.btnSelfListener();
                    }
                });

        btnQR.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        if(((MainActivity)getActivity()).lockuntilconnect() == true) {
//                            ChoosingMediaFragment mediaFrag = new ChoosingMediaFragment();
//                            Fragment fg = Mgr.findFragmentByTag("SetSource");
//                            trans.remove(fg).add(R.id.frameLayout, mediaFrag, "ChooseMedia");
//                            trans.addToBackStack("ChooseMedia");
//                            trans.commit();
//                        }
                        sourceListener.btnQrListener();
                    }
                });

        mContentView.setFocusableInTouchMode(true);
        mContentView.requestFocus();

        mContentView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if( keyCode == KeyEvent.KEYCODE_BACK ){
                    // back to previous fragment by tag
                    manager.removeGroup(channel,new WifiP2pManager.ActionListener(){
                        @Override
                        public void onSuccess() {
                            Log.d("Manager", "remove group success");
                        }
                        @Override
                        public void onFailure(int reason) {
                            Log.d("Manager", "remove group failed");
                        }
                    });
                    manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            // initiate clearing of the all service requests
                            manager.clearServiceRequests(channel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    // reset the service listeners, service requests, and discovery
                                }
                                @Override
                                public void onFailure(int i) {
                                    Log.d("stopPeer ", "FAILED to clear service requests ");
                                }
                            });

                        }
                        @Override
                        public void onFailure(int i) {
                            Log.d("stopPeer", "FAILED to stop discovery");
                        }
                    });
                    ((MainActivity)getActivity()).stopGS();
                    ((MainActivity)getActivity()).stopGC();
                    ((MainActivity)getActivity()).stopLocalProxy();


                    getActivity().getSupportFragmentManager().popBackStack();
                    Log.d("SetSource--" , "back success");
                    return true;
                }
                return false;
            }
        });

        return mContentView;
    }

    public interface SourceListener {

        void btnSelfListener();

        void btnOtherListener();

        void btnQrListener();

    }
}