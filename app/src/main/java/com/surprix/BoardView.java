package com.surprix;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

final class BoardView extends View {
    private static final int BOARD_COLOR = 0xFFE8C98B;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private GameState state;
    private Runnable onMove;
    private Runnable onIllegalMove;
    private float boardLeft;
    private float boardTop;
    private float boardSide;

    BoardView(Context context, GameState state, Runnable onMove, Runnable onIllegalMove) {
        super(context);
        this.state = state;
        this.onMove = onMove;
        this.onIllegalMove = onIllegalMove;
        setContentDescription("Game board. Tap an intersection or square to place the current player's mark.");
        setFocusable(true);
    }

    void setGame(GameState state) {
        this.state = state;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float side = Math.max(0f, Math.min(getWidth() - getPaddingLeft() - getPaddingRight(),
                getHeight() - getPaddingTop() - getPaddingBottom()) - 12f);
        boardSide = side;
        boardLeft = (getWidth() - side) / 2f;
        boardTop = (getHeight() - side) / 2f;

        paint.setColor(BOARD_COLOR);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(new RectF(boardLeft, boardTop, boardLeft + side, boardTop + side),
                8f, 8f, paint);
        drawGrid(canvas);
        drawStones(canvas);
    }

    private void drawGrid(Canvas canvas) {
        int size = state.getBoardSize();
        paint.setColor(0xFF594A36);
        paint.setStrokeWidth(Math.max(1f, boardSide / 480f));
        paint.setStyle(Paint.Style.STROKE);

        if (state.getGame() == GameState.Game.TIC_TAC_TOE) {
            float cell = boardSide / 3f;
            for (int line = 1; line < 3; line++) {
                float position = boardLeft + line * cell;
                canvas.drawLine(position, boardTop + cell * 0.14f, position,
                        boardTop + boardSide - cell * 0.14f, paint);
                position = boardTop + line * cell;
                canvas.drawLine(boardLeft + cell * 0.14f, position,
                        boardLeft + boardSide - cell * 0.14f, position, paint);
            }
            return;
        }

        float spacing = boardSide / (size - 1);
        for (int line = 0; line < size; line++) {
            float x = boardLeft + line * spacing;
            float y = boardTop + line * spacing;
            canvas.drawLine(x, boardTop, x, boardTop + boardSide, paint);
            canvas.drawLine(boardLeft, y, boardLeft + boardSide, y, paint);
        }

        if (state.getGame() == GameState.Game.GO) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF594A36);
            float radius = Math.max(2f, spacing * 0.07f);
            for (int row : new int[]{2, 4, 6}) {
                for (int column : new int[]{2, 4, 6}) {
                    canvas.drawCircle(boardLeft + column * spacing, boardTop + row * spacing,
                            radius, paint);
                }
            }
        }
    }

    private void drawStones(Canvas canvas) {
        int size = state.getBoardSize();
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                int cell = state.getCell(row, column);
                if (cell == 0) continue;

                float x;
                float y;
                float radius;
                if (state.getGame() == GameState.Game.TIC_TAC_TOE) {
                    float cellSide = boardSide / 3f;
                    x = boardLeft + (column + 0.5f) * cellSide;
                    y = boardTop + (row + 0.5f) * cellSide;
                    drawMark(canvas, cell, x, y, cellSide * 0.52f);
                } else {
                    float spacing = boardSide / (size - 1);
                    x = boardLeft + column * spacing;
                    y = boardTop + row * spacing;
                    radius = spacing * (state.getGame() == GameState.Game.GO ? 0.43f : 0.36f);
                    paint.setColor(cell == 1 ? 0xFF20252B : 0xFFF9F6EF);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(x, y, radius, paint);
                    paint.setColor(cell == 1 ? 0xFF101419 : 0xFF8E887D);
                    paint.setStrokeWidth(Math.max(1f, radius * 0.08f));
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(x, y, radius, paint);
                    if (state.getGame() == GameState.Game.GOMOKU) {
                        drawMark(canvas, cell, x, y, radius * 1.25f);
                    }
                }
            }
        }
    }

    private void drawMark(Canvas canvas, int player, float centerX, float centerY, float extent) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, extent * 0.09f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(player == 1 ? 0xFF20252B : 0xFF315C48);
        float padding = extent * 0.24f;
        if (player == 1) {
            canvas.drawLine(centerX - extent + padding, centerY - extent + padding,
                    centerX + extent - padding, centerY + extent - padding, paint);
            canvas.drawLine(centerX + extent - padding, centerY - extent + padding,
                    centerX - extent + padding, centerY + extent - padding, paint);
        } else {
            canvas.drawCircle(centerX, centerY, extent - padding, paint);
        }
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        if (event.getAction() != android.view.MotionEvent.ACTION_UP || boardSide <= 0) {
            return true;
        }
        float x = event.getX() - boardLeft;
        float y = event.getY() - boardTop;
        if (x < 0 || y < 0 || x > boardSide || y > boardSide) return true;

        int row;
        int column;
        if (state.getGame() == GameState.Game.TIC_TAC_TOE) {
            row = Math.min(2, (int) (y / (boardSide / 3f)));
            column = Math.min(2, (int) (x / (boardSide / 3f)));
        } else {
            float spacing = boardSide / (state.getBoardSize() - 1);
            row = Math.round(y / spacing);
            column = Math.round(x / spacing);
            if (Math.abs(y - row * spacing) > spacing * 0.48f
                    || Math.abs(x - column * spacing) > spacing * 0.48f) {
                return true;
            }
        }

        if (state.play(row, column)) {
            invalidate();
            onMove.run();
        } else {
            onIllegalMove.run();
        }
        return true;
    }
}
