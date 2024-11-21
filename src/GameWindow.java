import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class GameWindow {
    @FXML
    MenuBar menuBar;

    @FXML
    Pane boardPane;

    @FXML
    Canvas canvas;

    @FXML
    VBox playerStats;

    Stage stage;
    AnimationTimer animationTimer;

    final String imagePath = "images/";
    final String[] symbols = {"bg", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "Joker"};
    final Image[] images = new Image[symbols.length];
    static GameEngine gameEngine;

    public GameWindow(Stage stage, String ip, String port) throws IOException {
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

        stage.show();
        initCanvas();

        gameStart();
    }

    private void gameStart() {
        animationTimer.start();
    }

    private void loadImages() throws IOException {
        for (int i = 0; i < symbols.length; i++)
            images[i] = new Image(Files.newInputStream(Paths.get(imagePath + symbols[i] + ".png")));
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
            Label scoreLabel = new Label("Score: " + gameEngine.playerList.get(i).getScore());
            Label levelLabel = new Label("Level: " + gameEngine.playerList.get(i).getLevel());
            Label comboLabel = new Label("Combo: " + gameEngine.playerList.get(i).getCombo());
            Label moveCountLabel = new Label("# of Moves: " + gameEngine.getMoveCount());

            VBox playerVBox = new VBox(5); // Create a VBox for each player
            playerVBox.getChildren().addAll(nameLabel, scoreLabel, levelLabel, comboLabel, moveCountLabel);

            playerHBox.getChildren().add(playerVBox); // Add player's VBox to the HBox
        }

        playerStats.getChildren().add(playerHBox); // Add the HBox to the main VBox
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
        // send ip address and port to gameEngine
    }

//    public void setIp(String ip) throws IOException {
//        gameEngine.setIp(ip);
//        // send ip address and port to gameEngine
//    }
//
//    public void setPort(String port) throws IOException {
//        gameEngine.setPort(port);
//        // send ip address and port to gameEngine
//    }
}
