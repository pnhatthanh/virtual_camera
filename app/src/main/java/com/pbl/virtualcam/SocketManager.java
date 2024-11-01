package com.pbl.virtualcam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

public class SocketManager{
    private DatagramSocket serverSocket;
    public static Vector<SocketHandler> socketSet=new Vector<SocketHandler>();
    static public Bitmap bitmap;
    static public long timeStamp = -1;
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
    private Bitmap[][] blocks;
    private Bitmap bitmap;
    private byte[][][] listImageByte;
    private int[] pixels = new int[640*480];
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
        int divX = 2, divY = 2;
        this.blocks = new Bitmap[divX][divY];// INIT BLOCKS
        this.listImageByte = new byte[divX][divY][];// INIT LISTBYTE
        while (true){
            try{
                this.bitmap = SocketManager.bitmap;
                compressAndProcessImage(divX ,divY);
                long timeStamp = SocketManager.timeStamp;

//                String msg = 4+","+3;
//                DatagramPacket metaPac = new DatagramPacket(msg.getBytes(),msg.length(),this.clientAddress,this.clientPort);
//                this.serverSocket.send(metaPac);

                for (int row = 0; row < divX; row++) {
                    for (int col = 0; col < divY; col++) {
                        int length = this.listImageByte[row][col].length;
                        byte[] dataToSend = new byte[length + 9];
                        dataToSend[0] = (byte) ( row<<4 | col);
                        for (int j = 1; j <= 8; j++) {
                            dataToSend[j] = (byte) ((timeStamp >> (9 - j - 1) * 8) & 255);
                        }
                        System.arraycopy(this.listImageByte[row][col], 0, dataToSend, 9, length);
                        DatagramPacket packet = new DatagramPacket(dataToSend, dataToSend.length, this.clientAddress, this.clientPort);
                        this.serverSocket.send(packet);
//                        Thread.sleep(1);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private void divideImageIntoBlocks(int divX, int divY) {
        int blockWidth = this.bitmap.getWidth() / divX;
        int blockHeight = this.bitmap.getHeight() / divY;
//        Log.i("TAG", "divideImageIntoBlocks: "+bitmap.getWidth()+" "+bitmap.getHeight()+" "+blockWidth+" "+blockHeight);

        this.bitmap.getPixels(this.pixels,0,this.bitmap.getWidth(),0,0,this.bitmap.getWidth(), this.bitmap.getHeight());
        for (int x = 0; x < this.bitmap.getWidth(); x++){
            for (int y = 0; y < this.bitmap.getHeight(); y++){
                if (this.blocks[x%divX][y%divY]==null){
                    this.blocks[x%divX][y%divY] = Bitmap.createBitmap(blockWidth,blockHeight, Bitmap.Config.ARGB_8888);
                }
                this.blocks[x%divX][y%divY].setPixel(x/divX,y/divY,this.pixels[y * this.bitmap.getWidth() + x]);
            }
        }
    }



    private void compressAndProcessImage(int divX, int divY)  {
        divideImageIntoBlocks(divX,divY);
        for (int row = 0; row < divX; row++) {
            for (int col = 0; col < divY; col++) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                this.blocks[row][col].compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

//                ByteArrayOutputStream gzipByteArrayStream = new ByteArrayOutputStream();
//                GZIPOutputStream gzipOutputStream = null;
//                try {
//                    gzipOutputStream = new GZIPOutputStream(gzipByteArrayStream);
//                    gzipOutputStream.write(byteArrayOutputStream.toByteArray());
//                    gzipOutputStream.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
                this.listImageByte[row][col] = byteArrayOutputStream.toByteArray();
            }
        }
    }


    public InetAddress getClientAddress(){
        return this.clientAddress;
    }
}
