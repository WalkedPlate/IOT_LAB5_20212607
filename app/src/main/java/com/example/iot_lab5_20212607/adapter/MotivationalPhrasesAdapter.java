package com.example.iot_lab5_20212607.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_lab5_20212607.R;
import com.example.iot_lab5_20212607.model.MotivationalPhrase;

import java.util.List;

public class MotivationalPhrasesAdapter extends RecyclerView.Adapter<MotivationalPhrasesAdapter.ViewHolder> {
    private final List<MotivationalPhrase> phrases;
    private final OnPhraseActionListener listener;

    public interface OnPhraseActionListener {
        void onPhraseEdit(MotivationalPhrase phrase);
        void onPhraseDelete(MotivationalPhrase phrase);
    }

    public MotivationalPhrasesAdapter(List<MotivationalPhrase> phrases, OnPhraseActionListener listener) {
        this.phrases = phrases;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_motivational_phrase, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(phrases.get(position));
    }

    @Override
    public int getItemCount() {
        return phrases.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView phraseText;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            phraseText = itemView.findViewById(R.id.phraseText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(MotivationalPhrase phrase) {
            phraseText.setText(phrase.getPhrase());
            editButton.setOnClickListener(v -> listener.onPhraseEdit(phrase));
            deleteButton.setOnClickListener(v -> listener.onPhraseDelete(phrase));
        }
    }

    public void updatePhrases(List<MotivationalPhrase> newPhrases) {
        phrases.clear();
        phrases.addAll(newPhrases);
        notifyDataSetChanged();
    }
}
