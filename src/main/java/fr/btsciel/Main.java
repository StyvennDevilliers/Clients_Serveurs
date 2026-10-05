package fr.btsciel;

import exceptions.DiagnosticException;

import java.io.IOException;


public class Main {
    private static final int PORT = 4444;
    
    static void main(String[] args) throws IOException, InterruptedException {
        try {
            System.out.println("""
                    1- TCP AES Classe D
                    2- UDP Base
                    """);
            int choix = In.readInteger();
            switch (choix) {
                case 1:
                    new Serveur_TCP_AES_ClasseD();
                    break;
                case 2:
                    new Serveur_UDP_Base(PORT);
                    break;
                default:
            }
        }catch (Exception e){
            DiagnosticException.afficheException(e);
        }
    }
}
