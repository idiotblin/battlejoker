# BattleJoker

This is a multiplayer parody on the classic game **2048**. The game supports a single lobby with up to 4 concurrent players, who compete to achieve the highest score. After each game, results are stored, and players can see the results of their game, as well as the top 10 historical scores.

## Join the Game

- Ensure the server (file JokerServer.java) is running
- Run the client program (file BattleJoker.java)
- Input your username, IP address of the server and port

## Gameplay Instructions:

- Use arrow keys to move the cards on the board, similar to the original 2048 game.
- Each player has 4 moves per turn.
- The game ends when there are no legal moves left.

## Lobby and Queue System:

- When the first player joins, they can wait for other players to join or start the game whenever they want.
- When the 4th player joins, the game starts automatically.
- Players who join while a game is in progress are added to a queue and will join the next game as soon as the current one ends.

## Features

- **Background Selector**

  Choose from 3 different backgrounsd.
  
- **Spectator Mode**

  If a 5th player join when the lobby is live, they can choose to wait and spectate the live game or leave.
  
- **Upload and Download Puzzle**

  PLayers can save the current state of the board (puzzle) and upload custom puzzle before starting a new game 

- **Multicast Scoreboard**

  An individual program, to be run on client side. Shows the latest updated scores of the top 10 historical games.
  
## Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/idiotblin/battlejoker.git
    ```
2. Navigate to the project directory:
    ```bash
    cd battleJoker
    ```

## For Multicast Scoreboard

Separate the file to a different directory and run it while the JokerServer program is active.
