package com.github.socketdemo.socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.github.socketdemo.bean.Transmission;
import com.github.socketdemo.utils.Base64Utils;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by boby on 2017/2/18.
 */

public class ClientThread extends Thread {

    PrintWriter mPrintWriter;
    BufferedReader mBufferedReader;
    Socket mSocket;

    Handler mSendHandler;
    Handler mWriteHandler;

    Gson mGson;

    public ClientThread(Handler handler) {
        mSendHandler = handler;
        mGson = new Gson();
    }

    @Override
    public void run() {
        super.run();

        try {
            //创建socket
            mSocket = new Socket(Constants.HOST, Constants.PORT);
            //获取到读写对象
            mPrintWriter = new PrintWriter(mSocket.getOutputStream());
            mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

            //新开线程读取消息 并发送消息
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    String content = null;
                    try {
                        while ((content = mBufferedReader.readLine()) != null) {
                            Transmission trans = mGson.fromJson(content, Transmission.class);
                            if (trans.transmissionType == Constants.TRANSFER_STR) {
                                Message msg = new Message();
                                msg.what = Constants.RECEIVE_MSG;
                                msg.obj = content;
                                mSendHandler.sendMessage(msg);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            //当前线程创建 handler
            Looper.prepare();
            mWriteHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == Constants.SEND_MSG) {
                        mPrintWriter.write(msg.obj.toString() + "\r\n");
                        mPrintWriter.flush();
                    } else if (msg.what == Constants.SEND_FILE) {//传输文件
                        //定义标记判定是字符串还是文件
                        sendFile(msg.obj.toString());
                    }
                }
            };
            Looper.loop();

        } catch (IOException e) {
            e.printStackTrace();
            //出现异常关闭资源
            try {
                if (mPrintWriter != null) {
                    mPrintWriter.close();
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
        }

    }

    /**
     * 文件路径
     *
     * @param filePath
     */
    private void sendFile(String filePath) {
        FileInputStream fis = null;
        File file = new File(filePath);

        try {
            mSendHandler.sendEmptyMessage(Constants.PROGRESS);

            fis = new FileInputStream(file);

            Transmission trans = new Transmission();
            trans.transmissionType = Constants.TRANSFER_FILE;
            trans.fileName = file.getName();
            trans.fileLength = file.length();
            trans.transLength = 0;

            byte[] bytes = new byte[1024];
            int length = 0;
            while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                trans.transLength += length;
                trans.content = Base64Utils.encode(bytes);
                mPrintWriter.write(mGson.toJson(trans) + "\r\n");
                mPrintWriter.flush();

                Message message = new Message();
                message.what = Constants.PROGRESS;
                message.obj = 100 * trans.transLength / trans.fileLength;
                mSendHandler.sendMessage(message);
            }
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            mPrintWriter.close();
        }
    }

    public Handler getWriteHandler() {
        return mWriteHandler;
    }

    public void setWriteHandler(Handler writeHandler) {
        mWriteHandler = writeHandler;
    }

    public Handler getSendHandler() {
        return mSendHandler;
    }

    public void setSendHandler(Handler sendHandler) {
        mSendHandler = sendHandler;
    }
}
