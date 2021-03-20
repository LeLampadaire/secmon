package graphics;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IndexFXController {
    private BufferedReader fromServer;
    private PrintWriter toServer;

    public IndexFXController(final SSLSocket daemonSocket){
        try{
            fromServer = new BufferedReader(new InputStreamReader(daemonSocket.getInputStream(), StandardCharsets.UTF_8));
            toServer = new PrintWriter(new OutputStreamWriter(daemonSocket.getOutputStream(), StandardCharsets.UTF_8), true);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @FXML
    private TextField inputCommand;

    @FXML
    private Label zoneResultDisplay;

    @FXML
    private void inputTextCommand(){
        if(!inputCommand.getText().isEmpty()){
            try {
                // Envoi du message au daemon
                toServer.print(inputCommand.getText().trim() + "\r\n");
                toServer.flush();

                inputCommand.setDisable(true);

                // Attente de la réponse du daemon
                final String messageReturn = readLine(fromServer);

                inputCommand.setDisable(false);

                // Récupération de la valeur
                if(messageReturn.equals("null")){
                    zoneResultDisplay.setText("Commande incorrecte.");
                    inputCommand.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                }else {
                    displayValue(messageReturn);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            zoneResultDisplay.setText("Commande incorrecte.");
            inputCommand.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        }
    }

    private void displayValue(final String returnValueByDaemon){
        /*
            add_service_resp = ("+OK" / "-ERR") [sp message] crlf
            list_service_resp = "SRV" 0*100(sp id) crlf
            state_service_resp = "STATE" sp id sp url sp state crlf
         */

        String tempString;
        final String[] tempStringTab;

        // On va setter le texte à afficher à l'utilisateur
        if(returnValueByDaemon.equals("PERMISSION REFUSED")) {
            zoneResultDisplay.setTextFill(Color.RED);
            zoneResultDisplay.setText("Vous n'avez pas accès à cette commande.");
            inputCommand.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            inputCommand.clear();
        }else {
            zoneResultDisplay.setTextFill(Color.BLACK);
            Matcher regexMessage = Pattern.compile("(?<code>^[^ ]+)\\ ").matcher(returnValueByDaemon);

            if(regexMessage.find()) {
                switch (regexMessage.group("code")) {
                    case "+OK":
                        regexMessage = Pattern.compile("(?<code>^[^ ]+)\\ (?<message>.*)").matcher(returnValueByDaemon);

                        if(regexMessage.find()){
                            zoneResultDisplay.setText(regexMessage.group("message"));
                            zoneResultDisplay.setTextFill(Color.GREEN);
                            inputCommand.setBorder(new Border(new BorderStroke(Color.GREEN, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                            inputCommand.clear();
                        }

                        break;
                    case "-ERR":
                        regexMessage = Pattern.compile("(?<code>^[^ ]+)\\ (?<message>.*)").matcher(returnValueByDaemon);

                        if(regexMessage.find()) {
                            zoneResultDisplay.setText(regexMessage.group("message"));
                            zoneResultDisplay.setTextFill(Color.RED);
                            inputCommand.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                        }

                        break;
                    case "SRV":
                        regexMessage = Pattern.compile("[^ ]+").matcher(returnValueByDaemon);

                        if(regexMessage.find()) {
                            zoneResultDisplay.setText("Une nouvelle page a été ouverte.");

                            tempString = "";

                            while (regexMessage.find()) {
                                tempString += regexMessage.group(0) + "\n";
                            }

                            final Text textView = new Text();
                            textView.setText(tempString);

                            // Création d'un flowpane avec le texte qui affiche la réponse du daemon que l'utilisateur a demandé
                            final FlowPane flowPane = new FlowPane(textView);
                            flowPane.setPadding(new Insets(10));

                            final Group root = new Group(flowPane);

                            openStage(root, "Liste des serveurs");

                            inputCommand.clear();
                            inputCommand.setBorder(new Border(new BorderStroke(Color.GREEN, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                        }

                        break;
                    case "STATE":
                        regexMessage = Pattern.compile("(?<code>^[^ ]+)\\ (?<id>[^ ]+)\\ (?<url>[^ ]+)\\ (?<state>[^ ]+)").matcher(returnValueByDaemon);

                        if(regexMessage.find()) {
                            zoneResultDisplay.setText("ID : " + regexMessage.group("id") + "\nURL : " + regexMessage.group("url") + "\nSTATUS : " + regexMessage.group("state") + "\n");
                            inputCommand.clear();
                            inputCommand.setBorder(new Border(new BorderStroke(Color.GREEN, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                        }

                        break;
                    default:
                        zoneResultDisplay.setText("Erreur !");
                        inputCommand.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                }
            }else{
                zoneResultDisplay.setText("Erreur !");
                inputCommand.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            }
        }
    }

    @FXML
    private void buttonInformation(){
        // Création d'une page qui affiche les personnes qui ont participé au projet
        final FlowPane flowPane = new FlowPane(new Text("Projet SecMon pour le cours de réseau du Campus Guillemins à Liège.\n\n" +
                "Participants : \n" +
                "- Florent Lequien\n- Nathan Lemoine\n- Rausin Julien\n"));
        flowPane.setPadding(new Insets(10));

        final Group root = new Group(flowPane);

        openStage(root, "Crédits");
    }

    private void openStage(final Group group, final String namePage){
        final Scene sceneValidation = new Scene(group);

        final Stage stageValidation = new Stage();
        stageValidation.setTitle(namePage);
        stageValidation.setScene(sceneValidation);
        stageValidation.setResizable(false);
        stageValidation.show();
    }

    private String readLine(final BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if(line != null && line.length() > 2 && line.startsWith("\uFEFF"))
            return line.substring("\uFEFF".length());
        return line;
    }
}
