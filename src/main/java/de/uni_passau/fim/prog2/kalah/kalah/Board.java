package de.uni_passau.fim.prog2.kalah.kalah;

/**
 * Interface for the Mancala game Kalah. We call the pods/houses of the players
 * pits. Each player has a (winning) store. The pebbles/tiles are called seeds.
 * <p>
 * A human plays against the machine.
 * <p>
 * Numbering conventions: Pits are always zero-indexed and numbered/ordered
 * counter-clockwise starting with 0 for the human's left pit. The human store
 * follows the human pits. Then the machine's pits follow. Finally, the highest
 * number defines the machine's store.
 */
public interface Board extends Cloneable {

    /**
     * The number of pits per player in the classical Kalah game.
     */
    int DEFAULT_PITS_PER_PLAYER = 6;

    /**
     * The initial number of seeds in each pit.
     */
    int DEFAULT_SEEDS_PER_PIT = 3;

    /**
     * Gets the player who should open or already has opened the game by the
     * initial move.
     *
     * @return The player who makes the initial move.
     */
    Player getOpeningPlayer();

    /**
     * Gets the player who owns the next game turn.
     *
     * @return The player who is allowed to make the next turn.
     */
    Player next();

    /**
     * Executes a human move. This method does not change the state of this
     * instance, which is treated here as immutable. Instead, a new board/game
     * is returned, which is a copy of {@code this} with the move executed.
     *
     * @param pit The number of the human pit whose contained seeds will be
     *            sowed counter-clockwise.
     * @return A new board with the move executed. If the move is not valid,
     * i.e., the pit is empty, then {@code null} will be returned.
     * @throws IllegalMoveException     If the game is already over, or it is
     *                                  not the human's turn.
     * @throws IllegalArgumentException If the provided parameter is invalid,
     *                                  e.g., the defined pit is not on the
     *                                  grid.
     */
    de.uni_passau.fim.prog2.kalah.kalah.Board move(int pit);

    /**
     * Executes a machine move. This method does not change the state of this
     * instance, which is treated here as immutable. Instead, a new board/game
     * is returned, which is a copy of {@code this} with the move executed.
     *
     * @return A new board with the move executed.
     * @throws IllegalMoveException If the game is already over, or it is not
     *                              the machine's turn.
     * @throws InterruptedException {@link Thread#interrupt()} was called on the
     *                              executing thread. Thus, the execution stops
     *                              prematurely.
     */
    de.uni_passau.fim.prog2.kalah.kalah.Board machineMove()
            throws InterruptedException;

    /**
     * Sets the skill level of the machine.
     *
     * @param level The skill as a number, must be at least 1.
     */
    void setLevel(int level);

    /**
     * Checks if the game is over. Either one player has won or there is a tie,
     * i.e., both players gained the same number of seeds.
     *
     * @return {@code true} if and only if the game is over.
     */
    boolean isGameOver();

    /**
     * Checks if the game state is won. Should only be called if
     * {@link #isGameOver()} returns {@code true}.
     * <p>
     * A game is won by a player if her own or the opponents pits are all empty,
     * and the number of seeds in the own store plus the seeds in the own pits
     * is more than the sum of seeds in the opponents pits and store.
     *
     * @return The winner or nobody in case of a tie.
     */
    Player getWinner();

    /**
     * Gets the number of seeds of the specified pit index {@code pit}.
     *
     * @param pit The number of the pit.
     * @return The pit's content.
     */
    int getSeeds(int pit);

    /**
     * Gets the number of the source pit of the last executed move. A number of
     * one of the stores is not possible.
     *
     * @return The ordering number of the last move's source pit.
     */
    int sourcePitOfLastMove();

    /**
     * Gets the number of the target pit of the last executed move. The number
     * of the move opponent's stores is not possible.
     *
     * @return The ordering number of the last move's target pit.
     */
    int targetPitOfLastMove();

    /**
     * Gets the number of pits per player in this game.
     *
     * @return The number of pits per player.
     */
    int getPitsPerPlayer();

    /**
     * Gets the initial number of seeds in each pit of the players.
     *
     * @return The initial number of seeds per pit.
     */
    int getSeedsPerPit();

    /**
     * Gets the current number of the seeds of the player {@code player}. This
     * is the sum of the seeds in her pits and in her store.
     *
     * @param player The player for which to sum up her seeds.
     * @return The sum of the seeds per player.
     */
    int getSeedsOfPlayer(Player player);

    /**
     * Creates and returns a deep copy of this board.
     *
     * @return A clone.
     */
    de.uni_passau.fim.prog2.kalah.kalah.Board clone();

    /**
     * Gets the string representation of the current board with the numbers of
     * contained seeds representing a pit. The upper line belongs to the machine
     * and the lower to the human. The winning store is always the one to the
     * right in the respective game direction of the player, i.e., the one with
     * no opponent pit on the other line. Numbers are right aligned in columns
     * of width digits of the maximum number in any pit or store. These columns
     * are horizontally separated by an extra single white space.
     *
     * @return The string representation of the current game status with pits by
     * number of currently contained seeds.
     */
    @Override
    String toString();

}
