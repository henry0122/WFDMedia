package com.example.azure.wdmedia;

/**
 * Created by baiqiaoyu on 2017/6/5.
 */

        import android.app.Service;
        import android.net.wifi.p2p.WifiP2pInfo;
        import android.util.Log;
        import android.widget.Toast;

        import java.io.DataInputStream;
        import java.io.DataOutputStream;
        import java.io.IOException;
        import java.io.ObjectInputStream;
        import java.io.ObjectOutputStream;
        import java.net.InetSocketAddress;
        import java.net.ServerSocket;
        import java.net.Socket;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Timer;
        import java.util.TimerTask;


public class ReqClient implements  Runnable {
    private Socket ReqClientSocket;
    private int reqPort = 8921;
    private ServiceInfo serviceInfo;

    public ReqClient(ServiceInfo input){
        serviceInfo = input;
    }

    public void stop() {

        if(ReqClientSocket != null){
            try{
                ReqClientSocket.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void run(){
        try {
            ReqClientSocket = new Socket(serviceInfo.getD_ID(), reqPort);
            ObjectOutputStream OOS = new ObjectOutputStream(ReqClientSocket.getOutputStream());
            OOS.writeObject(serviceInfo);
            OOS.flush();
            Log.d("Request cli", "send req success");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

