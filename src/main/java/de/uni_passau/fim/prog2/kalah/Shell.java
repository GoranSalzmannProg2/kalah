package de.uni_passau.fim.prog2.kalah;

import de.uni_passau.fim.prog2.kalah.kalah.Board;
import de.uni_passau.fim.prog2.kalah.kalah.IllegalMoveException;
import de.uni_passau.fim.prog2.kalah.kalah.Kalah;
import de.uni_passau.fim.prog2.kalah.kalah.Player;

import java.util.Scanner;

/**
 * Command line interface for the game of kalah. Interacts directly with an
 * implementation of the board interface.
 */
final class Shell {
    private static int currentLevel;
    private static Board kalah;
    private static final String PROMPT = "kalah> ";
    private static final String HELP = """
            Mancala/Kalah - all commands:
            NEW <p> <s>:    Creates a new game with <p> pits per player and <s>
                            seeds per pit. The difficulty and the opening player
                            will be copied from the previous game, if possible.
                            If this is the first game the difficulty will be set
                            to 3 and the human player will begin.
                        
            LEVEL <i>:      Sets the difficulty from 1 to 7. Changes will be
                            immediately reflected in the bots behaviour.
                        
            MOVE <p>:       Lets the player choose a pit from which to take the
                            seeds for his move. If the game doesn't end after 
                            the players turn, the computer will make its move 
                            immediately after.
                        
            SWITCH:         Restarts the game and changes the opening player.
                        
            PRINT:          Displays the current state of the board in the 
                            console. The first line are the computers pits, the 
                            second line shows the players pits.
                            
            HELP:           Prints a helping dialog for the game, explaining all
                            available commands.
                            
            QUIT:           Exits the game and stops the program.
            """;

    private Shell() throws IllegalAccessException {
        throw new IllegalAccessException("Instantiating not allowed.");
    }

    /**
     * Entry point for the program. Implements the main game loop.
     *
     * @param args Command line parameters for the program.
     * @throws InterruptedException {@link Thread#interrupt()} was called on the
     *                              executing thread. Thus, the execution stops
     *                              prematurely.
     */
    public static void main(String[] args) throws InterruptedException {
        Scanner in = new Scanner(System.in);

        while (true) {
            if (checkMachineMove()) {
                continue;
            }
            System.out.print(PROMPT);
            String userInput = in.nextLine();
            if (userInput == null) {
                continue;
            }
            String[] slices = userInput.split("\s+");
            if (slices.length == 0) {
                continue;
            }
            String command = slices[0].toUpperCase();
            switch (command) {
                case "HELP" -> System.out.println(HELP);
                case "QUIT" -> System.exit(0);
                case "PRINT" -> displayCommand();
                case "NEW" -> newCommand(slices);
                case "SWITCH" -> switchCommand();
                case "LEVEL" -> levelCommand(slices);
                case "MOVE" -> moveCommand(slices);
                default -> printErr("Not a valid command.");
            }
        }
    }

    private static boolean checkMachineMove() throws InterruptedException {
        if (kalah != null && !kalah.isGameOver()
                && kalah.next() == Player.COMPUTER) {
            kalah = kalah.machineMove();
            System.out.println("Machine chose pit "
                    + kalah.sourcePitOfLastMove()
                    + " with seeds reaching pit "
                    + kalah.targetPitOfLastMove() + ".");
            if (kalah.isGameOver()) {
                checkGameOver();
            } else if (kalah.next() == Player.COMPUTER) {
                System.out.println("You must miss a turn.");
            }
            return true;
        }
        return false;
    }

    private static void checkGameOver() {
        if (kalah.isGameOver()) {
            int human = kalah.getSeedsOfPlayer(Player.HUMAN);
            int computer = kalah.getSeedsOfPlayer(Player.COMPUTER);
            switch (kalah.getWinner()) {
                case HUMAN ->
                        System.out.println("Congratulations! You won with "
                                + human + " seeds versus " + computer
                                + " seeds of the machine.");
                case COMPUTER -> System.out.println("Sorry! Machine wins with "
                        + computer + " seeds versus your " + human + ".");
                default -> System.out.println("Nobody wins. Tie with "
                        + human + " for each player");
            }
        }
    }

    private static void moveCommand(String[] slices) {
        Integer pit = null;
        try {
            pit = Integer.parseInt(slices[1]);
        } catch (NumberFormatException e) {
            printErr("First argument must be an integer.");
        }
        if (kalah != null && pit != null) {
            try {
                kalah = kalah.move(pit);
            } catch (IllegalMoveException | IllegalArgumentException e) {
                printErr(e.getMessage());
            }
            if (kalah.next() == Player.HUMAN) {
                System.out.println("Machine must miss a turn.");
            }
            checkGameOver();
        }
    }

    private static void levelCommand(String[] slices) {
        if (slices.length < 2) {
            printErr("Not enough arguments supplied!");
            return;
        }
        if (kalah == null) {
            printErr("There is currently no board present.");
        } else {
            Integer level = null;
            try {
                level = Integer.parseInt(slices[1]);
            } catch (NumberFormatException e) {
                printErr("Argument must be an integer.");
            }
            if (level != null) {
                if (level >= 1) {
                    kalah.setLevel(level);
                    currentLevel = level;
                } else {
                    printErr("Level must be greater than 0.");
                }
            }

        }
    }

    private static void switchCommand() {
        if (kalah == null) {
            printErr("There currently is no board present.");
        } else {
            kalah = new Kalah(kalah.getPitsPerPlayer(), kalah.getSeedsPerPit(),
                    kalah.getOpeningPlayer().getOpposite(), currentLevel);
        }
    }

    private static void newCommand(String[] slices) {
        if (slices.length < 3) {
            printErr("Not enough arguments supplied!");
            return;
        }
        Integer pitsPerPlayer = null, seedsPerPit = null;
        try {
            pitsPerPlayer = Integer.parseInt(slices[1]);
            seedsPerPit = Integer.parseInt(slices[2]);
        } catch (NumberFormatException e) {
            printErr("First and second argument need to be an integer.");
        }
        if (pitsPerPlayer == null || seedsPerPit == null) {
            return;
        }
        if (kalah == null) {
            kalah = new Kalah(pitsPerPlayer, seedsPerPit,
                    Player.HUMAN, 3);
            currentLevel = 3;
        } else {
            kalah = new Kalah(pitsPerPlayer, seedsPerPit,
                    kalah.getOpeningPlayer(), currentLevel);
        }
    }

    private static void displayCommand() {
        if (kalah == null) {
            printErr("There is currently no board present.");
        } else {
            System.out.println(kalah);
        }
    }

    private static void printErr(String msg) {
        System.out.println("Error! " + msg);
    }
}
