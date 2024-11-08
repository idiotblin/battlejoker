import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class JokerServer {
    ArrayList<Player> playerList = new ArrayList<>();
    ArrayList<Socket> gameList = new ArrayList<>();
    TreeMap<Socket, ArrayList<InetAddress>> gamePlayers;
    public static final int SIZE = 4;
    final int[] board = new int[SIZE * SIZE];
    private int combo;
    private final Map<String, Runnable> actionMap = new HashMap<>();
    private int numOfTilesMoved;
    private int level = 1;
    private int score;
    private boolean gameOver;
    private int totalMoveCount;
    public static final int LIMIT = 14;
    private final int MAX_MOVE = 4;
    private String playerName;

    Random random = new Random(0);

    public JokerServer(int port) {
        actionMap.put("U", this::moveUp);
        actionMap.put("D", this::moveDown);
        actionMap.put("L", this::moveLeft);
        actionMap.put("R", this::moveRight);

        nextRound();

        try {
            ServerSocket srvSocket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = srvSocket.accept();
//                if (!gameList.contains(clientSocket)) {   not clientSocket, but the ip that Client sends from the form
//                    gameList.add(clientSocket);
//                    gamePlayers.put(clientSocket, new ArrayList<>());
//                }
//                gamePlayers.get(clientSocket).add(clientSocket.getInetAddress());

                Player player = new Player(clientSocket); // initiate a player instance, get the names after
                synchronized (playerList) {
                    playerList.add(player);
                    if (playerList.size() == 4) {
                        // start the game
                    }
                }


                Thread t = new Thread(() -> {
                    try {
                        serve(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace(); // debugging
                        System.out.println("The client is disconnected! "
                                + clientSocket.getInetAddress().toString());
                        synchronized (playerList) {
                            playerList.remove(clientSocket);
                        }
                    }
                });
                t.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void serve(Socket clientSocket) throws IOException {
        System.out.println("New connection:");
        System.out.println(clientSocket.getInetAddress());
        System.out.println(clientSocket.getLocalPort());


        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream _out = new DataOutputStream(clientSocket.getOutputStream());
        sendPuzzle(_out);
        DataInputStream[] dis = new DataInputStream[gameList.size()];
        while (true) {
            /*
             for loop to iterate over dis (nuts)

             */
            String playerName;
            char dir = '0';

            char charToken = (char) in.read(); // reads one byte or char

            switch (charToken) {
                case 'N':
                    playerName = String.valueOf(in.read());
                    break;
                case 'D':
                    dir = (char) in.read();
                default:
                    System.out.println(charToken);
            }

            synchronized (playerList) {
                moveMerge("" + dir);

                for (int i : board) {
                    System.out.print(i + " ");
                }

//              gameOver = !nextRound();

                for (Player p : playerList) {
                    DataOutputStream out = new DataOutputStream(p.getSocket().getOutputStream());
                    out.write(dir);
                    out.flush();

                    sendPuzzle(out);
                }
            }
        }
    }

    public void sendPuzzle(DataOutputStream out) throws IOException {
        out.write('A'); // want to send an array to client (Application level protocol)
        out.writeInt(board.length); // array size
        for (int i : board) {
            out.writeInt(i); // send values of the array
        }
        out.flush(); // force java to send out
    }   // need to send player name, score,

    public void moveMerge(String dir) {
        synchronized (board) { // this should be on the server side --> to give newly joined players the current map
            if (actionMap.containsKey(dir)) {
                combo = numOfTilesMoved = 0;

                // go to the hash map, find the corresponding method and call it
                actionMap.get(dir).run();

                // calculate the new score
                score += combo / 5 * 2;

                // determine whether the game is over or not
                if (numOfTilesMoved > 0) {
                    totalMoveCount++;
                    gameOver = level == LIMIT || !nextRound();
                } else
                    gameOver = isFull();


                if (gameOver) {
                    try {
                        Database.putScore(playerName, score, level);
                        for (Player p : playerList) {
                            DataOutputStream _out = new DataOutputStream(p.getSocket().getOutputStream());
                            _out.write('S');
                            Database.getScores().forEach(data -> {
                                String scoreStr = String.format("%s (%s)", data.get("score"), data.get("level"));
                                try {
                                    _out.write(String.format("%10s | %10s | %s", data.get("name"), scoreStr, data.get("time").substring(0, 16)).getBytes());
                                } catch (IOException e) {
                                    System.out.println("MISTAKE WHEN SENDING SCORES, player name: " + playerName);
                                    System.out.println(e.getMessage());
                                }
                            });
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean nextRound() {
        if (isFull()) return false;
        int i;

        // randomly find an empty place
        do {
            i = random.nextInt(SIZE * SIZE);
        } while (board[i] > 0);

        // randomly generate a card based on the existing level, and assign it to the select place
        board[i] = random.nextInt(level) / 4 + 1;
        return true;
    }

    private boolean isFull() {
        for (int v : board)
            if (v == 0) return false;
        return true;
    }

    private void moveDown() {
        for (int i = 0; i < SIZE; i++)
            moveMerge(SIZE, SIZE * (SIZE - 1) + i, i);
    }

    /**
     * move the values upward and merge them.
     */
    private void moveUp() {
        for (int i = 0; i < SIZE; i++)
            moveMerge(-SIZE, i, SIZE * (SIZE - 1) + i);
    }

    /**
     * move the values rightward and merge them.
     */
    private void moveRight() {
        for (int i = 0; i <= SIZE * (SIZE - 1); i += SIZE)
            moveMerge(1, SIZE - 1 + i, i);
    }

    /**
     * move the values leftward and merge them.
     */
    private void moveLeft() {
        for (int i = 0; i <= SIZE * (SIZE - 1); i += SIZE)
            moveMerge(-1, i, SIZE - 1 + i);
    }

    private void moveMerge(int d, int s, int l) {
        int v, j;
        for (int i = s - d; i != l - d; i -= d) {
            j = i;
            if (board[j] <= 0) continue;
            v = board[j];
            board[j] = 0;
            while (j + d != s && board[j + d] == 0)
                j += d;

            if (board[j + d] == 0) {
                j += d;
                board[j] = v;
            } else {
                while (j != s && board[j + d] == v) {
                    j += d;
                    board[j] = 0;
                    v++;
                    score++;
                    combo++;
                }
                board[j] = v;
                if (v > level) level = v;
            }
            if (i != j)
                numOfTilesMoved++;

        }
    }

    public int getValue(int r, int c) {
        synchronized (board) {
            return board[r * SIZE + c];
        }

    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setPlayerName(String name) {
        playerName = name;
    }

    public int getScore() {
        return score;
    }

    public int getCombo() {
        return combo;
    }

    public int getLevel() {
        return level;
    }

    public int getMoveCount() {
        return totalMoveCount;
    }

    public static void main(String[] args) throws IOException {
        new JokerServer(12345);
    }
}
