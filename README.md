# BattleJoker

BattleJoker game made multiplayer. Supports a single lobby of 4 concurrent players.

## Extra Features

- **Background Selector**

  Choose from 3 different backgrounsd.
  
- **Spectator Mode**
    If a 5th player join when the lobby is live, they can choose to wait and spectate the live game or leave.
  
- **Upload and Download Puzzle**
    PLayers can save the current state of the puzzle and start a new game with that puzzle

  - **Multicast Scoreboard**
    An individual program, to be run on client side. Shows the latest updated scores of the top 10 historical games.
  
## Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/aswaddd/battleJoker.git
    ```
2. Navigate to the project directory:
    ```bash
    cd battleJoker
    ```

## Usage

Start the game by running:

JokerServer & BattleJoker classes.

## For Multicast Scoreboard

Separate the file to a different directory and run it while the JokerServer program is active.
