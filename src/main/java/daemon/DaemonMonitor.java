package daemon;

import json.JsonAccountRead;
import models.Account;
import security.TLS;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DaemonMonitor {
    public static final int DEFAULT_PORT_SERVER = 4042;
    private boolean stop = false;
    private boolean isStarted = false;
    private Queue<Task> queue;
    private Map<String, MemoryBlock> sharedMemory;
    private Map<String, Account> clientAccount;

    public DaemonMonitor() {
        queue = new ConcurrentLinkedQueue<>();
        sharedMemory = Collections.synchronizedMap(new HashMap<>());
        clientAccount = Collections.synchronizedMap(new HashMap<>());

        // Insertion des comptes clients
        JsonAccountRead.readFromJson(clientAccount);

        // Création du thread ServerProbeThread
        (new Thread(new ServerProbeThread(queue))).start();

        // Création du thread Worker
        (new Thread(new Worker(queue, sharedMemory))).start();
        // Boucle d'écoute du serveur client
        serverClient(); // Démarage du serveur client (Ecouteur des clients)
    }

    /**
     * SERVEUR pour les clients
     */
    private void serverClient() {
        SSLServerSocket serverSocket = null;
        try {
            serverSocket = TLS.createServerSocketSSL(DEFAULT_PORT_SERVER);
            isStarted = true;
            System.out.println("Démarrage du serveur sur l'adresse " + serverSocket.getInetAddress() + " avec le port " + DEFAULT_PORT_SERVER);

            while(!stop) {
                final SSLSocket clientSocket = (SSLSocket) serverSocket.accept();

                (new Thread(new ClientThread(clientSocket, queue, sharedMemory, clientAccount))).start();
            }
        } catch(IOException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | KeyManagementException | CertificateException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (isStarted)
                    serverSocket.close();
            } catch(IOException ex) { /* INTENTIONEL parce que si le close lève une exception, cela veut dire qu'il est déjà fermé */ }
        }
    }

    public static void main(final String[] args) {
        new DaemonMonitor();
    }
}