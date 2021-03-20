package TCP;

import security.AES;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TcpCommunication {
    ServerSocket ecoute = null;
    Socket client = null;
    private boolean stop = false;
    private boolean isStarted = false;
    private boolean isConnected = false;
    PrintWriter toServer;
    BufferedReader fromClient;
    private int port;

    public TcpCommunication(int port) {
        this.port = port;
    }

    public String listen() {
        try {
            ecoute = new ServerSocket(port);
            isStarted = true;
            while(!stop) {
                client = ecoute.accept();
                isConnected = true;
                fromClient = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                String message = readLine(fromClient);
                isConnected = false;
                client.close();

                return new AES().decryptCommunication(Base64.getDecoder().decode(message.replaceAll("\r\n", "")));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (isConnected)
                    client.close();
                if (isStarted)
                    ecoute.close();
            } catch (IOException ex) {
            }
        }
        return "";
    }

    public void push(String message,int definedPort){
        try (Socket server = new Socket("localhost", definedPort)) {
            toServer = new PrintWriter(new OutputStreamWriter(server.getOutputStream(), StandardCharsets.UTF_8), true);
            toServer.print(Base64.getEncoder().encodeToString(new AES().encryptCommunication(message)) + "\r\n");
            toServer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String readLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if(line != null && line.length() > 2 && line.startsWith("\uFEFF"))
            return line.substring("\uFEFF".length());
        return line;
    }
}
