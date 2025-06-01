package com.example.calculatorapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class MainActivity extends AppCompatActivity {

    LinearLayout historyContainer;
    int maxHistoryItems = 20; // Max number of history items to keep

    TextView displayResult, displayHistory;
    StringBuilder expression = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayResult = findViewById(R.id.displayResult);
        displayHistory = findViewById(R.id.displayHistory);
        historyContainer = findViewById(R.id.historyContainer);

        setupNumberButtons();
        setupOperatorButtons();
        setupControlButtons();
    }

    private void addToHistory(String entry) {
        TextView tv = new TextView(this);
        tv.setText(entry);
        tv.setTextSize(16);
        tv.setTextColor(getColor(android.R.color.holo_orange_light));
        tv.setPadding(8, 4, 8, 4);

        historyContainer.addView(tv, 0); // Add to top

        // Remove oldest item if we exceed max
        if (historyContainer.getChildCount() > maxHistoryItems) {
            historyContainer.removeViewAt(historyContainer.getChildCount() - 1);
        }
    }

    private void setupNumberButtons() {
        int[] numberButtons = {
                R.id.btn0, R.id.btn1, R.id.btn2,
                R.id.btn3, R.id.btn4, R.id.btn5,
                R.id.btn6, R.id.btn7, R.id.btn8,
                R.id.btn9
        };

        for (int id : numberButtons) {
            findViewById(id).setOnClickListener(v -> {
                Button btn = (Button) v;
                expression.append(btn.getText());
                updateDisplay();
            });
        }
    }



    private void setupOperatorButtons() {
        int[] operatorButtons = {
                R.id.btnAdd, R.id.btnSubtract,
                R.id.btnMultiply, R.id.btnDivide
        };

        for (int id : operatorButtons) {
            findViewById(id).setOnClickListener(v -> {
                Button btn = (Button) v;
                if (expression.length() > 0) {
                    expression.append(btn.getText());
                    updateDisplay();
                }
            });
        }
    }

    private void setupControlButtons() {
        findViewById(R.id.btnEqual).setOnClickListener(v -> {
            try {
                String exprStr = expression.toString();
                // Replace × → *, ÷ → /
                String formattedExpr = exprStr.replace('×', '*').replace('÷', '/');

                Expression exp = new ExpressionBuilder(formattedExpr).build();
                double result = exp.evaluate();
                String resultStr = formatResult(result);

                // Set history line above result
                displayHistory.setText(exprStr + " =");

                // Add full entry to history panel
                addToHistory(exprStr + " = " + resultStr);

                // Show result
                expression.setLength(0);
                expression.append(resultStr);
                updateDisplay();
            } catch (Exception e) {
                displayResult.setText("Error");
            }
        });



        findViewById(R.id.btnClear).setOnClickListener(v -> {
            expression.setLength(0);
            updateDisplay();
            displayHistory.setText("");
        });

        findViewById(R.id.btnBackspace).setOnClickListener(v -> {
            if (expression.length() > 0) {
                expression.deleteCharAt(expression.length() - 1);
                updateDisplay();
            }
        });

        findViewById(R.id.btnPlusMinus).setOnClickListener(v -> {
            if (expression.length() > 0 && !isLastCharAnOperator()) {
                String lastNum = getLastNumber(expression.toString());
                if (!lastNum.isEmpty()) {
                    int lastIndex = expression.length() - lastNum.length();
                    double value = Double.parseDouble(lastNum) * -1;
                    expression.replace(lastIndex, expression.length(), formatResult(value));
                    updateDisplay();
                }
            }
        });

        // Dot button
        findViewById(R.id.btnDot).setOnClickListener(v -> {
            if (expression.length() == 0) {
                expression.append("0.");
            } else if (!containsDecimalPoint()) {
                expression.append(".");
            }
            updateDisplay();
        });

        findViewById(R.id.btnPercent).setOnClickListener(v -> {
            if (expression.length() == 0) return;

            String expr = expression.toString();
            int i = expr.length() - 1;
            StringBuilder currentNumber = new StringBuilder();

            while (i >= 0 && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                currentNumber.insert(0, expr.charAt(i));
                i--;
            }

            if (currentNumber.length() == 0) return;

            double num = Double.parseDouble(currentNumber.toString());

            if (i >= 0 && "+-×÷".indexOf(expr.charAt(i)) != -1) {
                char op = expr.charAt(i);
                int operatorIndex = i;

                i--;
                StringBuilder prevNumberBuilder = new StringBuilder();

                while (i >= 0 && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    prevNumberBuilder.insert(0, expr.charAt(i));
                    i--;
                }

                if (prevNumberBuilder.length() > 0) {
                    double prevNum = Double.parseDouble(prevNumberBuilder.toString());
                    double percentValue = (prevNum * num) / 100;

                    expression.replace(operatorIndex + 1, expr.length(), formatResult(percentValue));
                    updateDisplay();
                    return;
                }
            }

            double percentOfSelf = num / 100;
            int startIdx = expr.length() - currentNumber.length();
            expression.replace(startIdx, expr.length(), formatResult(percentOfSelf));
            updateDisplay();
        });
    }

    private boolean isLastCharAnOperator() {
        if (expression.length() == 0) return true;
        char lastChar = expression.charAt(expression.length() - 1);
        return "+-×÷".indexOf(lastChar) != -1;
    }

    private String getLastNumber(String expr) {
        StringBuilder num = new StringBuilder();
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if ("+-×÷".indexOf(c) != -1) break;
            num.insert(0, c);
        }
        return num.toString();
    }

    private String formatResult(double value) {
        if (value == (long) value)
            return String.valueOf((long) value);
        else
            return String.valueOf(value);
    }

    private boolean containsDecimalPoint() {
        String expr = expression.toString();
        int length = expr.length();

        for (int i = length - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == '.') return true;
            if ("+-×÷".indexOf(c) != -1) break;
        }

        return false;
    }

    private void updateDisplay() {
        displayResult.setText(expression.length() == 0 ? "0" : expression);
    }
}