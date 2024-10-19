package com.pbl.virtualcam;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Vector;

public class SocketManager{
    private DatagramSocket serverSocket;
    public static Vector<SocketHandler> socketSet=new Vector<SocketHandler>();
    static public byte[] imgByte;
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
    private MediaCodec mediaCodec;
    private DatagramSocket serverSocket;
    private InetAddress clientAddress;
    private int clientPort;

    public SocketHandler(DatagramSocket _socket, InetAddress _clientAddress, int _clientPort) {

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

                byte[] imageByte = SocketManager.imgByte;

//                Log.i("lenBuffer", "run: "+rtpPacket.length);
//                DatagramPacket pac = new DatagramPacket(imageByte,imageByte.length,this.clientAddress,this.clientPort);
//                this.serverSocket.send(pac);
                int packetSize = 1024;  // Set the packet size (you can experiment with the size)

                int len = imageByte.length;
                for (int i = 0; i < len; i += packetSize) {
                    int length = Math.min(len - i, packetSize);
                    byte[] packet = new byte[length];
                    System.arraycopy(imageByte, i, packet, 0, length);

                    DatagramPacket datagramPacket = new DatagramPacket(packet, packet.length, this.clientAddress,this.clientPort);
                    this.serverSocket.send(datagramPacket);
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
}
