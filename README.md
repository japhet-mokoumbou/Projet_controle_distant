# RemoteControlApp

**RemoteControlApp** est une application client-serveur développée en JavaFX permettant à un utilisateur d'exécuter des commandes système à distance via une interface graphique intuitive. Le serveur écoute les connexions des clients, exécute les commandes reçues et renvoie les résultats, tandis que le client offre une interface pour se connecter, envoyer des commandes et afficher l'historique.

## Fonctionnalités
- Interface graphique moderne avec JavaFX pour le client et le serveur.
- Connexion réseau via sockets TCP.
- Exécution de commandes système (ex. `dir` sur Windows, `ls` sur Unix/Linux).
- Historique des commandes et logs en temps réel.
- Gestion multi-clients côté serveur.
- Support multi-plateforme (Windows, Linux, macOS).

## Prérequis
Pour exécuter cette application, vous devez avoir installé :
- **Java 8 ou supérieur** : Téléchargez-le depuis [java.com](https://www.java.com) ou utilisez un JDK (ex. OpenJDK).
- **JavaFX SDK** : Téléchargez-le depuis [gluonhq.com](https://gluonhq.com/products/javafx/) ou via un gestionnaire de dépendances comme Maven/Gradle.
- **Git** : Pour cloner le dépôt (optionnel, voir [git-scm.com](https://git-scm.com/)).

## Installation
1. **Cloner le dépôt** :
   git clone https://github.com/japhet-mokoumbou/Projet_controle_distant.git
   cd Projet_controle_distant