import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class GetNameDialog {
    @FXML
    TextField nameField;

    @FXML
    TextField ipField;

    @FXML
    TextField portField;


    @FXML
    ComboBox<String> backgroundSelector;

    @FXML
    Button goButton;

    Stage stage;
    String playername;
    String ip;
    String port;
    String selectedBackground;

    public GetNameDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("getNameUI.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Battle Joker");
        stage.setMinWidth(scene.getWidth());
        stage.setMinHeight(scene.getHeight());

        backgroundSelector.getItems().addAll("bg", "bg1", "bg2");
        backgroundSelector.setValue("bg");

        goButton.setOnMouseClicked(this::OnButtonClick);

        stage.showAndWait();
    }

    @FXML
    void OnButtonClick(Event event) {
        playername = nameField.getText().trim();
        ip = ipField.getText().trim();
        port = portField.getText().trim();

        selectedBackground = backgroundSelector.getValue();

        // add ip and port
        if (!playername.isEmpty() && !ip.isEmpty() && !port.isEmpty()) // AND ip !isEmpty AND port !isEmpty
            stage.close();
    }

    public String getPlayerName() {
        return playername;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public String getSelectedBackground() {
        return selectedBackground;
    }
}
