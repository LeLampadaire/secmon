package https;

import TCP.TcpCommunication;
import multicast.MulticastCommunication;
import interfaces.Connection;
import models.DataHttps;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpsProbe implements Connection {
    private final Map<String, DataHttps> dataHttps;
    private MulticastCommunication multicastCommunication;
    private TcpCommunication tcpCommunication;
    private int httpsRefreshRate = 90000; //Par défaut le raffraicihissement des urls de la probe http est de 90 secondes

    //Le constructeur initialise directement les valeurs des attributs, en se utilisant les méthodes qui se connectent au site
    public HttpsProbe() {
        multicastCommunication = new MulticastCommunication();
        this.dataHttps = new HashMap<>();
        sendAnnounce(); //Annonce de "je suis en vie"
        sendNotification(); //Annonce de nouvelles données disponibles
    }


    private DataHttps getData(final String url) {
        final String regexHttps = "(?<id>.*)\\!(?<url>[^!]+)\\!(?<min>[^!]+)\\!(?<max>[^!]+)\\!(?<refreshRate>[^!]+)";
        final Pattern pattern = Pattern.compile(regexHttps);
        final Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            if (matcher.group("url") != null && matcher.group("min") != null && matcher.group("max") != null && matcher.group("refreshRate") != null) {
                httpsRefreshRate= Integer.parseInt(matcher.group("refreshRate"));
                return new DataHttps(matcher.group("url"), Double.parseDouble(matcher.group("min")), Double.parseDouble(matcher.group("max")), Long.parseLong(matcher.group("refreshRate")));
            }
        }
        return null;
    }

    public String getID(final String url) {
        final Matcher matcher = Pattern.compile("(?<id>.+?)!").matcher(url);
        if (matcher.find()) {
            return matcher.group("id");
        }
        return "";
    }

    @Override
    public String getValue(final String key) {
        final URLConnection connection;
        try {
            connection = getConnection(dataHttps.get(key).getUrl());
            this.dataHttps.get(key).setValue(getInputStreamValue(connection));
            return String.valueOf(dataHttps.get(key).getValue());
        } catch (IOException e) {
            System.out.println("Impossible de se connecter avec l'url.");
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public boolean insertValue(final String url) {
        final String id = getID(url);
        final DataHttps dataHttps = getData(url);
        if (dataHttps != null) {
            this.dataHttps.put(id, dataHttps);
            return true;
        } else {
            return false;
        }
    }

    //Création de la connection depuis une url https
    private URLConnection getConnection(final String urlTemp) throws IOException {
        final URL url = new URL(urlTemp);
        URLConnection urlConnection = null;
        try {
            urlConnection = url.openConnection();
        } catch (ConnectException ex) {
            System.out.println("Impossible de se connecter avec l'url.");
            ex.printStackTrace();
        }
        return urlConnection;
    }

    // Lecture de la ligne
    private double getInputStreamValue(final URLConnection connection) throws IOException {
        final InputStream inputStream = connection.getInputStream();
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            return formatValue(br.readLine());
        } catch (Exception ex) {
            System.out.println("Erreur de lecture de la valeur.");
            ex.printStackTrace();
        }
        return 0;
    }

    // Formate la valeur et en récupère le double
    private double formatValue(final String toFormat) {
        return Double.parseDouble(toFormat.substring(toFormat.indexOf(":") + 2, toFormat.indexOf("}") - 1));
    }

    public void sendAnnounce(){
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                try {
                    multicastCommunication.send("IAMHERE https 15001\r\n");
                    tcpCommunication = new TcpCommunication(15001);
                    waitAnnounceResponse();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            }, 0, 90000);
        //announce = "IAMHERE" sp protocol sp port crlf
        //Ce message est envoyé toutes les 90 secondes (et occasionnera, en retour, une connexion et l’envoi du message config par le monitor daemon)
    }



    private void waitAnnounceResponse() {
        final String line = tcpCommunication.listen();
        final Matcher regexMessage = Pattern.compile("[^ ]+").matcher(line);

        if(regexMessage.find()){
            if(regexMessage.group(0).equals("CURCONFIG")){

                while(regexMessage.find()){
                    insertValue(regexMessage.group(0));
                }
            }
        }
    }

    public void sendNotification(){
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                try {
                    TcpCommunication tcp2 = new TcpCommunication(15002);
                    multicastCommunication.send("NOTIFY https 15002\r\n");
                    waitNotificationResponse(tcp2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 2000, httpsRefreshRate); //Le délai sert à  laisser le temps au worker d'envoyer le curconfig
        //notification = "NOTIFY" sp protocol sp port crlf
        //Le message notification est envoyé par la probe en multicast pour annoncer que des nouvelles données sont disponibles
    }

    private void waitNotificationResponse(TcpCommunication tcp2) throws IOException {
        for(int i =0; i<dataHttps.size(); i++){
            final String line = tcp2.listen();

            final Matcher regexMessage = Pattern.compile("(?<code>[^ ]+)\\ (?<id>[^ ]+)").matcher(line);
            if(regexMessage.find()){
                if(regexMessage.group("code").equals("STATEREQ")){
                    verifyValues(regexMessage.group("id"));
                }
            }
        }
    }

    /**
     * SELON LA VALEUR DU SERVICE, RENVOIE UN STRING CORRESPONDANT (OK,ALARM,DOWN) ET ENVOIE UN STATERESP AVEC CE STRING ("STATERESP" sp id sp state crlf)
     * @param id id du service à inspecter
     * @throws IOException
     */
    private void verifyValues(final String id) throws IOException {
        final String currentValue = getValue(id);
        String state;

        if(dataHttps.get(id).getMaxValue()<Double.parseDouble(currentValue) || dataHttps.get(id).getMinValue()>Double.parseDouble(currentValue)){
            state = "ALARM";
        }else if(currentValue.equals("")){
            state = "DOWN";
        }else{
            state = "OK";
        }
        multicastCommunication.send("STATERESP "+id+" "+state+"\r\n");
    }

    public static void main(String[] args) {
        new HttpsProbe();
    }
}
