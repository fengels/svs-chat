/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bhtberlin.svschatclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.ArrayUtils;

/**
 * MessageSender pools the given queue for new messages and sends them out by
 * DatagramSocket.
 *
 * @author nto
 */
class FileSender implements Runnable {

    private  DatagramSocket sendSocket;
    private  int targetServerPort;
    private  InetAddress serverAddress;
    private  File file;
    private  String receiverName; //TODO implementieren

    /**
     * 
     * @param receiverName
     * @param file
     * @param targetServerPort
     * @param serverAddress 
     */
    public FileSender(final String receiverName, final File file, final int targetServerPort, final InetAddress serverAddress) {
        this.file = file;
        try {
            this.sendSocket = new DatagramSocket(0);
        } catch (SocketException ex) {
            Logger.getLogger(FileSender.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.targetServerPort = targetServerPort;
        this.serverAddress = serverAddress;
        this.receiverName = receiverName;
    }
    
    String s ="/part ";
    int slen = s.getBytes().length;
    
    
    @Override
    public void run() {
        try {
            DatagramPacket dp = null;

            FileInputStream fileInputStream;
            fileInputStream = new FileInputStream(file);
            byte[] data = new byte[1024];
            int i =0;
            while(fileInputStream.read(data) != -1){
                if(i==0){
                    s="/file ";
                }else{
                    s="/part ";
                }
                byte[] both = ArrayUtils.addAll(s.getBytes(),data);
                dp = new DatagramPacket(both, both.length);
                dp.setPort(this.targetServerPort);
                dp.setAddress(this.serverAddress);
                sendSocket.send(dp); // send?
                i++;
            }
            dp.setData("/eofe ".getBytes());
            sendSocket.send(dp);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileSender.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioex){
            Logger.getLogger(FileSender.class.getName()).log(Level.SEVERE, null, ioex);
        }
    }
}