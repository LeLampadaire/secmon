package multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastCommunication{
    private final byte[] buf = new byte[256];

    //Destiné à recevoir les messages des probes
    public String receive() throws IOException {
        MulticastSocket socket = new MulticastSocket(60402);
        InetAddress group = InetAddress.getByName("224.50.50.50"); // ON NE PEUT PAS METTRE LOCALHOST car les adresses  autorisées par InetAdress commencent à 224.0.0.0
        socket.joinGroup(group);

        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        String received = new String(packet.getData(), 0, packet.getLength());
        if ("end".equals(received)) {
           return "Connection Finished";
        }else{
            return received;
        }
    }

    public void send(String multicastMessage) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName("224.50.50.50");

        byte[] buf = multicastMessage.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 60402);
        socket.send(packet);
        socket.close();
    }
}
