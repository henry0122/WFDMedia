package com.example.azure.wdmedia;

/**
 * Created by baiqiaoyu on 2017/6/5.
 */

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
        import java.util.Timer;
        import java.util.TimerTask;
        import java.util.Iterator;

public class GroupServer implements Runnable{
    private ServerSocket GSsocket;
    private ServerSocket maintainSocket;
    private List<String> groupMember;
    private Map<String, ServiceInfo> serviceSupply;
    private int groupPort = 8901;
    private int maintainPort = 8989;
    private WifiP2pInfo info;
    private HashMap<Socket, Socket> socketList;
    private List<Socket> mtsocketList;
    private HashMap<String, Thread> requestThreadPool;
    private List<MTReqestThread> mtrequestThreadPool;
    private List<Socket> resocketlist;
    private Thread thread1;
    private int keynum = 1;
    private boolean ontask;
    private boolean finishcreate = false;

    public GroupServer(WifiP2pInfo info) {
        //this.s = s;
        this.info = info;
        finishcreate = false;
        groupMember = new ArrayList<String>();
        socketList = new HashMap<Socket, Socket>();
        mtsocketList = new ArrayList<Socket>();
        requestThreadPool = new HashMap<String, Thread>();
        serviceSupply = new HashMap<String, ServiceInfo>();
        mtrequestThreadPool = new ArrayList<MTReqestThread>();
        resocketlist = new ArrayList<Socket>();

    }

    public void showList(){
        for(String key : serviceSupply.keySet()){
            System.out.println(key + " : " + serviceSupply.get(key).getOther());
            System.out.println(serviceSupply.size());
        }
    }

    public boolean getInfo(ServiceInfo inputInfo){
        if(inputInfo.getFuncname().equals("add")){
            Log.d("Get in ","function one");
            serviceSupply.put(inputInfo.getD_ID(), inputInfo);
            showList();
            return false;
        }else if(inputInfo.getFuncname().equals("delete")){
            serviceSupply.remove(inputInfo.getD_ID());
            return false;
        }else if(inputInfo.getFuncname().equals("getSupplyList")){
            Log.d("Get in ","funciton two");
            return true;
        }else if(inputInfo.getFuncname().equals("buffer")) {
            broadcast(inputInfo);
            return false;
        }else{
            return false;
        }
    }

    public class RequestThread implements Runnable {
        private Socket clientSocket;

        public RequestThread(Socket clientSocket){
            this.clientSocket = clientSocket;
        }

