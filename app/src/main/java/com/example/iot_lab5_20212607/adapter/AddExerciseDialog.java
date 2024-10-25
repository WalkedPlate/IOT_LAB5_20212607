package com.example.iot_lab5_20212607.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.iot_lab5_20212607.R;
import com.example.iot_lab5_20212607.SQLite.DatabaseHelper;
import com.example.iot_lab5_20212607.databinding.DialogAddExerciseBinding;
import com.example.iot_lab5_20212607.model.Exercise;

import java.util.Date;
import java.util.Locale;

public class AddExerciseDialog extends DialogFragment {
    private DialogAddExerciseBinding binding;
    private DatabaseHelper databaseHelper;
    private OnExerciseAddedListener listener;

    public interface OnExerciseAddedListener {
        void onExerciseAdded();
    }

    public static AddExerciseDialog newInstance() {
        return new AddExerciseDialog();
    }

    public void setOnExerciseAddedListener(OnExerciseAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_App_MaterialAlertDialog);
        databaseHelper = new DatabaseHelper(requireContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogAddExerciseBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setView(binding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        setupButtons(dialog);
        return dialog;
    }

    private void setupButtons(AlertDialog dialog) {
        binding.cancelButton.setOnClickListener(v -> dismiss());

        binding.saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                saveExercise();
                dialog.dismiss();
                if (listener != null) {
                    listener.onExerciseAdded();
                }
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String exerciseName = binding.exerciseNameInput.getText().toString().trim();
        if (exerciseName.isEmpty()) {
            binding.exerciseNameLayout.setError("Ingrese el nombre del ejercicio");
            isValid = false;
        } else {
            binding.exerciseNameLayout.setError(null);
        }

        String duration = binding.durationInput.getText().toString().trim();
        if (duration.isEmpty()) {
            binding.durationLayout.setError("Ingrese la duración");
            isValid = false;
        } else {
            try {
                int durationValue = Integer.parseInt(duration);
                if (durationValue <= 0) {
                    binding.durationLayout.setError("La duración debe ser mayor a 0");
                    isValid = false;
                } else {
                    binding.durationLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                binding.durationLayout.setError("Ingrese un número válido");
                isValid = false;
            }
        }

        String caloriesBurned = binding.caloriesBurnedInput.getText().toString().trim();
        if (caloriesBurned.isEmpty()) {
            binding.caloriesBurnedLayout.setError("Ingrese las calorías quemadas");
            isValid = false;
        } else {
            try {
                int caloriesValue = Integer.parseInt(caloriesBurned);
                if (caloriesValue <= 0) {
                    binding.caloriesBurnedLayout.setError("Las calorías deben ser mayores a 0");
                    isValid = false;
                } else {
                    binding.caloriesBurnedLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                binding.caloriesBurnedLayout.setError("Ingrese un número válido");
                isValid = false;
            }
        }

        return isValid;
    }

    private void saveExercise() {
        String exerciseName = binding.exerciseNameInput.getText().toString().trim();
        int duration = Integer.parseInt(binding.durationInput.getText().toString().trim());
        int caloriesBurned = Integer.parseInt(binding.caloriesBurnedInput.getText().toString().trim());

        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Exercise exercise = new Exercise(exerciseName, duration, caloriesBurned, currentTime, currentDate);
        databaseHelper.addExercise(exercise);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
