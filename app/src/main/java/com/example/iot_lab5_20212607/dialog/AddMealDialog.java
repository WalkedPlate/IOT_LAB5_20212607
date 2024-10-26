package com.example.iot_lab5_20212607.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.iot_lab5_20212607.R;
import com.example.iot_lab5_20212607.SQLite.DatabaseHelper;
import com.example.iot_lab5_20212607.databinding.DialogAddMealBinding;
import com.example.iot_lab5_20212607.model.CommonFood;
import com.example.iot_lab5_20212607.model.Meal;
import com.google.android.material.chip.Chip;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddMealDialog extends DialogFragment {
    private DialogAddMealBinding binding;
    private DatabaseHelper databaseHelper;
    private OnMealAddedListener listener;

    // Alimentos comunes
    private final Map<Integer, CommonFood> commonFoods = new HashMap<Integer, CommonFood>() {{
        put(R.id.chipArroz, new CommonFood("Arroz (1 taza)", 130));
        put(R.id.chipPollo, new CommonFood("Pollo (100g)", 165));
        put(R.id.chipEnsalada, new CommonFood("Ensalada César", 45));
        put(R.id.chipPan, new CommonFood("Pan Integral", 75));
        put(R.id.chipFruta, new CommonFood("Manzana", 60));
    }};



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

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        setupCommonFoodsChips();
        setupButtons(dialog);
        return dialog;
    }

    private void setupCommonFoodsChips() {
        // Configurar el listener para el ChipGroup
        binding.commonFoodsChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != View.NO_ID) {
                CommonFood selectedFood = commonFoods.get(checkedId);
                if (selectedFood != null) {
                    // Autocompletar los campos con la comida seleccionada
                    binding.mealNameInput.setText(selectedFood.getName());
                    binding.caloriesInput.setText(String.valueOf(selectedFood.getCalories()));
                }
            }
        });

        // Limpiar selección cuando se modifica manualmente
        binding.mealNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Si el usuario modifica el texto manualmente, deseleccionar el chip
                if (binding.commonFoodsChipGroup.getCheckedChipId() != View.NO_ID) {
                    binding.commonFoodsChipGroup.clearCheck();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.caloriesInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Si el usuario modifica las calorías manualmente, deseleccionar el chip
                if (binding.commonFoodsChipGroup.getCheckedChipId() != View.NO_ID) {
                    binding.commonFoodsChipGroup.clearCheck();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
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
