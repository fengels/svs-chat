package de.bhtberlin.svschatclient;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nto
 */
class FileReceiver implements Runnable {

    private DatagramSocket receiveSocket;
    private final int bufferSize;
    private DatagramPacket dp;

    public FileReceiver(final int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public void run() {

        try {
            this.receiveSocket = new DatagramSocket(9603);
        } catch (SocketException ex) {
            Logger.getLogger(FileReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] buf = new byte[this.bufferSize];

        while (!Thread.currentThread().isInterrupted()) {
            synchronized (this) {

                try {
                    this.dp = new DatagramPacket(buf, buf.length);
                    receiveSocket.receive(this.dp);
                    handleFilePackage(new String(this.dp.getData()));
                } catch (IOException ex) {
                    Logger.getLogger(FileReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Received: " + new String(this.dp.getData()));
            }
        }
    }
    
    File file;
    private void handleFilePackage(final String input) {
        StringTokenizer st = new StringTokenizer(input);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.matches("/file .+")) {
                String name = st.nextToken();
                file = new File(st.nextToken());
            }
            if (token.matches("/eof .+")) {
                // = st.nextToken();
            } else if (token.matches("/part")) {
            }
        }

    }
}
