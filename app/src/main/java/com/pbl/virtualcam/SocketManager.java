package com.pbl.virtualcam;

import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Vector;

public class SocketManager{
    private DatagramSocket serverSocket;
    public static Vector<SocketHandler> socketSet= new Vector<>();
    public static long timeStamp=-1;
    public static Bitmap bitmap=null;
    public static byte[][][] listImageByte=new byte[2][2][];

    public SocketManager(int port){
        try{
            serverSocket=new DatagramSocket(port);
            Thread process= new Thread(()->compressAndProcessImage());
            process.start();
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

    private static void compressAndProcessImage(){
        while (true){
            int[] pixels = new int[640*480];
            Bitmap[][] blocks=new Bitmap[2][2];
            Bitmap _bitmap=bitmap;
            if(_bitmap==null)
                continue;
            int blockWidth = _bitmap.getWidth() / 2;
            int blockHeight = _bitmap.getHeight() / 2;
            _bitmap.getPixels(pixels,0,_bitmap.getWidth(),0,0,_bitmap.getWidth(), _bitmap.getHeight());
            for (int x = 0; x < _bitmap.getWidth(); x++){
                for (int y = 0; y < _bitmap.getHeight(); y++){
                    if (blocks[x%2][y%2]==null){
                        blocks[x%2][y%2] = Bitmap.createBitmap(blockWidth,blockHeight, Bitmap.Config.ARGB_8888);
                    }
                    blocks[x%2][y%2].setPixel(x/2,y/2,pixels[y * _bitmap.getWidth() + x]);
                }
            }
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    blocks[row][col].compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    listImageByte[row][col] = byteArrayOutputStream.toByteArray();
                }
            }
        }
    }
}
class SocketHandler extends Thread{
    private DatagramSocket serverSocket;
    private InetAddress clientAddress;
    private int clientPort;
    private byte[][][] listImageByte;


    public SocketHandler(DatagramSocket _socket, InetAddress _clientAddress, int _clientPort) {
        this.serverSocket=_socket;
        this.clientAddress=_clientAddress;
        this.clientPort=_clientPort;
    }
    public void run(){
        this.listImageByte = SocketManager.listImageByte;
        while (true){
            try{
                long timeStamp = SocketManager.timeStamp;
                for (int row = 0; row < 2; row++) {
                    for (int col = 0; col < 2; col++) {
                        int length = this.listImageByte[row][col].length;
                        byte[] dataToSend = new byte[length + 9];
                        dataToSend[0] = (byte) ( row<<4 | col);
                        for (int j = 1; j <= 8; j++) {
                            dataToSend[j] = (byte) ((timeStamp >> (9 - j - 1) * 8) & 255);
                        }
                        System.arraycopy(this.listImageByte[row][col], 0, dataToSend, 9, length);
                        DatagramPacket packet = new DatagramPacket(dataToSend, dataToSend.length, this.clientAddress, this.clientPort);
                        this.serverSocket.send(packet);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public String getNameClient(){
        return this.clientAddress.getHostAddress();
    }
}
