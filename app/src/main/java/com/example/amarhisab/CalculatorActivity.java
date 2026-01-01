package com.example.amarhisab;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CalculatorActivity extends AppCompatActivity {

    private TextView tvExpression, tvResult, tvCalculatorTitle;
    private ImageButton btnBack, btnHistory;

    private String currentExpression = "";
    private String currentNumber = "";
    private String operator = "";
    private double firstNumber = 0;
    private boolean isNewCalculation = true;

    private SharedPreferences prefs;
    private boolean isBangla = true;

    private ArrayList<CalculatorHistory> historyList = new ArrayList<>();
    private static final String PREF_HISTORY = "calculator_history";
    private static final int MAX_HISTORY = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        prefs = getSharedPreferences("AmarHisabPrefs", Context.MODE_PRIVATE);
        isBangla = prefs.getBoolean("isBangla", true);

        initViews();
        setupListeners();
        updateLanguage();
        loadHistory();
    }

    private void initViews() {
        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);
        tvCalculatorTitle = findViewById(R.id.tvCalculatorTitle);
        btnBack = findViewById(R.id.btnBack);
        btnHistory = findViewById(R.id.btnHistory);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnHistory.setOnClickListener(v -> showHistoryDialog());

        // Number buttons
        setNumberButtonListener(R.id.btn0, "0");
        setNumberButtonListener(R.id.btn1, "1");
        setNumberButtonListener(R.id.btn2, "2");
        setNumberButtonListener(R.id.btn3, "3");
        setNumberButtonListener(R.id.btn4, "4");
        setNumberButtonListener(R.id.btn5, "5");
        setNumberButtonListener(R.id.btn6, "6");
        setNumberButtonListener(R.id.btn7, "7");
        setNumberButtonListener(R.id.btn8, "8");
        setNumberButtonListener(R.id.btn9, "9");
        setNumberButtonListener(R.id.btnDot, ".");

        // Operator buttons
        setOperatorButtonListener(R.id.btnPlus, "+");
        setOperatorButtonListener(R.id.btnMinus, "-");
        setOperatorButtonListener(R.id.btnMultiply, "×");
        setOperatorButtonListener(R.id.btnDivide, "÷");

        // Special buttons
        findViewById(R.id.btnClear).setOnClickListener(v -> clear());
        findViewById(R.id.btnBackspace).setOnClickListener(v -> backspace());
        findViewById(R.id.btnPercent).setOnClickListener(v -> calculatePercent());
        findViewById(R.id.btnEquals).setOnClickListener(v -> calculate());
    }

    private void setNumberButtonListener(int buttonId, String number) {
        findViewById(buttonId).setOnClickListener(v -> {
            animateButton(v);
            appendNumber(number);
        });
    }

    private void setOperatorButtonListener(int buttonId, String op) {
        findViewById(buttonId).setOnClickListener(v -> {
            animateButton(v);
            setOperator(op);
        });
    }

    private void appendNumber(String number) {
        if (isNewCalculation) {
            currentNumber = "";
            isNewCalculation = false;
        }

        if (number.equals(".") && currentNumber.contains(".")) {
            return;
        }

        if (currentNumber.equals("0") && !number.equals(".")) {
            currentNumber = number;
        } else {
            currentNumber += number;
        }

        updateDisplay();
    }

    private void setOperator(String op) {
        if (!currentNumber.isEmpty()) {
            if (!operator.isEmpty()) {
                calculate();
            } else {
                firstNumber = Double.parseDouble(currentNumber);
            }
        }

        operator = op;
        currentExpression = formatNumber(firstNumber) + " " + op;
        currentNumber = "";
        updateDisplay();
    }

    // ==================== REAL PERCENTAGE CALCULATION ====================
    // যখন % বাটন চাপা হয়, তখন firstNumber এর percentage হিসেব করবে
    // উদাহরণ: 200 + 50% = 300 (200 এর 50% = 100, তাই 200 + 100 = 300)
    private void calculatePercent() {
        if (!currentNumber.isEmpty() && !operator.isEmpty()) {
            // Operator আছে এবং second number আছে
            double secondNum = Double.parseDouble(currentNumber);

            // First number এর percentage calculate করুন
            double percentValue = (firstNumber * secondNum) / 100;

            currentNumber = formatNumber(percentValue);
            updateDisplay();
        } else if (!currentNumber.isEmpty()) {
            // শুধুমাত্র একটি সংখ্যা আছে (operator নেই)
            // 50% = 0.5 (শুধু divide by 100)
            double num = Double.parseDouble(currentNumber);
            currentNumber = formatNumber(num / 100);
            updateDisplay();
        }
    }

    private void calculate() {
        if (currentNumber.isEmpty() || operator.isEmpty()) {
            return;
        }

        double secondNumber = Double.parseDouble(currentNumber);
        double result = 0;

        switch (operator) {
            case "+":
                result = firstNumber + secondNumber;
                break;
            case "-":
                result = firstNumber - secondNumber;
                break;
            case "×":
                result = firstNumber * secondNumber;
                break;
            case "÷":
                if (secondNumber != 0) {
                    result = firstNumber / secondNumber;
                } else {
                    showToast(isBangla ? "শূন্য দিয়ে ভাগ করা যায় না" : "Cannot divide by zero");
                    clear();
                    return;
                }
                break;
        }

        String expression = formatNumber(firstNumber) + " " + operator + " " + formatNumber(secondNumber);
        String resultStr = formatNumber(result);

        // Save to history
        saveToHistory(expression, resultStr);

        currentExpression = expression + " =";
        currentNumber = String.valueOf(result);
        firstNumber = result;
        operator = "";
        isNewCalculation = true;

        animateResult();
        updateDisplay();
    }

    private void clear() {
        currentExpression = "";
        currentNumber = "";
        operator = "";
        firstNumber = 0;
        isNewCalculation = true;
        updateDisplay();
    }

    private void backspace() {
        if (!currentNumber.isEmpty()) {
            currentNumber = currentNumber.substring(0, currentNumber.length() - 1);
            updateDisplay();
        }
    }

    private void updateDisplay() {
        tvExpression.setText(currentExpression);
        tvResult.setText(currentNumber.isEmpty() ? "0" : currentNumber);
    }

    private String formatNumber(double number) {
        // যদি সংখ্যাটি integer হয়, তাহলে দশমিক দেখাবেন না
        if (number == Math.floor(number) && !Double.isInfinite(number)) {
            return String.format(Locale.getDefault(), "%.0f", number);
        } else {
            DecimalFormat df = new DecimalFormat("#.########");
            return df.format(number);
        }
    }

    private void animateButton(View button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.88f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.88f, 1f);
        scaleX.setDuration(120);
        scaleY.setDuration(120);
        scaleX.start();
        scaleY.start();
    }

    private void animateResult() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvResult, "scaleX", 0.85f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvResult, "scaleY", 0.85f, 1.15f, 1f);
        scaleX.setDuration(350);
        scaleY.setDuration(350);
        scaleX.start();
        scaleY.start();
    }

    private void updateLanguage() {
        if (isBangla) {
            tvCalculatorTitle.setText("ক্যালকুলেটর");
        } else {
            tvCalculatorTitle.setText("Calculator");
        }
    }

    // ==================== HISTORY MANAGEMENT ====================

    private void saveToHistory(String expression, String result) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
        String timestamp = sdf.format(new Date());

        CalculatorHistory history = new CalculatorHistory(expression, result, timestamp);
        historyList.add(0, history);

        if (historyList.size() > MAX_HISTORY) {
            historyList.remove(historyList.size() - 1);
        }

        saveHistoryToPrefs();
    }

    private void loadHistory() {
        String historyJson = prefs.getString(PREF_HISTORY, "");
        if (!historyJson.isEmpty()) {
            String[] items = historyJson.split("\\|\\|\\|");
            for (String item : items) {
                String[] parts = item.split(";;;");
                if (parts.length == 3) {
                    historyList.add(new CalculatorHistory(parts[0], parts[1], parts[2]));
                }
            }
        }
    }

    private void saveHistoryToPrefs() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < historyList.size(); i++) {
            CalculatorHistory h = historyList.get(i);
            sb.append(h.expression).append(";;;")
                    .append(h.result).append(";;;")
                    .append(h.timestamp);
            if (i < historyList.size() - 1) {
                sb.append("|||");
            }
        }
        prefs.edit().putString(PREF_HISTORY, sb.toString()).apply();
    }

    private void clearHistory() {
        historyList.clear();
        prefs.edit().remove(PREF_HISTORY).apply();
        showToast(isBangla ? "ইতিহাস মুছে ফেলা হয়েছে" : "History cleared");
    }

    private void showHistoryDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_calculator_history, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewHistory);
        LinearLayout emptyLayout = dialogView.findViewById(R.id.emptyHistoryLayout);
        TextView tvHistoryTitle = dialogView.findViewById(R.id.tvHistoryTitle);
        TextView tvEmptyHistory = dialogView.findViewById(R.id.tvEmptyHistory);
        MaterialButton btnClearHistory = dialogView.findViewById(R.id.btnClearHistory);
        MaterialButton btnCloseHistory = dialogView.findViewById(R.id.btnCloseHistory);

        if (isBangla) {
            tvHistoryTitle.setText("হিসাবের ইতিহাস");
            tvEmptyHistory.setText("কোনো ইতিহাস নেই");
            btnClearHistory.setText("সব মুছুন");
            btnCloseHistory.setText("বন্ধ করুন");
        } else {
            tvHistoryTitle.setText("Calculation History");
            tvEmptyHistory.setText("No history");
            btnClearHistory.setText("Clear All");
            btnCloseHistory.setText("Close");
        }

        if (historyList.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            CalculatorHistoryAdapter adapter = new CalculatorHistoryAdapter(historyList, dialog);
            recyclerView.setAdapter(adapter);
        }

        btnClearHistory.setOnClickListener(v -> {
            if (!historyList.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle(isBangla ? "নিশ্চিত করুন" : "Confirm")
                        .setMessage(isBangla ? "সব ইতিহাস মুছে ফেলবেন?" : "Clear all history?")
                        .setPositiveButton(isBangla ? "হ্যাঁ" : "Yes", (d, w) -> {
                            clearHistory();
                            dialog.dismiss();
                        })
                        .setNegativeButton(isBangla ? "না" : "No", null)
                        .show();
            }
        });

        btnCloseHistory.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // ==================== HISTORY DATA CLASS ====================

    static class CalculatorHistory {
        String expression;
        String result;
        String timestamp;

        CalculatorHistory(String expression, String result, String timestamp) {
            this.expression = expression;
            this.result = result;
            this.timestamp = timestamp;
        }
    }
}