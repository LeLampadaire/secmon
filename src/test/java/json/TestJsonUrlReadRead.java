package json;

import com.google.gson.Gson;
import models.Account;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TestJsonUrlReadRead {
    @Test
    public void test(){
        final Path path = Paths.get("src", "main", "resources", "accounts.json");

        // Permet d'écrire plus tard dans le fichier
        StringBuilder data = new StringBuilder();

        /* Lecture d'un item */
        try {
            final File myObj = new File(path.toString()); // Récupère le fichier du path
            final Scanner myReader = new Scanner(myObj); // Récupère les données du fichier

            // Tant qu'il y a des lignes, on lit les données
            while (myReader.hasNextLine()) {
                data.append(myReader.nextLine()); // Ajout dans la variable data
            }

            myReader.close(); // Fermeture
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        // On appelle une méthode de GSON qui permet de récupérer depuis le json les données et les mettre dans l'objet demandé
        Account[] accounts = new Gson().fromJson(data.toString(), Account[].class);


        Map<String, Account> clientAccount = Collections.synchronizedMap(new HashMap<>());
        for(Account account : accounts){
            clientAccount.put(account.getUsername(), new Account(
                    account.getUsername(),
                    account.getPassword(),
                    account.isAdmin()
            ));
        }
    }
}
