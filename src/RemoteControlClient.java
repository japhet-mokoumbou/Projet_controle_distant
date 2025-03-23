import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class RemoteControlClient extends Application {
    // Variables de classe pour gérer la connexion réseau et l'interface
    private PrintWriter pw; // Flux de sortie pour envoyer des commandes au serveur
    private Socket socket; // Socket pour la connexion réseau
    private ObservableList<String> commandHistory = FXCollections.observableArrayList(); // Liste observable pour l'historique des commandes
    private Button buttonSend; // Bouton "Envoyer" accessible dans toute la classe

    // Point d'entrée de l'application JavaFX
    public static void main(String[] args) {
        launch(args); // Lance l'application JavaFX
    }

    // Méthode principale qui configure et affiche l'interface graphique
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Remote Control Client"); // Définit le titre de la fenêtre

        // Crée le layout principal avec un fond dégradé bleu
        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #3498db);");

        // Ajoute la section de connexion en haut
        HBox connectionBox = createConnectionBox();
        borderPane.setTop(connectionBox);

        // Crée la liste pour afficher l'historique des commandes
        ListView<String> outputView = new ListView<>(commandHistory);
        outputView.setStyle("-fx-control-inner-background: #ffffff; -fx-background-color: #ffffff; " +
                "-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #cccccc;"); // Style avec fond blanc et bordures arrondies
        outputView.setPrefHeight(400); // Hauteur fixe pour la liste
        
        // Crée une boîte verticale pour le titre et la liste, centrée au milieu
        VBox centerBox = new VBox(15, createTitleLabel("Historique des Commandes"), outputView);
        centerBox.setPadding(new Insets(20)); // Espacement intérieur
        centerBox.setAlignment(Pos.CENTER); // Alignement centré
        borderPane.setCenter(centerBox);

        // Ajoute la section de commande en bas
        HBox commandBox = createCommandBox();
        borderPane.setBottom(commandBox);

        // Configure et affiche la scène
        Scene scene = new Scene(borderPane, 800, 600); // Taille de la fenêtre : 800x600
        primaryStage.setScene(scene);
        primaryStage.show(); // Affiche la fenêtre
    }

    // Crée la section de connexion avec les champs host/port et boutons
    private HBox createConnectionBox() {
        Label labelHost = createStyledLabel("Host:"); // Étiquette pour le champ hôte
        TextField textFieldHost = createStyledTextField("localhost"); // Champ texte avec valeur par défaut
        Label labelPort = createStyledLabel("Port:"); // Étiquette pour le champ port
        TextField textFieldPort = createStyledTextField("5000"); // Champ texte avec port par défaut
        Button buttonConnect = createStyledButton("Connecter", "#27ae60"); // Bouton vert pour se connecter
        Button buttonDisconnect = createStyledButton("Déconnecter", "#c0392b"); // Bouton rouge pour se déconnecter
        buttonDisconnect.setDisable(true); // Désactivé par défaut

        // Crée une boîte horizontale pour organiser les éléments
        HBox box = new HBox(15, labelHost, textFieldHost, labelPort, textFieldPort, 
                          buttonConnect, buttonDisconnect);
        box.setPadding(new Insets(20)); // Espacement intérieur
        box.setAlignment(Pos.CENTER); // Alignement centré
        box.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 10;"); // Fond semi-transparent
        
        // Définit les actions des boutons
        buttonConnect.setOnAction(evt -> connect(textFieldHost.getText(), 
            Integer.parseInt(textFieldPort.getText()), buttonConnect, buttonDisconnect));
        buttonDisconnect.setOnAction(evt -> disconnect(buttonConnect, buttonDisconnect));

        return box;
    }

    // Crée la section pour entrer et envoyer des commandes
    private HBox createCommandBox() {
        Label labelCommand = createStyledLabel("Commande:"); // Étiquette pour le champ commande
        TextField textFieldCommand = createStyledTextField(""); // Champ texte vide pour la commande
        textFieldCommand.setPrefWidth(500); // Largeur fixe pour le champ
        buttonSend = createStyledButton("Envoyer", "#2980b9"); // Bouton bleu pour envoyer
        buttonSend.setDisable(true); // Désactivé par défaut jusqu'à connexion

        // Crée une boîte horizontale pour organiser les éléments
        HBox box = new HBox(15, labelCommand, textFieldCommand, buttonSend);
        box.setPadding(new Insets(20)); // Espacement intérieur
        box.setAlignment(Pos.CENTER); // Alignement centré
        box.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 10;"); // Fond semi-transparent

        buttonSend.setOnAction(evt -> sendCommand(textFieldCommand)); // Action du bouton Envoyer

        return box;
    }

    // Crée une étiquette stylisée avec texte blanc
    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE); // Couleur du texte
        label.setFont(Font.font("Arial", 14)); // Police et taille
        return label;
    }

    // Crée un champ texte stylisé avec bordures arrondies
    private TextField createStyledTextField(String defaultText) {
        TextField tf = new TextField(defaultText);
        tf.setStyle("-fx-background-color: white; -fx-background-radius: 5; " +
                   "-fx-border-radius: 5; -fx-padding: 8;"); // Style avec fond blanc
        tf.setFont(Font.font("Arial", 14)); // Police et taille
        return tf;
    }

    // Crée un bouton stylisé avec couleur personnalisée et effet d'ombre
    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-background-radius: 5; -fx-padding: 8 15 8 15;"); // Style avec couleur et bordures
        btn.setFont(Font.font("Arial", 14)); // Police et taille
        btn.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.3))); // Effet d'ombre
        return btn;
    }

    // Crée un titre stylisé pour les sections
    private Label createTitleLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE); // Couleur du texte
        label.setFont(Font.font("Arial", 16)); // Police et taille plus grande
        return label;
    }

    // Gère la connexion au serveur
    private void connect(String host, int port, Button connectBtn, Button disconnectBtn) {
        try {
            socket = new Socket(host, port); // Crée une nouvelle connexion
            pw = new PrintWriter(socket.getOutputStream(), true); // Initialise le flux de sortie
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Flux d'entrée
            connectBtn.setDisable(true); // Désactive le bouton Connecter
            disconnectBtn.setDisable(false); // Active le bouton Déconnecter
            buttonSend.setDisable(false); // Active le bouton Envoyer
            commandHistory.add("Connecté au serveur " + host + ":" + port); // Ajoute au log

            // Lance un thread pour recevoir les réponses du serveur
            new Thread(() -> {
                try {
                    String response;
                    while ((response = br.readLine()) != null) {
                        String finalResponse = response;
                        Platform.runLater(() -> commandHistory.add(finalResponse)); // Met à jour l'historique
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> commandHistory.add("Erreur: " + e.getMessage()));
                }
            }).start();
        } catch (IOException e) {
            commandHistory.add("Erreur de connexion: " + e.getMessage()); // Affiche les erreurs
        }
    }

    // Gère la déconnexion du serveur
    private void disconnect(Button connectBtn, Button disconnectBtn) {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Ferme la connexion
                commandHistory.add("Déconnecté du serveur"); // Ajoute au log
                connectBtn.setDisable(false); // Réactive le bouton Connecter
                disconnectBtn.setDisable(true); // Désactive le bouton Déconnecter
                buttonSend.setDisable(true); // Désactive le bouton Envoyer
            }
        } catch (IOException e) {
            commandHistory.add("Erreur lors de la déconnexion: " + e.getMessage()); // Affiche les erreurs
        }
    }

    // Envoie une commande au serveur
    private void sendCommand(TextField commandField) {
        String command = commandField.getText(); // Récupère la commande saisie
        if (!command.isEmpty() && pw != null) { // Vérifie que la commande n'est pas vide et qu'il y a une connexion
            pw.println(command); // Envoie la commande
            commandHistory.add("Commande envoyée: " + command); // Ajoute au log
            commandField.clear(); // Vide le champ texte
        }
    }
}