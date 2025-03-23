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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// Classe principale pour le serveur avec interface graphique
public class RemoteControlServerGUI extends Application {
    private ServerSocket serverSocket; // Socket serveur pour écouter les connexions
    private boolean isActive = false; // Indicateur d'état du serveur (actif ou non)
    private final List<ClientHandler> clients = new ArrayList<>(); // Liste des clients connectés
    private final ObservableList<String> serverLogs = FXCollections.observableArrayList(); // Liste observable pour les logs

    // Point d'entrée de l'application JavaFX
    public static void main(String[] args) {
        launch(args); // Lance l'application JavaFX
    }

    // Méthode principale qui configure et affiche l'interface graphique
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Remote Control Server"); // Définit le titre de la fenêtre

        // Crée le layout principal avec un fond dégradé bleu
        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #3498db);");

        // Ajoute la section de contrôle en haut
        HBox controlBox = createControlBox();
        borderPane.setTop(controlBox);

        // Crée la liste pour afficher les logs du serveur
        ListView<String> logView = new ListView<>(serverLogs);
        logView.setStyle("-fx-control-inner-background: #ffffff; -fx-background-color: #ffffff; " +
                "-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #cccccc;"); // Style avec fond blanc et bordures
        logView.setPrefHeight(400); // Hauteur fixe pour la liste

        // Crée une boîte verticale pour le titre et la liste, centrée au milieu
        VBox centerBox = new VBox(15, createTitleLabel("Logs du Serveur"), logView);
        centerBox.setPadding(new Insets(20)); // Espacement intérieur
        centerBox.setAlignment(Pos.CENTER); // Alignement centré
        borderPane.setCenter(centerBox);

