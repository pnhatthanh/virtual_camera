package com.pbl.virtualcam;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class SocketManager{
    private DatagramSocket serverSocket;
    public static Map<String, SocketHandler> socketSet=new HashMap<>();
    static public byte[] bytes;
    static public long timeStamp = -1;
    public SocketManager(int port){
        try{
            serverSocket=new DatagramSocket(port);
            while(true){
                byte[] receiveData=new byte[10000];
                DatagramPacket receivePacket=new DatagramPacket(receiveData,receiveData.length);
                serverSocket.receive(receivePacket);
                String message=new String(receivePacket.getData());
                Log.i("Message",message.trim());
                if(message.trim().equals("Connect to VCam")){
                    SocketHandler socketHandler=new SocketHandler(serverSocket,receivePacket.getAddress(),receivePacket.getPort());
                    socketSet.put(receivePacket.getAddress().getHostAddress(),socketHandler);
                    socketHandler.start();
                    continue;
                }
                if(message.trim().equals("Disconnect to VCam")){
                    Log.i("connect","hhhh");
                    if(socketSet.get(receivePacket.getAddress().getHostAddress())==null){
                        continue;
                    }
                    socketSet.get(receivePacket.getAddress().getHostAddress()).StopThread();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

class SocketHandler extends Thread{
    private DatagramSocket serverSocket;
    private InetAddress clientAddress;
    private boolean isRunning;
    private int clientPort;
    private int lenPac = 10000;
    private byte[] bytes;
    private long timeStamp;
    public SocketHandler(DatagramSocket serverSocket, InetAddress _clientAddress, int _clientPort) {
        this.clientAddress=_clientAddress;
        this.clientPort=_clientPort;
        this.serverSocket=serverSocket;
        this.isRunning=true;
    }
    public void run(){
        while (isRunning){
            try{
                byte[] dataToSend = new byte[lenPac + 10];
                this.bytes = SocketManager.bytes;
                this.timeStamp = SocketManager.timeStamp;
                int numPac = (int)Math.ceil(this.bytes.length*1.0/this.lenPac);
                for (int i = 0; i<numPac; i++){
                    int realLenPac = this.lenPac;
                    if (this.bytes.length - i * this.lenPac < realLenPac)
                        realLenPac = this.bytes.length - i * this.lenPac;
                    dataToSend[0] = (byte) (i);
                    dataToSend[1] = (byte) numPac;
                    for (int j = 2; j <= 9; j++) {
                        dataToSend[j] = (byte) ((this.timeStamp >> (7 - (j - 2)) * 8) & 255);
                    }
                    System.arraycopy(this.bytes,i * this.lenPac, dataToSend,10,realLenPac);
                    DatagramPacket pacToSend = new DatagramPacket(dataToSend,0,realLenPac + 10,this.clientAddress,this.clientPort);
                    this.serverSocket.send(pacToSend);
                    Thread.sleep(10);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void StopThread(){
        isRunning=false;
        SocketManager.socketSet.remove(this.getClientAddress());
    }
    public String getClientAddress(){
        return this.clientAddress.getHostAddress();
    }

}
