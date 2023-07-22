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

import com.mike.ledcube.CubeCommunication.EffectTypes;
import com.mike.ledcube.Dialogs.FillEffectPreferencesActivity;
import com.mike.ledcube.Dialogs.FireEffectPreferencesActivity;
import com.mike.ledcube.Effects.DrawEffectActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EffectsFragment extends Fragment {
    private final ArrayList<EffectsFragment.EffectState> effects = new ArrayList<>();

    public EffectsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SetEffects();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_effects, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView effectsRecyclerView = requireView().findViewById(R.id.effects_recyclerview);
        effectsRecyclerView.setAdapter(new EffectsFragment.EffectAdapter(effects));
    }

    private void SetEffects() {
        EffectsFragment.EffectState fill = new EffectsFragment.EffectState(getString(R.string.fill_effect_name), EffectTypes.Fill);
        EffectsFragment.EffectState fire = new EffectsFragment.EffectState(getString(R.string.fire_effect_name), EffectTypes.Fire);
        EffectsFragment.EffectState draw = new EffectsFragment.EffectState(getString(R.string.draw_effect_name), EffectTypes.Draw);
        effects.add(fill);
        effects.add(fire);
        effects.add(draw);
    }

    private void startEffectActivity(EffectTypes effect) {
        //PreferencesDialog
        switch (effect){
            case Fill:
                startActivity(new Intent(requireContext(), FillEffectPreferencesActivity.class));
                break;
            case Fire:
                startActivity(new Intent(requireContext(), FireEffectPreferencesActivity.class));
                break;
            case Draw:
                startActivity(new Intent(requireContext(), DrawEffectActivity.class));
                break;
        }
    }

    private static class EffectState {
        private final String name;
        private final EffectTypes effect;
        private EffectState(String name, EffectTypes Effect) {
            this.name = name;
            this.effect = Effect;
        }

        public String getName() {
            return name;
        }
        public EffectTypes getEffect() {
            return effect;
        }
    }
    private class EffectAdapter extends RecyclerView.Adapter<EffectsFragment.EffectViewHolder> {
        private final List<EffectsFragment.EffectState> effects;

        private EffectAdapter(List<EffectsFragment.EffectState> effects) {
            this.effects = effects;
        }

        @NotNull
        @Override
        public EffectsFragment.EffectViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new EffectsFragment.EffectViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.effect_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NotNull EffectsFragment.EffectViewHolder holder, int position) {
            EffectsFragment.EffectState state = effects.get(position);
            holder.nameTextView.setText(state.getName());
            holder.effectLayout.setOnClickListener((view) -> startEffectActivity(state.getEffect()));
        }

        @Override
        public int getItemCount() {
            return effects.size();
        }
    }
    private static class EffectViewHolder extends RecyclerView.ViewHolder {
        final TextView nameTextView;
        final ConstraintLayout effectLayout;
        EffectViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.effect_name_textview);
            effectLayout = view.findViewById(R.id.effect_layout);
        }
    }
}