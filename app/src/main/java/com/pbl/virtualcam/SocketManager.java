package com.pbl.virtualcam;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Vector;

public class SocketManager{
    private DatagramSocket serverSocket;
    public static Vector<SocketHandler> socketSet=new Vector<SocketHandler>();
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
                if(!message.trim().equals("Connect to VCam")){
                    continue;
                }
                SocketHandler socketHandler=new SocketHandler(receivePacket.getAddress(),receivePacket.getPort());
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
    private int lenPac = 10000;
    private byte[] bytes;
    private long timeStamp;
    public SocketHandler( InetAddress _clientAddress, int _clientPort) {
        this.clientAddress=_clientAddress;
        this.clientPort=_clientPort;
        try {
            this.serverSocket=new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
    public void run(){
        while (true){
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
    public String getClientAddress(){
        return this.clientAddress.getHostAddress();
    }

}
