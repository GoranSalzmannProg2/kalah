package de.uni_passau.fim.prog2.kalah.kalah;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Standard implementation for the board interface. Allows for playing a game
 * of kalah against an enemy ai of varying difficulty.
 */
public class Kalah implements Board, Comparable<Kalah> {
    private int pitsPerPlayer;
    private final int pitsPerPlayerIncludingStore;
    private int seedsPerPit;
    private Player openingPlayer;
    private Player nextPlayer;
    private Integer sourcePitOfLastMove;
    private Integer targetPitOfLastMove;
    private int level;
    private int[] pits;
    private final int humanFirstPit;
    private final int humanLastPit;
    private final int humanStore;
    private final int computerFirstPit;
    private final int computerLastPit;
    private final int computerStore;

    /**
     * Default constructor for the Kalah class.
     *
     * @param pitsPerPlayer The number of pits each player should have on the
     *                      board excluding the store.
     * @param seedsPerPit   The number of seeds each pit excluding the stores
     *                      should start with.
     * @param openingPlayer The player who has the first move.
     * @param level         The difficulty of the enemy ai.
     */
    public Kalah(int pitsPerPlayer, int seedsPerPit, Player openingPlayer,
                 int level) {
        this.pitsPerPlayer = pitsPerPlayer;
        this.pitsPerPlayerIncludingStore = pitsPerPlayer + 1;
        this.humanFirstPit = 1;
        this.humanLastPit = pitsPerPlayer;
        this.humanStore = pitsPerPlayerIncludingStore;
        this.computerFirstPit = pitsPerPlayerIncludingStore + 1;
        this.computerLastPit = pitsPerPlayerIncludingStore + pitsPerPlayer;
        this.computerStore = pitsPerPlayerIncludingStore * 2;
        this.seedsPerPit = seedsPerPit;
        this.openingPlayer = openingPlayer;
        this.nextPlayer = openingPlayer;
        this.level = level;
        this.sourcePitOfLastMove = null;
        this.targetPitOfLastMove = null;
        this.pits = new int[pitsPerPlayerIncludingStore * 2];
        for (int i = humanFirstPit; i <= humanLastPit; i++) {
            this.pits[i - 1] = seedsPerPit;
        }
        for (int i = computerFirstPit; i <= computerLastPit; i++) {
            this.pits[i - 1] = seedsPerPit;
        }
    }

    /**
     * Gets the player who should open or already has opened the game by the
     * initial move.
     *
     * @return The player who makes the initial move.
     */
    @Override
    public Player getOpeningPlayer() {
        return this.openingPlayer;
    }

    /**
     * Gets the player who owns the next game turn.
     *
     * @return The player who is allowed to make the next turn.
     */
    @Override
    public Player next() {
        return this.nextPlayer;
    }

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
    @Override
    public Board move(int pit) {
        if (nextPlayer != Player.HUMAN) {
            throw new IllegalMoveException("Human may not make a move now.");
        } else if (pit < humanFirstPit || pit > humanLastPit) {
            throw new IllegalArgumentException("Pit does not belong to human");
        } else {
            return simulateMove(pit).board();
        }
    }

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
    @Override
    public Board machineMove() throws InterruptedException {
        List<Kalah> possibleMoves = new ArrayList<>(pitsPerPlayer);
        for (int j = computerFirstPit; j <= computerLastPit; j++) {
            if (getSeeds(j) != 0) {
                possibleMoves.add((Kalah) simulateMove(j).board());
            }
        }
        return possibleMoves.stream().max(Kalah::compareTo).get();
    }

    /**
     * Sets the skill level of the machine.
     *
     * @param level The skill as a number, must be at least 1.
     */
    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Checks if the game is over. Either one player has won or there is a tie,
     * i.e., both players gained the same number of seeds.
     *
     * @return {@code true} if and only if the game is over.
     */
    @Override
    public boolean isGameOver() {
        boolean humanPitsEmpty = true;
        for (int i = humanFirstPit; i <= humanLastPit; i++) {
            if (getSeeds(i) != 0) {
                humanPitsEmpty = false;
            }
        }
        boolean computerPitsEmpty = true;
        for (int i = computerFirstPit; i <= computerLastPit; i++) {
            if (getSeeds(i) != 0) {
                computerPitsEmpty = false;
            }
        }
        return humanPitsEmpty || computerPitsEmpty;
    }

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
    @Override
    public Player getWinner() {
        if (isGameOver()) {
            int humanSeeds = getSeedsOfPlayer(Player.HUMAN);
            int computerSeeds = getSeedsOfPlayer(Player.COMPUTER);
            if (humanSeeds > computerSeeds) {
                return Player.HUMAN;
            } else if (computerSeeds > humanSeeds) {
                return Player.COMPUTER;
            } else {
                return Player.NOBODY;
            }
        } else {
            return null;
        }
    }

    /**
     * Gets the number of seeds of the specified pit index {@code pit}.
     *
     * @param pit The number of the pit.
     * @return The pit's content.
     */
    @Override
    public int getSeeds(int pit) {
        return this.pits[pit - 1];
    }

