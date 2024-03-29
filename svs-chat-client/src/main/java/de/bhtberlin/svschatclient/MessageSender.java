/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bhtberlin.svschatclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MessageSender pools the given queue for new messages and sends them out by
 * DatagramSocket.
 * @author nto
 */
 class MessageSender implements Runnable {
    private final Logger LOGGER = Logger.getLogger(MessageSender.class.getName());
        private final BlockingQueue<String> messageQueue;
        private final DatagramSocket sendSocket;
        private final int targetServerPort;
        private final InetAddress serverAddress;
        
        public MessageSender(
                final int targetServerPort,
                final InetAddress serverAddress,
                final DatagramSocket socket,
                final BlockingQueue<String> queue)
        {
            this.messageQueue = queue;
            this.sendSocket = socket;
            this.targetServerPort = targetServerPort;
            this.serverAddress = serverAddress;
            
        }
        @Override
        public void run(){
            DatagramPacket dp = null;
            
            while(!Thread.currentThread().isInterrupted()) {
                synchronized (this.messageQueue) {
                if (this.messageQueue.isEmpty()) {
                        try {
                            this.messageQueue.wait();
                        } catch (IllegalMonitorStateException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        } catch (InterruptedException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        }
                    }
                }
                String message = messageQueue.poll();
                if(message==null){
                    continue;
                }else{
                    byte[] buff = message.getBytes(Charset.forName("UTF-8"));
                    dp = new DatagramPacket(buff, buff.length);
                    dp.setPort(this.targetServerPort);
                    dp.setAddress(this.serverAddress);
                    try {
                        sendSocket.send(dp);
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }