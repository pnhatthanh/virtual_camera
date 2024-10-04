package com.pbl.virtualcam;

import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Vector;

public class SocketManager{
    private DatagramSocket serverSocket;
    public static Vector<SocketHandler> socketSet=new Vector<SocketHandler>();
    static byte[] dataToSend;

    public SocketManager(int port){
        try{
            serverSocket=new DatagramSocket(port);
            while(true){
                byte[] receiveData=new byte[1024];
                DatagramPacket receivePacket=new DatagramPacket(receiveData,receiveData.length);
                serverSocket.receive(receivePacket);
                String message=new String(receivePacket.getData());
                if(!message.trim().equals("Connect to VCam")){
                    continue;
                }
                SocketHandler socketHandler=new SocketHandler(serverSocket,receivePacket.getAddress(),receivePacket.getPort());
                socketSet.add(socketHandler);
                socketHandler.start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

class SocketHandler extends Thread{
    private DatagramSocket serverSocket;
    private InetAddress clientAddress;
    private int clientPort;

    public SocketHandler(DatagramSocket _socket, InetAddress _clientAddress, int _clientPort) {
        this.serverSocket=_socket;
        this.clientAddress=_clientAddress;
        this.clientPort=_clientPort;
    }
    public void run(){
        byte[] dataToSend;
        while (true){
            dataToSend=SocketManager.dataToSend;
            try{
                DatagramPacket sendPacket=new DatagramPacket(dataToSend,dataToSend.length,clientAddress,clientPort);
                serverSocket.send(sendPacket);
                Thread.sleep(70);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public InetAddress getClientAddress(){
        return this.clientAddress;
    }
}