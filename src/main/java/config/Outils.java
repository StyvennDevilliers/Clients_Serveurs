package config;

import modele.IPV4;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;


public class Outils {
    public static ArrayList<IPV4> getSystemIP() throws SocketException {
        ArrayList<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        ArrayList<IPV4> ipv4s = new ArrayList<>();
        interfaces.forEach(networkInterface -> {
            try {
                if(networkInterface.isUp()&&!networkInterface.isLoopback()){
                    ArrayList<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
                    inetAddresses.forEach(inetAddress -> ipv4s.add(new IPV4(networkInterface.getDisplayName(),inetAddress.getHostName(),inetAddress.getHostAddress())));
                }
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        });
        return ipv4s;
    }
}
