package graphics;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import security.SHA;
import security.TLS;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnexionFXController {
    public static final String DEFAULT_DESTINATION = "localhost";
    public static final int DEFAULT_PORT_DESTINATION = 4042;
    private SSLSocket daemonSocket;

    public ConnexionFXController(){
        try {
            daemonSocket = TLS.createClientSocketSSL(DEFAULT_DESTINATION, DEFAULT_PORT_DESTINATION);
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | KeyManagementException | CertificateException e) {
            e.printStackTrace();
            Platform.exit();
            System.exit(0);
        }
    }

    @FXML
    private TextField inputPseudoConnexion;

    @FXML
    private PasswordField inputPasswordConnexion;

    @FXML
    private Button buttonConnexionID;

    @FXML
    private void buttonConnexion(){
        /*
            connexion_resp = "CONNEXION" sp ("+OK" / "-ERR") crlf
        * */
        final String username = inputPseudoConnexion.getText();
        final String password = inputPasswordConnexion.getText();
        boolean testConnexion = false;

        try{
            final PrintWriter toServer = new PrintWriter(new OutputStreamWriter(daemonSocket.getOutputStream(), StandardCharsets.UTF_8), true);

            // Envoi de la connexion
            toServer.print("CONNEXION "+ username +" "+ SHA.hash(password) +"\r\n");
            toServer.flush();

            // Attente de la réponse
            final BufferedReader fromServer = new BufferedReader(new InputStreamReader(daemonSocket.getInputStream(), StandardCharsets.UTF_8));
            final String messageReturn = readLine(fromServer);

            if(!messageReturn.equals("null")){
                final Matcher regexMessage = Pattern.compile("(?<code>[^ ]+)\\ (?<messageErreur>[^ ]+)").matcher(messageReturn);

                if(regexMessage.find()){
                    if(regexMessage.group("messageErreur").equals("+OK")){
                        testConnexion = true;
                    }
                }
            }

            if(testConnexion){
                // Fermeture de la scène précédente (Page de connexion)
                Stage stageConnexion = (Stage) inputPseudoConnexion.getScene().getWindow();
                stageConnexion.close();

                // Création de la nouvelle page (Index)
                final Parent root;

                try {
                    // Récupérer la fichier FXML
                    final FXMLLoader chargerFXML = new FXMLLoader(getClass().getResource("/index.fxml"));

                    // Le controleur FXML connait le controlleur
                    final IndexFXController indexFXController = new IndexFXController(daemonSocket);
                    chargerFXML.setController(indexFXController);

                    root = chargerFXML.load();
                    final Scene scene = new Scene(root);
                    final Stage stageIndex = new Stage();
                    stageIndex.setScene(scene);

                    // Page de base
                    stageIndex.setTitle("Panel");
                    stageIndex.setResizable(false);
                    stageIndex.show();
                } catch (IOException ex) {
                    System.err.println(Arrays.toString(ex.getStackTrace()));
                }
            }else{
                inputPseudoConnexion.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                inputPasswordConnexion.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private String readLine(final BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if(line != null && line.length() > 2 && line.startsWith("\uFEFF"))
            return line.substring("\uFEFF".length());
        return line;
    }
}
