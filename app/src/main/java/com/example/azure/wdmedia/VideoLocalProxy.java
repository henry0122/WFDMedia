package com.example.azure.wdmedia;

import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import static com.example.azure.wdmedia.MainActivity.myPlayer;

public class VideoLocalProxy implements Runnable, VideoPlayerFragment.VideoListen {

    private static final String TAG = VideoLocalProxy.class.getSimpleName();

    private int localPort = 7777;
    private int remotePort = 7878;

    private ServerSocket mServerSocket;
    private Socket client;
    private File mVideoFile;

    private String name = null;
    private String path = null;

    private Thread linkOutThread;
    private boolean isRunning = true;

    private ExternalResourceDataSource dataSource;
    private ServerProxy serverProxy;
    private Thread thread;
    private String otherid;
    public static int curTime;

    //private LinkIPListen link;

    public VideoLocalProxy() {
        String fileName = "myvideo.mp4";
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        mVideoFile = new File(path, fileName);
    }

    public String getUrl() {
        return "http://127.0.0.1:7777";
    }

    public void setOtherid(String s){
        otherid = s;
    }

    @Override
    public void pathChange(String name, String path) {
        this.name = name;
        this.path = path;
        mVideoFile = new File(path);
    }

    @Override
    public void run() {

        serverProxy = new ServerProxy();
        thread = new Thread(serverProxy);
        thread.start();
        int x = 0;
        int s;
        byte[] buf = new byte[1024];
        try {
            // Create a server
            mServerSocket = new ServerSocket(localPort);

            while(true) {

                client = mServerSocket.accept();

                Log.d(TAG, "Client link in "+x);
                x++;
                Socket linkout = new Socket(otherid, remotePort);

                LinkOutWrite linkOutWrite = new LinkOutWrite(client, linkout);

                linkOutThread = new Thread(linkOutWrite);
                linkOutThread.start();
//
                InputStream input = null;
                OutputStream output = null;

                try {
                    input = linkout.getInputStream();
                    output = client.getOutputStream();

                    while (-1 != (s = input.read(buf))) {
                        output.write(buf, 0, s);
                    }
                    Log.d("VideoLocalProxy client", "end");
                } catch (SocketException e) {

                    // Ignore when the client breaks connection
                    Log.e("gggg", "Ignoring " + e.getMessage());
                    linkout.close();
                    output.close();
                } catch (IOException e) {
                    Log.e("gggg", "Error getting content stream.", e);

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class LinkOutWrite implements Runnable{
        Socket client;
        Socket linkout;
        InputStream input;
        OutputStream output;
        int s;
        byte[] buf = new byte[1024];
        public LinkOutWrite(Socket socket, Socket Linkout){
            client = socket;
            linkout = Linkout;
        }

        @Override
        public void run() {
            try {
                input = client.getInputStream();
                output = linkout.getOutputStream();

//                Log.d("VideoLocalProxy","linkout "+client.getRemoteSocketAddress());
//                Log.d("VideoLocalProxy","linkout ip"+linkout.getRemoteSocketAddress());
//                Log.d("VideoLocalProxy","linkout "+linkout.isConnected());

                while ( -1 != (s = input.read(buf))) {
                    output.write(buf, 0, s);
                    Log.d("VideoLocalProxy", "linkout write:"+s+" "+client.getRemoteSocketAddress());
                    break;
                }
                Log.d("VideoLocalProxy linkout","end");
            } catch (IOException e) {
                e.printStackTrace();
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
            String headers = "";
            headers += "GET / HTTP/1.1\r\n";
            headers += "Accept-Encoding: gzip2\r\n";
            headers += "Accept-Encoding: gzip3\r\n";
            headers += "Accept-Encoding: gzip4\r\n";
            headers += "Position: bytes="+myPlayer.getCurrentPosition() + "-\r\n";
            headers += "\r\n";

            output.write(headers.getBytes(),0,headers.getBytes().length);
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

    protected class ServerProxy implements Runnable {

        private ServerSocket proxyServer;

        public ServerProxy () {}

        @Override
        public void run() {
            try {
                proxyServer = new ServerSocket(remotePort);
                Log.d("ServerProxy ", "@@@---Start---@@@");

                while (isRunning) {

                    Socket socket = proxyServer.accept();
                    Log.e(TAG, "link ip:"+socket.getInetAddress().getHostAddress());
                    // use listener to tell SyncServer to add ip
                    //link.addLinkIP(socket.getInetAddress().getHostAddress());

                    handleResponse(socket);

                }

            } catch (IOException e) {
                Log.d("ServerProxy ", "@@@---Failed---@@@");
                e.printStackTrace();
            }
        }

        public void handleResponse(Socket socket) throws IOException {

            InputStream is = socket.getInputStream();
            dataSource = new ExternalResourceDataSource(mVideoFile);
            // Find the header
            final int bufsize = 8192;
            byte[] buf = new byte[bufsize];
            int splitbyte;
            int rlen = 0;
            int read;
            while ((read = is.read(buf, rlen, bufsize - rlen)) > 0) {
                rlen += read;
                splitbyte = findHeaderEnd(buf, rlen);

                if (splitbyte > 0) break;
            }

            // Create a BufferedReader for parsing the header.
            ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, rlen);
            BufferedReader hin = new BufferedReader(new InputStreamReader(hbis));
            Properties pre = new Properties();
            Properties parms = new Properties();
            Properties header = new Properties();

            try {
                decodeHeader(hin, pre, parms, header);
            } catch (InterruptedException e1) {
                Log.e(TAG, "Exception: " + e1.getMessage());
                e1.printStackTrace();
            }

            for (Map.Entry<Object, Object> e : header.entrySet()) {
                Log.e(TAG, "Header: " + e.getKey() + " : " + e.getValue());
            }

            String range = header.getProperty("range");
            long cbSkip = 0;
            boolean seekRequest = false;
            if (range != null) {
                Log.e(TAG, "range is: " + range);

                seekRequest = true;
                range = range.substring(6);
                int charPos = range.indexOf('-');
                if (charPos > 0) {
                    range = range.substring(0, charPos);
                }
                cbSkip = Long.parseLong(range);

                Log.e(TAG, "range found!! " + cbSkip);
            }

            String position = header.getProperty("position");

            if(position != null){
                Log.e(TAG, "123position is: " + position);

                position = position.substring(6);
                int charPos = position.indexOf('-');
                if (charPos > 0) {
                    position = position.substring(0, charPos);
                }
                curTime = Integer.parseInt(position);

                Log.e(TAG, "position found!! " + curTime);
                return;
            }

            String headers = "";
            if (seekRequest) {      // It is a seek or skip request if there's a Range
                headers += "HTTP/1.1 206 Partial Content\r\n";
                headers += "Content-Type: " + dataSource.getContentType() + "\r\n";
                headers += "Accept-Ranges: bytes\r\n";
                headers += "Content-Length: " + (dataSource.getContentLength(false)) + "\r\n";
                headers += "Content-Range: bytes " + cbSkip + "-" + (dataSource.getContentLength(true)) + "\r\n";
                headers += "\r\n";
            } else {
                headers += "HTTP/1.1 200 OK\r\n";
                headers += "Content-Type: " + dataSource.getContentType() + "\r\n";
                headers += "Accept-Ranges: bytes\r\n";
                headers += "Content-Length: " + (dataSource.getContentLength(false)) + "\r\n";
                headers += "\r\n";
            }

            // Load and send content
            InputStream data = null;
            try {
                // Send header
                byte[] buffer = headers.getBytes();
                Log.e(TAG, "writing to client bytes:" + buffer.length);
                Log.e(TAG, "ip:"+socket.getInetAddress().getHostAddress());
                Log.e(TAG, "isConnected:"+socket.isConnected());
//                Log.e(TAG, "");
                socket.getOutputStream().write(buffer, 0, buffer.length);

                // Start sending content.
                data = dataSource.createInputStream();
                byte[] buff = new byte[(int) mVideoFile.length()];

                Log.e(TAG, "No of bytes skipped: " + data.skip(cbSkip));
                int cbSentThisBatch = 0;
                while (isRunning) {
                    int cbRead = data.read(buff);

                    socket.getOutputStream().write(buff);
                    socket.getOutputStream().flush();

                    cbSkip += cbRead;
                    cbSentThisBatch += cbRead;
                }
                Log.e(TAG, "cbSentThisBatch: " + cbSentThisBatch);

                // If we did nothing this batch, block for a second
                if (cbSentThisBatch == 0) {
                    Log.e(TAG, "Blocking until more data appears");
                    Thread.sleep(1000);
                }
            } catch (SocketException e) {
                // Ignore when the client breaks connection
                Log.e(TAG, "Ignoring " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "Error getting content stream.", e);
            } catch (Exception e) {
                Log.e(TAG, "Error streaming file content.", e);
            } finally {
                if (data != null) data.close();
                Log.e(TAG, "finally");
            }
        }


        private int findHeaderEnd(final byte[] buf, int rlen) {
            int splitbyte = 0;
            while (splitbyte + 3 < rlen) {
                if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n'
                        && buf[splitbyte + 2] == '\r' && buf[splitbyte + 3] == '\n')
                    return splitbyte + 4;
                splitbyte++;
            }
            return 0;
        }

        private void decodeHeader(BufferedReader in, Properties pre,
                                  Properties parms, Properties header) throws InterruptedException {
            try {
                // Read the request line
                String inLine = in.readLine();
                if (inLine == null){
                    Log.e(TAG,"null request");
                    return;
                }
                Log.e(TAG,"inLine..."+inLine);
                StringTokenizer st = new StringTokenizer(inLine);
                if (!st.hasMoreTokens())
                    Log.e(TAG, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");

                String method = st.nextToken();
                pre.put("method", method);
                Log.e(TAG, "..."+method);

                if (!st.hasMoreTokens())
                    Log.e(TAG, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");

                String uri = st.nextToken();
                Log.e(TAG, "..."+uri);

                // Decode parameters from the URI
                int qmi = uri.indexOf('?');
                if (qmi >= 0) {
                    decodeParms(uri.substring(qmi + 1), parms);
                    uri = decodePercent(uri.substring(0, qmi));
                    Log.e(TAG, "have '?' ");
                } else {
                    uri = decodePercent(uri);
                    Log.e(TAG, "not have '?' ");
                }
                // If there's another token, it's protocol version,
                // followed by HTTP headers. Ignore version but parse headers.
                // NOTE: this now forces header names lowercase since they are
                // case insensitive and vary by client.
                if (st.hasMoreTokens()) {
                    Log.e(TAG, "has MoreTokens");
                    String line = in.readLine();
                    Log.e(TAG, "line..."+line);
                    while (line != null && line.trim().length() > 0) {
                        int p = line.indexOf(':');
                        Log.d("xxxxxx","xxxxxx");
                        if (p >= 0)
                            header.put(line.substring(0, p).trim().toLowerCase(), line.substring(p + 1).trim());
                        line = in.readLine();
                    }
                }

                pre.put("uri", uri);
            } catch (IOException ioe) {
                Log.e(TAG, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            }
        }

        private void decodeParms(String parms, Properties p) throws InterruptedException {
            if (parms == null) return;

            StringTokenizer st = new StringTokenizer(parms, "&");
            while (st.hasMoreTokens()) {
                String e = st.nextToken();
                int sep = e.indexOf('=');
                if (sep >= 0)
                    p.put(decodePercent(e.substring(0, sep)).trim(), decodePercent(e.substring(sep + 1)));
            }
        }

        private String decodePercent(String str) throws InterruptedException {
            try {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    switch (c) {
                        case '+':
                            sb.append(' ');
                            break;
                        case '%':
                            sb.append((char) Integer.parseInt(str.substring(i + 1, i + 3), 16));
                            i += 2;
                            break;
                        default:
                            sb.append(c);
                            break;
                    }
                }
                Log.e(TAG,sb.toString());
                return sb.toString();
            } catch (Exception e) {
                Log.e(TAG, "BAD REQUEST: Bad percent-encoding.");
                return null;
            }
        }

    }


    /**
     * provides meta-data and access to a stream for resources on SD card.
     */
    protected class ExternalResourceDataSource {

        private FileInputStream inputStream;
        private final File movieResource;
        long contentLength;

        public ExternalResourceDataSource(File resource) {
            movieResource = resource;
            Log.e(TAG, "resourcePath is: " + mVideoFile.getPath());
        }

        public String getContentType() { return "video/mp4"; }

        public InputStream createInputStream() throws IOException {
            getInputStream();
            return inputStream;
        }

        public long getContentLength(boolean ignoreSimulation) {
            if (!ignoreSimulation) return -1;
            return contentLength;
        }

        private void getInputStream() {
            try {
                inputStream = new FileInputStream(movieResource);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            contentLength = movieResource.length();
            Log.e(TAG, "file exists?? " + movieResource.exists() + " and content length is: " + contentLength);
        }

    }

//    public void setOnLinkIPListener(LinkIPListen l)
//    {
//        link = l;
//    }
//
//
//    public interface LinkIPListen
//    {
//        public void addLinkIP(String s);
//    }

}
