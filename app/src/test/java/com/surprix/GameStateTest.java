package com.surprix;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameStateTest {
    @Test
    public void ticTacToeDetectsWinAndRejectsMovesAfterward() {
        GameState state = new GameState(GameState.Game.TIC_TAC_TOE);
        assertTrue(state.play(0, 0));
        assertTrue(state.play(1, 0));
        assertTrue(state.play(0, 1));
        assertTrue(state.play(1, 1));
        assertTrue(state.play(0, 2));

        assertTrue(state.isFinished());
        assertEquals(1, state.getWinner());
        assertFalse(state.play(2, 2));
    }

    @Test
    public void gomokuWinsWithFiveInARow() {
        GameState state = new GameState(GameState.Game.GOMOKU);
        for (int column = 0; column < 4; column++) {
            assertTrue(state.play(7, column));
            assertTrue(state.play(8, column));
        }
        assertTrue(state.play(7, 4));

        assertTrue(state.isFinished());
        assertEquals(1, state.getWinner());
    }

    @Test
    public void goCapturesAdjacentGroupAndCountsCapturedStones() {
        GameState state = new GameState(GameState.Game.GO);
        assertTrue(state.play(0, 1));
        assertTrue(state.play(1, 1));
        assertTrue(state.play(1, 0));
        assertTrue(state.play(8, 8));
        assertTrue(state.play(2, 1));
        assertTrue(state.play(8, 7));
        assertTrue(state.play(1, 2));

        assertEquals(0, state.getCell(1, 1));
        assertEquals(1, state.getCapturesX());
    }

    @Test
    public void goRejectsSuicideWithoutChangingTurnOrBoard() {
        GameState state = new GameState(GameState.Game.GO);
        assertTrue(state.play(0, 0));
        assertTrue(state.play(0, 1));
        assertTrue(state.play(8, 8));
        assertTrue(state.play(1, 0));
        assertTrue(state.play(8, 7));
        assertTrue(state.play(2, 1));
        assertTrue(state.play(8, 6));
        assertTrue(state.play(1, 2));

        assertFalse(state.play(1, 1));
        assertEquals(1, state.getTurn());
        assertEquals(0, state.getCell(1, 1));
    }

    @Test
    public void goEndsAfterTwoPassesAndCountsEnclosedTerritory() {
        GameState state = new GameState(GameState.Game.GO);
        assertTrue(state.play(3, 4));
        assertTrue(state.play(0, 0));
        assertTrue(state.play(4, 3));
        assertTrue(state.play(0, 2));
        assertTrue(state.play(4, 5));
        assertTrue(state.play(0, 4));
        assertTrue(state.play(5, 4));
        assertTrue(state.play(0, 6));
        assertTrue(state.pass());
        assertTrue(state.pass());

        assertTrue(state.isFinished());
        assertEquals(1, state.getWinner());
        assertTrue(state.getAreaScore()[0] > state.getAreaScore()[1]);
    }

    @Test
    public void goPreventsImmediateKoButPassEndsKoRestriction() {
        GameState state = new GameState(GameState.Game.GO);
        assertTrue(state.play(0, 1));
        assertTrue(state.play(1, 1));
        assertTrue(state.play(1, 0));
        assertTrue(state.play(0, 2));
        assertTrue(state.play(2, 1));
        assertTrue(state.play(2, 2));
        assertTrue(state.play(8, 8));
        assertTrue(state.play(1, 3));
        assertTrue(state.play(1, 2));

        assertFalse(state.play(1, 1));
        assertEquals(1, state.getCell(1, 2));

        assertTrue(state.pass());
        assertTrue(state.play(7, 7));
        assertTrue(state.play(1, 1));
        assertEquals(0, state.getCell(1, 2));
    }
}
