import java.net.Socket;

public class Player {
    private String name;
    private int level = 1;
    private int score;
    private int combo;
    private Socket socket;

    public Player(String name, Socket socket) {
        this.name = name;
        this.socket = socket;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCombo() {
        return combo;
    }

    public void setCombo(int combo) {
        this.combo = combo;
    }

    public Socket getSocket() {
        return socket;
    }
}
