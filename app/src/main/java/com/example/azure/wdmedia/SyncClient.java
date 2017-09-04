package com.example.azure.wdmedia;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by azure on 2017/8/7.
 */

public class SyncClient implements Runnable {

    private int syncPort = 9453;
    private Socket syncClientSocket;
    private String serverId;
    private boolean isProvider;

    private InputStream is;

    private ChangeListener change;

    // Let client's AudioPlayerFragment know that the song has changed
    public void setOnChangeListener(ChangeListener med)
    {
        change = med;
    }

    public SyncClient(boolean b)
    {
        isProvider = b;
    }

    public SyncClient(String s, boolean b)
    {
        isProvider = b;
        serverId = s; // need the music source's ip
    }


    @Override
    public void run()
    {
        try {
            while (true) {
                if (!isProvider) // listen to other's music
                {

                    syncClientSocket = new Socket(serverId, syncPort);
                    System.out.println("Link to server in SyncClient's try of run().");

                    ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    is = syncClientSocket.getInputStream();
                    int length;
                    String result;
                    // continuously observe whether provider has changed music

                    while ((length = is.read(buffer)) != -1) {
                        System.out.println(" in SyncClient's length: " + length);
                        tmp.write(buffer, 0, length);
                    }
                    result = tmp.toString();
                    System.out.println("SyncClient result : " + result);

                    if (result.matches("FUCKCHANGE")) {
                        System.out.println("SyncClient change.media.");
                        change.mediaChange();
                    }
                    tmp.flush();

                }
                else{} // provide music to others, no need to get anything
            }
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Error in SyncClient's try of run().");}

    }

    public void Stop(){
        try{
            if(syncClientSocket != null){
                syncClientSocket.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public interface ChangeListener
    {
        void mediaChange();
    }
}
