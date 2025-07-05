package utils;

import java.io.*;
import java.net.*;

public class FileTransferManager {
    private static final int PORT = 12345;
    private static final String HOST = "localhost";
    private static final String CERTIFICATES_DIR = "certificates/";

    // Server side (AdminMain)
    public static void startFileReceiver() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         InputStream is = clientSocket.getInputStream();
                         ObjectInputStream ois = new ObjectInputStream(is)) {
                        
                        // Receive file data
                        String fileName = ois.readUTF();
                        
                        // Validate file is PDF
                        if (!fileName.toLowerCase().endsWith(".pdf")) {
                            System.err.println("Rejected non-PDF file: " + fileName);
                            continue;
                        }
                        
                        long fileLength = ois.readLong();
                        byte[] fileContent = new byte[(int) fileLength];
                        ois.readFully(fileContent);
                        
                        // Save to certificates directory
                        File certDir = new File(CERTIFICATES_DIR);
                        if (!certDir.exists()) {
                            certDir.mkdirs();
                        }
                        
                        File outputFile = new File(certDir, fileName);
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            fos.write(fileContent);
                        }
                        
                        System.out.println("Received PDF certificate: " + fileName);
                    } catch (IOException e) {
                        System.err.println("Error receiving file: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("Could not start file receiver: " + e.getMessage());
            }
        }).start();
    }

    // Client side (UserMain/HelperMain)
    public static boolean sendFile(File fileToSend) {
        // Validate file is PDF before sending
        if (!fileToSend.getName().toLowerCase().endsWith(".pdf")) {
            System.err.println("Only PDF files can be sent as certificates");
            return false;
        }

        try (Socket socket = new Socket(HOST, PORT);
             OutputStream os = socket.getOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(os);
             FileInputStream fis = new FileInputStream(fileToSend)) {
            
            // Read file content
            byte[] fileContent = new byte[(int) fileToSend.length()];
            fis.read(fileContent);
            
            // Send file metadata and content
            oos.writeUTF(fileToSend.getName());
            oos.writeLong(fileToSend.length());
            oos.write(fileContent);
            oos.flush();
            
            return true;
        } catch (IOException e) {
            System.err.println("Error sending file: " + e.getMessage());
            return false;
        }
    }

    // For AdminMain to check for new certificates
    public static File[] getReceivedCertificates() {
        File certDir = new File(CERTIFICATES_DIR);
        if (!certDir.exists()) {
            certDir.mkdirs();
            return new File[0];
        }
        
        // Filter to only show PDF files
        return certDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
    }
}