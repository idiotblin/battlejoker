import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.sql.SQLException;
import java.util.*;

public class JokerServer {
    ArrayList<Player> playerList = new ArrayList<>();
    ArrayList<Socket> clientList = new ArrayList<>();
    ArrayList<Boolean> connected = new ArrayList<>();
    public static final int SIZE = 4;
    final int[] board = new int[SIZE * SIZE];
    private final Map<String, Runnable> actionMap = new HashMap<>();
    private int numOfTilesMoved;
    private int level = 1;
    private int combo;
    private int score;
    private int lobbySize = 0;
    private int gameTurn = 0;
    private int curMoveCount = 0;
    private boolean gameStarted = false;
    private boolean gameOver;
    private int totalMoveCount;
    public static final int LIMIT = 14;
    private final int MAX_MOVE = 4;
    private String MULTICAST_ADDRESS = "224.0.7.7";
    private int MULTICAST_PORT = 39993;
    private DatagramSocket multicastSocket;
    Random random = new Random(0);

    public JokerServer(int port) {
        actionMap.put("U", this::moveUp);
        actionMap.put("D", this::moveDown);
        actionMap.put("L", this::moveLeft);
        actionMap.put("R", this::moveRight);
        nextRound();

        try {
            Database.connect();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Thread tMulticast = new Thread(() -> { // Send scoreboard with UDP
            while (true) {
                System.out.println("Sending Scores to Multicast Address...");
                sendMulticast();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        tMulticast.start();

        try {
            ServerSocket srvSocket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = srvSocket.accept();

                if (!clientList.contains(clientSocket)) {
                    synchronized (clientList) {
                        clientList.add(clientSocket);
                    }
                    synchronized (playerList) {
                        playerList.add(new Player(clientSocket.getInetAddress().toString())); // initiate player
                        if (!gameStarted) {
                            lobbySize++;
                            connected.add(true);
                            sendInGame(clientSocket, lobbySize < 4);
                        } else {
                            sendInGame(clientSocket, false);
                        }
                        if (lobbySize == 4) { // start the game automatically
                            gameStarted = true;
                            gameTurn = 0;
                        }
                    }
                } else {
                    synchronized (connected) {
                        connected.set(getCurrentPlayerIndex(clientSocket), true);
                    }
                    sendInGame(clientSocket, true);
                }

                Thread t = new Thread(() -> {
                    try {
                        serve(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace(); // debugging
                        System.out.println("The client is disconnected! "
                                + clientSocket.getInetAddress().toString());

                        // Delete disconnected player from the lists
                        int ind = getCurrentPlayerIndex(clientSocket);
                        if (!gameStarted || ind >= lobbySize) { // If game isn't started or player is in waiting list, just delete
                            if (ind < connected.size()) {
                                synchronized (connected) {
                                    connected.remove(ind);
                                    lobbySize--;
                                }
                            }
                            synchronized (clientList) {
                                clientList.remove(clientSocket);
                            }
                            synchronized (playerList) {
                                playerList.remove(getCurrentPlayer(clientSocket));
                            }
                        } else { // if one of the current game's player is disconnected, mark as disconnected
                            synchronized (connected) {
                                connected.set(ind, false);
                            }
                        }
                        for (int i = 0; i < clientList.size(); i++) { // update users whether they are in game or not
                            try {
                                sendInGame(clientList.get(i), i < lobbySize);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
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

        char nameToken = (char) in.read();
        if (nameToken == 'N') {
            int nameLength = in.readInt();
            byte[] nameBytes = new byte[nameLength];
            in.read(nameBytes, 0, nameLength);
            playerList.get(getCurrentPlayerIndex(clientSocket)).setName(new String(nameBytes));
        }
        System.out.println("User: " + playerList.get(getCurrentPlayerIndex(clientSocket)).getName() + " connected!");

        for (Socket s : clientList) {
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            sendPuzzle(out);
            sendPlayerStats(out);
        }

        while (true) {
            System.out.println("Lobbysize: " + lobbySize);
            System.out.println("turn: " + gameTurn);
            if (gameOver) {
                try {
                    sendGameOver();
                    gameStarted = false;
                    gameTurn = -1;
                    curMoveCount = 0;
                    gameOver = false;
                    if (lobbySize > 0) {
                        clientList.subList(0, lobbySize).clear();
                        playerList.subList(0, lobbySize).clear();
                    }
                    lobbySize = Math.min(4, clientList.size());
                    if (lobbySize == 4) {
                        gameStarted = true;
                        gameTurn = 0;
                    }
                    for (int i = 0; i < clientList.size(); i++) {
                        sendInGame(clientList.get(i), i < lobbySize);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            char dir = '0';
            char charToken = (char) in.read();

            switch (charToken) {
                case 'D':
                    dir = (char) in.read();
                    break;
                case 'P':
                    receiveUploadedPuzzle(in);
                    break;
                default:
                    System.out.println(charToken);
            }

            synchronized (playerList) {
                int ind = getCurrentPlayerIndex(clientSocket);
                if ((!gameStarted && ind > 0) || (gameStarted && ind != gameTurn)) {
                    continue;
                }
                if (!gameStarted && ind == 0) {
                    gameStarted = true;
                }
                moveMerge(playerList.get(ind), "" + dir);
                curMoveCount++;

                if (curMoveCount == MAX_MOVE) {
                    curMoveCount = 0;
                    gameTurn = (gameTurn + 1) % lobbySize;
                }
                System.out.print("Current board: ");
                for (int i : board) {
                    System.out.print(i + " ");
                }
                System.out.println();
//              gameOver = !nextRound();

                for (Socket s : clientList) {
                    DataOutputStream out = new DataOutputStream(s.getOutputStream());
                    out.write(dir);
                    out.flush();

                    sendPuzzle(out);
                    sendPlayerStats(out);
                    sendTurn(out);
                }
            }
        }
    }

    private Player getCurrentPlayer(Socket clientSocket) {
        Player target = new Player("");
        for (Player p : playerList) {
            if (p.getIpAddress().equals(clientSocket.getInetAddress().toString())) {
                target = p;
            }
        }
        return target;
    }

    private int getCurrentPlayerIndex(Socket clientSocket) {
        int index = 0;
        for (int i = 0; i < playerList.size(); i++) {
            if (playerList.get(i).getIpAddress().equals(clientSocket.getInetAddress().toString())) {
                index = i;
            }
        }
        return index;
    }

    public void sendPlayerStats(DataOutputStream out) throws IOException {
        out.write('S');

        int numOfPlayers = lobbySize;
        out.writeInt(numOfPlayers);

        for (int i = 0; i < lobbySize; i++) {
            if (!connected.get(i))
                continue;
            Player player = playerList.get(i);

            String curPlayerName = player.getName();
            out.writeInt(curPlayerName.length());
            out.write(curPlayerName.getBytes());

            out.writeInt(level);
            out.writeInt(player.getScore());
            out.writeInt(player.getCombo());
            out.writeInt(totalMoveCount);
        }
        out.flush();
    }

    public void sendPuzzle(DataOutputStream out) throws IOException {
        out.write('A'); // want to send an array to client (Application level protocol)
        out.writeInt(board.length); // array size
        for (int i : board) {
            out.writeInt(i); // send values of the array
        }
        out.flush(); // force java to send out
    }   // need to send player name, score,

    public void sendTurn(DataOutputStream out) throws IOException {
        out.write('T');
        if (gameStarted)
            out.writeInt(gameTurn);
        else
            out.writeInt(-1);
        out.flush();
    }

    public void sendInGame(Socket client, boolean inGame) throws IOException {
        DataOutputStream out = new DataOutputStream(client.getOutputStream());
        out.write('I');
        out.writeBoolean(inGame);
        if (!inGame)
            out.write(Math.max(1, getCurrentPlayerIndex(client) - lobbySize + 1));
        out.flush();
    }

    public void receiveUploadedPuzzle(DataInputStream in) throws IOException {
        System.out.println("receiveUploadedPuzzle---");
        int len = in.readInt();
        byte[] puzzleBytes = new byte[len];
        in.read(puzzleBytes, 0, len);
        String[] puzzle = (new String(puzzleBytes)).split(" ");
        if (gameStarted)
            return;
        for (int i = 0; i < SIZE * SIZE; i++) {
            board[i] = Integer.parseInt(puzzle[i]);
        }
    }

    private ArrayList<HashMap<String, String>> winner() throws SQLException {
        int max = -1;
        Player ans = null;
        for (int i = 0; i < lobbySize; ++i) {
            if (playerList.get(i).getScore() > max) {
                max = playerList.get(i).getScore();
                ans = playerList.get(i);
            }
        }
        Database.putScore(ans.getName(), ans.getScore(), ans.getLevel());
        return Database.getPlayer(ans.getName(), ans.getScore(), ans.getLevel());
    }

    public void sendGameOver() throws IOException, SQLException {
        ArrayList<HashMap<String, String>> scores = winner();
        scores.addAll(Database.getScores());
        for (Socket client : clientList) {
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            out.write('G');
            out.writeInt(scores.size());
            scores.forEach(data -> {
                String scoreStr = String.format("%s (%s)", data.get("score"), data.get("level"));
                try {
                    String sendString = String.format("%10s | %10s | %s", data.get("name"), scoreStr, data.get("time").substring(0, 16));
                    out.writeInt(sendString.length());
                    out.write(sendString.getBytes());
                } catch (IOException e) {
                    System.out.println("MISTAKE WHEN SENDING SCORES, player name: " + playerList.get(getCurrentPlayerIndex(client)).getName());
                    System.out.println(e.getMessage());
                }
            });
        }
    }

    public void sendMulticast() {
        ArrayList<HashMap<String, String>> scores = new ArrayList<>();

        try {
            scores.addAll(Database.getScores());
        } catch (SQLException ignored) {
        }

        StringBuilder tempResult = new StringBuilder();
        scores.forEach(data -> {
            String scoreStr = String.format("%s (%s)", data.get("score"), data.get("level"));
            String sendString = String.format("%10s | %10s | %s", data.get("name"), scoreStr, data.get("time").substring(0, 16));
            tempResult.append(sendString).append("\n"); // Add a newline as a delimiter
        });
        String finalResult = tempResult.toString();

        try {
            multicastSocket = new DatagramSocket(0);
            sendMsg(finalResult, MULTICAST_ADDRESS, MULTICAST_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String str, String destIP, int port) throws IOException {
        InetAddress destination = InetAddress.getByName(destIP);
        DatagramPacket packet =
                new DatagramPacket(str.getBytes(), str.length(), destination, port);
        multicastSocket.send(packet);
    }

    public void moveMerge(Player curPlayer, String dir) {
        synchronized (board) { //to give newly joined players the current map
            if (actionMap.containsKey(dir)) {
                combo = numOfTilesMoved = 0;
                score = curPlayer.getScore();
                // go to the hash map, find the corresponding method and call it
                actionMap.get(dir).run();

                // calculate the new score
                curPlayer.setCombo(combo);
                curPlayer.setScore(score + combo / 5 * 2);

                // determine whether the game is over or not
                if (numOfTilesMoved > 0) {
                    totalMoveCount++;
                    gameOver = level == LIMIT || !nextRound();
                } else
                    gameOver = isFull();
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

    public boolean isGameOver() {
        return gameOver;
    } // get status of game with this :id

    public static void main(String[] args) throws IOException {
        new JokerServer(12345);
    }
}
