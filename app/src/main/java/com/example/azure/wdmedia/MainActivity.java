package com.example.azure.wdmedia;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.wifi.p2p.*;
import android.text.LoginFilter;
import android.util.Log;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;
import android.widget.TextView;

import com.example.azure.wdmedia.ReqServer.RequestListener;
import static com.example.azure.wdmedia.KeyInUsernameFragment.Username;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements WiFiDirectServicesList.DeviceClickListener , WifiP2pManager.ConnectionInfoListener , RequestListener {

    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";

    private FragmentManager fragMgr;
    private FragmentTransaction transaction;

    private FragmentControl fragmentControl;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    public static MediaPlayer myPlayer = new MediaPlayer();
    private Thread GServerThread, GClientThread, RClientThread, RServerThread, ProxyThread, VideoProxyThread;
    private static GroupServer GS;
    private static GroupClient GC;
    private static ReqClient RC;
    private static ReqServer RS;
    public static boolean createGC = false;
    public static LocalProxy localProxy;
    public static VideoLocalProxy videoLocalProxy;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private HashMap<String, String> ServiceType;
    CharSequence[] items;

    //////////////////////////////////////// To Synchronize the service between server and client
    private static SyncServer SS;
    private  static SyncClient SC;
    private Thread SServerThread, SClientThread;

    ///////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        manager.removeGroup(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("Disconnect", "Disconnect successs");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("Disconnect", "Disconnect failed. Reason :" + reason);
            }
        });
        ServiceType = new HashMap<String, String>();
        ServiceType.put("Audio", "Audio");
        ServiceType.put("Video", "Video");
        ServiceType.put("Live", "Live");
        stopLocalProxy();
        stopGC();
        stopGS();
        stopSS();
        stopSC();
        Log.d("Stop", " ALL");
        // create fragment on activity
