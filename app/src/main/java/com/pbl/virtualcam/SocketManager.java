package com.pbl.virtualcam;

import android.media.Image;
import android.os.Build;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


public class SocketManager{
    private ServerSocket serverSocket;
    public static Vector<SocketHandler> socketSet=new Vector<SocketHandler>();

    public SocketManager(int port){
        try{
            serverSocket =new ServerSocket(port);
            while(true){
                Socket socket= serverSocket.accept();
                SocketHandler socketHandler=new SocketHandler(socket);
                socketSet.add(socketHandler);
                socketHandler.start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void SendData(byte[] imageData){
        for(SocketHandler socketHandeler : socketSet){
            socketHandeler.SetImageData(imageData);
        }
    }
}

class SocketHandler extends Thread{
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private byte[] imageData;

    public SocketHandler(Socket _socket) {
        try {
            this.socket = _socket;
            this.dataOutputStream=new DataOutputStream(this.socket.getOutputStream());
            this.imageData=null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void run(){
        while (!socket.isClosed()){
            if(imageData==null) continue;
            try{
                dataOutputStream.writeInt(imageData.length);
                dataOutputStream.flush();
                dataOutputStream.write(imageData);
                dataOutputStream.flush();
                this.imageData=null;
                }catch (Exception e){
                }

        }
    }
    public void SetImageData(byte[] image){
        this.imageData=image;
    }
}