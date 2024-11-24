import javafx.application.Application;
import javafx.fxml.FXML;
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



    @Override
    public void start(Stage primaryStage) throws Exception {
        new MulticastScoreboardWindow(primaryStage);
    }


    public static void main(String[] args) {
        System.setErr(new FilteredStream(System.err));

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
