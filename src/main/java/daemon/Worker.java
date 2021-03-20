package daemon;

import TCP.TcpCommunication;
import json.JsonUrlRead;
import json.JsonWriterUrl;

import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Worker implements Runnable {

    /**
     * List queue des messages reçus
     * Exemple :
     *  - JE SUIS EN VIE
     *  - NOTIFY
     */
    private final Queue<Task> queue;

    private TcpCommunication tcpCommunicationHttp;
    private TcpCommunication tcpCommunicationSnmp;

    private final Map<String, MemoryBlock> sharedMemory;

    public Worker(final Queue<Task> queue, final Map<String, MemoryBlock> map) {
        this.queue = queue;
        this.tcpCommunicationHttp = new TcpCommunication(15000);
        this.tcpCommunicationSnmp = new TcpCommunication(15001);
        this.sharedMemory = map;
    }

    @Override
    public void run() {
        while(true) {
            final Task currentTask = this.queue.poll();

            if(currentTask == null) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                Matcher message;
                final Matcher regexMessage = Pattern.compile("(?<code>^[^ ]+)\\ ").matcher(currentTask.getMessage());

                if(regexMessage.find()){
                    switch (regexMessage.group("code")) {
                        case "IAMHERE" :
                            message = Pattern.compile("(?<code>.*)\\ (?<protocol>.*)\\ (?<port>.*)").matcher(currentTask.getMessage());

                            if(message.find()){
                                switch (currentTask.getProtocol()) {
                                    case "https" :
                                        //Envoi du CURCONFIG pour chaque url
                                        sendHttpConfig(Integer.parseInt(message.group("port")));
                                        break;
                                    case "snmp" :
                                        //Envoi du CURCONFIG pour chaque url
                                        sendSnmpConfig(Integer.parseInt(message.group("port")));
                                        break;
                                }
                            }
                            break;
                        case "NOTIFY" :
                            message = Pattern.compile("(?<code>.*)\\ (?<protocol>.*)\\ (?<port>.*)").matcher(currentTask.getMessage());

                            if(message.find()){
                                switch (currentTask.getProtocol()) {
                                    case "https" :
                                        // Envoi la demande de l'état
                                        sendHttpsReq(Integer.parseInt(message.group("port")));
                                        break;
                                    case "snmp" :
                                        // Envoi la demande de l'état
                                        sendSnmpReq(Integer.parseInt(message.group("port")));
                                        break;
                                }
                            }
                            break;
                        case "STATERESP" :
                            message = Pattern.compile("(?<code>.*)\\ (?<id>.*)\\ (?<state>.*)").matcher(currentTask.getMessage());

                            if(message.find()) {
                                putInSharedMemory(message.group("id"), message.group("state"));
                            }
                            break;
                        case"ADDSRV" :
                            message = Pattern.compile("(?<code>.*)\\ (?<augmentedUrl>.*)").matcher(currentTask.getMessage());

                            if (message.find()){
                                final Matcher matcherSNMP = Pattern.compile("(?<id>.*)\\!(?<protocole>.*)\\:\\/{2}(?<community>.*)\\@(?<ip>.*)\\:(?<port>.*)\\/(?<oid>[^!]+)\\!(?<min>[^!]+)\\!(?<max>[^!]+)\\!(?<refreshRate>[^!]+)$").matcher(message.group("augmentedUrl"));
                                final Matcher matcherHTTPS = Pattern.compile("(?<id>.*)\\!(?<url>[^!]+)\\!(?<min>[^!]+)\\!(?<max>[^!]+)\\!(?<refreshRate>[^!]+)$").matcher(message.group("augmentedUrl"));

                                if(matcherSNMP.find()){
                                    JsonWriterUrl.writeANewUrl(message.group("protocole"), message.group("augmentedUrl"));
                                }else if(matcherHTTPS.find()){
                                    JsonWriterUrl.writeANewUrl("https", message.group("augmentedUrl"));
                                }
                            }
                            break;
                    }
                }
            }
        }
    }

    /**
     * State_req = état d'un service
     */
    private void sendSnmpReq(final int port) {
        synchronized (this.sharedMemory) {
            for (Map.Entry<String, MemoryBlock> entry : this.sharedMemory.entrySet()) {
                final Matcher matcherSNMP = Pattern.compile("(?<protocole>.*)\\:\\/{2}(?<community>.*)\\@(?<ip>.*)\\:(?<port>.*)\\/(?<oid>[^!]+)").matcher(entry.getValue().getUrl());
                if(matcherSNMP.find()){
                    try {
                        Thread.sleep(500);
                        this.tcpCommunicationSnmp.push("STATEREQ " + entry.getKey(), port);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void sendHttpsReq(final int port) {
        // -> Réception de l'état
        synchronized (this.sharedMemory) {
            for (Map.Entry<String, MemoryBlock> entry : this.sharedMemory.entrySet()) {
                final Matcher matcherHTTPS = Pattern.compile("(?<protocol>.*)\\:\\/{2}").matcher(entry.getValue().getUrl());
                if(matcherHTTPS.find()){
                    if(matcherHTTPS.group("protocol").equals("https")){
                        try {
                            Thread.sleep(500);
                            this.tcpCommunicationHttp.push("STATEREQ " + entry.getKey(), port);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Config = services à gérer
     */
    private void sendHttpConfig(final int port) {
        this.tcpCommunicationHttp = new TcpCommunication(port);
        final StringBuilder curconfigMessage = new StringBuilder();

        curconfigMessage.append("CURCONFIG");
        for (String url: JsonUrlRead.readHttps()) {
            curconfigMessage.append(" ").append(url);
            synchronized (this.sharedMemory) {
                final Matcher matcherHTTPS = Pattern.compile("(?<id>.*)\\!(?<url>[^!]+)\\!(?<min>[^!]+)\\!(?<max>[^!]+)\\!(?<refreshRate>[^!]+)$").matcher(url);
                if(matcherHTTPS.find()){
                    this.sharedMemory.put(matcherHTTPS.group("id"),new MemoryBlock(matcherHTTPS.group("url"),"UNKNOW"));
                }
            }
        }
        this.tcpCommunicationHttp.push(curconfigMessage.toString(), port);
    }

    private void sendSnmpConfig(final int port) {
        this.tcpCommunicationSnmp = new TcpCommunication(port);

        final StringBuilder curconfigMessage = new StringBuilder();
        curconfigMessage.append("CURCONFIG");
        for (String url: JsonUrlRead.readSnmp()) {
            curconfigMessage.append(" ").append(url);
            synchronized (this.sharedMemory) {
                final Matcher matcherSNMP = Pattern.compile("(?<id>.*)\\!(?<protocole>.*)\\:\\/{2}(?<community>.*)\\@(?<ip>.*)\\:(?<port>.*)\\/(?<oid>[^!]+)\\!(?<min>[^!]+)\\!(?<max>[^!]+)\\!(?<refreshRate>[^!]+)$").matcher(url);
                if(matcherSNMP.find())
                    this.sharedMemory.put(matcherSNMP.group("id"),new MemoryBlock(matcherSNMP.group("protocole")+"://"+matcherSNMP.group("community")+"@"+matcherSNMP.group("ip")+":"+matcherSNMP.group("port")+"/"+matcherSNMP.group("oid"),"UNKNOWN"));
            }
        }
        this.tcpCommunicationSnmp.push(curconfigMessage.toString(), port);
    }

    public void putInSharedMemory(final String id,final String state) {
        synchronized (this.sharedMemory) {
            this.sharedMemory.get(id).setState(state);
        }
    }
}
