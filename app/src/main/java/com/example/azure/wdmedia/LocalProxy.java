package com.example.azure.wdmedia;

/**
 * Created by user on 2017/2/19.
 */

import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.Buffer;
import java.util.ArrayList;

import static com.example.azure.wdmedia.MainActivity.myPlayer;
import com.example.azure.wdmedia.ReqServer.RequestListener;
import com.example.azure.wdmedia.AudioPlayerFragment.AudioListen;
/**
 * Created by user on 2017/2/18.
 */

public class LocalProxy implements Runnable , AudioListen {

    private final static int localPort = 6146;
    private final int remotePort = 6666;
    private volatile boolean isStop=false;
    ServerSocket serverSocket;
    Socket client = null;
    Socket linkToServer = null;
    Thread thread;
    ServerProxy serverProxy;
    private String linkOutIp;
    private byte[] LocalBuffer;
    private String name = null;
    private String path = null;

    public LocalProxy() {}

    public void Stop() {

        this.isStop=true;
        try {
            if(serverSocket != null)
                serverSocket.close();
            if(client != null)
                client.close();
            if(linkToServer != null)
                linkToServer.close();
            if(serverProxy != null)
                serverProxy.stop();
            if(thread != null) {
                thread.interrupt();
                thread = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        serverProxy = new ServerProxy();
        thread = new Thread(serverProxy);
        thread.start();

        try {
            Log.d("Localproxy", "@@@@@@--Proxy start--@@@@@@");
            serverSocket = new ServerSocket(localPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            while(true) {
                client = serverSocket.accept();
                Log.d("Localproxy ", "@@@@@@--Some one connect--@@@@@@--"+client.getInetAddress().getHostAddress());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    private void writeServerError(PrintStream output) {
        output.println("HTTP/1.0 500 Internal Server Error");
        output.flush();
    }

    private byte[] loadContent() throws IOException {
        InputStream input = null;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            Log.d("Content " , this.path+" "+this.name);


            //skip currentTime's byte of file

            File file = new File(this.path);

            input = new FileInputStream(file);
            input.skip((file.length()*myPlayer.getCurrentPosition())/myPlayer.getDuration());

            byte[] buffer = new byte[1024];
            int size;
            while (-1 != (size = input.read(buffer))) {
                output.write(buffer, 0, size);
                //Log.d("Size", Integer.toString(size));
            }
            output.flush();
            return output.toByteArray();
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            if (null != input) {
                input.close();
            }
        }
    }

    public void SendData(String ip) throws IOException {
        BufferedReader reader = null;
        PrintStream output = null;
        try {

            // Read HTTP headers and parse out the route.
            Socket socket = new Socket(ip, remotePort);

            // Output stream that we send the response to
            output = new PrintStream(socket.getOutputStream());

            byte[] bytes = loadContent();
            if (null == bytes) {
                Log.d("SendData", "write byte null");
                writeServerError(output);
                return;
            }

            // Send out the content.
            output.write(bytes);
            output.flush();

        }catch(Exception e){
            Log.d(" Send :  ", " failed" );
        }
        finally {

            if (output != null) {
                output.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    @Override
    public void pathChange(String name, String path) {
        this.name = name;
        this.path = path;
    }
    @Override
    public void stopLink(){
        try {
            if (client != null) {
                client.close();
            }
        }
        catch (Exception E){
            E.printStackTrace();
        }
    }

    public class ServerProxy implements Runnable{

        InputStream input;
        PrintStream output;
        ByteArrayOutputStream writeToBuffer;
        ServerSocket proxyServer;
        public void stop() throws IOException {
            if(proxyServer != null){
                proxyServer.close();
            }
        }

        @Override
        public void run() {
            proxyServer = null;
            try {
                proxyServer = new ServerSocket(remotePort);
                Log.d("ServerProxy ", "@@@---Start---@@@");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("ServerProxy ", "@@@---Failed---@@@");
            }
                try {
                    while(true) {

                        Socket socket = proxyServer.accept();
                        input = socket.getInputStream();
                        while (client == null) ;
                        while (!client.isConnected()) Log.d("Localproxy", " client link");
                        output = new PrintStream(client.getOutputStream());
                        writeToBuffer = new ByteArrayOutputStream();

                        try {

                            byte[] buffer = new byte[1024];
                            int size;
                            int tmp = 0;
                            while (-1 != (size = input.read(buffer))) {
                                writeToBuffer.write(buffer, 0, size);
                                tmp += size;
                            }

                            writeToBuffer.flush();
                            LocalBuffer = writeToBuffer.toByteArray();

                            output.println("HTTP/1.0 206 Partial Content");
                            output.println("Content-Type: " + "audio/mpeg3");
                            output.println("Content-Length: " + LocalBuffer.length);
                            output.println();
                            output.write(LocalBuffer);
                            Log.d("Recieve : ", "send " + Integer.toString(tmp) + " bytes done");

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
//                            if (output != null) {
//                                output.close();
//                            }
//                            if (client != null) {
//                                client.close();
//                                Log.d("Local","client close");
//                            }
//                            socket.close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


        }
    }



}

