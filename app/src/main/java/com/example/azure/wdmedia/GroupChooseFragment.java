package com.example.azure.wdmedia;


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
import java.util.HashSet;
import java.util.Map;

import static com.example.azure.wdmedia.MainActivity.myPlayer;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupChooseFragment extends Fragment {

    private Button btnCreate;
    private Button btnJoin;
    private GroupChooseListener groupChooseListener;
    private View mContentView = null;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WiFiDirectServicesList groupList;


    public GroupChooseFragment() {
        // Required empty public constructor
    }

    public void setOnGroupChooseListener(GroupChooseListener g) {
        groupChooseListener = g;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContentView = inflater.inflate(R.layout.fragment_choose_group,null);
        btnCreate = (Button) mContentView.findViewById(R.id.btn_Create);
        btnJoin = (Button) mContentView.findViewById(R.id.btn_Join);

        btnCreate.setOnClickListener (
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        groupChooseListener.btnCreateListener();
                    }
                });

        btnJoin.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        groupChooseListener.btnJoinListener();
                    }
                });

        mContentView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK ){
                    // back to previous fragment by tag
                    getActivity().getSupportFragmentManager().popBackStack();
                    Log.d("Group"," back success");
                    return true;
                }
                return false;
            }
        });

        return mContentView;
    }

    public void startRegistration() {

        WifiP2pDnsSdServiceInfo serviceInfo;

        manager = ((MainActivity)getActivity()).getManager();
        channel = ((MainActivity)getActivity()).getChannel();
        Map<String, String> record = new HashMap<String, String>();
        record.put("available", "visible");

        serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_wifidemotest", "_presence._tcp", record);
        manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("Discover "," Add Local service success");
            }

            @Override
            public void onFailure(int error) {
                Log.d("Discover "," Add Local service failed");
            }
        });

        discoverService();
    }

    private void discoverService() {

        WifiP2pDnsSdServiceRequest serviceRequest;

        manager = ((MainActivity)getActivity()).getManager();
        channel = ((MainActivity)getActivity()).getChannel();

        manager.setDnsSdResponseListeners(channel,
                new WifiP2pManager.DnsSdServiceResponseListener() {

                    HashMap<String,WiFiP2pService> test=new HashMap<String,WiFiP2pService>();

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {

                        // A service has been discovered. Is this our app?

                        if (instanceName.equalsIgnoreCase("_wifidemotest")) {

                            // update the UI and add the item the discovered
                            WiFiDirectServicesList fragment = (WiFiDirectServicesList) getActivity().getSupportFragmentManager().findFragmentByTag("services");
                            if (fragment != null) {
                                WiFiDirectServicesList.WiFiDevicesAdapter adapter = ((WiFiDirectServicesList.WiFiDevicesAdapter) fragment
                                        .getListAdapter());

                                WiFiP2pService service = new WiFiP2pService();
                                service.device = srcDevice;
                                service.instanceName = instanceName;
                                service.serviceRegistrationType = registrationType;

                                test.put(service.device.deviceAddress,service);
                                adapter.clearList();
                                for(WiFiP2pService s : test.values())
                                    adapter.add(s);
                                adapter.notifyDataSetChanged();
                                Log.d("WiFiDiscover ", "onBonjourServiceAvailable "
                                        + instanceName);
                            }
                        }

                    }
                }, new WifiP2pManager.DnsSdTxtRecordListener() {

                    /**
                     * A new TXT record is available. Pick up the advertised
                     * buddy name.
                     */
                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                        Log.d("WiFiDiscover ", device.deviceName + " is "
                                + record.get("available"));
                    }
                });

        // After attaching listeners, create a service request and initiate
        // discovery.
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest,
                new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.d("Service "," Add service discovery request");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        Log.d("Service "," Failed add service");
                    }

                });
        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d("DiscoverService ", " Success");
                Toast.makeText( getActivity() , "Discover Success", Toast.LENGTH_SHORT).show();
            }

            public void onFailure(int arg0) {
                Log.d("DiscoverService ", " Failed" + arg0);
                Toast.makeText( getActivity() , "Discover Service failed", Toast.LENGTH_SHORT).show();
                if (arg0 == WifiP2pManager.NO_SERVICE_REQUESTS) {
                    // initiate a stop on service discovery
                    manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            // initiate clearing of the all service requests
                            manager.clearServiceRequests(channel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    // reset the service listeners, service requests, and discovery
//                                    discoverService();
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
                }
            }
        });

    }

    public interface GroupChooseListener {

        void btnJoinListener();

        void btnCreateListener();

    }
}
