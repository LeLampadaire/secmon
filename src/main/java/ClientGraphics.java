import graphics.ConnexionFXController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.util.Arrays;

public class ClientGraphics extends Application {
    @Override
    public void start(final Stage stage) {
        final Parent root;

        try {
            // Récupérer la fichier FXML
            final FXMLLoader chargerFXML = new FXMLLoader(getClass().getResource("/connexion.fxml"));

            // Le controleur FXML connait le controlleur
            final ConnexionFXController panelFXController = new ConnexionFXController();
            chargerFXML.setController(panelFXController);

            root = chargerFXML.load();
            final Scene scene = new Scene(root);
            stage.setScene(scene);

            // Page de base
            stage.setTitle("Connexion");
            stage.setResizable(false);
            stage.setOnCloseRequest(windowEvent -> {
                Platform.exit();
                System.exit(0);
            });
            stage.show();
        } catch (IOException ex) {
            System.err.println(Arrays.toString(ex.getStackTrace()));
        }
    }

    /**
     * Point d'entrée du programme
     *
     * @param args String
     */
    public static void main(final String[] args) {
        launch(args);
    }
}