    /**
     * Gets the number of the source pit of the last executed move. A number of
     * one of the stores is not possible.
     *
     * @return The ordering number of the last move's source pit.
     */
    @Override
    public int sourcePitOfLastMove() {
        return this.sourcePitOfLastMove;
    }

    /**
     * Gets the number of the target pit of the last executed move. The number
     * of the move opponent's stores is not possible.
     *
     * @return The ordering number of the last move's target pit.
     */
    @Override
    public int targetPitOfLastMove() {
        return this.targetPitOfLastMove;
    }

    /**
     * Gets the number of pits per player in this game.
     *
     * @return The number of pits per player.
     */
    @Override
    public int getPitsPerPlayer() {
        return this.pitsPerPlayer;
    }

    /**
     * Gets the initial number of seeds in each pit of the players.
     *
     * @return The initial number of seeds per pit.
     */
    @Override
    public int getSeedsPerPit() {
        return this.seedsPerPit;
    }

    /**
     * Gets the current number of the seeds of the player {@code player}. This
     * is the sum of the seeds in her pits and in her store.
     *
     * @param player The player for which to sum up her seeds.
     * @return The sum of the seeds per player.
     */
    @Override
    public int getSeedsOfPlayer(Player player) {
        int sum = 0;
        if (player == Player.HUMAN) {
            for (int i = humanFirstPit; i <= humanStore; i++) {
                sum += getSeeds(i);
            }
        } else if (player == Player.COMPUTER) {
            for (int i = computerFirstPit; i <= computerStore; i++) {
                sum += getSeeds(i);
            }
        } else {
            throw new IllegalArgumentException("Cannot get seeds of NOBODY.");
        }
        return sum;
    }

