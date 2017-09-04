package com.example.azure.wdmedia;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by pohan on 2017/6/25.
 */

public class ReqServer implements Runnable {

    private ServerSocket ReqServerSocket;
    private int reqPort = 8921;
    ObjectInputStream OIS;
    RequestListener requestListener;

    public ReqServer() {
        OIS = null;
    }

    public void setOnRequestListener(RequestListener rlis){
        requestListener = rlis;
    }

    public class RequestThread implements Runnable {
        private Socket clientSocket;
        private ServiceInfo serviceInfo;

        @Override
        public void run() {

        }

        public RequestThread(Socket socket, ServiceInfo serviceInfo){
            this.clientSocket = socket;
            this.serviceInfo = serviceInfo;
        }

    }


    public void run() {

        Log.d("ReqServer --","ReqServer start");
        try{

            ReqServerSocket = new ServerSocket(reqPort);

            while(true) {

                Socket socket = ReqServerSocket.accept();
                OIS = new ObjectInputStream(socket.getInputStream());
                ServiceInfo input = (ServiceInfo)OIS.readObject();
                input.setRID(socket.getInetAddress().getHostAddress());
//                RequestThread requestThread = new RequestThread(socket, input);
//                Thread thread = new Thread(requestThread);
//                thread.start();
                if(input.getOther().equals("Audio")) {

                    requestListener.SendData(input.getR_ID());

                }
                else {

                    requestListener.SendCurTimeTest(input.getR_ID());
                    Log.d("ReqServer", "send video");

                }

                Log.d("ReqServer --","get Request");

            }

        } catch(Exception e){
            e.printStackTrace();
        }

    }

    public interface RequestListener {

        void SendData(String ip);

        void SendCurTimeTest(String ip);

    }

}
