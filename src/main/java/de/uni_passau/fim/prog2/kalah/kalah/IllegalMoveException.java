package de.uni_passau.fim.prog2.kalah.kalah;

/**
 * An exception indicating that a move was executed at the wrong time. This
 * may occur if the game of kalah was already over or, if a player tried to
 * make a move when it wasn't his turn.
 */
public class IllegalMoveException extends RuntimeException {
    /**
     * Default constructor.
     */
    public IllegalMoveException() {
    }

    /**
     * String based constructor.
     *
     * @param message A message hinting at what went wrong.
     */
    public IllegalMoveException(String message) {
        super(message);
    }

    /**
     * Throwable based constructor.
     *
     * @param cause A throwable cause hinting at what caused the error.
     */
    public IllegalMoveException(Throwable cause) {
        super(cause);
    }

    /**
     * String and throwable based constructor.
     *
     * @param message A message hinting at what went wrong.
     * @param cause   A throwable cause hinting at what caused the error.
     */
    public IllegalMoveException(String message, Throwable cause) {
        super(message, cause);
    }
}
