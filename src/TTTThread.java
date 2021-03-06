/*
  Created by Corey Shrader & Chris Groppe on 4/5/17.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class TTTThread implements Runnable {

    private Socket client;
    static private final char[][] board = {{' ', ' ', ' '},
            {' ', ' ', ' '},
            {' ', ' ', ' '}};
    static private int moveCount = 0;

    public TTTThread(Socket conn) {
        client = conn;
    }

    @Override
    public void run() {

        try {
            while (true) {

                boolean play = true;
                clearBoard();
                moveCount = 0;

                //AT THIS POINT CONNECTION MADE
                System.out.println("RECEIVED REQUEST");

                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintStream out = new PrintStream(client.getOutputStream());

                int r = ThreadLocalRandom.current().nextInt(1, 3); //gen number between 1 and 2

                if (r == 1) { // client goes
                    out.println("NONE");
                } else { // server goes
                    int[] move = move();
                    out.println("MOVE " + move[0] + " " + move[1]);
                }

                while (play) {
                    String response = in.readLine();
                    String[] words = response.split(" ");
                    update(Integer.parseInt(words[1]), Integer.parseInt(words[2]), 'O');

                    if (checkWin()) {
                        out.println("MOVE 0 0 WIN");
                        play = false;
                        continue;
                    } else if (checkTie()) {
                        out.println("MOVE 0 0 TIE");
                        play = false;
                        continue;
                    }

                    int[] move = move();
                    if (checkWin()) {
                        out.println("MOVE " + move[0] + " " + move[1] + " LOSS");
                        play = false;
                        continue;
                    } else if (checkTie()) {
                        out.println("MOVE " + move[0] + " " + move[1] + " TIE");
                        play = false;
                        continue;
                    }
                    out.println("MOVE " + move[0] + " " + move[1]);
                }
                client.close();
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void clearBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static boolean checkWin() {
        return (board[0][0] == board[0][1] && board[0][1] == board[0][2] && board[0][2] != ' ')
                || (board[1][0] == board[1][1] && board[1][1] == board[1][2] && board[1][2] != ' ')
                || (board[2][0] == board[2][1] && board[2][1] == board[2][2] && board[2][2] != ' ')
                || (board[0][0] == board[1][0] && board[1][0] == board[2][0] && board[2][0] != ' ')
                || (board[0][1] == board[1][1] && board[1][1] == board[2][1] && board[2][1] != ' ')
                || (board[0][2] == board[1][2] && board[1][2] == board[2][2] && board[2][2] != ' ')
                || (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[2][2] != ' ')
                || (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[2][0] != ' ');
    }

    private static boolean checkTie() {
        return moveCount >= 9;
    }

    private static int[] move() {
        ArrayList<Integer[]> legalMoves = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    Integer[] coords = {i, j};
                    legalMoves.add(coords);
                }
            }
        }
        int r = ThreadLocalRandom.current().nextInt(0, legalMoves.size()); //gen number between 0 and array size

        int[] move = {legalMoves.get(r)[0], legalMoves.get(r)[1]};

        update(move[0], move[1], 'X');
        return move;
    }

    private static void update(int r, int c, char m){// local board update with chosen move
        //move is always row-column
        //assumes that board has been checked for open spot
        board[r][c] = m;
        moveCount++;
    }
}
