package fr.btsciel;

import aes.Aes_cbc;
import config.Lecture_Json;
import modele.Config_AES;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Serveur_TCP_AES {
    private final int PORT;
    private static Aes_cbc aes;

    private static final String MESSAGE_ACCUEIL = """
            Envoyer une requête parmis celle-ci :
                • « HELLO », le serveur répond un message de bienvenue.
                • « TIME », le serveur répond la date et l'heure.
                • « ECHO …une phrase… », le serveur répond en répétant la phrase.
                • « YOU » ou « WHOAREYOU? », le serveur renvoi sa socket (IP@ et port).
                • « ME » ou « WHOAMI? », le serveur renvoi la socket du client (IP@ et port).
                • « FIN », le client est déconnecté.
            """;

    public Serveur_TCP_AES(int port) throws IOException {
            this.PORT = port;
            ServerSocket serveur = new ServerSocket(PORT);
            System.out.println("Serveur en fonctionnement sur le port " + PORT + ".");
            while (true) {
                try {
                    Socket client = serveur.accept();
                    OutputStream outS = client.getOutputStream();
                    InputStream inS = client.getInputStream();
                    System.out.println("Connexion avec : " + client);
                    Lecture_Json lectureJson = new Lecture_Json("src/main/resources/configuration_json.json");
                    Config_AES configAes = lectureJson.getConfAES();
                    System.out.println("motDePasse = " + configAes.motDePasse());
                    System.out.println("iv = " + configAes.iv());
                    if (configAes.motDePasse() == null) {
                        throw new RuntimeException("motDePasse est null");
                    }
                    if (configAes.iv() == null) {
                        throw new RuntimeException("iv est null");
                    }
                    aes = new Aes_cbc(configAes.getMotdepasse(), configAes.getIV());
                    outS.write(aes.cryptage(MESSAGE_ACCUEIL.getBytes(StandardCharsets.UTF_8)));
                    byte[] bufferByte = new byte[65535];

                    while (true) {
                        int nblus = inS.read(bufferByte);
                        if (nblus <= 0) break;
                        String message;
                        byte[] bufferByteTemps = Arrays.copyOf(bufferByte, nblus);
                        message = new String(aes.decryptage(bufferByteTemps));
                        System.out.println("Message reçu : " + message);
                        message = message.trim();
                        if (TraitementSwitch(message, client, outS)) {
                            inS.close();
                            outS.write(aes.cryptage("exit".getBytes(StandardCharsets.UTF_8)));
                            outS.flush();
                            outS.close();
                            client.close();
                            break;
                        }
                    }

                } catch(IOException e){
                    System.err.println("Connexion interrompue, genre stopper finito pipo : " + e.getMessage());
                }
                System.out.println("Client déconnecté.");
            }
    }

    boolean TraitementSwitch(String message, Socket client, OutputStream sortie) throws IOException {
        System.out.println("Message Traiter: " + message);
        switch (message.toLowerCase()){
            case "fin","exit" ->
            {
                message = "JE VOUS DECONNECTE bande de vilain !!!";
                sortie.write(aes.cryptage(message.getBytes(StandardCharsets.UTF_8)));
                return true;
            }
            case "hello"-> {
                message = "Bienvenue vous êtes bien connecté.";
                sortie.write(aes.cryptage(message.getBytes(StandardCharsets.UTF_8)));
            }
            case "time"-> {
                LocalDateTime date = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                sortie.write(aes.cryptage((formatter.format(date).getBytes(StandardCharsets.UTF_8))));
            }
            case "you","whoareyou?"-> {
                message = InetAddress.getLocalHost() + ":" + PORT;
                sortie.write(aes.cryptage((message.getBytes(StandardCharsets.UTF_8))));
            }
            case "me","whoami?"-> {
                message = client.getRemoteSocketAddress().toString() ;
                sortie.write(aes.cryptage((message.getBytes(StandardCharsets.UTF_8))));
            }
            default -> {
                try{
                    if(message.substring(0,4).equalsIgnoreCase("echo")){
                        message = message.substring(4);
                        sortie.write(aes.cryptage((message.getBytes(StandardCharsets.UTF_8))));
                    }
                }catch (Exception e){

                }
            }
        }
        sortie.flush();
        return false;
    }

    boolean TraitementIf(String message,Socket client,PrintWriter sortie) throws IOException {
        System.out.println("Message envoyé : " + message);
        if(message.toLowerCase().equals("fin")){
            message = "JE VOUS DECONNECTE bande de vilain !!!";
            sortie.println(message);
            return true;
        }
        if(message.toLowerCase().equals("hello")){
            message = "Bienvenue vous êtes bien connecté.";
            sortie.println(message);
        }
        if(message.toLowerCase().equals("time")){
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            sortie.println(formatter.format(date));
        }
        if(message.toLowerCase().equals("you")||message.toLowerCase().equals("whoareyou?")){
            message = InetAddress.getLocalHost() + ":" + PORT;
            sortie.println(message);
        }
        if(message.toLowerCase().equals("me")||message.toLowerCase().equals("whoami?")){
            message = client.getRemoteSocketAddress().toString() ;
            sortie.println(message);
        }
        if(message.toLowerCase().equals("time")){
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            sortie.println(formatter.format(date));
        }
        if(message.substring(0,4).equalsIgnoreCase("echo")){
            message = message.substring(4);
            sortie.println(message);
        }
        return false;
    }

}