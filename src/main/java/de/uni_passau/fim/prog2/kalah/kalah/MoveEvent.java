package de.uni_passau.fim.prog2.kalah.kalah;

/**
 * Record containing all important information for what happened after a move.
 *
 * @param board         The new board resulting from the executed move.
 * @param seedsCaptured How many seeds have been captured in the move.
 */
public record MoveEvent(Board board, int seedsCaptured) {
}
