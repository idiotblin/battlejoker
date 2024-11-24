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
import java.util.List;

public class MulticastScoreboardWindow {
	Stage stage;

    @FXML
    ListView<String> scoreList = new ListView<>();

    public MulticastScoreboardWindow(Stage stage, List<String> strings) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("multicastScoreUI.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        this.stage = stage;
        stage.setScene(scene);
        stage.setTitle("Top 10 historical Scores");
        stage.setMinWidth(scene.getWidth());
        stage.setMinHeight(scene.getHeight());

        setFont(14);
        updateList(strings);

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

    private void updateList(List<String> strings) {
        try {
            ObservableList<String> items = FXCollections.observableArrayList();
            items.addAll(strings);
            scoreList.setItems(items);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}

