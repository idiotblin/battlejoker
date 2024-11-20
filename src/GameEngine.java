import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class GameEngine {
    Player curPlayer;
    ArrayList<Player> playerList = new ArrayList<>();
    Thread receiverThread; // for receiving data sent from the server

    Socket clientSocket;
    DataInputStream in;
    DataOutputStream out;

//    public static final int LIMIT = 14;
    public static final int SIZE = 4;
    final int[] board = new int[SIZE * SIZE];
//    Random random = new Random(0);

    private static GameEngine instance;
//    private boolean gameOver;

    private String playerName;
    private int level = 1;
    private int score;
    private int combo;
    private int totalMoveCount;
    private int numOfTilesMoved;
//    private ArrayList<Integer> scores;
//    private ArrayList<Integer> combos;
//    private ArrayList<String> playerNames;

//    private final Map<String, Runnable> actionMap = new HashMap<>();

    private GameEngine() throws IOException { // will be passed ip and Port
        clientSocket = new Socket("127.0.0.1", 12345); // connect using Ip and Port
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());

        curPlayer = new Player(InetAddress.getLocalHost().getHostAddress());
        // if playerList.size() < 4: ... else do not allow
        playerList.add(curPlayer);

        receiverThread = new Thread(()->{
            try {
                while (true) {
                    char data = (char) in.read();
                    System.out.println(data);

                    switch(data) {
                        case 'A': // server sent an array, can add more stuff here like a score or username so can catch them here
                            receiveArray(in);
                            break;
                        case 'S':
                            receivePlayerStats(in);
                            break;
                        default:
                            System.out.println(data);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        receiverThread.start();


        // define a hash map to contain the links from the actions to the corresponding methods
//        actionMap.put("UP", this::moveUp);
//        actionMap.put("DOWN", this::moveDown);
//        actionMap.put("LEFT", this::moveLeft);
//        actionMap.put("RIGHT", this::moveRight);

        // start the first round
//        nextRound();
    }

    private void receivePlayerStats(DataInputStream in) throws IOException {
        int numOfPlayers = in.readInt(); // check players, if exists update, if absent add
        for (int i = 0; i < numOfPlayers; i++) {

            int ipLength = in.readInt();
            byte[] ipBytes = new byte[ipLength];
            in.read(ipBytes, 0, ipLength);
            String ipAddress = new String(ipBytes);

            updatePlayerList(in, ipAddress);
        }
    }

    private void updatePlayerList(DataInputStream in, String ipAddress) throws IOException {
        Player player = new Player(ipAddress);

        // name
        int nameLength = in.readInt();
        byte[] nameBytes = new byte[nameLength];
        in.read(nameBytes, 0, nameLength);
        player.setName(new String(nameBytes));

        int tempLevel = in.readInt();
        player.setLevel(tempLevel);

        int tempScore = in.readInt();
        player.setScore(tempScore);

        int tempCombo = in.readInt();
        player.setCombo(tempCombo);

        int tempTotalMoveCount = in.readInt();
        player.setTotalMoveCount(tempTotalMoveCount);
        if (playerList.isEmpty() || (!playerListContains(ipAddress))) {
            playerList.add(player);
        } else {
            playerList.get(getPlayerIndex(ipAddress)).setTotalMoveCount(tempTotalMoveCount);
        }
    }

    private int getPlayerIndex(String ipAddress) {
        int index = -1;
        for (int i = 0; i < playerList.size(); i++) {
            if (playerList.get(i).equals(ipAddress))
                index = i;
        }
        return index;
    }

    private boolean playerListContains(String ipAddress) {
        for (Player p : playerList)
            if (p.getIpAddress().equals(ipAddress))
                return true;

        return false;
    }

    public void receiveArray(DataInputStream in) throws IOException {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            board[i] = in.readInt();
        }
    }

    public static GameEngine getInstance() { // will be passed Ip and Port
        if (instance == null) {
            try {
                instance = new GameEngine(); // pass Ip and port
            } catch (IOException e) {
                e.printStackTrace(); //remove from final program, just exit
                System.exit(-1);
            }
        }
        return instance;
    }

    /**
     * Generate a new random value and determine the game status.
     * @return true if the next round can be started, otherwise false.
     */
//    private boolean nextRound() {
//        if (isFull()) return false;
//        int i;
//
//        // randomly find an empty place
//        do {
//            i = random.nextInt(SIZE * SIZE);
//        } while (board[i] > 0);
//
//        // randomly generate a card based on the existing level, and assign it to the select place
//        board[i] = random.nextInt(level) / 4 + 1;
//        return true;
//    }

    /**
     * @return true if all blocks are occupied.
     */
//    private boolean isFull() {
//        for (int v : board)
//            if (v == 0) return false;
//        return true;
//    }
    public void sendPlayerName(String name) throws IOException {
        out.write('N');
        out.write(name.length());
        out.write(name.getBytes());
    }

    /**
     * Move and combine the cards based on the input direction
     * @param dir
     */
    public void moveMerge(String dir) throws IOException {
        System.out.println(dir);
        out.write('D');
        out.write(dir.charAt(0));
        out.flush();

//        synchronized (board) { // this should be on the server side --> to give newly joined players the current map
//            if (actionMap.containsKey(dir)) {
//                combo = numOfTilesMoved = 0;
//
//                // go to the hash map, find the corresponding method and call it
//                actionMap.get(dir).run();
//
//                // calculate the new score
//                score += combo / 5 * 2;
//
//                // determine whether the game is over or not
//                if (numOfTilesMoved > 0) {
//                    totalMoveCount++;
//                    gameOver = level == LIMIT || !nextRound();
//                } else
//                    gameOver = isFull();
//
//                // update the database if the game is over
//                if (gameOver) {
//                    try {
//                        Database.putScore(playerName, score, level);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        }
    }

    /**
     * move the values downward and merge them.
     */
//    private void moveDown() {
//        for (int i = 0; i < SIZE; i++)
//            moveMerge(SIZE, SIZE * (SIZE - 1) + i, i);
//    }
//
//    /**
//     * move the values upward and merge them.
//     */
//    private void moveUp() {
//        for (int i = 0; i < SIZE; i++)
//            moveMerge(-SIZE, i, SIZE * (SIZE - 1) + i);
//    }
//
//    /**
//     * move the values rightward and merge them.
//     */
//    private void moveRight() {
//        for (int i = 0; i <= SIZE * (SIZE - 1); i += SIZE)
//            moveMerge(1, SIZE - 1 + i, i);
//    }
//
//    /**
//     * move the values leftward and merge them.
//     */
//    private void moveLeft() {
//        for (int i = 0; i <= SIZE * (SIZE - 1); i += SIZE)
//            moveMerge(-1, i, SIZE - 1 + i);
//    }

//    /**
//     * Move and merge the values in a specific row or column. The moving direction and the specific row or column is determined by d, s, and l.
//     * @param d - move distance
//     * @param s - the index of the first element in the row or column
//     * @param l - the index of the last element in the row or column.
//     */
//    private void moveMerge(int d, int s, int l) {
//        int v, j;
//        for (int i = s - d; i != l - d; i -= d) {
//            j = i;
//            if (board[j] <= 0) continue;
//            v = board[j];
//            board[j] = 0;
//            while (j + d != s && board[j + d] == 0)
//                j += d;
//
//            if (board[j + d] == 0) {
//                j += d;
//                board[j] = v;
//            } else {
//                while (j != s && board[j + d] == v) {
//                    j += d;
//                    board[j] = 0;
//                    v++;
//                    score++;
//                    combo++;
//                }
//                board[j] = v;
//                if (v > level) level = v;
//            }
//            if (i != j)
//                numOfTilesMoved++;
//
//        }
//    }

    public int getValue(int r, int c) {
        synchronized (board) {
            return board[r * SIZE + c];
        }
    }

//    public boolean isGameOver() {
//        return gameOver;
//    }
//
//    public ArrayList<String> getPlayerNames() {
//        return playerNames;
//    }
//
//    public ArrayList<Integer> getScores() {
//        return scores;
//    }
//
//    public ArrayList<Integer> getCombos() {
//        return combos;
//    }

    public int getLevel() {
        return level;
    }

    public int getMoveCount() {
        return totalMoveCount;
    }

    public int getNumOfPlayers(){
        return playerList.size();
    }
//
//    public void addPlayerName(String name) {
//        playerNames.add(name);
//    }

    public void setCurPlayerName(String name) {
        curPlayer.setName(name);
    }
}
