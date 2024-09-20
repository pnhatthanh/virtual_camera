package com.pbl.virtualcam;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


public class SocketManager {
    private ServerSocket serverSocket;
    public static Vector<SocketHandler> socketSet=new Vector<SocketHandler>();

    public SocketManager(int port){
        try{
            serverSocket =new ServerSocket(port);
            while(true){
                Socket socket= serverSocket.accept();
                SocketHandler socketHandler=new SocketHandler(socket);
                socketSet.add(socketHandler);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void SendData(byte[] imageData){
        for(SocketHandler socketHandeler : socketSet){
            socketHandeler.SendImage(imageData);
        }
    }
}
class SocketHandler{
    private Socket socket;
    private DataOutputStream outputStream;
    public SocketHandler(Socket _socket){
        try{
            this.socket=_socket;
            outputStream=new DataOutputStream(socket.getOutputStream());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public void SendImage(byte[] imageData){
        new Thread(() -> {
            try {
                outputStream.writeInt(imageData.length);
                outputStream.write(imageData);
                outputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
