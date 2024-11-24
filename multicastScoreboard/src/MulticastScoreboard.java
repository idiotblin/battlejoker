import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MulticastScoreboard extends Application {
    final static String MULTICAST_ADDRESS = "223.0.7.7";
    final static int MULTICAST_PORT = 39993;

    private static List<String> strings = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        new MulticastScoreboardWindow(primaryStage, strings);
    }

    private static void receiveUDP() {
        System.setProperty("java.net.preferIPv4Stack", "true");

        try {
            MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
            socket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while(true) {
                socket.receive(packet);
                int len = packet.getLength();
                String src = packet.getAddress().getHostAddress().toString();
                int port = packet.getPort();
                System.out.println("Packet received from: " + src + " " + port);

                String rawMessage = new String(buffer, 0, len);
                strings = Arrays.asList(rawMessage.split("\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.setErr(new FilteredStream(System.err));
        
        Thread t = new Thread(()->{
        	receiveUDP();
        });
        t.start();

        launch();
    }
}

class FilteredStream extends PrintStream {

    public FilteredStream(OutputStream out) {
        super(out);
    }

    @Override
    public void println(String x) {
        if (x != null && !x.contains("SLF4J: "))
            super.println(x);
    }

    @Override
    public void print(String x) {
        if (x!= null && !x.contains("WARNING: Loading FXML document with JavaFX API of version 18"))
            super.print(x);
    }
}
