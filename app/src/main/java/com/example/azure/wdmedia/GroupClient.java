package com.example.azure.wdmedia;

/**
 * Created by baiqiaoyu on 2017/6/5.
 */

        import android.app.ActivityManager;
        import android.net.wifi.p2p.WifiP2pInfo;
        import android.util.Log;

        import java.io.DataInputStream;
        import java.io.DataOutputStream;
        import java.io.IOException;
        import java.io.InterruptedIOException;
        import java.io.ObjectInputStream;
        import java.io.ObjectOutputStream;
        import java.net.InetSocketAddress;
        import java.net.ServerSocket;
        import java.net.Socket;
        import java.nio.channels.ClosedByInterruptException;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Map;
        import java.util.HashMap;

public class GroupClient implements Runnable {
    private Socket clientSocket;
    private Socket mtclientSocket;
    private WifiP2pInfo info;
    private int groupPort = 8901;
    private int maintainPort = 8989;
    private HashMap<String, ServiceInfo> serviceSupply;
    private Thread thread;
    private boolean getlistcomplete;
    private boolean connectcomplete = false;

    public GroupClient(WifiP2pInfo info){
        this.info = info;
        serviceSupply = new HashMap<String, ServiceInfo>();
    }

    public void sendInfo(ServiceInfo Info){

        try {
            getlistcomplete = false;
            ObjectOutputStream ObjectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectOutputStream.writeObject(Info);
            ObjectOutputStream.flush();
            Log.d("Client --"," sendInfo success");


        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public HashMap<String, ServiceInfo> getServiceSupply(){
        return serviceSupply;
    }

    public void Stop(){
        try{
            connectcomplete = false;
            if(clientSocket != null){
                clientSocket.close();
            }
            if(mtclientSocket != null){
                mtclientSocket.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isGetlistcomplete(){
        return getlistcomplete;
    }

    public boolean isConnectcomplete() { return connectcomplete; }

    public class mtclientSocketThread implements Runnable{

        public mtclientSocketThread(){}

        public void run(){
            try{
                mtclientSocket = new Socket(info.groupOwnerAddress.getHostAddress(), maintainPort);
                ObjectOutputStream OOS = new ObjectOutputStream(mtclientSocket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(mtclientSocket.getInputStream());
                ServiceInfo checkInfo = new ServiceInfo();
                checkInfo = checkInfo.setfuncname("check");

                while(true) {
                    input.readObject();
                    OOS.writeObject(1);
                    OOS.flush();
                    Log.d("GroupClient --", " get check message");
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void run() {
        try{

            clientSocket = new Socket(info.groupOwnerAddress.getHostAddress(), groupPort);
            mtclientSocketThread mtcleint = new mtclientSocketThread();
            thread = new Thread(mtcleint);
            thread.start();
            Log.d("@@@@@@@@@"," Client connect success");
            connectcomplete = true;
            ObjectInputStream input = null;

            try{
                while(true){
                    input = new ObjectInputStream(clientSocket.getInputStream());
                    Log.d("client --"," get List success");
                    serviceSupply = (HashMap<String, ServiceInfo>) input.readObject();
                    getlistcomplete = true;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
