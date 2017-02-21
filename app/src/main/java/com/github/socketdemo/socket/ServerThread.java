package com.github.socketdemo.socket;

import com.github.socketdemo.bean.Transmission;
import com.github.socketdemo.utils.Base64Utils;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;

/**
 * Created by boby on 2017/2/16.
 */

public class ServerThread implements Runnable {

    Socket mSocket;
    BufferedReader mBufferedReader;
    Gson mGson;

    boolean mCreateFile = true;

    public ServerThread(Socket socket) throws IOException {

        mGson = new Gson();
        mSocket = socket;
        mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "utf-8"));
        //新开线程给客服端发送消息
        new Thread() {
            @Override
            public void run() {
                super.run();
                sendMessage();
            }
        }.start();
    }

    @Override
    public void run() {
        readMessage();
    }

    //读取的数据发送给客户端
    private void readMessage() {
        String content = null;
        FileOutputStream fos = null;
        try {
            while ((content = mBufferedReader.readLine()) != null) {
                Transmission trans = mGson.fromJson(content, Transmission.class);
                if (trans.transmissionType == Constants.TRANSFER_STR) {
                    System.out.println("" + content);
                    for (Iterator<Socket> it = MyServer.sSockets.iterator();
                         it.hasNext(); ) {
                        if (it == null) {
                            break;
                        }
                        Socket s = it.next();
                        try {

                            PrintWriter printWriter = new PrintWriter(s.getOutputStream());
                            printWriter.write(content + "\r\n");
                            printWriter.flush();

                        } catch (SocketException e) {
                            e.printStackTrace();
                            it.remove();
                        }
                    }
                } else {
                    long fileLength = trans.fileLength;
                    long transLength = trans.transLength;
                    if (mCreateFile) {
                        mCreateFile = false;
                        fos = new FileOutputStream(new File("d:/" + trans.fileName));
                    }
                    byte[] b = Base64Utils.decode(trans.content.getBytes());
                    fos.write(b, 0, b.length);
                    System.out.println("接收文件进度" + 100 * transLength / fileLength + "%...");
                    if (transLength == fileLength) {
                        mCreateFile = true;

                        fos.flush();
                        fos.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (fos != null) {
                    fos.close();
                }
                if (mBufferedReader != null) {
                    mBufferedReader.close();
                }
                if (mSocket != null) {
                    mSocket.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            MyServer.sSockets.remove(mSocket);
        }
    }

    //发送消息给连接的客服端
    private void sendMessage() {
        BufferedReader bufferedReader = null;
        try {
            while (true) {
                bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("请输入发送的字符串：");
                String str = bufferedReader.readLine();
                for (Iterator<Socket> it = MyServer.sSockets.iterator();
                     it.hasNext(); ) {
                    if (it == null) {
                        break;
                    }
                    Socket s = it.next();
                    try {
                        Transmission trans = new Transmission();
                        trans.itemType = Constants.CHAT_FROM;
                        trans.transmissionType = Constants.TRANSFER_STR;
                        trans.content = str;

                        PrintWriter printWriter = new PrintWriter(s.getOutputStream());
                        printWriter.write(mGson.toJson(trans) + "\r\n");
                        printWriter.flush();

                    } catch (SocketException e) {
                        e.printStackTrace();
                        s.close();
                        it.remove();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                MyServer.sSockets.remove(mSocket);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
