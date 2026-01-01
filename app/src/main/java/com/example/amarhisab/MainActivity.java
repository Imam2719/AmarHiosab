package com.example.amarhisab;

import android.Manifest;
import android.animation.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import android.util.Log;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    // Database
    private AppDatabase database;
    private SpendingDao spendingDao;

    // Toolbar Views
    MaterialToolbar toolbar;
    ImageButton btnThemeToggle, btnLanguageToggle, btnRecycleBin, btnToggleChart,btnCalculator;
    TextView tvAppTitle, tvAppSubtitle, tvBadgeCount;

    // Summary Cards
    TextView tvDailyAmount, tvMonthlyAmount, tvDailyLabel, tvMonthlyLabel;
    MaterialCardView cardDaily, cardMonthly;

    // RecyclerView
    RecyclerView recyclerViewSpending;
    SpendingAdapter adapter;
    ArrayList<Spending> spendingList = new ArrayList<>();
    ArrayList<Spending> recycleBinList = new ArrayList<>();

    // FAB
    ExtendedFloatingActionButton fabAddSpending;

    // Chart
    BarChart barChart;
    LinearLayout chartContent;
    TextView tvHighestSpendingDay, tvChartTitle;
    MaterialButtonToggleGroup toggleChartType;
    MaterialButton btnChartDaily, btnChartWeekly, btnChartMonthly;
    MaterialCardView layoutHighestSpending;

    // Empty state
    LinearLayout emptyStateLayout;
    TextView tvEmptyState, tvListHeader;

    // Totals
    double dailyTotal = 0;
    double monthlyTotal = 0;

    // SharedPreferences
    SharedPreferences prefs;
    boolean isBangla = true;
    boolean isDarkMode = false;

    // Image handling
    private Uri selectedImageUri;
    private byte[] selectedImageBytes;
    private ImageView ivImagePreview;
    private MaterialButton btnRemoveImage;
    private ProgressBar imageLoadingProgress;

    // Edit mode
    private boolean isEditMode = false;
    private int editPosition = -1;
    private int editEntityId = -1;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Database initialize
        database = AppDatabase.getInstance(this);
        spendingDao = database.spendingDao();

        prefs = getSharedPreferences("AmarHisabPrefs", Context.MODE_PRIVATE);
        isBangla = prefs.getBoolean("isBangla", true);
        isDarkMode = prefs.getBoolean("isDarkMode", false);

        // 🔥 Theme apply করুন BEFORE setContentView
        applyTheme(isDarkMode);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setupActivityLaunchers();
        initViews();
        setupRecyclerView();
        setupListeners();
        updateLanguage();
        setupChart();

        // ✅ CRITICAL: updateThemeIcon() call করুন AFTER initViews()
        updateThemeIcon();

        // Database থেকে data load করুন
        loadDataFromDatabase();

        animateEntranceViews();
    }

    // ==================== DATABASE OPERATIONS ====================

    private void loadDataFromDatabase() {
        // Main spending list লোড
        List<SpendingEntity> entities = spendingDao.getAllSpending();
        spendingList.clear();
        for (SpendingEntity entity : entities) {
            Spending s = new Spending(
                    entity.id,
                    entity.amount,
                    entity.note,
                    entity.date,
                    entity.time,
                    entity.imageBytes
            );
            spendingList.add(s);
        }

        // Recycle bin লোড
        List<SpendingEntity> recycleBinEntities = spendingDao.getRecycleBin();
        recycleBinList.clear();
        for (SpendingEntity entity : recycleBinEntities) {
            Spending s = new Spending(
                    entity.id,
                    entity.amount,
                    entity.note,
                    entity.date,
                    entity.time,
                    entity.imageBytes
            );
            recycleBinList.add(s);
        }

        // UI আপডেট
        adapter.notifyDataSetChanged();
        updateSummary();
        updateChart("weekly");

        if (spendingList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    // ==================== ANIMATIONS ====================

    private void animateEntranceViews() {
        toolbar.setTranslationY(-toolbar.getHeight());
        toolbar.animate()
                .translationY(0)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        cardDaily.setAlpha(0);
        cardDaily.setScaleY(0.8f);
        cardDaily.animate()
                .alpha(1f)
                .scaleY(1f)
                .setDuration(500)
                .setStartDelay(200)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();

        cardMonthly.setAlpha(0);
        cardMonthly.setScaleY(0.8f);
        cardMonthly.animate()
                .alpha(1f)
                .scaleY(1f)
                .setDuration(500)
                .setStartDelay(400)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();

        fabAddSpending.setScaleX(0);
        fabAddSpending.setScaleY(0);
        fabAddSpending.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setStartDelay(800)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();
    }

    private void animateButtonPress(View button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.92f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.92f, 1f);
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.start();
    }

    private void animateRotation(ImageButton button) {
        ObjectAnimator rotation = ObjectAnimator.ofFloat(button, "rotation", 0f, 360f);
        rotation.setDuration(400);
        rotation.setInterpolator(new DecelerateInterpolator());
        rotation.start();
    }

    private void animateCardReveal(View view) {
        view.setAlpha(0);
        view.setScaleX(0.8f);
        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    // ==================== ACTIVITY LAUNCHERS ====================

    private void setupActivityLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap bitmap = (Bitmap) extras.get("data");
                            if (bitmap != null) {
                                handleBitmapImage(bitmap);
                            }
                        }
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null && ivImagePreview != null) {
                            loadImageFromUri(selectedImageUri);
                        }
                    }
                }
        );

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        showToast(isBangla ? "ক্যামেরা অনুমতি প্রয়োজন" : "Camera permission required");
                    }
                }
        );
    }

    private void handleBitmapImage(Bitmap bitmap) {
        if (imageLoadingProgress != null) {
            imageLoadingProgress.setVisibility(View.GONE);
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        selectedImageBytes = stream.toByteArray();

        // ✅ IMMEDIATE DISPLAY
        if (ivImagePreview != null) {
            ivImagePreview.setImageBitmap(bitmap);
            ivImagePreview.setVisibility(View.VISIBLE);
            // ✅ Remove animation - instant show
        }

        if (btnRemoveImage != null) {
            btnRemoveImage.setVisibility(View.VISIBLE);
        }

        Log.d("ImageDebug", "Image loaded: " + selectedImageBytes.length + " bytes");
    }

    private void loadImageFromUri(Uri imageUri) {
        if (imageLoadingProgress != null) {
            imageLoadingProgress.setVisibility(View.VISIBLE);
        }
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            handleBitmapImage(bitmap); // ✅ This will show image immediately
        } catch (Exception e) {
            e.printStackTrace();
            if (imageLoadingProgress != null) {
                imageLoadingProgress.setVisibility(View.GONE);
            }
            showToast(isBangla ? "ছবি লোড করতে সমস্যা" : "Failed to load image");
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void showImagePickerOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = isBangla ? "ছবি নির্বাচন করুন" : "Select Image";
        builder.setTitle(title);

        String[] options = isBangla ?
                new String[]{"ক্যামেরা", "গ্যালারি", "বাতিল"} :
                new String[]{"Camera", "Gallery", "Cancel"};

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.CAMERA);
                    } else {
                        openCamera();
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.CAMERA}, 100);
                    } else {
                        openCamera();
                    }
                }
            } else if (which == 1) {
                openGallery();
            }
        });
        builder.show();
    }

    // ==================== UI INITIALIZATION ====================

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        btnThemeToggle = findViewById(R.id.btnThemeToggle);
        btnLanguageToggle = findViewById(R.id.btnLanguageToggle);
        btnRecycleBin = findViewById(R.id.btnRecycleBin);
        tvAppTitle = findViewById(R.id.tvAppTitle);
        tvAppSubtitle = findViewById(R.id.tvAppSubtitle);
        tvBadgeCount = findViewById(R.id.tvBadgeCount);

        tvDailyAmount = findViewById(R.id.tvDailyAmount);
        tvMonthlyAmount = findViewById(R.id.tvMonthlyAmount);
        tvDailyLabel = findViewById(R.id.tvDailyLabel);
        tvMonthlyLabel = findViewById(R.id.tvMonthlyLabel);
        cardDaily = findViewById(R.id.cardDaily);
        cardMonthly = findViewById(R.id.cardMonthly);

        recyclerViewSpending = findViewById(R.id.recyclerViewSpending);
        fabAddSpending = findViewById(R.id.fabAddSpending);

        barChart = findViewById(R.id.barChart);
        chartContent = findViewById(R.id.chartContent);
        btnToggleChart = findViewById(R.id.btnToggleChart);
        tvHighestSpendingDay = findViewById(R.id.tvHighestSpendingDay);
        tvChartTitle = findViewById(R.id.tvChartTitle);
        toggleChartType = findViewById(R.id.toggleChartType);
        btnChartDaily = findViewById(R.id.btnChartDaily);
        btnChartWeekly = findViewById(R.id.btnChartWeekly);
        btnChartMonthly = findViewById(R.id.btnChartMonthly);
        layoutHighestSpending = findViewById(R.id.layoutHighestSpending);

        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvListHeader = findViewById(R.id.tvListHeader);
        btnCalculator = findViewById(R.id.btnCalculator);
    }

    private void setupRecyclerView() {
        recyclerViewSpending.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SpendingAdapter(spendingList);
        recyclerViewSpending.setAdapter(adapter);
    }

    private void applyThemeWithoutRestart(boolean dark) {
        if (dark) {
            // Dark theme apply করুন
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            // Light theme apply করুন
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // ✅ UI elements manually update করুন
        updateUIForTheme(dark);
    }

    private void updateUIForTheme(boolean dark) {
        // Window এর status bar color update
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    dark ? getColor(R.color.primary_dark) : getColor(R.color.primary_light)
            );

            getWindow().setNavigationBarColor(
                    dark ? getColor(R.color.background_dark) : getColor(R.color.background_light)
            );
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.d("ThemeDebug", "onConfigurationChanged called");

        // ✅ Configuration change হলে icon update করুন
        updateThemeIcon();
    }
    private void setupListeners() {

        btnThemeToggle.setOnClickListener(v -> {
            animateRotation(btnThemeToggle);
            isDarkMode = !isDarkMode;
            prefs.edit().putBoolean("isDarkMode", isDarkMode).apply();

            // 🔥 Activity restart করুন theme apply করতে
            recreate();
        });

        btnCalculator.setOnClickListener(v -> {
            animateButtonPress(btnCalculator);
            Intent intent = new Intent(MainActivity.this, CalculatorActivity.class);
            startActivity(intent);
        });

        btnLanguageToggle.setOnClickListener(v -> {
            animateRotation(btnLanguageToggle);
            isBangla = !isBangla;
            prefs.edit().putBoolean("isBangla", isBangla).apply();
            updateLanguage();
            updateChart("weekly");
        });

        btnRecycleBin.setOnClickListener(v -> {
            animateButtonPress(btnRecycleBin);
            showRecycleBinDialog();
        });

        fabAddSpending.setOnClickListener(v -> {
            isEditMode = false;
            editPosition = -1;
            editEntityId = -1;
            showAddSpendingDialog();
        });

        btnToggleChart.setOnClickListener(v -> {
            animateRotation(btnToggleChart);
            toggleChartContent();
        });

        toggleChartType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnChartDaily) {
                    updateChart("daily");
                } else if (checkedId == R.id.btnChartWeekly) {
                    updateChart("weekly");
                } else if (checkedId == R.id.btnChartMonthly) {
                    updateChart("monthly");
                }
            }
        });

        cardDaily.setOnClickListener(v -> animateButtonPress(v));
        cardMonthly.setOnClickListener(v -> animateButtonPress(v));
    }

    private void toggleChartContent() {
        if (chartContent.getVisibility() == View.VISIBLE) {
            AnimatorSet set = new AnimatorSet();
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(chartContent, "scaleY", 1f, 0f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(chartContent, "alpha", 1f, 0f);
            set.playTogether(scaleY, alpha);
            set.setDuration(300);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    chartContent.setVisibility(View.GONE);
                }
            });
            set.start();
        } else {
            chartContent.setVisibility(View.VISIBLE);
            chartContent.setScaleY(0);
            chartContent.setAlpha(0);
            AnimatorSet set = new AnimatorSet();
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(chartContent, "scaleY", 0f, 1f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(chartContent, "alpha", 0f, 1f);
            set.playTogether(scaleY, alpha);
            set.setDuration(300);
            set.start();
        }
    }

    // ==================== THEME MANAGEMENT ====================

    private void applyTheme(boolean dark) {
        if (dark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    private void updateThemeIcon() {
        // Safety check
        if (btnThemeToggle == null) {
            Log.e("ThemeDebug", "btnThemeToggle is NULL!");
            return;
        }

        // Get current system night mode
        int currentNightMode = getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        boolean isCurrentlyDark = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        Log.d("ThemeDebug", "============ THEME ICON UPDATE ============");
        Log.d("ThemeDebug", "Current night mode value: " + currentNightMode);
        Log.d("ThemeDebug", "Is currently dark: " + isCurrentlyDark);
        Log.d("ThemeDebug", "Saved isDarkMode pref: " + isDarkMode);

        if (isCurrentlyDark) {
            // Dark mode চালু - MOON icon দেখাও
            btnThemeToggle.setImageResource(R.drawable.ic_moon);
            Log.d("ThemeDebug", "✅ Setting MOON icon (Dark mode)");
        } else {
            // Light mode চালু - SUN icon দেখাও
            btnThemeToggle.setImageResource(R.drawable.ic_sun);
            Log.d("ThemeDebug", "✅ Setting SUN icon (Light mode)");
        }

        // Language icon (original থাকবে)
        if (btnLanguageToggle != null) {
            btnLanguageToggle.setImageResource(android.R.drawable.ic_menu_sort_alphabetically);
            Log.d("ThemeDebug", "✅ Language icon set");
        }
    }

    // ==================== LANGUAGE MANAGEMENT ====================

    private void updateLanguage() {
        if (isBangla) {
            tvAppTitle.setText("আমার হিসাব");
            tvAppSubtitle.setText("স্মার্ট খরচ ব্যবস্থাপক");
            tvDailyLabel.setText("আজকের খরচ");
            tvMonthlyLabel.setText("এই মাসের খরচ");
            tvListHeader.setText("খরচের তালিকা");
            tvEmptyState.setText("কোনো খরচ যোগ করা হয়নি");
            tvChartTitle.setText("খরচের পরিসংখ্যান");
            fabAddSpending.setText("খরচ যোগ করুন");

            btnChartDaily.setText("দৈনিক");
            btnChartWeekly.setText("সাপ্তাহিক");
            btnChartMonthly.setText("মাসিক");
        } else {
            tvAppTitle.setText("My Expense");
            tvAppSubtitle.setText("Smart Expense Manager");
            tvDailyLabel.setText("Today's Spending");
            tvMonthlyLabel.setText("This Month's Spending");
            tvListHeader.setText("Spending List");
            tvEmptyState.setText("No spending added yet");
            tvChartTitle.setText("Spending Statistics");
            fabAddSpending.setText("Add Spending");

            btnChartDaily.setText("Daily");
            btnChartWeekly.setText("Weekly");
            btnChartMonthly.setText("Monthly");
            btnCalculator.setContentDescription(isBangla ? "ক্যালকুলেটর" : "Calculator");
        }

        updateChart("weekly");
    }

    private void setupChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        // ✅ FIX: Set default to WEEKLY instead of checking it first
        toggleChartType.check(R.id.btnChartWeekly);
        updateChart("weekly");
    }

    private void updateChart(String type) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        if (type.equals("daily")) {
            // ==================== DAILY: SUNDAY TO SATURDAY (LEFT TO RIGHT) ====================

            Calendar today = Calendar.getInstance();
            int todayDayOfWeek = today.get(Calendar.DAY_OF_WEEK);

            // Calculate how many days back to reach SUNDAY
            int daysBackToSunday;
            if (todayDayOfWeek == Calendar.SUNDAY) {
                daysBackToSunday = 0; // Today is Sunday
            } else {
                daysBackToSunday = todayDayOfWeek - 1;
            }

            Log.d("DailyChart", "=========== DAILY CHART ===========");
            Log.d("DailyChart", "Today: " + today.getTime() + " | DayOfWeek: " + todayDayOfWeek);
            Log.d("DailyChart", "Days back to Sunday: " + daysBackToSunday);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -(daysBackToSunday) + dayOffset);

                String date = sdf.format(cal.getTime());
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                String dayName = getLocalizedDayName(dayOfWeek, true);

                Log.d("DailyChart", "Position " + dayOffset + ": " + date + " (" + dayName + ")");

                double dayTotal = 0;

                for (Spending s : spendingList) {
                    if (s.date != null && s.date.equals(date)) {
                        dayTotal += s.amount;
                        Log.d("DailyChart", "  ✅ MATCH: " + date + " = ৳" + s.amount);
                    }
                }

                entries.add(new BarEntry(dayOffset, (float) dayTotal));
                labels.add(dayName);
            }

        } else if (type.equals("weekly")) {
            // ==================== WEEKLY: CALENDAR-WISE (1-7, 8-14, 15-21, 22-28, 29+) ====================
            // শুধুমাত্র CURRENT MONTH এর weeks দেখাবে
            // 1st-7th = Week 1, 8th-14th = Week 2, etc.

            Calendar today = Calendar.getInstance();
            int currentMonth = today.get(Calendar.MONTH);
            int currentYear = today.get(Calendar.YEAR);
            int currentDay = today.get(Calendar.DAY_OF_MONTH);

            // Get max day in current month
            int maxDayInMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH);

            Log.d("WeeklyChart", "=========== WEEKLY CHART ====================");
            Log.d("WeeklyChart", "Current Month: " + (currentMonth + 1) + " | Max Days: " + maxDayInMonth);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            // Calculate total weeks in month (1-7, 8-14, 15-21, 22-28, 29+)
            int totalWeeksInMonth = (maxDayInMonth - 1) / 7 + 1;

            Log.d("WeeklyChart", "Total Weeks in Month: " + totalWeeksInMonth);

            // Show only current month's weeks (max 4-5)
            for (int weekIndex = 0; weekIndex < totalWeeksInMonth; weekIndex++) {
                // Week boundary: Days (weekIndex * 7 + 1) to ((weekIndex + 1) * 7)
                int weekStartDay = (weekIndex * 7) + 1;
                int weekEndDay = Math.min((weekIndex + 1) * 7, maxDayInMonth);

                Calendar monthStart = Calendar.getInstance();
                monthStart.set(Calendar.YEAR, currentYear);
                monthStart.set(Calendar.MONTH, currentMonth);
                monthStart.set(Calendar.DAY_OF_MONTH, 1);

                Calendar weekStart = (Calendar) monthStart.clone();
                weekStart.set(Calendar.DAY_OF_MONTH, weekStartDay);

                Calendar weekEnd = (Calendar) monthStart.clone();
                weekEnd.set(Calendar.DAY_OF_MONTH, weekEndDay);

                String label = isBangla ? "সপ্তাহ " + (weekIndex + 1) : "Week " + (weekIndex + 1);

                Log.d("WeeklyChart", "Week " + (weekIndex + 1) + ": Days " + weekStartDay + "-" + weekEndDay);

                // ==================== CALCULATE WEEK TOTAL ====================
                double weekTotal = 0;

                try {
                    Date startDate = sdf.parse(sdf.format(weekStart.getTime()));
                    Date endDate = sdf.parse(sdf.format(weekEnd.getTime()));

                    for (Spending s : spendingList) {
                        try {
                            Date spendingDate = sdf.parse(s.date);

                            if (spendingDate != null && startDate != null && endDate != null) {
                                // Check if spending is in current month AND in week range
                                Calendar spendingCal = Calendar.getInstance();
                                spendingCal.setTime(spendingDate);

                                if (spendingCal.get(Calendar.MONTH) == currentMonth &&
                                        spendingCal.get(Calendar.YEAR) == currentYear) {

                                    // Date is in current month, now check week range
                                    if ((spendingDate.equals(startDate) || spendingDate.after(startDate)) &&
                                            (spendingDate.equals(endDate) || spendingDate.before(endDate))) {
                                        weekTotal += s.amount;
                                        Log.d("WeeklyChart", "  ✅ MATCHED Week " + (weekIndex + 1) + ": " + s.date + " = ৳" + s.amount);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("WeeklyChart", "Parse error: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    Log.e("WeeklyChart", "Week calculation error: " + e.getMessage());
                }

                entries.add(new BarEntry(weekIndex, (float) weekTotal));
                labels.add(label);
            }

            // ✅ FIX: Pad remaining positions to always show 4 weeks
            while (entries.size() < 5) {
                int weekNum = entries.size() + 1;
                entries.add(new BarEntry(entries.size(), 0));
                String emptyLabel = isBangla ? "সপ্তাহ " + weekNum : "Week " + weekNum;
                labels.add(emptyLabel);
            }

        } else if (type.equals("monthly")) {
            // ==================== MONTHLY: LAST 12 MONTHS ====================

            Calendar today = Calendar.getInstance();
            int currentMonth = today.get(Calendar.MONTH);
            int currentYear = today.get(Calendar.YEAR);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            Log.d("MonthlyChart", "=========== MONTHLY CHART ===========");

            for (int i = 11; i >= 0; i--) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, currentYear);
                cal.set(Calendar.MONTH, currentMonth);
                cal.add(Calendar.MONTH, -i);

                // Get month start and end
                Calendar monthStart = (Calendar) cal.clone();
                monthStart.set(Calendar.DAY_OF_MONTH, 1);

                Calendar monthEnd = (Calendar) cal.clone();
                monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));

                double monthTotal = 0;

                try {
                    Date startDate = sdf.parse(sdf.format(monthStart.getTime()));
                    Date endDate = sdf.parse(sdf.format(monthEnd.getTime()));

                    for (Spending s : spendingList) {
                        try {
                            Date spendingDate = sdf.parse(s.date);

                            if (spendingDate != null && startDate != null && endDate != null) {
                                if ((spendingDate.equals(startDate) || spendingDate.after(startDate)) &&
                                        (spendingDate.equals(endDate) || spendingDate.before(endDate))) {
                                    monthTotal += s.amount;
                                }
                            }
                        } catch (Exception e) {
                            Log.e("MonthlyChart", "Parse error: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    Log.e("MonthlyChart", "Month calculation error: " + e.getMessage());
                }

                entries.add(new BarEntry(11 - i, (float) monthTotal));
                String monthName = getLocalizedMonthName(cal.get(Calendar.MONTH));
                labels.add(monthName);
            }
        }

        // ==================== RENDER CHART ====================
        if (entries.isEmpty()) {
            barChart.clear();
            barChart.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, isBangla ? "খরচ" : "Spending");
        dataSet.setColor(getColor(android.R.color.holo_blue_bright));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.animateY(500);
        barChart.invalidate();

        // ==================== HIGHEST SPENDING ====================
        float maxValue = 0;
        int maxIndex = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getY() > maxValue) {
                maxValue = entries.get(i).getY();
                maxIndex = i;
            }
        }

        if (maxValue > 0) {
            layoutHighestSpending.setVisibility(View.VISIBLE);
            animateCardReveal(layoutHighestSpending);
            tvHighestSpendingDay.setText(labels.get(maxIndex) + " - ৳ " +
                    String.format(Locale.getDefault(), "%.0f", maxValue));
        } else {
            layoutHighestSpending.setVisibility(View.GONE);
        }
    }

    private void debugSpendingData() {
        Log.d("DEBUG_SPENDING", "========== SPENDING DATA DEBUG ==========");
        for (Spending s : spendingList) {
            Log.d("DEBUG_SPENDING", "ID: " + s.id);
            Log.d("DEBUG_SPENDING", "Amount: " + s.amount);
            Log.d("DEBUG_SPENDING", "Note: " + s.note);
            Log.d("DEBUG_SPENDING", "Date: '" + s.date + "'");
            Log.d("DEBUG_SPENDING", "Time: '" + s.time + "'");
            Log.d("DEBUG_SPENDING", "---");
        }
        Log.d("DEBUG_SPENDING", "Total items: " + spendingList.size());
    }

    // ==================== LOCALIZATION ====================
    private String getLocalizedDayName(int dayOfWeek, boolean shortForm) {
        if (isBangla) {
            switch (dayOfWeek) {
                case Calendar.SATURDAY: return "শনি";
                case Calendar.SUNDAY: return "রবি";
                case Calendar.MONDAY: return "সোম";
                case Calendar.TUESDAY: return "মঙ্গল";
                case Calendar.WEDNESDAY: return "বুধ";
                case Calendar.THURSDAY: return "বৃহ";
                case Calendar.FRIDAY: return "শুক্র";
            }
        } else {
            switch (dayOfWeek) {
                case Calendar.SATURDAY: return "Sat";
                case Calendar.SUNDAY: return "Sun";
                case Calendar.MONDAY: return "Mon";
                case Calendar.TUESDAY: return "Tue";
                case Calendar.WEDNESDAY: return "Wed";
                case Calendar.THURSDAY: return "Thu";
                case Calendar.FRIDAY: return "Fri";
            }
        }
        return "";
    }

    private String getLocalizedMonthName(int month) {
        if (isBangla) {
            switch (month) {
                case Calendar.JANUARY: return "জানুয়ারি";
                case Calendar.FEBRUARY: return "ফেব্রুয়ারি";
                case Calendar.MARCH: return "মার্চ";
                case Calendar.APRIL: return "এপ্রিল";
                case Calendar.MAY: return "মে";
                case Calendar.JUNE: return "জুন";
                case Calendar.JULY: return "জুলাই";
                case Calendar.AUGUST: return "আগস্ট";
                case Calendar.SEPTEMBER: return "সেপ্টেম্বর";
                case Calendar.OCTOBER: return "অক্টোবর";
                case Calendar.NOVEMBER: return "নভেম্বর";
                case Calendar.DECEMBER: return "ডিসেম্বর";
            }
        } else {
            switch (month) {
                case Calendar.JANUARY: return "January";
                case Calendar.FEBRUARY: return "February";
                case Calendar.MARCH: return "March";
                case Calendar.APRIL: return "April";
                case Calendar.MAY: return "May";
                case Calendar.JUNE: return "June";
                case Calendar.JULY: return "July";
                case Calendar.AUGUST: return "August";
                case Calendar.SEPTEMBER: return "September";
                case Calendar.OCTOBER: return "October";
                case Calendar.NOVEMBER: return "November";
                case Calendar.DECEMBER: return "December";
            }
        }
        return "";
    }

    // ==================== UPDATE SUMMARY ====================

    private void updateSummary() {
        dailyTotal = 0;
        monthlyTotal = 0;

        Calendar today = Calendar.getInstance();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.getTime());
        int currentMonth = today.get(Calendar.MONTH);
        int currentYear = today.get(Calendar.YEAR);

        for (Spending s : spendingList) {
            if (s.date.equals(todayDate)) {
                dailyTotal += s.amount;
            }

            try {
                Calendar spendingCal = Calendar.getInstance();
                spendingCal.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(s.date));
                if (spendingCal.get(Calendar.MONTH) == currentMonth && spendingCal.get(Calendar.YEAR) == currentYear) {
                    monthlyTotal += s.amount;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        animateSummaryUpdate(tvDailyAmount, "৳ " + String.format(Locale.getDefault(), "%.0f", dailyTotal));
        animateSummaryUpdate(tvMonthlyAmount, "৳ " + String.format(Locale.getDefault(), "%.0f", monthlyTotal));

        updateBadgeCount();
    }

    private void animateSummaryUpdate(TextView textView, String newText) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f);
        fadeOut.setDuration(150);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                textView.setText(newText);
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
                fadeIn.setDuration(150);
                fadeIn.start();
            }
        });
        fadeOut.start();
    }

    private void updateBadgeCount() {
        if (recycleBinList.isEmpty()) {
            tvBadgeCount.setVisibility(View.GONE);
        } else {
            tvBadgeCount.setVisibility(View.VISIBLE);
            tvBadgeCount.setText(String.valueOf(recycleBinList.size()));
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // ==================== DATA MODELS ====================

    static class Spending {
        int id; // Database ID
        double amount;
        String note, date, time;
        byte[] imageBytes;

        Spending(int id, double amount, String note, String date, String time, byte[] imageBytes) {
            this.id = id;
            this.amount = amount;
            this.note = note;
            this.date = date;
            this.time = time;
            this.imageBytes = imageBytes;
        }
    }

    // ==================== SPENDING ADAPTER ====================

    class SpendingAdapter extends RecyclerView.Adapter<SpendingAdapter.SpendingViewHolder> {

        ArrayList<Spending> list;

        SpendingAdapter(ArrayList<Spending> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public SpendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_spending, parent, false);
            return new SpendingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SpendingViewHolder holder, int position) {
            Spending s = list.get(position);

            holder.tvAmount.setText("৳ " + String.format(Locale.getDefault(), "%.0f", s.amount));
            holder.tvNote.setText(s.note);
            holder.tvDate.setText(s.date);
            holder.tvTime.setText(s.time);

            // ✅ ADD THIS: Image display logic
            if (s.imageBytes != null && s.imageBytes.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(s.imageBytes, 0, s.imageBytes.length);
                holder.ivItemImage.setImageBitmap(bitmap);
                holder.ivItemImage.setVisibility(View.VISIBLE);
            } else {
                holder.ivItemImage.setVisibility(View.GONE);
            }

            holder.itemView.setAlpha(0);
            holder.itemView.animate().alpha(1f).setDuration(300).start();

            holder.btnView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    showViewDetailsDialog(pos);
                }
            });

            holder.btnEdit.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    isEditMode = true;
                    editPosition = pos;
                    editEntityId = list.get(pos).id;
                    showAddSpendingDialog();
                }
            });

            holder.btnDelete.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    animateItemDeletion(holder.itemView, pos);
                }
            });
        }

        private void animateItemDeletion(View view, int pos) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(scaleX, alpha);
            set.setDuration(300);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Spending deletedSpending = list.get(pos);

                    // Database থেকে recycle bin এ move করুন
                    spendingDao.moveToRecycleBin(deletedSpending.id);

                    // Memory থেকে remove এবং recycle bin এ add
                    list.remove(pos);
                    recycleBinList.add(0, deletedSpending);

                    notifyItemRemoved(pos);
                    updateSummary();
                    updateChart("weekly");

                    if (list.isEmpty()) {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        animateCardReveal(emptyStateLayout);
                    }

                    showToast(isBangla ? "রিসাইকেল বিনে সরানো হয়েছে" : "Moved to recycle bin");
                }
            });
            set.start();
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class SpendingViewHolder extends RecyclerView.ViewHolder {
            TextView tvAmount, tvNote, tvDate, tvTime;
            ImageButton btnView, btnEdit, btnDelete;
            ImageView ivItemImage; // ✅ ADD THIS

            SpendingViewHolder(@NonNull View itemView) {
                super(itemView);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvNote = itemView.findViewById(R.id.tvNote);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvTime = itemView.findViewById(R.id.tvTime);
                btnView = itemView.findViewById(R.id.btnView);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
                ivItemImage = itemView.findViewById(R.id.ivItemImage); // ✅ ADD THIS
            }
        }
    }

    // ==================== RECYCLE BIN ADAPTER ====================

    class RecycleBinAdapter extends RecyclerView.Adapter<RecycleBinAdapter.RecycleBinViewHolder> {

        ArrayList<Spending> list;
        AlertDialog parentDialog;

        RecycleBinAdapter(ArrayList<Spending> list, AlertDialog parentDialog) {
            this.list = list;
            this.parentDialog = parentDialog;
        }

        @NonNull
        @Override
        public RecycleBinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_recycle_bin, parent, false);
            return new RecycleBinViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecycleBinViewHolder holder, int position) {
            Spending s = list.get(position);
            holder.tvAmount.setText("৳ " + String.format(Locale.getDefault(), "%.0f", s.amount));
            holder.tvNote.setText(s.note);
            holder.tvDate.setText(s.date);

            if (isBangla) {
                holder.btnRestore.setText("পুনরুদ্ধার করুন");
                holder.btnDeletePermanent.setText("স্থায়ীভাবে মুছুন");
            } else {
                holder.btnRestore.setText("Restore");
                holder.btnDeletePermanent.setText("Delete Permanently");
            }

            holder.btnRestore.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    animateItemRestore(pos);
                }
            });

            holder.btnDeletePermanent.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    showPermanentDeleteDialog(pos);
                }
            });
        }

        private void animateItemRestore(int pos) {
            Spending restoredSpending = list.get(pos);

            // Database এ restore করুন
            spendingDao.restoreFromRecycleBin(restoredSpending.id);

            // Recycle bin list থেকে remove করুন
            list.remove(pos);
            notifyItemRemoved(pos);

            // 🔥 Main activity এর data পুরো reload করুন
            MainActivity.this.runOnUiThread(() -> {
                loadDataFromDatabase(); // এটা সব কিছু refresh করে দেবে
                recyclerViewSpending.smoothScrollToPosition(0);
            });

            showToast(isBangla ? "পুনরুদ্ধার করা হয়েছে" : "Restored successfully");

            if (list.isEmpty()) {
                parentDialog.dismiss();
            }
        }

        private void showPermanentDeleteDialog(int pos) {
            String title = isBangla ? "স্থায়ীভাবে মুছে ফেলতে চান?" : "Delete Permanently?";
            String message = isBangla ?
                    "এটি স্থায়ীভাবে মুছে ফেলা হবে এবং পুনরুদ্ধার করা যাবে না।" :
                    "This will be permanently deleted and cannot be recovered.";
            String positiveBtn = isBangla ? "হ্যাঁ" : "Yes";
            String negativeBtn = isBangla ? "না" : "No";

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(positiveBtn, (dialog, which) -> {
                        animateItemDeletionBin(pos);
                    })
                    .setNegativeButton(negativeBtn, null)
                    .show();
        }

        private void animateItemDeletionBin(int pos) {
            Spending deletedSpending = list.get(pos);

            // Database থেকে permanently delete
            spendingDao.deletePermanently(deletedSpending.id);

            list.remove(pos);
            notifyItemRemoved(pos);
            showToast(isBangla ? "স্থায়ীভাবে মুছে ফেলা হয়েছে" : "Permanently deleted");

            if (list.isEmpty()) {
                parentDialog.dismiss();
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class RecycleBinViewHolder extends RecyclerView.ViewHolder {
            TextView tvAmount, tvNote, tvDate;
            MaterialButton btnRestore, btnDeletePermanent;

            RecycleBinViewHolder(@NonNull View itemView) {
                super(itemView);
                tvAmount = itemView.findViewById(R.id.tvRecycleAmount);
                tvNote = itemView.findViewById(R.id.tvRecycleNote);
                tvDate = itemView.findViewById(R.id.tvRecycleDate);
                btnRestore = itemView.findViewById(R.id.btnRestore);
                btnDeletePermanent = itemView.findViewById(R.id.btnDeletePermanent);
            }
        }
    }

    // ==================== DIALOG METHODS ====================

    // ==================== DIALOG METHODS - DATE & TIME PICKER ADDED ====================

    private void showAddSpendingDialog() {
        selectedImageUri = null;
        selectedImageBytes = null;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_spending, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextInputEditText etAmount = dialogView.findViewById(R.id.etAmount);
        TextInputEditText etNote = dialogView.findViewById(R.id.etNote);
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        TextView tvTime = dialogView.findViewById(R.id.tvTime);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvAutoAddedLabel = dialogView.findViewById(R.id.tvAutoAddedLabel);
        TextView tvSuggestionsLabel = dialogView.findViewById(R.id.tvSuggestionsLabel);
        TextView tvImageSectionLabel = dialogView.findViewById(R.id.tvImageSectionLabel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        btnRemoveImage = dialogView.findViewById(R.id.btnRemoveImage);
        ivImagePreview = dialogView.findViewById(R.id.ivImagePreview);
        imageLoadingProgress = dialogView.findViewById(R.id.imageLoadingProgress);

        // 📅 Calendar for date/time manipulation
        final Calendar selectedCalendar = Calendar.getInstance();

        // ==================== LANGUAGE UPDATE ====================
        if (isBangla) {
            tvDialogTitle.setText(isEditMode ? "খরচ সম্পাদনা করুন" : "নতুন খরচ যোগ করুন");
            tvAutoAddedLabel.setText("তারিখ ও সময় নির্বাচন করুন:");
            tvSuggestionsLabel.setText("দ্রুত নির্বাচন:");
            tvImageSectionLabel.setText("ছবি যোগ করুন (ঐচ্ছিক)");
            btnSave.setText("সংরক্ষণ করুন");
            btnCancel.setText("বাতিল");
            btnSelectImage.setText("ছবি নির্বাচন");
            btnRemoveImage.setText("মুছে ফেলুন");
        } else {
            tvDialogTitle.setText(isEditMode ? "Edit Spending" : "Add New Spending");
            tvAutoAddedLabel.setText("Select Date & Time:");
            tvSuggestionsLabel.setText("Quick Select:");
            tvImageSectionLabel.setText("Add Image (Optional)");
            btnSave.setText("Save");
            btnCancel.setText("Cancel");
            btnSelectImage.setText("Select Image");
            btnRemoveImage.setText("Remove");
        }

        // ==================== SUGGESTION CHIPS SETUP ====================
        Chip chipFood = dialogView.findViewById(R.id.chipFood);
        Chip chipBreakfast = dialogView.findViewById(R.id.chipBreakfast);
        Chip chipLunch = dialogView.findViewById(R.id.chipLunch);
        Chip chipDinner = dialogView.findViewById(R.id.chipDinner);
        Chip chipTeaSnacks = dialogView.findViewById(R.id.chipTeaSnacks);
        Chip chipGroceries = dialogView.findViewById(R.id.chipGroceries);
        Chip chipTransport = dialogView.findViewById(R.id.chipTransport);
        Chip chipMedicine = dialogView.findViewById(R.id.chipMedicine);
        Chip chipOther = dialogView.findViewById(R.id.chipOther);

        if (isBangla) {
            chipFood.setText("খাবার");
            chipBreakfast.setText("নাস্তা");
            chipLunch.setText("দুপুরের খাবার");
            chipDinner.setText("রাতের খাবার");
            chipTeaSnacks.setText("চা/স্ন্যাকস");
            chipGroceries.setText("বাজার");
            chipTransport.setText("যাতায়াত");
            chipMedicine.setText("ওষুধ");
            chipOther.setText("অন্যান্য");
        } else {
            chipFood.setText("Food");
            chipBreakfast.setText("Breakfast");
            chipLunch.setText("Lunch");
            chipDinner.setText("Dinner");
            chipTeaSnacks.setText("Tea/Snacks");
            chipGroceries.setText("Groceries");
            chipTransport.setText("Transport");
            chipMedicine.setText("Medicine");
            chipOther.setText("Other");
        }

        View.OnClickListener chipClickListener = v -> {
            Chip chip = (Chip) v;
            String currentText = etNote.getText() != null ? etNote.getText().toString() : "";
            if (currentText.isEmpty()) {
                etNote.setText(chip.getText().toString());
            } else {
                etNote.setText(currentText + ", " + chip.getText().toString());
            }
            etNote.setSelection(etNote.getText().length());
        };

        chipFood.setOnClickListener(chipClickListener);
        chipBreakfast.setOnClickListener(chipClickListener);
        chipLunch.setOnClickListener(chipClickListener);
        chipDinner.setOnClickListener(chipClickListener);
        chipTeaSnacks.setOnClickListener(chipClickListener);
        chipGroceries.setOnClickListener(chipClickListener);
        chipTransport.setOnClickListener(chipClickListener);
        chipMedicine.setOnClickListener(chipClickListener);
        chipOther.setOnClickListener(chipClickListener);

        // ==================== EDIT MODE CHECK ====================
        if (isEditMode && editPosition >= 0) {
            Spending spending = spendingList.get(editPosition);
            etAmount.setText(String.valueOf((int) spending.amount));
            etNote.setText(spending.note);
            tvDate.setText(spending.date);
            tvTime.setText(spending.time);

            // Parse existing date/time into calendar
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                selectedCalendar.setTime(dateFormat.parse(spending.date));
                Date timeDate = timeFormat.parse(spending.time);
                if (timeDate != null) {
                    Calendar timeCal = Calendar.getInstance();
                    timeCal.setTime(timeDate);
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                    selectedCalendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (spending.imageBytes != null) {
                selectedImageBytes = spending.imageBytes;
                Bitmap bitmap = BitmapFactory.decodeByteArray(spending.imageBytes, 0, spending.imageBytes.length);
                ivImagePreview.setImageBitmap(bitmap);
                ivImagePreview.setVisibility(View.VISIBLE);
                btnRemoveImage.setVisibility(View.VISIBLE);
            }
        } else {
            // Set current date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            tvDate.setText(dateFormat.format(selectedCalendar.getTime()));
            tvTime.setText(timeFormat.format(selectedCalendar.getTime()));
        }

        // ==================== 📅 DATE PICKER - CLICK TO CHANGE ====================
        tvDate.setOnClickListener(v -> {
            int year = selectedCalendar.get(Calendar.YEAR);
            int month = selectedCalendar.get(Calendar.MONTH);
            int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    MainActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        selectedCalendar.set(Calendar.YEAR, selectedYear);
                        selectedCalendar.set(Calendar.MONTH, selectedMonth);
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        tvDate.setText(dateFormat.format(selectedCalendar.getTime()));
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });

        // ==================== 🕐 TIME PICKER - CLICK TO CHANGE ====================
        tvTime.setOnClickListener(v -> {
            int hour = selectedCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = selectedCalendar.get(Calendar.MINUTE);

            android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                    MainActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        selectedCalendar.set(Calendar.MINUTE, selectedMinute);

                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        tvTime.setText(timeFormat.format(selectedCalendar.getTime()));
                    },
                    hour, minute, false // false = 12-hour format with AM/PM
            );

            timePickerDialog.show();
        });

        // ==================== IMAGE HANDLING ====================
        btnSelectImage.setOnClickListener(v -> showImagePickerOptions());

        btnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            selectedImageBytes = null;
            ivImagePreview.setVisibility(View.GONE);
            btnRemoveImage.setVisibility(View.GONE);
        });

        // ==================== SAVE BUTTON ====================
        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (amountStr.isEmpty()) {
                showToast(isBangla ? "দয়া করে পরিমাণ লিখুন" : "Please enter amount");
                return;
            }

            if (note.isEmpty()) {
                showToast(isBangla ? "দয়া করে বিবরণ লিখুন" : "Please enter description");
                return;
            }

            double amount = Double.parseDouble(amountStr);

            if (isEditMode && editPosition >= 0) {
                // Update existing spending
                Spending spending = spendingList.get(editPosition);
                spending.amount = amount;
                spending.note = note;
                spending.date = tvDate.getText().toString();
                spending.time = tvTime.getText().toString();

                if (selectedImageBytes != null && selectedImageBytes.length > 0) {
                    spending.imageBytes = selectedImageBytes;
                }

                SpendingEntity entity = new SpendingEntity(
                        amount,
                        note,
                        spending.date,
                        spending.time,
                        spending.imageBytes,
                        false
                );
                entity.id = spending.id;
                spendingDao.update(entity);

                adapter.notifyItemChanged(editPosition);
                showToast(isBangla ? "আপডেট সফল হয়েছে" : "Updated successfully");
            } else {
                // Add new spending
                SpendingEntity entity = new SpendingEntity(
                        amount,
                        note,
                        tvDate.getText().toString(),
                        tvTime.getText().toString(),
                        selectedImageBytes,
                        false
                );

                long id = spendingDao.insert(entity);

                Spending spending = new Spending(
                        (int) id,
                        amount,
                        note,
                        tvDate.getText().toString(),
                        tvTime.getText().toString(),
                        selectedImageBytes
                );

                spendingList.add(0, spending);
                adapter.notifyItemInserted(0);
                recyclerViewSpending.scrollToPosition(0);
                showToast(isBangla ? "সংরক্ষণ সফল হয়েছে" : "Saved successfully");
            }

            updateSummary();
            updateChart("weekly");
            emptyStateLayout.setVisibility(View.GONE);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void showViewDetailsDialog(int position) {
        Spending spending = spendingList.get(position);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_view_spending, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvAmount = dialogView.findViewById(R.id.tvViewAmount);
        TextView tvNote = dialogView.findViewById(R.id.tvViewNote);
        TextView tvDate = dialogView.findViewById(R.id.tvViewDate);
        TextView tvTime = dialogView.findViewById(R.id.tvViewTime);
        ImageView ivImage = dialogView.findViewById(R.id.ivViewImage);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnClose);

        if (isBangla) {
            tvTitle.setText("খরচের বিস্তারিত");
            btnClose.setText("বন্ধ করুন");
        } else {
            tvTitle.setText("Spending Details");
            btnClose.setText("Close");
        }

        tvAmount.setText("৳ " + String.format(Locale.getDefault(), "%.0f", spending.amount));
        tvNote.setText(spending.note);
        tvDate.setText(spending.date);
        tvTime.setText(spending.time);

        if (spending.imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(spending.imageBytes, 0, spending.imageBytes.length);
            ivImage.setImageBitmap(bitmap);
            ivImage.setVisibility(View.VISIBLE);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showRecycleBinDialog() {
        if (recycleBinList.isEmpty()) {
            showToast(isBangla ? "রিসাইকেল বিন খালি" : "Recycle bin is empty");
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_recycle_bin, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewRecycleBin);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnCloseRecycleBin);

        if (isBangla) {
            btnClose.setText("বন্ধ করুন");
        } else {
            btnClose.setText("Close");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecycleBinAdapter recycleBinAdapter = new RecycleBinAdapter(recycleBinList, dialog);
        recyclerView.setAdapter(recycleBinAdapter);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}