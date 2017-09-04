package com.example.azure.wdmedia;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by azure on 2017/8/7.
 */

public class SyncServer implements Runnable , AudioPlayerFragment.HasChangedListen {

    private int syncPort = 9453;
    private ServerSocket syncServerSocket;
    private HashMap<String, Socket> syncMember = new HashMap<String, Socket>();
    private boolean isProvider;
    private boolean hasChange;
    private Thread handleChangeThread;

    public SyncServer(boolean b)
    {
        isProvider = b;
        hasChange = false;
    }

    @Override
    public void run()
    {
        try
        {
            if(isProvider)
            {

                syncServerSocket = new ServerSocket(syncPort);

                while (true) {
                    System.out.println("Fuckin close or not: " + syncServerSocket.isClosed());
                    Socket acceptSocket = syncServerSocket.accept();
                    System.out.println("ServerSocket accept in SyncServer's run().");

                    // record a client
                    syncMember.put(acceptSocket.getInetAddress().getHostAddress(), acceptSocket);

                    System.out.println("SyncServer record : " + acceptSocket.getInetAddress().getHostAddress());

//                    HandleChange hc = new HandleChange();
//                    handleChangeThread = new Thread(hc);
//                    handleChangeThread.start();
                }
            }
            else {}
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in SyncServer's try of run().");}
    }

    public class HandleChange implements Runnable
    {
        @Override
        public void run()
        {
            while (true)
            {
                // Send message to client only if change
                //while (!hasChange) {}

            }
        }
    }

    public void Stop(){
        try{
            hasChange = false;
            if(syncServerSocket != null){
                syncServerSocket.close();
            }
            for(Map.Entry<String,Socket> e: syncMember.entrySet()) {
                if (e.getValue() != null) {
                    e.getValue().close();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void hasChanged(boolean b)
    {
        hasChange = b;
            if(hasChange)
            {
            String send = "FUCKCHANGE";
            byte[] bb = send.getBytes();
            System.out.println("SyncServer's inner class Bytes length = "+ bb.length);
            for(Map.Entry<String,Socket> e: syncMember.entrySet())
            {
                System.out.println("SyncServer inner class send one ip: "+ e.getKey());
                try {
                    OutputStream os = e.getValue().getOutputStream();
                    os.write(bb);
                    os.flush();
                    os.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.out.println("Error in SyncServer's inner class-HandleChange's run().");
                }
            }
            hasChange = false;
        }
        System.out.println("SyncServer hasChanged : " + hasChange);
    }
}
