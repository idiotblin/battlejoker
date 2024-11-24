import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScoreboardEngine {
    final static String MULTICAST_ADDRESS = "224.0.7.7";
    final static int MULTICAST_PORT = 39993;

    public static List<String> getStrings() {
        return strings;
    }

    private static List<String> strings = new ArrayList<>();
    private static ScoreboardEngine instance;
    public ScoreboardEngine() {
        Thread t1 = new Thread(()-> {
                receiveUDP();
        });
        t1.start();
    }

    private void receiveUDP() {
        System.setProperty("java.net.preferIPv4Stack", "true");

        try {
            MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
            socket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                socket.receive(packet);
                int len = packet.getLength();
                String src = packet.getAddress().getHostAddress().toString();
                int port = packet.getPort();
                System.out.println("Packet received from: " + src + " " + port);

                String rawMessage = new String(buffer, 0, len);
                String[] strArr = rawMessage.split("\n");
                strings = Arrays.asList(strArr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ScoreboardEngine getInstance() {
        if (instance == null)
            instance = new ScoreboardEngine();
        return instance;
    }
}
