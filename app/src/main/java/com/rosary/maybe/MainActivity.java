package com.rosary.maybe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Build;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {

    private SharedPreferences prefs;
    private int count = 0;
    private int goal = 33;
    private int dhikrIndex = 0;
    private boolean vibration = true;

    private TextView counter, dhikrText, progressText;
    private Button tasbeehButton, vibrationButton;
    private Spinner dhikrSpinner, goalSpinner;

    private final int background = Color.rgb(8,17,15);
    private final int cardColor = Color.rgb(19,36,29);
    private final int green = Color.rgb(32,180,105);
    private final int lightGreen = Color.rgb(178,255,208);
    private final int white = Color.WHITE;
    private final int gray = Color.rgb(174,193,181);

    private final String[] adhkar = {
        "سبحان الله",
        "الحمد لله",
        "الله أكبر",
        "أستغفر الله",
        "لا إله إلا الله",
        "سبحان الله وبحمده",
        "لا حول ولا قوة إلا بالله",
        "اللهم صلِّ وسلم على نبينا محمد"
    };

    private final int[] goals = {33,100,1000,0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("misbaha_data", MODE_PRIVATE);

        count = Math.max(0, prefs.getInt("count", 0));
        goal = prefs.getInt("goal", 33);
        dhikrIndex = prefs.getInt("dhikr", 0);
        vibration = prefs.getBoolean("vibration", true);

        if (goal != 33 && goal != 100 && goal != 1000 && goal != 0)
            goal = 33;

        if (dhikrIndex < 0 || dhikrIndex >= adhkar.length)
            dhikrIndex = 0;

        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);

        buildInterface();
        updateDisplay();
    }

    private void buildInterface() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(background);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(20), dp(25), dp(20), dp(25));
        scroll.addView(root);

        TextView title = makeText("📿 المسبحة الإلكترونية",26,white,true);
        title.setGravity(Gravity.CENTER);
        root.addView(title, fullWidth());

        TextView subtitle = makeText("سبّح واطمئن قلبك",16,lightGreen,false);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = fullWidth();
        subParams.topMargin = dp(10);
        subParams.bottomMargin = dp(22);
        root.addView(subtitle, subParams);

        LinearLayout dhikrCard = makeCard();

        TextView chooseLabel = makeText("اختر الذكر",16,gray,true);
        chooseLabel.setGravity(Gravity.CENTER);
        dhikrCard.addView(chooseLabel, fullWidth());

        dhikrSpinner = new Spinner(this);
        ArrayAdapter<String> dhikrAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, adhkar
        );
        dhikrAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        );
        dhikrSpinner.setAdapter(dhikrAdapter);
        dhikrSpinner.setSelection(dhikrIndex);

        LinearLayout.LayoutParams spinnerParams = fullWidth();
        spinnerParams.topMargin = dp(10);
        dhikrCard.addView(dhikrSpinner, spinnerParams);
        root.addView(dhikrCard, withBottom(18));

        dhikrSpinner.setOnItemSelectedListener(
            new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(
                    android.widget.AdapterView<?> parent,
                    View view, int position, long id
                ) {
                    dhikrIndex = position;
                    saveData();
                    updateDisplay();
                }

                @Override
                public void onNothingSelected(
                    android.widget.AdapterView<?> parent
                ) {}
            }
        );

        LinearLayout counterCard = makeCard();
        counterCard.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView counterLabel = makeText("عدد التسبيحات",17,gray,true);
        counterLabel.setGravity(Gravity.CENTER);
        counterCard.addView(counterLabel, fullWidth());

        counter = makeText("000",60,lightGreen,true);
        counter.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams numberParams = fullWidth();
        numberParams.topMargin = dp(8);
        counterCard.addView(counter, numberParams);

        dhikrText = makeText(adhkar[dhikrIndex],21,white,true);
        dhikrText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams textParams = fullWidth();
        textParams.topMargin = dp(5);
        counterCard.addView(dhikrText, textParams);

        progressText = makeText("",14,lightGreen,false);
        progressText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams progressParams = fullWidth();
        progressParams.topMargin = dp(12);
        progressParams.bottomMargin = dp(15);
        counterCard.addView(progressText, progressParams);

        tasbeehButton = new Button(this);
        tasbeehButton.setText("سَبِّح");
        tasbeehButton.setTextSize(27);
        tasbeehButton.setTextColor(white);
        tasbeehButton.setAllCaps(false);
        tasbeehButton.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        tasbeehButton.setBackground(rounded(green,150));
        tasbeehButton.setElevation(dp(7));

        LinearLayout.LayoutParams buttonParams =
            new LinearLayout.LayoutParams(dp(215),dp(215));
        buttonParams.gravity = Gravity.CENTER;
        counterCard.addView(tasbeehButton, buttonParams);

        tasbeehButton.setOnClickListener(v -> addTasbeeh());

        TextView hint = makeText(
            "اضغط على الزر لزيادة العدد",13,gray,false
        );
        hint.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams hintParams = fullWidth();
        hintParams.topMargin = dp(12);
        counterCard.addView(hint, hintParams);
        root.addView(counterCard, withBottom(18));

        LinearLayout settingsCard = makeCard();

        TextView goalLabel = makeText("🎯 هدف التسبيح",17,white,true);
        goalLabel.setGravity(Gravity.CENTER);
        settingsCard.addView(goalLabel, fullWidth());

        String[] goalNames = {
            "33 تسبيحة","100 تسبيحة","1000 تسبيحة","بدون هدف"
        };

        goalSpinner = new Spinner(this);
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, goalNames
        );
        goalAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        );
        goalSpinner.setAdapter(goalAdapter);
        goalSpinner.setSelection(getGoalIndex());

        LinearLayout.LayoutParams goalParams = fullWidth();
        goalParams.topMargin = dp(10);
        settingsCard.addView(goalSpinner, goalParams);

        goalSpinner.setOnItemSelectedListener(
            new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(
                    android.widget.AdapterView<?> parent,
                    View view, int position, long id
                ) {
                    goal = goals[position];
                    saveData();
                    updateDisplay();
                }

                @Override
                public void onNothingSelected(
                    android.widget.AdapterView<?> parent
                ) {}
            }
        );

        Button resetButton = makeButton(
            "تصفير العداد", Color.rgb(120,45,45)
        );

        LinearLayout.LayoutParams resetParams = fullWidth();
        resetParams.topMargin = dp(15);
        settingsCard.addView(resetButton, resetParams);
        resetButton.setOnClickListener(v -> confirmReset());

        vibrationButton = makeButton(
            vibration ? "إيقاف الاهتزاز" : "تشغيل الاهتزاز",
            cardColor
        );

        LinearLayout.LayoutParams vibrationParams = fullWidth();
        vibrationParams.topMargin = dp(8);
        settingsCard.addView(vibrationButton, vibrationParams);

        vibrationButton.setOnClickListener(v -> {
            vibration = !vibration;
            saveData();
            vibrationButton.setText(
                vibration ? "إيقاف الاهتزاز" : "تشغيل الاهتزاز"
            );
        });

        root.addView(settingsCard, withBottom(18));

        TextView footer = makeText(
            "يعمل دون إنترنت • يتم حفظ عدد التسبيحات",12,gray,false
        );
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, fullWidth());

        setContentView(scroll);
    }

    private void addTasbeeh() {
        if (count < Integer.MAX_VALUE) count++;

        saveData();
        updateDisplay();
        vibratePhone();

        if (goal > 0 && count == goal) {
            new AlertDialog.Builder(this)
                .setTitle("ما شاء الله!")
                .setMessage("وصلت إلى هدفك: " + goal + " تسبيحة.")
                .setPositiveButton("متابعة", null)
                .setNegativeButton("تصفير العداد", (dialog, which) -> {
                    count = 0;
                    saveData();
                    updateDisplay();
                })
                .show();
        }
    }

    private void vibratePhone() {
        if (!vibration) return;

        try {
            Vibrator vibrator = (Vibrator)
                getSystemService(Context.VIBRATOR_SERVICE);

            if (vibrator == null || !vibrator.hasVibrator()) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        18, VibrationEffect.DEFAULT_AMPLITUDE
                    )
                );
            } else {
                vibrator.vibrate(18);
            }
        } catch (Exception ignored) {}
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
            .setTitle("تصفير العداد؟")
            .setMessage("هل أنت متأكد أنك تريد تصفير عدد التسبيحات؟")
            .setPositiveButton("نعم، صفّر", (dialog, which) -> {
                count = 0;
                saveData();
                updateDisplay();
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void updateDisplay() {
        if (counter == null) return;

        counter.setText(String.format(
            java.util.Locale.getDefault(), "%03d", count
        ));

        dhikrText.setText(adhkar[dhikrIndex]);

        if (goal == 0) {
            progressText.setText("التسبيح بدون هدف محدد");
        } else if (count >= goal) {
            progressText.setText("اكتمل الهدف • يمكنك المتابعة");
        } else {
            progressText.setText(
                "باقي " + (goal - count) + " للوصول إلى الهدف"
            );
        }

        tasbeehButton.setContentDescription(
            "سبح. العدد الحالي " + count
        );
    }

    private void saveData() {
        if (prefs == null) return;

        prefs.edit()
            .putInt("count", count)
            .putInt("goal", goal)
            .putInt("dhikr", dhikrIndex)
            .putBoolean("vibration", vibration)
            .apply();
    }

    private int getGoalIndex() {
        for (int i = 0; i < goals.length; i++) {
            if (goals[i] == goal) return i;
        }
        return 0;
    }

    private LinearLayout makeCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16),dp(17),dp(16),dp(17));
        card.setBackground(rounded(cardColor,22));
        card.setElevation(dp(3));
        return card;
    }

    private TextView makeText(
        String value, int size, int color, boolean bold
    ) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);

        if (bold) {
            text.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        }

        return text;
    }

    private Button makeButton(String value, int color) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextSize(15);
        button.setTextColor(white);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        button.setBackground(rounded(color,15));
        return button;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        return shape;
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams withBottom(int bottom) {
        LinearLayout.LayoutParams params = fullWidth();
        params.bottomMargin = dp(bottom);
        return params;
    }

    private int dp(float value) {
        return (int) (
            value * getResources().getDisplayMetrics().density + 0.5f
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }
}