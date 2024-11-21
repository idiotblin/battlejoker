import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class ScoreboardWindow {
    Stage stage;

    @FXML
    ListView<String> scoreList;

    @FXML
    private Button showAllScores;

    @FXML
    private Button showTheWinner;

    private static ArrayList<String> scores;

    public ScoreboardWindow(ArrayList<String> scoreBoard) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("scoreUI.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Battle Joker");
        stage.setMinWidth(scene.getWidth());
        stage.setMinHeight(scene.getHeight());

        setFont(14);

        scores.addAll(scoreBoard);
        ArrayList<String> display = new ArrayList<>();
        display.add(scores.get(0));
        updateList(display);

        stage.showAndWait();
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

    private void updateList(ArrayList<String> scoreBoard) {
        try {
            ObservableList<String> items = FXCollections.observableArrayList();
            items.addAll(scoreBoard);
            scoreList.setItems(items);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleButton1Click() {
        showAllScores.setVisible(false);
        showTheWinner.setVisible(true);
        ArrayList<String> display = new ArrayList<>(scores);
        display.remove(0);
        updateList(display);
    }

    @FXML
    private void handleButton2Click() {
        showAllScores.setVisible(false);
        showTheWinner.setVisible(true);
        ArrayList<String> display = new ArrayList<>();
        display.add(scores.get(0));
        updateList(display);
    }
}
