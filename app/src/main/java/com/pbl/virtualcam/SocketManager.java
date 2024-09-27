package com.pbl.virtualcam;

import java.io.DataOutputStream;
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
        byte[] dataToSend;
        while (!socket.isClosed()){
            synchronized (this){
                if(imageData==null) continue;
                dataToSend=this.imageData;
                imageData=null;
            }
            try{
                dataOutputStream.writeInt(dataToSend.length);
                dataOutputStream.write(dataToSend);
            }catch (Exception e){
            }
        }
    }
    public synchronized void SetImageData(byte[] image){
        this.imageData=image;
    }
}