    /**
     * Creates and returns a deep copy of this board.
     *
     * @return A clone.
     */
    @Override
    public Board clone() {
        Kalah clone;
        try {
            clone = (Kalah) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        clone.pitsPerPlayer = this.pitsPerPlayer;
        clone.seedsPerPit = this.seedsPerPit;
        clone.openingPlayer = this.openingPlayer;
        clone.level = this.level;
        clone.nextPlayer = this.nextPlayer;
        clone.targetPitOfLastMove = this.targetPitOfLastMove;
        clone.sourcePitOfLastMove = this.sourcePitOfLastMove;
        clone.pits = this.pits.clone();
        return clone;
    }

    /**
     * Calculates a string representation of the current board. Takes into
     * account how many digits the biggest number has, allowing for dynamic
     * formatting.
     *
     * @return The string representation of the board.
     */
    @Override
    public String toString() {
        int mostSeeds = Arrays.stream(pits).reduce(0, Integer::max);
        int padding = String.valueOf(mostSeeds).length();
        StringBuilder sb = new StringBuilder();
        for (int i = computerStore; i >= computerFirstPit; i--) {
            sb.append(String.format("%" + padding + "s", getSeeds(i)))
                    .append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(System.lineSeparator())
                .append(String.format("%" + padding + "s", "")).append(" ");
        for (int i = humanFirstPit; i <= humanStore; i++) {
            sb.append(String.format("%" + padding + "s", getSeeds(i)))
                    .append(" ");
        }
        return sb.substring(0, sb.length() - 1);
    }

    private boolean isPlayersPit(Player player, int pit) {
        return switch (player) {
            case HUMAN -> pit >= humanFirstPit && pit <= humanLastPit;
            case COMPUTER -> pit >= computerFirstPit && pit <= computerLastPit;
            case NOBODY -> false;
        };
    }

    private int overrideSeeds(int pit, int newValue) {
        int oldValue = getSeeds(pit);
        pits[pit - 1] = newValue;
        return oldValue;
    }

    private int getOpposite(int pit) {
        if (pit == humanStore) {
            return computerStore;
        } else if (pit == computerStore) {
            return humanStore;
        } else {
            return pits.length - pit;
        }
    }

    private MoveEvent simulateMove(int pit) {
        if (getSeeds(pit) == 0 || isGameOver()) {
            throw new IllegalMoveException("Not a valid move.");
        } else {
            Integer store = switch (nextPlayer) {
                case COMPUTER -> computerStore;
                case HUMAN -> humanStore;
                case NOBODY -> null;
            };
            int capturedSeeds = 0;
            Kalah board = (Kalah) clone();
            int toSeed = board.overrideSeeds(pit, 0);
            int currentPit = pit;
            for (int i = 1; i <= toSeed; i++) {
                currentPit = nextPit(currentPit);
                if (currentPit == getOpposite(store)) {
                    currentPit = nextPit(currentPit);
                }
                if (i == toSeed) {
                    // last pit reached
                    if (board.isPlayersPit(board.nextPlayer, currentPit)
                            && board.getSeeds(currentPit) == 0
                            && board.getSeeds(getOpposite(currentPit)) != 0) {
                        // capture situation
                        capturedSeeds =
                                board.overrideSeeds(getOpposite(currentPit),
                                        0);
                        board.overrideSeeds(store,
                                board.getSeeds(store)
                                        + capturedSeeds + 1);
                    } else {
                        board.overrideSeeds(currentPit,
                                board.getSeeds(currentPit) + 1);
                    }
                    board.targetPitOfLastMove = currentPit;
                    board.sourcePitOfLastMove = pit;
                    if (currentPit != store) {
                        board.nextPlayer = board.nextPlayer.getOpposite();
                    }
                } else {
                    board.overrideSeeds(currentPit,
                            board.getSeeds(currentPit) + 1);
                }
            }
            return new MoveEvent(board, capturedSeeds);
        }
    }

    private int nextPit(int pit) {
        return (pit % (pitsPerPlayerIncludingStore * 2)) + 1;
    }

    private double scoreS() {
        double scoreSH = getSeeds(humanStore);
        double scoreSM = getSeeds(computerStore);
        double scoreS = scoreSM - 1.5 * scoreSH;
        return scoreS;
    }

    private double scoreC() {
        double scoreCH;
        double scoreCM;
        HashMap<Integer, Integer> humanCaught = new HashMap<>();
        for (int i = humanFirstPit; i <= humanLastPit; i++) {
            try {
                Kalah clone = (Kalah) clone();
                clone.nextPlayer = Player.HUMAN;
                MoveEvent event = clone.simulateMove(i);
                int seedsCaptured = event.seedsCaptured();
                int targetPit = event.board().targetPitOfLastMove();
                int seedsSavedBefore
                        = humanCaught.getOrDefault(targetPit, 0);
                humanCaught.put(targetPit, Integer.max(seedsCaptured,
                        seedsSavedBefore));
            } catch (IllegalMoveException e) {

            }
        }
        HashMap<Integer, Integer> computerCaught = new HashMap<>();
        for (int i = computerFirstPit; i <= computerLastPit; i++) {
            try {
                Kalah clone = (Kalah) clone();
                clone.nextPlayer = Player.COMPUTER;
                MoveEvent event = clone.simulateMove(i);
                int seedsCaptured = event.seedsCaptured();
                int targetPit = event.board().targetPitOfLastMove();
                int seedsSavedBefore
                        = computerCaught.getOrDefault(targetPit, 0);
                computerCaught.put(targetPit, Integer.max(seedsCaptured,
                        seedsSavedBefore));
            } catch (IllegalMoveException e) {

            }
        }
        scoreCH = humanCaught.values().stream().reduce(0, Integer::sum);
        scoreCM =
                computerCaught.values().stream().reduce(0, Integer::sum);
        double scoreC = scoreCM - 1.5 * scoreCH;
        return scoreC;
    }

    private double scoreP() {
        double scorePM = 0;
        double scorePH = 0;
        for (int i = humanFirstPit; i <= humanLastPit; i++) {
            if (getSeeds(i) == 0
                    && getSeeds(getOpposite(i)) >= seedsPerPit * 2) {
                scorePH++;
            }
        }
        for (int i = computerFirstPit; i <= computerLastPit; i++) {
            if (getSeeds(i) == 0
                    && getSeeds(getOpposite(i)) >= seedsPerPit * 2) {
                scorePM++;
            }
        }
        double scoreP = scorePM - 1.5 * scorePH;
        return scoreP;
    }

    private double scoreV(int i) {
        if (isGameOver()) {
            double scoreV = switch (getWinner()) {
                case HUMAN -> (500.0 / i) * -1.5;
                case COMPUTER -> 500.0 / i;
                case NOBODY -> 0.0;
            };
            return scoreV;
        } else {
            return 0.0;
        }
    }

    private double getScore(int i) {
        if (i == level) {
            double score = 3 * scoreS() + scoreC() + scoreP() + scoreV(i);
            return score;
        } else {
            double childOffset = switch (nextPlayer) {
                case HUMAN -> {
                    List<Double> possibleMoves
                            = new ArrayList<>(pitsPerPlayer);
                    for (int j = humanFirstPit; j <= humanLastPit; j++) {
                        try {
                            Kalah move = (Kalah) simulateMove(j).board();
                            double value = move.getScore(i + 1);
                            possibleMoves.add(value);
                        } catch (IllegalMoveException e) {

                        }
                    }
                    if (possibleMoves.isEmpty()) {
                        yield 0.0;
                    } else {
                        yield possibleMoves.stream()
                                .reduce(Double.POSITIVE_INFINITY, Double::min);
                    }
                }
                case COMPUTER -> {
                    List<Double> possibleMoves
                            = new ArrayList<>(pitsPerPlayer);
                    for (int j = computerFirstPit; j <= computerLastPit; j++) {
                        try {
                            Kalah move = (Kalah) simulateMove(j).board();
                            double value = move.getScore(i + 1);
                            possibleMoves.add(value);
                        } catch (IllegalMoveException e) {

                        }
                    }
                    if (possibleMoves.isEmpty()) {
                        yield 0.0;
                    } else {
                        yield possibleMoves.stream()
                                .reduce(Double.NEGATIVE_INFINITY, Double::max);
                    }
                }
                case NOBODY -> 0.0;
            };
            double score = 3 * scoreS() + scoreC() + scoreP() + scoreV(i);
            return childOffset + score;
        }
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Kalah o) {
        return Double.compare(this.getScore(1), o.getScore(1));
    }
}
