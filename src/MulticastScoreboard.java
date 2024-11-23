import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastScoreboard extends Application {
    String MULTICAST_ADDRESS = "223.0.7.7";
    int MULTICAST_PORT = 39993;

    @Override
    public void start(Stage primaryStage) throws Exception {
        MulticastScoreboardWindow win = new MulticastScoreboardWindow();
    }

    private void receiveUDP() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        try {
            MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
            socket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));



            while(true) {
                socket.receive(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        System.setErr(new FilteredStream(System.err));

        launch();
    }
}
