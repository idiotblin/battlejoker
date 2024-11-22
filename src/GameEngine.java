import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class GameEngine {
    private static GameEngine instance;

    ArrayList<Player> playerList = new ArrayList<>();
    Thread receiverThread; // for receiving data sent from the server

    Socket clientSocket;
    DataInputStream in;
    DataOutputStream out;

    public static final int SIZE = 4;
    final int[] board = new int[SIZE * SIZE];
    private int totalMoveCount;
    private boolean gameOver = false;
    private ArrayList<String> scoreBoard = new ArrayList<>();

    private int turn = -1;

    private GameEngine(String ip, String port) throws IOException { // will be passed ip and Port
        clientSocket = new Socket(ip, Integer.parseInt(port)); // connect using Ip and Port
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());

//        Player curPlayer = new Player(InetAddress.getLocalHost().getHostAddress());
//        // if playerList.size() < 4: ... else do not allow
//        playerList.add(curPlayer);

        receiverThread = new Thread(()->{
            try {
                while (true) {
                    char data = (char) in.read();
                    System.out.println(data);

                    switch(data) {
                        case 'A':
                            receiveArray(in);
                            break;
                        case 'S':
                            receivePlayerStats(in);
                            break;
                        case 'G':
                            gameOver = true;
                            receiveScoreBoard(in);
                            break;
                        case 'T':
                            receiveTurn(in); // you didn't add cause its cooked?
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

    }

    private void receiveScoreBoard(DataInputStream in) throws IOException {
        int numOfScores = in.readInt();
        for (int i = 0; i < numOfScores; i++) {
            int recLength = in.readInt();
            byte[] recBytes = new byte[recLength];
            in.read(recBytes, 0, recLength);
            String record = new String(recBytes);
            scoreBoard.add(record);
        }
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

    private void receiveTurn(DataInputStream in) throws IOException {
        this.turn = in.readInt();
    }

    private void updatePlayerList(DataInputStream in, String ipAddress) throws IOException {
        Player player = new Player(ipAddress);

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

        totalMoveCount = in.readInt();

        if (playerList.isEmpty() || (!playerListContains(ipAddress))) {
            playerList.add(player);
        } else {
            playerList.set(getPlayerIndex(ipAddress), player);
        }
    }
    private int getPlayerIndex(String ipAddress) {
        int index = -1;
        for (int i = 0; i < playerList.size(); i++) {
            if (playerList.get(i).getIpAddress().equals(ipAddress))
                index = i;
        }
        if (index == -1) {
            System.out.println("\n----------INDEX FROM getPlayerIndex() IS -1");
            index++;
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

    public static GameEngine getInstance(String ip, String port) { // will be passed Ip and Port
        if (instance == null) {
            try {
                instance = new GameEngine(ip, port); // pass Ip and port
            } catch (IOException e) {
                e.printStackTrace(); //remove from final program, just exit
                System.exit(-1);
            }
        }
        return instance;
    }

    public void sendPlayerName(String name) throws IOException {
        out.write('N');
        out.writeInt(name.length());
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
    }

    public int getValue(int r, int c) {
        synchronized (board) {
            return board[r * SIZE + c];
        }
    }

    public int getMoveCount() {
        return totalMoveCount;
    }

    public int getNumOfPlayers(){
        return playerList.size();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public ArrayList<String> getScoreBoard() {
        return scoreBoard;
    }

    public String getTurnName() {
        if (turn == -1)
            return null;
        return playerList.get(turn).getName();
    }
}
