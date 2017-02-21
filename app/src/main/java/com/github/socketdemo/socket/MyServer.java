package com.github.socketdemo.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by boby on 2017/2/18.
 */

public class MyServer {

    //多客户端
    public static ArrayList<Socket> sSockets = new ArrayList<Socket>();

    public static void main(String[] args) {
        //DatagramSocket 基于UDP协议的
        ServerSocket serverSocket = null;

        try {
            //创建服务器的socket对象
            serverSocket = new ServerSocket(Constants.PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                sSockets.add(socket);

                new Thread(new ServerThread(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