        // Configure et affiche la scène
        Scene scene = new Scene(borderPane, 800, 600); // Taille de la fenêtre : 800x600
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(evt -> stopServer()); // Ferme proprement le serveur à la fermeture
        primaryStage.show(); // Affiche la fenêtre
    }

    // Crée la section de contrôle avec le champ port et les boutons
    private HBox createControlBox() {
        Label labelPort = createStyledLabel("Port:"); // Étiquette pour le champ port
        TextField textFieldPort = createStyledTextField("5000"); // Champ texte avec port par défaut
        Button buttonStart = createStyledButton("Démarrer", "#27ae60"); // Bouton vert pour démarrer
        Button buttonStop = createStyledButton("Arrêter", "#c0392b"); // Bouton rouge pour arrêter
        buttonStop.setDisable(true); // Désactivé par défaut

        // Crée une boîte horizontale pour organiser les éléments
        HBox box = new HBox(15, labelPort, textFieldPort, buttonStart, buttonStop);
        box.setPadding(new Insets(20)); // Espacement intérieur
        box.setAlignment(Pos.CENTER); // Alignement centré
        box.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 10;"); // Fond semi-transparent

        // Définit les actions des boutons
        buttonStart.setOnAction(evt -> startServer(textFieldPort, buttonStart, buttonStop));
        buttonStop.setOnAction(evt -> stopServer(buttonStart, buttonStop, textFieldPort));

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

    // Démarre le serveur sur le port spécifié
    private void startServer(TextField textFieldPort, Button buttonStart, Button buttonStop) {
        if (!isActive) { // Vérifie si le serveur n'est pas déjà actif
            int port;
            try {
                port = Integer.parseInt(textFieldPort.getText()); // Convertit le port en entier
            } catch (NumberFormatException e) {
                serverLogs.add("Erreur: Port invalide"); // Affiche une erreur si le port est invalide
                return;
            }

            // Lance un thread pour exécuter le serveur
            new Thread(() -> {
                try {
                    serverSocket = new ServerSocket(port); // Crée le socket serveur
                    isActive = true; // Marque le serveur comme actif
                    Platform.runLater(() -> {
                        serverLogs.add("Serveur démarré sur le port " + port); // Ajoute au log
                        buttonStart.setDisable(true); // Désactive le bouton Démarrer
                        buttonStop.setDisable(false); // Active le bouton Arrêter
                        textFieldPort.setDisable(true); // Désactive le champ port
                    });

                    // Boucle pour accepter les connexions des clients
                    while (isActive) {
                        Socket clientSocket = serverSocket.accept(); // Accepte une nouvelle connexion
                        ClientHandler clientHandler = new ClientHandler(clientSocket, clients.size() + 1);
                        clients.add(clientHandler); // Ajoute le client à la liste
                        clientHandler.start(); // Démarre le gestionnaire du client
                    }
                } catch (IOException e) {
                    if (isActive) {
                        Platform.runLater(() -> serverLogs.add("Erreur serveur: " + e.getMessage()));
                    }
                }
            }).start();
        }
    }

    // Arrête le serveur et ferme toutes les connexions
    private void stopServer(Button buttonStart, Button buttonStop, TextField textFieldPort) {
        if (isActive) { // Vérifie si le serveur est actif
            try {
                isActive = false; // Marque le serveur comme inactif
                for (ClientHandler client : clients) {
                    client.close(); // Ferme chaque client connecté
                }
                clients.clear(); // Vide la liste des clients
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close(); // Ferme le socket serveur
                }
                serverLogs.add("Serveur arrêté"); // Ajoute au log
                buttonStart.setDisable(false); // Réactive le bouton Démarrer
                buttonStop.setDisable(true); // Désactive le bouton Arrêter
                textFieldPort.setDisable(false); // Réactive le champ port
            } catch (IOException e) {
                serverLogs.add("Erreur lors de l'arrêt: " + e.getMessage()); // Affiche les erreurs
            }
        }
    }

    // Méthode appelée à la fermeture de la fenêtre pour arrêter le serveur
    private void stopServer() {
        if (isActive) { // Vérifie si le serveur est actif
            try {
                isActive = false; // Marque le serveur comme inactif
                for (ClientHandler client : clients) {
                    client.close(); // Ferme chaque client connecté
                }
                clients.clear(); // Vide la liste des clients
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close(); // Ferme le socket serveur
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture: " + e.getMessage()); // Erreur dans la console
            }
        }
    }

    // Classe interne pour gérer chaque client connecté
    class ClientHandler extends Thread {
        private final Socket socket; // Socket du client
        private final int clientNumber; // Numéro unique du client
        private PrintWriter pw; // Flux de sortie pour envoyer des réponses au client

        // Constructeur du gestionnaire de client
        public ClientHandler(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
        }

        // Méthode exécutée dans un thread pour gérer le client
        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Flux d'entrée
                pw = new PrintWriter(socket.getOutputStream(), true); // Flux de sortie
                String clientIP = socket.getRemoteSocketAddress().toString(); // Adresse IP du client
                Platform.runLater(() -> serverLogs.add("Client " + clientNumber + " connecté depuis " + clientIP));
                pw.println("Bienvenue, vous êtes le client " + clientNumber); // Message de bienvenue

                String command;
                // Boucle pour recevoir et traiter les commandes du client
                while ((command = br.readLine()) != null && isActive) {
                    final String currentCommand = command; // Capture la commande actuelle
                    Platform.runLater(() -> serverLogs.add("Commande reçue de " + clientIP + ": " + currentCommand));
                    String result = executeCommand(currentCommand); // Exécute la commande
                    pw.println(result); // Envoie le résultat au client
                }
            } catch (IOException e) {
                if (isActive) {
                    Platform.runLater(() -> serverLogs.add("Erreur avec le client " + clientNumber + ": " + e.getMessage()));
                }
            } finally {
                close(); // Ferme la connexion à la fin
            }
        }

        // Exécute une commande système et retourne le résultat
        private String executeCommand(String command) {
            try {
                ProcessBuilder pb;
                // Détermine la commande shell selon le système d'exploitation
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    pb = new ProcessBuilder("cmd.exe", "/c", command); // Windows
                } else {
                    pb = new ProcessBuilder("/bin/sh", "-c", command); // Unix/Linux
                }

                Process process = pb.start(); // Lance le processus
                StringBuilder output = new StringBuilder(); // Stocke la sortie

                // Lit la sortie standard
                BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                while ((line = stdOut.readLine()) != null) {
                    output.append(line).append("\n"); // Ajoute chaque ligne à la sortie
                }
                // Lit les erreurs
                while ((line = stdErr.readLine()) != null) {
                    output.append("Erreur: ").append(line).append("\n"); // Ajoute les erreurs à la sortie
                }

                process.waitFor(); // Attend la fin de l'exécution
                return output.length() > 0 ? output.toString() : "Commande exécutée sans sortie.";
            } catch (IOException | InterruptedException e) {
                return "Erreur lors de l'exécution: " + e.getMessage(); // Retourne l'erreur si échec
            }
        }

        // Ferme la connexion avec le client
        public void close() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close(); // Ferme le socket
                }
                Platform.runLater(() -> serverLogs.add("Client " + clientNumber + " déconnecté"));
            } catch (IOException e) {
                Platform.runLater(() -> serverLogs.add("Erreur lors de la fermeture du client " + clientNumber + ": " + e.getMessage()));
            }
        }
    }
}