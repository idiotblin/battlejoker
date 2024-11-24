import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class MulticastScoreboardWindow {
    Stage stage;

    @FXML
    ListView<String> scoreList = new ListView<>();

    final ScoreboardEngine scoreboardEngine = ScoreboardEngine.getInstance();

    private ObservableList<String> observableStrings = FXCollections.observableArrayList();
    private AnimationTimer animationTimer;

    public MulticastScoreboardWindow(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("multicastScoreUI.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        this.stage = stage;
        stage.setScene(scene);
        stage.setTitle("Top 10 Historical Scores");
        stage.setMinWidth(scene.getWidth());
        stage.setMinHeight(scene.getHeight());

        setFont(14);

        scoreList.setItems(observableStrings);
        startRendering();

        stage.show();
    }

    private void setFont(int fontSize) {
        scoreList.setCellFactory(param -> {
            TextFieldListCell<String> cell = new TextFieldListCell<>();
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                cell.setFont(Font.font("Courier New", fontSize));
            } else if (osName.contains("mac")) {
                cell.setFont(Font.font("Menlo", fontSize));
            } else {
                cell.setFont(Font.font("Monospaced", fontSize));
            }
            return cell;
        });
    }

    private void startRendering() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateList();
            }
        };
        animationTimer.start();
    }

    private void updateList() {
        Platform.runLater(() -> {
            try {
                observableStrings.setAll(scoreboardEngine.getStrings());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}