package com.pbl.virtualcam;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Vector;

public class SocketManager{
    private DatagramSocket serverSocket;
    public static Vector<SocketHandler> socketSet=new Vector<SocketHandler>();
    static byte[] dataToSend;
    //private static final SettingStorage setting = new SettingStorage(App.getContext());

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
    public static int getCompressQuality(){
//        String quality=setting.GetValue(ValueSetting.Quality,"Auto");
//        switch (quality){
//            case "Cao":
//                return 90;
//            case "Thấp":
//                return 50;
//            case "Trung bình":
//                return 75;
//            default:
//                return SocketHandler.quality;
//        }
        return 75;
    }
}

class SocketHandler extends Thread{
    private DatagramSocket serverSocket;
    private InetAddress clientAddress;
    private int clientPort;
    private long  lastSent=0;
    public static int quality=75;

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
                int packetSize = 1024;
                int len = dataToSend.length;
                if(this==SocketManager.socketSet.get(0)){
                    long start=System.nanoTime();
                    for (int i = 0; i < len; i += packetSize){
                        int length = Math.min(len - i, packetSize);
                        byte[] packet = new byte[length];
                        System.arraycopy(dataToSend, i, packet, 0, length);
                        DatagramPacket datagramPacket = new DatagramPacket(packet, packet.length, this.clientAddress,this.clientPort);
                        this.serverSocket.send(datagramPacket);
                    }
                    long takeTime=System.nanoTime()-start;
                    if(System.currentTimeMillis()-this.lastSent>=5000){
                        setQuality(len,(double) takeTime/1000000);
                        this.lastSent=System.currentTimeMillis();
                    }
                }else{
                    for (int i = 0; i < len; i += packetSize){
                        int length = Math.min(len - i, packetSize);
                        byte[] packet = new byte[length];
                        System.arraycopy(dataToSend, i, packet, 0, length);
                        DatagramPacket datagramPacket = new DatagramPacket(packet, packet.length, this.clientAddress,this.clientPort);
                        this.serverSocket.send(datagramPacket);
                    }
                }
                DatagramPacket datagramPacket = new DatagramPacket(new byte[]{1}, 1, this.clientAddress,this.clientPort);
                this.serverSocket.send(datagramPacket);
                Thread.sleep(100);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public InetAddress getClientAddress(){
        return this.clientAddress;
    }

    private void setQuality(int dataSize, double takenTime){
        //kB
        double bandWidth =  takenTime==0 ? (double) dataSize*1000/1024 : (dataSize*1000)/(1024*takenTime);
        Log.i("bandwidth: ",bandWidth+"");
        if(bandWidth>50000){
            quality=90;
        }else if(bandWidth>=30000 && bandWidth <= 50000){
            quality=75;
        }else if(bandWidth<30000){
            quality=50;
        }
    }
}