        public void run(){
            ObjectOutputStream output = null;
            ObjectInputStream input = null;
            ServiceInfo inputInfo = null;


                try {
                    while(true) {
                        input = new ObjectInputStream(this.clientSocket.getInputStream());
                        Log.d("Server --", " get input");
                        inputInfo = (ServiceInfo) input.readObject();
                        inputInfo.setDID(clientSocket.getInetAddress().getHostAddress());
                        Log.d("Input data is", "" + inputInfo.getFuncname());

                        if (getInfo(inputInfo) == true) {
                            output = new ObjectOutputStream(this.clientSocket.getOutputStream());
                            Log.d("Server --", " give client supply list");
                            output.writeObject(serviceSupply);
                            output.flush();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

    }

    public class MTReqestThread implements Runnable{
        private Socket mtclientSocket;
        private boolean keeptimer;
        ObjectOutputStream OOS ;
        ObjectInputStream OIS ;
        private Timer timer;

        public MTReqestThread(Socket mtclientSocket){
            this.mtclientSocket = mtclientSocket;
            OOS = null;
            OIS = null;
        }

        public void closetimer(){
            keeptimer = false;
        }

        public void sendList(){
            ontask = true;
            Log.d("debug","!!!!!!");
            try{
                OOS.writeObject(1);
                OOS.flush();
                OIS.readObject();
                Log.d("groupServer --","get re ");
            }catch(Exception e){
                Log.d("groupMaintain--", "send faild "+" "+groupMember.size()+" "+mtsocketList.size()+" "+mtclientSocket.getInetAddress().getHostAddress());
                socketList.remove(mtclientSocket);
                groupMember.remove(mtclientSocket.getInetAddress().getHostAddress());
                serviceSupply.remove(mtclientSocket.getInetAddress().getHostAddress());
                mtsocketList.remove(mtclientSocket);
                keeptimer = false;
                e.printStackTrace();
                Log.d("groupMaintain--", "delete success");
            }
            ontask = false;
        }

        public void run(){
            try {
                OOS = new ObjectOutputStream(mtclientSocket.getOutputStream());
                OIS = new ObjectInputStream(mtclientSocket.getInputStream());
            }catch(Exception e){
                e.printStackTrace();
            }
            timer = new Timer();
            keeptimer = true;
            timer.schedule(task,0,5000);
        }
        private TimerTask task = new TimerTask() {
            public void run() {
                Log.d("groupMaintain", " renew list");
                if(keeptimer == true) {
                    sendList();
//                    Log.d("mtThreadPool " ," "+mtrequestThreadPool.size());
                }else{
                    Log.d("debug --", " cancel timer");
                    timer.cancel();
                }
            }
        };
    }

    public void Stop() {
        while(ontask){}
        try{
            if(GSsocket != null){
                GSsocket.close();
            }
            if(maintainSocket != null){
                maintainSocket.close();
            }
            if(serviceSupply != null){
                serviceSupply = null;
            }
            for(int i=0; i< mtrequestThreadPool.size(); i++){
                mtrequestThreadPool.get(i).closetimer();
            }

            for(int i=0; i < mtsocketList.size();i++){
                Log.d("debug --"," del");
                mtsocketList.get(i).close();
            }
            for(int i=0; i< resocketlist.size(); i++){
                resocketlist.get(i).close();
            }
            Log.d("debug --","  Stop!!!!");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void broadcast(ServiceInfo serviceInfo){
        try{
            for(Map.Entry<Socket, Socket> iterator : socketList.entrySet()){
                try {
                    ObjectOutputStream OOS = new ObjectOutputStream(iterator.getValue().getOutputStream());
                    OOS.writeObject(serviceInfo);
                    OOS.flush();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /*public void sendList(){
        ServiceInfo checkInfo = new ServiceInfo();
        checkInfo =checkInfo.setfuncname("check");
        try{
            for(Socket socket : mtsocketList){
                try{
                    ObjectOutputStream OOS =new ObjectOutputStream(socket.getOutputStream());
                    OOS.writeObject(checkInfo);
                    OOS.flush();
                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                    Log.d("groupServer --","get re ");
                } catch(Exception e){
                    Log.d("groupMaintain--", "send faild");
                    e.printStackTrace();
                    socketList.remove(socket);
                    groupMember.remove(socket.getInetAddress().getHostAddress());
                    serviceSupply.remove(socket.getInetAddress().getHostAddress());
                    mtsocketList.remove(socket);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }*/

    public boolean getfinish(){
        return finishcreate;
    }

    public void run(){
        Log.d("GroupServer --","GroupServer Start ");
        try{
            GSsocket = new ServerSocket(groupPort);
            maintainSocket = new ServerSocket(maintainPort);
            finishcreate = true;
            while(true) {
                Socket socket = GSsocket.accept();
                Socket mtsocket = maintainSocket.accept();
                Log.d("Server --"," get connect with client");
                groupMember.add(socket.getInetAddress().getHostAddress());
                resocketlist.add(socket);
                socketList.put(mtsocket, socket);
                mtsocketList.add(mtsocket);
                RequestThread requestThread = new RequestThread(socket);
                Thread thread2 = new Thread(requestThread);
                thread2.start();

                MTReqestThread mtReqestThread = new MTReqestThread(mtsocket);
                Thread thread3 = new Thread(mtReqestThread);
                mtrequestThreadPool.add(mtReqestThread);
                thread3.start();

                requestThreadPool.put(socket.getInetAddress().getHostAddress(), thread2);

            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

}
