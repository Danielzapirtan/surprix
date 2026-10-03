package com.surprix;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class GameState {
    enum Game {
        TIC_TAC_TOE("Tic-Tac-Toe", 3),
        GOMOKU("Gomoku", 15),
        GO("Go", 9);

        final String title;
        final int boardSize;

        Game(String title, int boardSize) {
            this.title = title;
            this.boardSize = boardSize;
        }
    }

    private Game game;
    private int[][] board;
    private int turn;
    private int winner;
    private boolean finished;
    private int capturesX;
    private int capturesZero;
    private int consecutivePasses;
    private int[][] previousGoPosition;

    GameState(Game game) {
        reset(game);
    }

    void reset(Game newGame) {
        game = newGame;
        board = new int[game.boardSize][game.boardSize];
        turn = 1;
        winner = 0;
        finished = false;
        capturesX = 0;
        capturesZero = 0;
        consecutivePasses = 0;
        previousGoPosition = null;
    }

    Game getGame() {
        return game;
    }

    int getBoardSize() {
        return game.boardSize;
    }

    int getCell(int row, int column) {
        return board[row][column];
    }

    int getTurn() {
        return turn;
    }

    int getWinner() {
        return winner;
    }

    boolean isFinished() {
        return finished;
    }

    int getCapturesX() {
        return capturesX;
    }

    int getCapturesZero() {
        return capturesZero;
    }

    boolean play(int row, int column) {
        if (finished || row < 0 || column < 0 || row >= board.length || column >= board.length
                || board[row][column] != 0) {
            return false;
        }

        if (game == Game.GO) {
            return playGo(row, column);
        }

        board[row][column] = turn;
        if (hasLine(row, column, game == Game.TIC_TAC_TOE ? 3 : 5)) {
            winner = turn;
            finished = true;
        } else if (isFull()) {
            finished = true;
        } else {
            turn = other(turn);
        }
        return true;
    }

    private boolean playGo(int row, int column) {
        int[][] positionBeforeMove = copyBoard(board);
        board[row][column] = turn;
        Set<Integer> captured = new HashSet<>();

        for (int[] neighbor : neighbors(row, column)) {
            int neighborRow = neighbor[0];
            int neighborColumn = neighbor[1];
            if (board[neighborRow][neighborColumn] != other(turn)) {
                continue;
            }
            List<Integer> group = getGroup(neighborRow, neighborColumn);
            if (!hasLiberty(group)) {
                captured.addAll(group);
            }
        }
        for (int point : captured) {
            board[point / board.length][point % board.length] = 0;
        }

        List<Integer> ownGroup = getGroup(row, column);
        if (!hasLiberty(ownGroup) || samePosition(board, previousGoPosition)) {
            board = positionBeforeMove;
            return false;
        }

        if (turn == 1) {
            capturesX += captured.size();
        } else {
            capturesZero += captured.size();
        }
        previousGoPosition = positionBeforeMove;
        consecutivePasses = 0;
        turn = other(turn);
        return true;
    }

    boolean pass() {
        if (game != Game.GO || finished) {
            return false;
        }
        previousGoPosition = null;
        consecutivePasses++;
        turn = other(turn);
        if (consecutivePasses == 2) {
            finished = true;
            int[] score = getAreaScore();
            winner = score[0] == score[1] ? 0 : score[0] > score[1] ? 1 : 2;
        }
        return true;
    }

    int[] getAreaScore() {
        int[] score = new int[2];
        boolean[][] visited = new boolean[board.length][board.length];

        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board.length; column++) {
                if (board[row][column] == 1 || board[row][column] == 2) {
                    score[board[row][column] - 1]++;
                } else if (!visited[row][column]) {
                    List<Integer> region = new ArrayList<>();
                    Set<Integer> boundaryColors = new HashSet<>();
                    ArrayDeque<Integer> queue = new ArrayDeque<>();
                    queue.add(row * board.length + column);
                    visited[row][column] = true;

                    while (!queue.isEmpty()) {
                        int point = queue.removeFirst();
                        int currentRow = point / board.length;
                        int currentColumn = point % board.length;
                        region.add(point);
                        for (int[] neighbor : neighbors(currentRow, currentColumn)) {
                            int neighborRow = neighbor[0];
                            int neighborColumn = neighbor[1];
                            int value = board[neighborRow][neighborColumn];
                            if (value == 0 && !visited[neighborRow][neighborColumn]) {
                                visited[neighborRow][neighborColumn] = true;
                                queue.addLast(neighborRow * board.length + neighborColumn);
                            } else if (value != 0) {
                                boundaryColors.add(value);
                            }
                        }
                    }
                    if (boundaryColors.size() == 1) {
                        score[boundaryColors.iterator().next() - 1] += region.size();
                    }
                }
            }
        }
        return score;
    }

    private List<Integer> getGroup(int row, int column) {
        int color = board[row][column];
        boolean[][] visited = new boolean[board.length][board.length];
        List<Integer> group = new ArrayList<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(row * board.length + column);
        visited[row][column] = true;

        while (!queue.isEmpty()) {
            int point = queue.removeFirst();
            int currentRow = point / board.length;
            int currentColumn = point % board.length;
            group.add(point);
            for (int[] neighbor : neighbors(currentRow, currentColumn)) {
                int neighborRow = neighbor[0];
                int neighborColumn = neighbor[1];
                if (!visited[neighborRow][neighborColumn]
                        && board[neighborRow][neighborColumn] == color) {
                    visited[neighborRow][neighborColumn] = true;
                    queue.addLast(neighborRow * board.length + neighborColumn);
                }
            }
        }
        return group;
    }

    private boolean hasLiberty(List<Integer> group) {
        for (int point : group) {
            for (int[] neighbor : neighbors(point / board.length, point % board.length)) {
                if (board[neighbor[0]][neighbor[1]] == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<int[]> neighbors(int row, int column) {
        List<int[]> result = new ArrayList<>(4);
        if (row > 0) result.add(new int[]{row - 1, column});
        if (row + 1 < board.length) result.add(new int[]{row + 1, column});
        if (column > 0) result.add(new int[]{row, column - 1});
        if (column + 1 < board.length) result.add(new int[]{row, column + 1});
        return result;
    }

    private boolean hasLine(int row, int column, int target) {
        int color = board[row][column];
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (int[] direction : directions) {
            int count = 1;
            count += countDirection(row, column, direction[0], direction[1], color);
            count += countDirection(row, column, -direction[0], -direction[1], color);
            if (count >= target) return true;
        }
        return false;
    }

    private int countDirection(int row, int column, int rowStep, int columnStep, int color) {
        int count = 0;
        row += rowStep;
        column += columnStep;
        while (row >= 0 && column >= 0 && row < board.length && column < board.length
                && board[row][column] == color) {
            count++;
            row += rowStep;
            column += columnStep;
        }
        return count;
    }

    private boolean isFull() {
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == 0) return false;
            }
        }
        return true;
    }

    private static int other(int player) {
        return player == 1 ? 2 : 1;
    }

    private static int[][] copyBoard(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int row = 0; row < source.length; row++) {
            copy[row] = Arrays.copyOf(source[row], source[row].length);
        }
        return copy;
    }

    private static boolean samePosition(int[][] first, int[][] second) {
        if (second == null) return false;
        for (int row = 0; row < first.length; row++) {
            if (!Arrays.equals(first[row], second[row])) return false;
        }
        return true;
    }
}
