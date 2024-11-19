import java.net.Socket;

public class Player {
    private String name;
    private int level = 1;
    private int score;
    private int combo;
    private Socket socket;
    private int totalMoveCount;

    public Player(Socket socket) {
        this.socket = socket;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean nameEmpty() {
        return this.name.isEmpty();
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

    public int getTotalMoveCount() {
        return totalMoveCount;
    }

    public void setTotalMoveCount(int totalMoveCount) {
        this.totalMoveCount = totalMoveCount;
    }
}
