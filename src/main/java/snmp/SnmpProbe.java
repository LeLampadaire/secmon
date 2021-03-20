package snmp;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import TCP.TcpCommunication;
import interfaces.Connection;
import models.DataSnmp;
import multicast.MulticastCommunication;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpProbe implements Connection {
    public final int DEFAULT_VERSION = SnmpConstants.version2c;
    public final String DEFAULT_PROTOCOL = "udp";
    public final long DEFAULT_TIMEOUT = 3 * 1000L;
    public final int DEFAULT_RETRY = 3;
    private Map<String, DataSnmp> dataSnmps;
    private MulticastCommunication multicastCommunication;
    private TcpCommunication tcpCommunication;
    private int snmpRefreshRate  = 120000; //Par défaut le raffraicihissement des urls de la probe snmp est de 120 secondes

    public SnmpProbe() {
        multicastCommunication = new MulticastCommunication();
        this.dataSnmps = new HashMap<>();
        sendAnnounce(); //Annonce de "je suis en vie"
        sendNotification(); //Annonce de nouvelles données disponibles
    }

    public String getID(final String url) {
        final Matcher matcher = Pattern.compile("(?<id>.+?)!", Pattern.MULTILINE).matcher(url);
        if (matcher.find()) {
            return matcher.group("id");
        }
        return "";
    }

    private DataSnmp getData(final String url) {
        final String regex = "(?<id>.*)\\!(?<protocole>.*)\\:\\/{2}(?<community>.*)\\@(?<ip>.*)\\:(?<port>.*)\\/(?<oid>[^!]+)\\!(?<min>[^!]+)\\!(?<max>[^!]+)\\!(?<refreshRate>[^!]+)$";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(url);

        DataSnmp newDataSnmp = null;

        if (matcher.find()) {
            if (matcher.group("protocole") != null && matcher.group("ip") != null && matcher.group("port") != null && matcher.group("oid") != null && matcher.group("min") != null && matcher.group("max") != null && matcher.group("refreshRate") != null && matcher.group("community") != null) {
                newDataSnmp = new DataSnmp(matcher.group("protocole"), matcher.group("ip"), matcher.group("port"));
                newDataSnmp.setOid(matcher.group("oid"));
                newDataSnmp.setMinValue(Double.parseDouble(matcher.group("min")));
                newDataSnmp.setMaxValue(Double.parseDouble(matcher.group("max")));
                newDataSnmp.setTimeRefresh(Long.parseLong(matcher.group("refreshRate")));
                newDataSnmp.setCommunity(matcher.group("community"));
            }
        }
        return newDataSnmp;
    }

    @Override
    public String getValue(final String key) {
        final StringBuilder returnValue = new StringBuilder();
        CommunityTarget<Address> target = null;

        // La classe Snmp est le noyau de SNMP4J. Elle fournit des fonctions pour envoyer et recevoir des PDU SNMP. Tous les types de PDU SNMP peuvent être envoyés. Les PDU confirmées peuvent être envoyées de manière synchrone et asynchrone.
        Snmp snmp = null;

        // La classe PDU représente une unité de données du protocole SNMP. La version de la PDU supportée par les méthodes de décodage et d'encodage du BER de cette classe est la v2.
        // Le type de PDU par défaut est GET.
        final PDU pdu = new PDU();

        // Dépend du choix
        target = createDefault(dataSnmps.get(key));

        // pdu.add à besoin d'un objet VariableBinding donc on va lui passer un objet OID grâce à notre string oid
        pdu.add(new VariableBinding(new OID(dataSnmps.get(key).getOid())));

        try {
            // Le DefaultUdpTransportMapping met en œuvre un mappage de transport UDP basé sur la norme Java IO et utilisant un thread interne pour l'écoute sur le socket entrant.
            final DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();

            snmp = new Snmp(transport);
            snmp.listen(); // Mets le DefaultUdpTransportMapping sur écoute

            // Envoie un PDU à la cible donnée et renvoie le PDU de réponse reçu.
            // respEvent à des informations importantes comme l'adresse, la reponse, ...
            // L'adresse : respEvent.getPeerAddress()
            // La réponse : respEvent.getResponse().get(0).toValueString();
            pdu.setType(PDU.GET);
            ResponseEvent<Address> respEvent = snmp.send(pdu, target);
            PDU response = respEvent.getResponse();

            // Si la réponse n'est pas null, j'ai reçu une valeur, je l'ajoute dans mon stringbuilder
            if (response != null) {
                // Récupère toutes les réponses et les affiches
                for (int i = 0; i < response.size(); i++) {
                    returnValue.append(respEvent.getResponse().get(i).toValueString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SNMP Get Exception:" + e);
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex) {
                    snmp = null;
                    System.out.println("SNMP Get Exception when it closes:" + ex);
                }
            }
        }
        return returnValue.toString();
    }

    @Override
    public boolean insertValue(final String url) {
        final String id = getID(url);
        final DataSnmp dataSnmp = getData(url);
        if (dataSnmp != null) {
            this.dataSnmps.put(id, dataSnmp);
            return true;
        } else {
            return false;
        }
    }

    private CommunityTarget<Address> createDefault(final DataSnmp dataValue) {
        final Address address = GenericAddress.parse(DEFAULT_PROTOCOL + ":" + dataValue.getIp() + "/" + dataValue.getPort());

        final CommunityTarget<Address> target = new CommunityTarget<>();

        target.setAddress(address);
        target.setCommunity(new OctetString(dataValue.getCommunity()));
        target.setVersion(DEFAULT_VERSION);
        target.setTimeout(DEFAULT_TIMEOUT); // milliseconds
        target.setRetries(DEFAULT_RETRY);

        return target;
    }

    private void sendNotification() {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                try {
                    TcpCommunication tcp2 = new TcpCommunication(15004);
                    multicastCommunication.send("NOTIFY snmp 15004\r\n");
                    waitNotificationResponse(tcp2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 2000, snmpRefreshRate);
        //notification = "NOTIFY" sp protocol sp port crlf
        //Le message notification est envoyé par la probe en multicast pour annoncer que des nouvelles données sont disponibles
    }

    private void waitNotificationResponse(TcpCommunication tcp2) throws IOException {
        for(int i =0; i<dataSnmps.size(); i++){
            final String line = tcp2.listen();

            final Matcher regexMessage = Pattern.compile("(?<code>[^ ]+)\\ (?<id>[^ ]+)").matcher(line);
            if(regexMessage.find()){
                if(regexMessage.group("code").equals("STATEREQ")){
                    verifyValues(regexMessage.group("id"));
                }
            }
        }
    }

    private void verifyValues(final String id) throws IOException {
        final String currentValue = getValue(id);
        String state = "";
        if(dataSnmps.get(id).getMaxValue()<Double.parseDouble(currentValue) || dataSnmps.get(id).getMinValue()>Double.parseDouble(currentValue)){
            state = "ALARM";
        }else if(currentValue.equals("")){
            state = "DOWN";
        }else{
            state = "OK";
        }
        multicastCommunication.send("STATERESP "+id+" "+state+"\r\n");
    }

    private void sendAnnounce() {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                try {
                    multicastCommunication.send("IAMHERE snmp 15003\r\n");
                    tcpCommunication = new TcpCommunication(15003);
                    waitAnnounceResponse();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 90000);
        //announce = "IAMHERE" sp protocol sp port crlf
        //Ce message est envoyé toutes les 90000 secondes (et occasionnera, en retour, une connexion et l’envoi du message config par le monitor daemon)
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

    public static void main(String[] args) {
        new SnmpProbe();
    }
}