package com.example.iot_lab5_20212607.dialog;

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
import com.example.iot_lab5_20212607.databinding.DialogAddMealBinding;
import com.example.iot_lab5_20212607.model.Meal;

import java.util.Date;
import java.util.Locale;

public class AddMealDialog extends DialogFragment {
    private DialogAddMealBinding binding;
    private DatabaseHelper databaseHelper;
    private OnMealAddedListener listener;

    public interface OnMealAddedListener {
        void onMealAdded();
    }

    public static AddMealDialog newInstance() {
        return new AddMealDialog();
    }

    public void setOnMealAddedListener(OnMealAddedListener listener) {
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
        binding = DialogAddMealBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setView(binding.getRoot())
                .create();

        // Asegurar que el diálogo ocupe el ancho correcto
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
                saveMeal();
                dialog.dismiss();
                if (listener != null) {
                    listener.onMealAdded();
                }
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String mealName = binding.mealNameInput.getText().toString().trim();
        if (mealName.isEmpty()) {
            binding.mealNameLayout.setError("Ingrese el nombre de la comida");
            isValid = false;
        } else {
            binding.mealNameLayout.setError(null);
        }

        String calories = binding.caloriesInput.getText().toString().trim();
        if (calories.isEmpty()) {
            binding.caloriesLayout.setError("Ingrese las calorías");
            isValid = false;
        } else {
            try {
                int caloriesValue = Integer.parseInt(calories);
                if (caloriesValue <= 0) {
                    binding.caloriesLayout.setError("Las calorías deben ser mayores a 0");
                    isValid = false;
                } else {
                    binding.caloriesLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                binding.caloriesLayout.setError("Ingrese un número válido");
                isValid = false;
            }
        }

        return isValid;
    }

    private void saveMeal() {
        String mealName = binding.mealNameInput.getText().toString().trim();
        int calories = Integer.parseInt(binding.caloriesInput.getText().toString().trim());

        // Obtener la hora actual
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        // Obtener la fecha actual
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Meal meal = new Meal(mealName, currentTime, calories, currentDate);
        databaseHelper.addMeal(meal);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
