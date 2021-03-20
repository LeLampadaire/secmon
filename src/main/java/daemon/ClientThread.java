package daemon;

import models.Account;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientThread implements Runnable {
    private BufferedReader fromClient;
    private PrintWriter toClient;
    private Socket clientSocket;
    private Queue<Task> queue;
    private final Map<String, MemoryBlock> sharedMemory;
    private boolean stop = false;
    private Map<String, Account> clientAccount;
    private boolean isAdminAccount = false;
    private boolean isConnected = false;

    public ClientThread(final Socket clientSocket, final Queue<Task> queue, final Map<String, MemoryBlock> sharedMemory, final Map<String, Account> clientAccount) {
        this.queue = queue;
        this.sharedMemory = sharedMemory;
        this.clientAccount = clientAccount;

        try{
            this.clientSocket = clientSocket;
            fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            toClient = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while(!stop){
                String messageFromClient;
                String messageSendToClient = "null\r\n";

                // Récupération du message de l'utilisateur
                try{
                    messageFromClient = readLine(fromClient);
                }catch (SSLException ex){ // Si l'utilisateur a quitté sa page de client alors le message est null
                    messageFromClient = null;
                }

                if(messageFromClient == null) {
                    stop = true;
                }else if(!messageFromClient.isEmpty()){
                    // Le message n'est ni null, ni vide alors on peut vérifier son contenu
                    Matcher regexMessage = Pattern.compile("(?<code>^[^ ]+)\\ ").matcher(messageFromClient);
                    Matcher regexList = Pattern.compile("(?<listsrv>^[^ ]+)").matcher(messageFromClient);

                    if(regexMessage.find()){
                        switch (regexMessage.group("code")){
                            /*
                                add_service_req = "ADDSRV" sp augmented_url crlf
                                list_service_req = "LISTSRV" crlf
                                state_service_req = "STATESRV" sp id crlf
                                connexion_req = "CONNEXION" sp username sp password crlf
                             */
                            case "ADDSRV": // Ajout à la queue le serveur
                                /*
                                    [0] = ADDSRV
                                    [1] = augmented_url
                                * */
                                if(isConnected && isAdminAccount){
                                    regexMessage = Pattern.compile("(?<code>.*)\\ (?<augmentedUrl>.*)").matcher(messageFromClient);

                                    if(regexMessage.find()){
                                        final Matcher matcherSNMP = Pattern.compile("(?<id>.*)\\!(?<protocole>.*)\\:\\/{2}(?<community>.*)\\@(?<ip>.*)\\:(?<port>.*)\\/(?<oid>[^!]+)\\!(?<min>[^!]+)\\!(?<max>[^!]+)\\!(?<refreshRate>[^!]+)").matcher(regexMessage.group("augmentedUrl"));
                                        final Matcher matcherHTTPS = Pattern.compile("(?<id>.*)\\!(?<url>[^!]+)\\!(?<min>[^!]+)\\!(?<max>[^!]+)\\!(?<refreshRate>[^!]+)").matcher(regexMessage.group("augmentedUrl"));

                                        // Regex => Si une des deux regex match alors on l'ajoute à la queue
                                        if(matcherSNMP.find()){
                                            if(matcherSNMP.group("id").length() < 5 || matcherSNMP.group("id").length() > 10){
                                                messageSendToClient = "-ERR L'id doit avoir une taille entre 5 et 10 caractères inclus !\r\n";
                                            }else{
                                                queue.add(new Task(matcherSNMP.group("protocole"), messageFromClient));
                                                messageSendToClient = "+OK Le serveur a été ajouté !\r\n";
                                            }
                                        }else if(matcherHTTPS.find()){
                                            if(matcherHTTPS.group("id").length() < 5 || matcherHTTPS.group("id").length() > 10){
                                                messageSendToClient = "-ERR L'id doit avoir une taille entre 5 et 10 caractères inclus !\r\n";
                                            }else{
                                                final Matcher protocoleMatcher = Pattern.compile("(?<protocole>[^ ]+)\\:").matcher(messageFromClient);

                                                if(protocoleMatcher.find()){
                                                    queue.add(new Task(protocoleMatcher.group("protocole"), matcherHTTPS.group("url")));
                                                    messageSendToClient = "+OK Le serveur a été ajouté !\r\n";
                                                }else{
                                                    messageSendToClient = "-ERR Le protocole de l'url n'a pas été trouvé !\r\n";
                                                }
                                            }
                                        }else{
                                            messageSendToClient = "-ERR L'url ne respecte pas le pattern !\r\n";
                                        }
                                    }else{
                                        messageSendToClient = "-ERR L'url ne respecte pas le pattern !\r\n";
                                    }
                                }else{
                                    messageSendToClient = "PERMISSION REFUSED\r\n";
                                }

                                break;
                            case "STATESRV": // Récupère l'état d'un serveur grâce à l'id
                                /*
                                    [0] = STATESRV
                                    [1] = id
                                * */
                                if(isConnected){
                                    regexMessage = Pattern.compile("(?<code>.*)\\ (?<id>.*)").matcher(messageFromClient);

                                    if(regexMessage.find()){
                                        // Demande des données à la mémoire partagée
                                        messageSendToClient = "STATE " + regexMessage.group("id") + " ";

                                        synchronized (sharedMemory){
                                            final MemoryBlock memoryBlockTemp = sharedMemory.get(regexMessage.group("id"));
                                            if(memoryBlockTemp != null){
                                                messageSendToClient += memoryBlockTemp.getUrl() + " " + memoryBlockTemp.getState();
                                            }else{
                                                messageSendToClient = "null";
                                            }
                                        }

                                        messageSendToClient += "\r\n";
                                    }else{
                                        messageSendToClient = "null\r\n";
                                    }

                                }else{
                                    messageSendToClient = "null\r\n";
                                }

                                break;
                            case "CONNEXION": // Vérification des identifiants depuis la map clientAccount
                                /*
                                    [0] = CONNEXION
                                    [1] = username
                                    [2] = password
                                * */
                                regexMessage = Pattern.compile("(?<code>.*)\\ (?<username>.*)\\ (?<password>.*)").matcher(messageFromClient);

                                if(regexMessage.find()){
                                    final Account account = clientAccount.get(regexMessage.group("username"));
                                    // Si la clef (username) n'existe pas alors le accountValue sera null
                                    if(account == null){
                                        messageSendToClient = "CONNEXION -ERR\r\n";
                                    }else{ // Sinon la clef (username) existe
                                        if(regexMessage.group("password").equals(account.getPassword())){ // On test si le mot de passe est correct
                                            messageSendToClient = "CONNEXION +OK\r\n";
                                            isAdminAccount = account.isAdmin();
                                            isConnected = true;
                                        }else{ // Le mot de passe n'est pas bon
                                            messageSendToClient = "CONNEXION -ERR\r\n";
                                        }
                                    }
                                }else{
                                    messageSendToClient = "CONNEXION -ERR\n";
                                }

                                break;
                            default:
                                messageSendToClient = "null\r\n";
                        }
                    }else if(regexList.find()){ // Récupère la liste des id
                        if(isConnected && regexList.group("listsrv").equals("LISTSRV")){
                            // Demande des données dans la mémoire partagée
                            messageSendToClient = "SRV ";

                            synchronized (sharedMemory){
                                for (final String id : sharedMemory.keySet()) {
                                    messageSendToClient += id + " ";
                                }
                            }

                            messageSendToClient += "\r\n";
                        }else{
                            messageSendToClient = "null\r\n";
                        }
                    }else{
                        messageSendToClient = "null\r\n";
                    }

                    // Envoie de la réponse à l'utilisateur
                    toClient.print(messageSendToClient);
                    toClient.flush();
                }
            }

            // Déconnexion du client
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                // Déconnexion du client
                clientSocket.close();
            } catch(IOException ex) { /* INTENTIONEL parce que si le close lève une exception, cela veut dire qu'il est déjà fermé */ }
        }
    }

    private String readLine(final BufferedReader reader) throws IOException, SSLException {
        String line = reader.readLine();
        if(line != null && line.length() > 2 && line.startsWith("\uFEFF"))
            return line.substring("\uFEFF".length());
        return line;
    }
}