//        fragMgr = getSupportFragmentManager();
//        KeyInUsernameFragment keyinFrag = new KeyInUsernameFragment();
//        transaction = fragMgr.beginTransaction();
//        transaction.add(R.id.frameLayout, keyinFrag, "KeyInUsername");
//        transaction.commit();
        fragmentControl = new FragmentControl();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        manager.removeGroup(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("Disconnect", "Disconnect successs");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("Disconnect", "Disconnect failed. Reason :" + reason);
            }
        });
        if (GServerThread != null) {
            GS.Stop();
            GServerThread.interrupt();
            GServerThread = null;
        }
        if (GClientThread != null) {
            GC.Stop();
            GClientThread.interrupt();
            GClientThread = null;
        }
        if (ProxyThread != null) {
            localProxy.Stop();
            ProxyThread.interrupt();
            ProxyThread = null;
        }
        stopSS();
        stopSC();

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public WifiP2pManager getManager() {
        return manager;
    }

    public WifiP2pManager.Channel getChannel() {
        return channel;
    }


    @Override
    public void connectP2p(WiFiP2pService wifiP2pService) {
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiP2pService.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (serviceRequest != null) {
            manager.removeServiceRequest(channel, serviceRequest,
                    new ActionListener() {

                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure(int arg0) {

                        }

                    });
        }
        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                Log.d("Discover ", " connect to " + config.deviceAddress);
            }

            @Override
            public void onFailure(int errorCode) {
                Log.d("Discover ", " connect failed");
            }

        });
    }

    public void disconnect() {
/*
        manager.removeGroup(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("Disconnect", "Disconnect successs");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("Disconnect", "Disconnect failed. Reason :" + reason);
            }
        });
*/
    }

    public void addService(String type) {
        Toast.makeText(this, "add " + type + " success", Toast.LENGTH_SHORT).show();
        ServiceInfo outputInfo = new ServiceInfo();
        outputInfo.setfuncname("add").setOther(ServiceType.get(type)).setDdevice(Username);
        GC.sendInfo(outputInfo);
    }

    public void delService() {
        ServiceInfo outputInfo = new ServiceInfo();
        outputInfo.setfuncname("delete");
        GC.sendInfo(outputInfo);
    }

    public boolean lockuntilconnect() {
        if (GC == null) {
            Toast.makeText(this, " Waiting for Connect ", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (GC.isConnectcomplete()) {
                return true;
            } else {
                Toast.makeText(this, " Waiting for Connect ", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }

    public void showserviceList() {
        ServiceInfo testingInfo = new ServiceInfo();
        testingInfo.setfuncname("getSupplyList");
        GC.sendInfo(testingInfo);
        while (GC.isGetlistcomplete() != true) {
        }
    }

    private void showDialog() {
        int num;
        int i = 0;

        final HashMap<String, ServiceInfo> groupServiceSupply = getServiceSupply();
        num = groupServiceSupply.size();
        items = new CharSequence[num];
        final String[] chooseService = new String[num];
        for (Map.Entry<String, ServiceInfo> iterator : groupServiceSupply.entrySet()) {
            Log.d("showDialog--", iterator.getValue().getOther());
            try {
                items[i] = iterator.getValue().getOther().subSequence(0, iterator.getValue().getOther().length()) + " Service from " + iterator.getValue().getD_device();
                chooseService[i] = iterator.getKey();
                i++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d("showDialog", " success");
        new AlertDialog.Builder(this)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        sendReq(groupServiceSupply.get(chooseService[which]));
                    }
                }).show();
    }

    public HashMap<String, ServiceInfo> getServiceSupply() {
        return GC.getServiceSupply();
    }

    public void sendReq(ServiceInfo serviceInfo) {

        if (RC != null) {
            RC.stop();
            RC = null;

        }

        RC = new ReqClient(serviceInfo);
        RClientThread = new Thread(RC);
        RClientThread.start();

    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {


        if (info.groupFormed && info.isGroupOwner) {

            Log.d("onConnection", " Owner");
            // group maintain
            // null or not
            // TODO 123567
            if (GServerThread == null) {
                //stopGroupMaintain();
                Log.d("debug --", "GS open success");
                GS = new GroupServer(info);
                GServerThread = new Thread(GS);
                GServerThread.start();
            }

            if (GClientThread == null) {
                //stopGroupMaintain();
                while (true) {
                    if (GS.getfinish()) {
                        Log.d("debug --", "GC open success");
                        GC = new GroupClient(info);
                        GClientThread = new Thread(GC);
                        GClientThread.start();
                        break;
                    }
                }
            }

            if (ProxyThread == null) {
                Log.d("debug --", "GS audio proxy thread");
                localProxy = new LocalProxy();
                ProxyThread = new Thread(localProxy);
                ProxyThread.start();
            }

            if (videoLocalProxy == null) {
                Log.d("debug --", "GS video proxy thread");
                videoLocalProxy = new VideoLocalProxy();
                VideoProxyThread = new Thread(videoLocalProxy);
                VideoProxyThread.start();
            }

        } else if (info.groupFormed) {

            Log.d("onConnection", " Member");
            // group maintain
            if (GClientThread == null) {
                //stopGroupMaintain();
                Log.d("onConnection", " GC");
                GC = new GroupClient(info);
                GClientThread = new Thread(GC);
                GClientThread.start();
            }
            if (ProxyThread == null) {
                Log.d("debug --", "GＭ proxy thread");
                Log.d("onConnection", " Proxy");
                localProxy = new LocalProxy();
                ProxyThread = new Thread(localProxy);
                ProxyThread.start();
            }
            if (videoLocalProxy == null) {
                Log.d("debug --", "GS video proxy thread");
                videoLocalProxy = new VideoLocalProxy();
                VideoProxyThread = new Thread(videoLocalProxy);
                VideoProxyThread.start();
            }

        }

        if (RServerThread == null) {
            RS = new ReqServer();
            RS.setOnRequestListener(this);
            RServerThread = new Thread(RS);
            RServerThread.start();
        }

        Toast.makeText(this, " Connected Success ", Toast.LENGTH_SHORT).show();
    }

    public void stopLocalProxy() {
        if (localProxy != null) {
            localProxy.Stop();
        }
        if (ProxyThread != null) {
            ProxyThread.interrupt();
            ProxyThread = null;
        }
        if (videoLocalProxy != null) {
            //videoLocalProxy.stop();
        }
        if (VideoProxyThread != null) {
            VideoProxyThread.interrupt();
            VideoProxyThread = null;
        }
        localProxy = null;
    }

    public void stopGS() {
        if (GS != null) {
            GS.Stop();
        }
        if (GServerThread != null) {
            GServerThread.interrupt();
            GServerThread = null;
        }
    }

    public void stopGC() {
        if (GC != null) {
            GC.Stop();
        }
        if (GClientThread != null) {
            GClientThread.interrupt();
            GClientThread = null;
        }

    }

    public void stopSS()
    {
        if(SS != null)
        {
            SS.Stop();
        }
        if(SServerThread != null)
        {
            SServerThread.interrupt();
            SServerThread = null;
        }
    }

    public void stopSC()
    {
        if(SC != null)
        {
            SC.Stop();
        }
        if(SClientThread != null)
        {
            SClientThread.interrupt();
            SClientThread = null;
        }
    }

    @Override
    public void SendData(String ip) {
        try {
            localProxy.SendData(ip);
            Log.d("SendData ", "sendData");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SendCurTimeTest(String ip) {
        try {
            videoLocalProxy.SendData(ip);
            Log.d("SendData ", "sendData");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class FragmentControl implements KeyInUsernameFragment.KeyListener,
            GroupChooseFragment.GroupChooseListener,
            WiFiDirectServicesList.WifiServiceListener,
            SetSourceFragment.SourceListener,
            ServiceList.ServiceListListener,
            ChoosingMediaFragment.MediaListener {

        private FragmentManager Mgr;
        private FragmentTransaction Trans;

        private KeyInUsernameFragment keyInUsernameFragment;
        private GroupChooseFragment groupChooseFragment;
        private SetSourceFragment setSourceFragment;
        private WiFiDirectServicesList wiFiDirectServicesList;
        private ServiceList serviceList;
        private ChoosingMediaFragment choosingMediaFragment;

        public FragmentControl() {

            Mgr = getSupportFragmentManager();
            Trans = Mgr.beginTransaction();

            keyInUsernameFragment = new KeyInUsernameFragment();
            keyInUsernameFragment.setOnKeyListener(this);

            Trans.add(R.id.frameLayout, keyInUsernameFragment, "KeyInUsername");
            Trans.commit();

        }

        @Override
        public void btnOKListener() {

            groupChooseFragment = new GroupChooseFragment();
            groupChooseFragment.setOnGroupChooseListener(this);
            Fragment fg = Mgr.findFragmentByTag("KeyInUsername");
            Trans = Mgr.beginTransaction();
            Trans.hide(fg).add(R.id.frameLayout, groupChooseFragment, "GroupChoose");
            Trans.addToBackStack(null);
            Trans.commit();

        }

        @Override
        public void btnJoinListener() {


            wiFiDirectServicesList = new WiFiDirectServicesList();
            wiFiDirectServicesList.setOnWifiServiceListener(this);
            Fragment fg = Mgr.findFragmentByTag("GroupChoose");
            Trans = Mgr.beginTransaction();
            Trans.remove(fg).add(R.id.frameLayout, wiFiDirectServicesList, "services");
            Trans.addToBackStack(null);
            Trans.commit();

            startRegistration();
        }

        @Override
        public void btnCreateListener() {

            setSourceFragment = new SetSourceFragment();
            setSourceFragment.setOnSourceListener(this);
            Trans = Mgr.beginTransaction();
            Fragment fg = Mgr.findFragmentByTag("GroupChoose");
            Trans.remove(fg).add(R.id.frameLayout, setSourceFragment, "SetSource");
            Trans.addToBackStack("SetSource");
            Trans.commit();

            startRegistration();
            manager.createGroup(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    // Device is ready to accept incoming connections from peers.
//                    Toast.makeText( , "P2P group creation success.",
//                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
//                    Toast.makeText(, "P2P group creation failed. Retry.",
//                            Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public void btnClick() {
            setSourceFragment = new SetSourceFragment();
            setSourceFragment.setOnSourceListener(this);
            Trans = Mgr.beginTransaction();
            Fragment fg = Mgr.findFragmentByTag("services");
            Trans.remove(fg).add(R.id.frameLayout, setSourceFragment, "SetSource");
            Trans.addToBackStack("SetSource");
            Trans.commit();
        }

        @Override
        public void btnSelfListener() {
            if(lockuntilconnect() == true) {
                choosingMediaFragment = new ChoosingMediaFragment();
                choosingMediaFragment.setOnMediaListener(this);
                Fragment fg = Mgr.findFragmentByTag("SetSource");
                Trans = Mgr.beginTransaction();
                Trans.remove(fg).add(R.id.frameLayout, choosingMediaFragment, "ChooseMedia");
                Trans.addToBackStack("ChooseMedia");
                Trans.commit();
            }
        }

        @Override
        public void btnOtherListener() {
            if(lockuntilconnect() == true) {
                showserviceList();
                serviceList = new ServiceList();
                serviceList.setOnServiceListListener(this);
                Fragment fg = Mgr.findFragmentByTag("SetSource");
                Trans = Mgr.beginTransaction();
                Trans.remove(fg).add(R.id.frameLayout, serviceList, "ServiceList");
                Trans.addToBackStack(null);
                Trans.commit();
            }
        }

        @Override
        public void ServiceListener(int pos,String choose) {

            Trans = Mgr.beginTransaction();
            showserviceList();
            HashMap<String, ServiceInfo> checklist = new HashMap<String, ServiceInfo>();
            checklist = getServiceSupply();

            if(getServiceSupply().containsKey(choose)) {

                if(getServiceSupply().get(choose).getOther().equals(getServiceSupply().get(choose).getOther())) {
                    sendReq(getServiceSupply().get(choose));
                    //Toast.makeText(getActivity(),"press" + position , Toast.LENGTH_SHORT).show();

                    if (getServiceSupply().get(choose).getOther().equals("Video")) {

                        VideoPlayerFragment playerFragment = VideoPlayerFragment.newInstance(true);
                        playerFragment.setOnVideoListen(videoLocalProxy);
                        videoLocalProxy.setOtherid(getServiceSupply().get(choose).getD_ID());
                        Fragment fg = Mgr.findFragmentByTag("ServiceList");
                        Trans.remove(fg).add(R.id.frameLayout, playerFragment, "VideoPlayer");
                        Trans.addToBackStack(null);
                        Trans.commit();

                    } else if (getServiceSupply().get(choose).getOther().equals("Audio")) {
                        AudioPlayerFragment playerFragment = AudioPlayerFragment.newInstance(true);
                        playerFragment.setOnAudioListen(videoLocalProxy);
                        videoLocalProxy.setOtherid(getServiceSupply().get(choose).getD_ID());

                        stopSS();
                        stopSC();
                        // Synccc
                        System.out.println("SS new in Syncclient.");
                        SS = new SyncServer(false);
                        System.out.println("SC new in Syncclient.");
                        SC = new SyncClient(getServiceSupply().get(choose).getD_ID(), false);
                        SC.setOnChangeListener(playerFragment);
                        System.out.println("Thread new in Syncclient.");
                        SServerThread = new Thread(SS);
                        SClientThread = new Thread(SC);
                        SServerThread.start();
                        SClientThread.start();


                        Fragment fg = Mgr.findFragmentByTag("ServiceList");
                        Trans.remove(fg).add(R.id.frameLayout, playerFragment, "AudioPlayer");
                        Trans.addToBackStack(null);
                        Trans.commit();
                    } else {
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "service has been remove", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(), "service has been remove", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void btnAudioListener(ArrayList<String> nameList, ArrayList<String> pathList) {
            addService("Audio");
            AudioPlayerFragment playerFragment = AudioPlayerFragment.newInstance(nameList, pathList);
            Fragment fg = Mgr.findFragmentByTag("ChooseMedia");
            playerFragment.setOnAudioListen(videoLocalProxy);

            stopSS();
            stopSC();
            // Synccc
            System.out.println("SS new in Syncserver.");
            SS = new SyncServer(true);
            System.out.println("SC new in Syncserver.");
            SC = new SyncClient(true);
            playerFragment.setOnChangedListener(SS);
            System.out.println("Thread new in Syncserver.");
            SServerThread = new Thread(SS);
            SClientThread = new Thread(SC);
            SServerThread.start();
            SClientThread.start();

            Trans = Mgr.beginTransaction();
            Trans.replace(R.id.frameLayout,playerFragment,"AudioPlayer");
            Trans.addToBackStack(null);
            Trans.commit();
        }

        @Override
        public void btnVideoListener(ArrayList<String> nameList, ArrayList<String> pathList) {
            addService("Video");
            VideoPlayerFragment playerFragment = VideoPlayerFragment.newInstance(nameList, pathList);
            Fragment fg = Mgr.findFragmentByTag("ChooseMedia");
            playerFragment.setOnVideoListen(videoLocalProxy);
            Trans = Mgr.beginTransaction();
            Trans.replace(R.id.frameLayout, playerFragment, "VideoPlayer");
            Trans.addToBackStack(null);
            Trans.commit();
        }

        @Override
        public void btnLiveListener() {

        }

    }



    public void startRegistration() {

        WifiP2pDnsSdServiceInfo serviceInfo;

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

        manager.setDnsSdResponseListeners(channel,
                new WifiP2pManager.DnsSdServiceResponseListener() {

                    HashMap<String,WiFiP2pService> test=new HashMap<String,WiFiP2pService>();

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {

                        // A service has been discovered. Is this our app?

                        if (instanceName.equalsIgnoreCase("_wifidemotest")) {

                            // update the UI and add the item the discovered
                            WiFiDirectServicesList fragment = (WiFiDirectServicesList) getSupportFragmentManager().findFragmentByTag("services");
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
                Toast.makeText( MainActivity.this, "Discover Success", Toast.LENGTH_SHORT).show();
            }

            public void onFailure(int arg0) {
                Log.d("DiscoverService ", " Failed" + arg0);
                Toast.makeText( MainActivity.this , "Discover Service failed", Toast.LENGTH_SHORT).show();
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

        Toast.makeText( this , "Discover Service failed", Toast.LENGTH_SHORT).show();

    }

}
