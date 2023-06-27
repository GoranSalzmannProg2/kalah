package de.uni_passau.fim.prog2.kalah.kalah;

/**
 * All possible variants of an player, actor or any kind of person in the game.
 */
public enum Player {
    /**
     * The human player.
     */
    HUMAN,
    /**
     * The machine player, i.e. the enemy ai.
     */
    COMPUTER,
    /**
     * A variant representing absence of a specific player.
     */
    NOBODY;

    private de.uni_passau.fim.prog2.kalah.kalah.Player opposite;

    static {
        HUMAN.opposite = COMPUTER;
        COMPUTER.opposite = HUMAN;
        NOBODY.opposite = NOBODY;
    }

    /**
     * Helper method on a player object, for determining the opposing player.
     *
     * @return The enum variant, of the opposing player.
     */
    public de.uni_passau.fim.prog2.kalah.kalah.Player getOpposite() {
        return opposite;
    }
}


