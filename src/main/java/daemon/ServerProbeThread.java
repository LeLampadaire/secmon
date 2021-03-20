package daemon;

import multicast.MulticastCommunication;

import java.io.*;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerProbeThread implements Runnable{
    final MulticastCommunication multicast = new MulticastCommunication();
    final Queue<Task> queue;

    public ServerProbeThread(Queue<Task> string){
        this.queue = string;
    }

    public void run(){
        while(true){
            try {
                final String receiveString = multicast.receive();

                final Matcher messageRegex = Pattern.compile("(?<code>.*)\\ (?<protocole>.*)\\ (?<state>.*)").matcher(receiveString);

                if(messageRegex.find()){
                    Task task = new Task(messageRegex.group("protocole"), receiveString);
                    queue.add(task);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

