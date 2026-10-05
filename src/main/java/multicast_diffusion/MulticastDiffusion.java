package multicast_diffusion;

import fr.btsciel.Serveur_TCP_AES_ClasseD;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MulticastDiffusion {
    private MulticastSocket ds;
    private final String INTERFACE_NAME = "ethernet_32769";
    private InetAddress IA = InetAddress.getByName("224.0.0.250");
    private byte[] dataReponse= new byte[19];
    private int port = 5555;
    public static boolean enMarche;


    private byte[] dataSortie = (InetAddress.getLocalHost().getHostAddress() + ";" + Serveur_TCP_AES_ClasseD.port_TCP + ";" + Serveur_TCP_AES_ClasseD.port_UDP).getBytes(StandardCharsets.UTF_8);
    private int portReponse = 5556;
    private byte ttl = 60;
    private DatagramPacket dp;
    private MulticastSocket ms;

    public MulticastDiffusion() throws IOException {

        ms = new MulticastSocket();
        NetworkInterface ni = NetworkInterface.getByName(INTERFACE_NAME);
        if (ni != null) ms.setNetworkInterface(ni);
        ms.setTimeToLive(ttl);

        ds = new MulticastSocket(port);
        ds.joinGroup(new InetSocketAddress(IA,port),ni);


        new Thread(()->{
            while(true){
                try{
                    dp = new DatagramPacket(dataReponse, dataReponse.length);
                    ds.receive(dp);

                    String message = new String(dp.getData(), 0, dp.getLength(), StandardCharsets.UTF_8).trim();
                    if(message.equals("Tu es qui?") && !enMarche){
                        DatagramPacket reponse = new DatagramPacket(dataSortie,dataSortie.length,dp.getAddress(),portReponse);
                        ms.send(reponse);
                        System.out.println(Arrays.toString(reponse.getData()));

                    }
                } catch (IOException | RuntimeException e) {
                    System.err.println(e);
                }
            }
        }).start();
    }


}
