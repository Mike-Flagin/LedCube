package com.mike.ledcube;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.mike.ledcube.CubeCommunication.GameTypes;
import com.mike.ledcube.Dialogs.SnakeGamePreferencesActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class GamesFragment extends Fragment {
    private final ArrayList<GameState> games = new ArrayList<>();

    public GamesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SetGames();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_games, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView gamesRecyclerView = requireView().findViewById(R.id.games_recyclerview);
        gamesRecyclerView.setAdapter(new GameAdapter(games));
    }

    private void SetGames() {
        GameState snake = new GameState(getString(R.string.snake_game_name), GameTypes.Snake);
        games.add(snake);
    }

    private void startGameActivity(GameTypes game) {
        //PreferencesDialog
        //TODO:tetris game
        switch (game){
            case Snake:
                startActivity(new Intent(requireContext(), SnakeGamePreferencesActivity.class));
                break;
        }
    }

    private static class GameState {
        private final String name;
        private final GameTypes game;
        private GameState(String name, GameTypes game) {
            this.name = name;
            this.game = game;
        }

        public String getName() {
            return name;
        }
        public GameTypes getGame() {
            return game;
        }
    }
    private class GameAdapter extends RecyclerView.Adapter<GamesFragment.GameViewHolder> {
        private final List<GameState> games;

        private GameAdapter(List<GameState> games) {
            this.games = games;
        }

        @NotNull
        @Override
        public GamesFragment.GameViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new GameViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.game_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull GamesFragment.GameViewHolder holder, int position) {
            GameState state = games.get(position);
            holder.nameTextView.setText(state.getName());
            holder.gameLayout.setOnClickListener((view) -> startGameActivity(state.getGame()));
        }

        @Override
        public int getItemCount() {
            return games.size();
        }
    }
    private static class GameViewHolder extends RecyclerView.ViewHolder {
        final TextView nameTextView;
        final ConstraintLayout gameLayout;
        GameViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.game_name_textview);
            gameLayout = view.findViewById(R.id.game_layout);
        }
    }
}