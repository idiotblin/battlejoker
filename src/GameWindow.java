import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GameWindow {
    @FXML
    MenuBar menuBar;

    @FXML
    Pane boardPane;

    @FXML
    Canvas canvas;

    @FXML
    VBox playerStats;

    @FXML
    Label turnLabel;

    @FXML
    Button waitButton;

    @FXML
    Button leaveButton;

    @FXML
    Label waiting;

    @FXML
    Label queueLabel;

    @FXML
    Button leave;

    Stage stage;
    Stage waitOrLeaveStage;

    AnimationTimer animationTimer;

    final String imagePath = "images/";
    final String bgImagePath = "backgrounds/";
    String bgName = "bg";
    final String[] symbols = {"bg", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "Joker"};
    final Image[] images = new Image[symbols.length];
    static GameEngine gameEngine;

    public GameWindow(Stage stage, String ip, String port, String background) throws IOException {
        setBgName(background);
        loadImages();
        gameEngine = GameEngine.getInstance(ip, port);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainUI.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        this.stage = stage;

        stage.setScene(scene);
        stage.setTitle("Battle Joker");
        stage.setMinWidth(scene.getWidth());
        stage.setMinHeight(scene.getHeight());

        stage.widthProperty().addListener(w -> onWidthChangedWindow(((ReadOnlyDoubleProperty) w).getValue()));
        stage.heightProperty().addListener(h -> onHeightChangedWindow(((ReadOnlyDoubleProperty) h).getValue()));
        stage.setOnCloseRequest(event -> quit());
        leave.setOnMouseClicked(event -> quit());

        stage.show();
        initCanvas();
        if (!gameEngine.isInGame()) {
            showWaitOrLeaveUI();
        } else {
            gameStart();
        }
    }

    private void showWaitOrLeaveUI() {
        try {
            FXMLLoader waitLoader = new FXMLLoader(getClass().getResource("waitOrLeaveUI.fxml"));
            waitLoader.setController(this);
            Parent waitRoot = waitLoader.load();
            this.waitOrLeaveStage = new Stage();
            waitOrLeaveStage.initModality(Modality.APPLICATION_MODAL);
            waitOrLeaveStage.setTitle("Connecting...");
            waitOrLeaveStage.setScene(new Scene(waitRoot));
            waitButton.setOnMouseClicked(this::waitButtonClicked);
            leaveButton.setOnMouseClicked(this::leaveButtonClicked);
            waitOrLeaveStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitButtonClicked(Event event) {
        waitOrLeaveStage.close();
        gameStart();
    }

    private void leaveButtonClicked(Event event) {
        waitOrLeaveStage.close();
        quit();
    }

    private void gameStart() {
        animationTimer.start();
    }

    private void loadImages() throws IOException {
        for (int i = 0; i < symbols.length; i++)
            images[i] = new Image(Files.newInputStream(Paths.get(imagePath + symbols[i] + ".png")));

        // set background if selected different from default
        if (!bgName.equals("bg")) {
            System.out.println("BG SELECTED!!!");
            images[0] = new Image(Files.newInputStream(Paths.get(bgImagePath + bgName + ".png")));
        }
    }

    private void initCanvas() {
        canvas.setOnKeyPressed(event -> {
            try {
                gameEngine.moveMerge(event.getCode().toString());
            } catch (IOException e) {
                e.printStackTrace(); // debugging
                System.exit(-1); // give a dialogue box about network problem rather than just exit
            }
            updatePlayerStats();
        });

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                onWidthChangedWindow(stage.getWidth());
                onHeightChangedWindow(stage.getHeight());
                render();
                if (gameEngine.isGameOver()) {
                    System.out.println("Game Over!");
                    animationTimer.stop();
                    Platform.runLater(() -> {
                        try {
                            new ScoreboardWindow(gameEngine.getScoreBoard());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });

                }
            }
        };
        canvas.requestFocus();
    }

    private void updatePlayerStats() {
        playerStats.getChildren().clear(); // Clear existing labels
        int numberOfPlayers = gameEngine.getNumOfPlayers();

        HBox playerHBox = new HBox(10); // Create an HBox to contain player VBoxes
        for (int i = 0; i < numberOfPlayers; i++) {
            Label nameLabel = new Label(gameEngine.playerList.get(i).getName());
            nameLabel.setFont(Font.font("Impact", 30));

            Label scoreLabel = new Label("Score: " + gameEngine.playerList.get(i).getScore());
            Label levelLabel = new Label("Level: " + gameEngine.playerList.get(i).getLevel());
            Label comboLabel = new Label("Combo: " + gameEngine.playerList.get(i).getCombo());
            Label moveCountLabel = new Label("# of Moves: " + gameEngine.getMoveCount());

            VBox playerVBox = new VBox(5); // Create a VBox for each player
            playerVBox.getChildren().addAll(nameLabel, scoreLabel, levelLabel, comboLabel, moveCountLabel);

            playerHBox.getChildren().add(playerVBox); // Add player's VBox to the HBox
        }
        playerStats.getChildren().add(playerHBox); // Add the HBox to the main VBox

        String name = gameEngine.getTurnName(); // Show if game is not started yet; if yes, display whose turn it is now
        if (name == null) {
            turnLabel.setText("Waiting for game to start...");
        } else {
            turnLabel.setText(String.format("Player %s's turn!", name));
        }

        if (!gameEngine.isInGame()) {
            int pos = gameEngine.getPosInQueue();
            waiting.setVisible(true);
            queueLabel.setText("Your position in the queue: " + pos);
            queueLabel.setVisible(true);
        } else {
            waiting.setVisible(false);
            queueLabel.setVisible(false);
            leave.setVisible(false);
        }
    }

    private void render() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        double sceneSize = Math.min(w, h);
        double blockSize = sceneSize / GameEngine.SIZE;
        double padding = blockSize * .05;
        double startX = (w - sceneSize) / 2;
        double startY = (h - sceneSize) / 2;
        double cardSize = blockSize - (padding * 2);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        double y = startY;
        int v;

        // Draw the background and cards from left to right, and top to bottom.
        for (int i = 0; i < GameEngine.SIZE; i++) {
            double x = startX;
            for (int j = 0; j < GameEngine.SIZE; j++) {
                gc.drawImage(images[0], x, y, blockSize, blockSize);  // Draw the background

                v = gameEngine.getValue(i, j);

                if (v > 0)  // if a card is in the place, draw it
                    gc.drawImage(images[v], x + padding, y + padding, cardSize, cardSize);

                x += blockSize;
            }
            y += blockSize;
        }
        updatePlayerStats();
    }

    void onWidthChangedWindow(double w) {
        double width = w - boardPane.getBoundsInParent().getMinX();
        boardPane.setMinWidth(width);
        canvas.setWidth(width);
        render();
    }

    void onHeightChangedWindow(double h) {
        double height = h - boardPane.getBoundsInParent().getMinY() - menuBar.getHeight();
        boardPane.setMinHeight(height);
        canvas.setHeight(height);
        render();
    }

    void quit() {
        System.out.println("Bye bye");
        stage.close();
        System.exit(0);
    }

    public void setName(String name) throws IOException {
        gameEngine.sendPlayerName(name);
    }

    public void setBgName(String bgName) {
        this.bgName = bgName;
    }
}
