package de.bhtberlin.svschatclient;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * SVS UDP Chat Client
 *
 * @version v0.01
 * @author Sven Höche
 * @author Fabian Engels
 *
 * Notes: - Timeout value for incoming messages has to be only limited for own
 * messages.
 * nachricht mit /file name "pfad"
 * bekomme ein port auf anfrage
 * 
 */
public class Client {

    private final int localPort = 0;
    private final int recivePort = 9602;
    private int targetPort = 9600;
    private String clientName = "";
    private String serverIP = "37.5.33.49";
    
    private Scanner in;
    private String inputLine = "";
    private DatagramSocket dsocket;
    private InetAddress ia;
    private DatagramPacket dPackage;
    private Thread receiverThread;
    private boolean wasProcessLine = false;
    
    private final String portRegEx = "/port";
    private final String nameRegEx = "/name";
    private final String serverIPRegEx = "/ip";
    private String fileReciver = "";
    private String filePath = "";

    public Client() {
        this.in = new Scanner(System.in);
        if (!serverIP.isEmpty()){
            setNewServerIP(this.serverIP);
        }else{
            askForServerIP();
        }
    }

    public void run() {
        displayUsage();

        if (targetPort == -1) {
            askForPort();
        }
        if(clientName.isEmpty()){
            askForClientName();
        }
        try {
            this.dsocket = new DatagramSocket(localPort); //UDP
        } catch (SocketException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        receiverThread = new ReceiverThread(recivePort);
        receiverThread.start();

        try {
            while (true) {
                System.out.print(clientName + ": ");
                if(!in.hasNextLine()){
                    System.out.println();
                }else{
                    this.inputLine = in.nextLine();
                }
                
                processInput(this.inputLine);
                
                this.inputLine = nameRegEx + " " + clientName + ":" + this.inputLine;
                
                if(wasProcessLine == true){
                    wasProcessLine = false;
                    continue;
                }
                
                byte[] data = this.inputLine.getBytes();

                this.dPackage = new DatagramPacket(data, data.length, ia, localPort);

                this.dPackage.setPort(targetPort);
                this.dsocket.send(dPackage);
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Dringend umbauen !!! Die methoden unten verwenden Exceptionhandling
    private void processInput(final String input) {
        String[] args = input.split(" "); // Was: \\W
        
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("/close")) {
                this.receiverThread.interrupt();
                this.dsocket.close();
                wasProcessLine = true;
                System.out.println("Program is shuting down.");
                System.exit(0);
            }
            if (args[0].equalsIgnoreCase("/port")) {
                this.targetPort = Integer.parseInt(args[1]);
                System.out.println("New target /port " + this.targetPort + " set.");
                wasProcessLine = true;
            }
            if (args[0].equalsIgnoreCase("/ip")) {
                this.serverIP = args[1];
                setNewServerIP(this.serverIP);
                System.out.println("New server /ip " + this.serverIP + " set.");
                wasProcessLine = true;
            }
            if (args[0].equalsIgnoreCase("/name")) {
                this.clientName = args[1];
                System.out.println("New client /name " + this.clientName + " set.");
                wasProcessLine = true;
            }
            //Is Transmitting this to server
            if (args[0].equalsIgnoreCase("/file")) {
                String[] sa1 = args[1].split(":");
                int i1 = args[1].indexOf(":");

                if (sa1.length > 0) {
                    this.fileReciver = sa1[0];
                    this.filePath = args[1].substring(i1 + 1, args[1].length());
                }
                System.out.println("Transmiting /file " + this.filePath + " to " + fileReciver + ".");
            }
        }
    }
    
    public void setNewServerIP(String ip){
      try {
            this.ia = InetAddress.getByName(ip);
      } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public void displayUsage() {
        final String text =
                "SVS UDP Chat Client\n"
                + "*usage* to the change the targeted port type: /port <number>";
        System.out.println(text);
    }
    
    public void askForPort() {
        System.out.println("Please choose a server port! (/port ...)");

        String[] inPut = this.in.nextLine().split(" ");

        if (inPut[0].contains(portRegEx) && inPut.length > 1 && inPut[1].matches("\\d+")) {
            System.out.println(portRegEx + " " + inPut[1] + " set.");
            this.targetPort = Integer.parseInt(inPut[1]);
        } else {
            askForPort();
        }
    }

    private void askForClientName() {
        System.out.println("Please choose a Name! (/name ...)");
        
        String[] inPut = this.in.nextLine().split(" ");
        
        if (inPut[0].contains(nameRegEx) && inPut.length > 1 && inPut[1].matches("\\w+")) {
            System.out.println(nameRegEx + " " + inPut[1] + " set.");
            for(int i = 1; i<inPut.length; i++){
                this.clientName = clientName + inPut[i] + " ";
            }
        }else{
            askForClientName();
        }
    }

    private void askForServerIP() {
        System.out.println("Please choose a Server-IP! (/ip ...)");
        
        String[] inPut = this.in.nextLine().split(" ");
        
        if (inPut[0].contains(serverIPRegEx) && inPut.length > 1 && inPut[1].matches("\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b")) {
            System.out.println(serverIPRegEx + " " + inPut[1] + " set.");
            this.serverIP = inPut[1];
            setNewServerIP(this.serverIP);
        }else{
            askForServerIP();
        }
    }

    public class ReceiverThread extends Thread {

        private DatagramSocket datagramSocket;
        private int port;
        private final int bufferSize = 256;

        public ReceiverThread(final int port) {
            this.port = port;
            System.out.println("Listen on port: " + port);
        }

        @Override
        public void interrupt() {
            super.interrupt();
            if (datagramSocket != null) {
                this.datagramSocket.close();
            }
        }

        @Override
        public void run() {
            try {
                this.datagramSocket = new DatagramSocket(port);
            } catch (SocketException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            byte[] buf;
            DatagramPacket dp;
            Pattern closePat = Pattern.compile("filetrans@.+");
            
            try {
                while (true) {
                    buf = new byte[bufferSize];
                    dp = new DatagramPacket(buf, buf.length);
                    this.datagramSocket.receive(dp);
                    byte[] textBuf = dp.getData();
                    /* 
                     * for (byte b : textBuf) { System.out.print(b); }
                     * System.out.println();
                     */
                    String text = new String(textBuf); //UTF8
                    StringBuilder sb = new StringBuilder();
                    sb.append(dp.getAddress().toString().substring(1));
                    sb.append("> ");
                    sb.append(text);
                    System.out.println(sb);

                    if (closePat.matcher(text).matches()) {
                        String[] sa = text.split("@");
                        int filePort = Integer.parseInt(sa[1]);

                        Path path = Paths.get(filePath);
                        byte[] data = Files.readAllBytes(path);
                        int fixedData = data.length / 1024;

                        if (data.length % 1024 != 0) {
                            fixedData++;
                        }

                        for (int i = 0; i < fixedData; i += 1024) {
                            byte[] copy = Arrays.copyOfRange(data, i, i + 1024);
                            DatagramPacket dgp = new DatagramPacket(copy, copy.length, dp.getAddress(), filePort);
                            datagramSocket.send(dgp);
                        }
                    }

                    System.out.print(clientName + " : ");
                }
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}