package MAIN;

import java.io.*;
import java.util.Scanner;

import BOARD_INFO.Board;
import ENGINE.GameEngine;
import GUI.BoardDisplay;
import MULTIPLAYER.Player;
import ENUM.Color;

public class Main {


    public static void main(String[] args) {
        boolean exit = false;
        Color winner = Color.EMPTY;
        boolean gameDraw = false;
        String currMove = "";
        System.out.print("Local or Online (L/O): ");
        Scanner input = new Scanner(System.in);
        String choice = input.nextLine().toLowerCase();

        while(!choice.equals("o") && !choice.equals("l")){
            System.out.print("Invalid Input (L/O): ");
            choice = input.nextLine().toLowerCase();
        }
        if(choice.equals("o")){
            System.out.println("Server Address(Default - localhost): ");
            String address = input.nextLine();
            GameEngine game = new GameEngine(new Board());
            String lastMove = "";
            Player player = new Player(game);
            player.connectToServer(address);
            BoardDisplay gui = new BoardDisplay(game, true, player.playerID);
            try{
                player.boardString = player.csc.dataIn.readUTF();
            }
            catch (IOException ex){
                System.out.println("IOException in board print.");
            }
            if(player.playerID != 1){
                Thread t = new Thread(player::updateTurn);
                t.start();
            }
            System.out.println(player.boardString);
            currMove = "";
            System.out.print("Enter move: ");
            if(player.playerID == 2 ){
                gui.setTurnToggle(false);
            }
            while(!exit){
                while((game.turn % 2 == 0 && player.playerID == 2) || (game.turn % 2 == 1 && player.playerID == 1) )
                {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                currMove = "";
                gui.setUpdate(currMove);

                gui.setTurnToggle(true);
                while(currMove.equals("")){
                    currMove = gui.getUpdate();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }

                    currMove = "";
                    gui.setUpdate(currMove);

                    gui.setTurnToggle(true);
                    while(currMove.equals("")){
                        currMove = gui.getUpdate();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    System.out.println(currMove);
                    player.csc.sendMove(currMove);
                    Thread t2 = new Thread(() -> {
                        player.updateTurn();
                        player.updateTurn();
                    });
                    t2.start();
                    gui.setTurnToggle(false);
                }
            }
        }
        else if(choice.equals("l")){
            GameEngine game = new GameEngine(new Board());
            new BoardDisplay(game, false, 0);

            game.board.printBoard();
            while(!exit){
                if(winner != Color.EMPTY){
                    System.out.println("Checkmate: " + winner.name() + " wins.\n");
                    break;
                }
                else if (gameDraw){
                    System.out.println("Draw: gameover");
                    break;
                }
                else {
                    System.out.print("Enter move: ");
                    currMove = input.nextLine().toLowerCase();
                    if(currMove.equals("quit")){
                        return;
                    }
                    else if(currMove.equals("restart")){
                        game.restart();
                    }
                    else if(game.tryMove(currMove)){
                        game.board.printBoard();
                        winner = game.checkmateHandler();
                        gameDraw = game.drawChecker();
                    }
                    else{
                        System.out.println("Try Again");
                    }
                }
            }
        }
    }
}

