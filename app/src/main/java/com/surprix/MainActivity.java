package com.surprix;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private static final int BACKGROUND = Color.rgb(245, 242, 234);
    private static final int GREEN = Color.rgb(49, 92, 72);
    private GameState state = new GameState(GameState.Game.TIC_TAC_TOE);
    private TextView status;
    private BoardView boardView;
    private Button passButton;
    private Button[] gameButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BACKGROUND);
        getWindow().setNavigationBarColor(BACKGROUND);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        }
        setContentView(createContent());
        updateUi();
    }

    private View createContent() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(16), dp(8), dp(16), dp(8));
        root.setBackgroundColor(BACKGROUND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                view.setPadding(dp(16) + bars.left, dp(8) + bars.top,
                        dp(16) + bars.right, dp(8) + bars.bottom);
                return insets;
            });
        }

        TextView title = new TextView(this);
        title.setText("Surprix");
        title.setTextColor(Color.rgb(32, 42, 38));
        title.setTextSize(28);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(42)));

        LinearLayout games = new LinearLayout(this);
        games.setOrientation(LinearLayout.HORIZONTAL);
        gameButtons = new Button[GameState.Game.values().length];
        for (int i = 0; i < GameState.Game.values().length; i++) {
            GameState.Game game = GameState.Game.values()[i];
            Button button = new Button(this);
            button.setText(game.title);
            button.setTextSize(12);
            button.setAllCaps(false);
            button.setOnClickListener(view -> {
                state.reset(game);
                boardView.setGame(state);
                updateUi();
            });
            gameButtons[i] = button;
            games.addView(button, new LinearLayout.LayoutParams(0, dp(48), 1f));
        }
        root.addView(games, new LinearLayout.LayoutParams(-1, dp(52)));

        status = new TextView(this);
        status.setTextColor(Color.rgb(48, 57, 52));
        status.setTextSize(14);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(4), dp(3), dp(4), dp(3));
        root.addView(status, new LinearLayout.LayoutParams(-1, dp(72)));

        boardView = new BoardView(this, state, this::updateUi,
                () -> Toast.makeText(this, "Illegal move: occupied point or invalid Go move",
                        Toast.LENGTH_SHORT).show());
        root.addView(boardView, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);
        Button newGameButton = new Button(this);
        newGameButton.setText("New game");
        newGameButton.setAllCaps(false);
        newGameButton.setOnClickListener(view -> {
            state.reset(state.getGame());
            boardView.invalidate();
            updateUi();
        });
        passButton = new Button(this);
        passButton.setText("Pass");
        passButton.setAllCaps(false);
        passButton.setOnClickListener(view -> {
            state.pass();
            updateUi();
        });
        actions.addView(newGameButton, new LinearLayout.LayoutParams(0, dp(52), 1f));
        actions.addView(passButton, new LinearLayout.LayoutParams(0, dp(52), 1f));
        root.addView(actions, new LinearLayout.LayoutParams(-1, dp(56)));
        return root;
    }

    private void updateUi() {
        GameState.Game game = state.getGame();
        for (int i = 0; i < gameButtons.length; i++) {
            gameButtons[i].setTextColor(gameButtons[i].getText().toString().equals(game.title)
                    ? GREEN : Color.DKGRAY);
        }
        passButton.setVisibility(game == GameState.Game.GO ? View.VISIBLE : View.GONE);

        String text;
        if (game == GameState.Game.GO) {
            int[] score = state.getAreaScore();
            String scoring = "Area X " + score[0] + "  ·  0 " + score[1];
            String captures = "Captured: X " + state.getCapturesX() + "  ·  0 "
                    + state.getCapturesZero();
            if (state.isFinished()) {
                text = winnerText() + "\n" + scoring + "\n" + captures;
            } else {
                text = "Turn: " + playerName(state.getTurn()) + "\n" + scoring + "\n" + captures;
            }
        } else if (state.isFinished()) {
            text = state.getWinner() == 0 ? "Draw" : playerName(state.getWinner()) + " wins";
        } else {
            text = "Turn: " + playerName(state.getTurn())
                    + (game == GameState.Game.GOMOKU ? "  ·  Make five in a row" : "");
        }
        status.setText(text);
    }

    private String winnerText() {
        if (state.getWinner() == 0) return "Game over · Area score tied";
        return "Game over · " + playerName(state.getWinner()) + " wins";
    }

    private static String playerName(int player) {
        return player == 1 ? "X" : "0";
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
