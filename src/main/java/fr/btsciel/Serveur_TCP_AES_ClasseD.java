package fr.btsciel;

import aes.Aes_cbc;
import config.Lecture_Json;
import config.Outils;
import exceptions.DiagnosticException;
import modele.Config_AES;
import modele.Config_Client;
import modele.IPV4;
import multicast_diffusion.MulticastDiffusion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Serveur_TCP_AES_ClasseD {
    public static int port_TCP;
    public static int port_UDP;
    private static Aes_cbc aes;
    private static final String FICHIER_CONFIG = "src/main/resources/configuration_json.json";
    private final Config_AES configAes;
    private final ServerSocket serveur;

    private static final String MESSAGE_ACCUEIL = """
            Envoyer une requête parmis celle-ci :
                • « HELLO », le serveur répond un message de bienvenue.
                • « TIME », le serveur répond la date et l'heure.
                • « ECHO …une phrase… », le serveur répond en répétant la phrase.
                • « YOU » ou « WHOAREYOU? », le serveur renvoi sa socket (IP@ et port).
                • « ME » ou « WHOAMI? », le serveur renvoi la socket du client (IP@ et port).
                • « FIN », le client est déconnecté.
            """;

    public Serveur_TCP_AES_ClasseD() throws IOException, InterruptedException {
        Random random = new Random();
        port_TCP = random.nextInt(9999 - 1024 + 1) + 1024;
        port_UDP = random.nextInt(9999 - 1024 + 1) + 1024 ;
        MulticastDiffusion multicastDiffusion = new MulticastDiffusion();


        configAes = new Lecture_Json(FICHIER_CONFIG).getConfAES();
        System.out.println("motDePasse = " + configAes.motDePasse());
        System.out.println("iv = " + configAes.iv());
        if (configAes.motDePasse() == null) throw new RuntimeException("motDePasse est null");
        if (configAes.iv() == null) throw new RuntimeException("iv est null");

        serveur = new ServerSocket(port_TCP);
        System.out.println("Serveur en fonctionnement sur le port:" + port_TCP);
        while (true) {
            Socket client = serveur.accept();
            MulticastDiffusion.enMarche = true;
            try {

                OutputStream outS = client.getOutputStream();
                InputStream inS = client.getInputStream();
                System.out.println("Connexion avec : " + client);


                aes = new Aes_cbc(configAes.getMotdepasse(), configAes.getIV());
                outS.write(aes.cryptage(MESSAGE_ACCUEIL.getBytes(StandardCharsets.UTF_8)));
                byte[] bufferByte = new byte[65535];

                while (true) {
                    int nblus = inS.read(bufferByte);
                    if (nblus <= 0) break;
                    byte[] bufferByteTemps = Arrays.copyOf(bufferByte, nblus);
                    String message = new String(aes.decryptage(bufferByteTemps)).trim();
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
            }finally{
                client.close();
                MulticastDiffusion.enMarche = false;
                System.out.println("Client déconnecté.");
            }

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
                message = InetAddress.getLocalHost() + ":" + port_TCP;
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


}