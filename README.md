# আমার হিসাব (Amar Hisab) 📊

> **Smart Expense Manager** - Track your daily spending with ease, available in both Bengali and English.

## 📖 Introduction

**Amar Hisab** (আমার হিসাব) is a modern, bilingual expense tracking application designed specifically for Bengali-speaking users, with full English support. Built with Material Design 3 principles, this app provides an intuitive and beautiful interface for managing personal finances, featuring real-time charts, image attachments, and a built-in calculator.

The app solves the common problem of losing track of daily expenses by providing a simple, fast, and visually appealing way to record and analyze spending patterns. Whether you're tracking your morning tea costs or monthly grocery bills, Amar Hisab makes financial management accessible to everyone.

### 🎯 Why Amar Hisab?

- **Truly Bilingual**: Full support for Bengali (বাংলা) and English languages
- **Offline First**: Works completely offline with local database storage
- **Privacy Focused**: All data stays on your device - no cloud, no tracking
- **Beautiful UI**: Modern Material Design 3 with smooth animations
- **Feature Rich**: Beyond basic tracking with calculator, charts, and more

## 🎯 Objectives

### Primary Objectives

1. **Simplify Expense Tracking**
   - Enable users to record expenses in under 10 seconds
   - Provide quick-select categories for common expenses (Food, Transport, Medicine, etc.)
   - Auto-fill date and time with manual override options

2. **Data Visualization**
   - Display spending patterns through interactive bar charts
   - Show daily, weekly, and monthly spending trends
   - Highlight highest spending periods for better awareness

3. **Cultural Accessibility**
   - Provide full Bengali language support for local users
   - Use familiar currency symbols (৳) and date formats
   - Maintain bilingual consistency across all features

4. **Data Security & Privacy**
   - Store all data locally using Room Database
   - Implement soft-delete with recycle bin for accidental deletions
   - No internet permissions required - complete offline functionality

### Secondary Objectives

1. **Enhanced User Experience**
   - Support dark and light themes for comfort
   - Add smooth animations for better feedback
   - Enable image attachments for receipts/bills

2. **Utility Integration**
   - Built-in calculator with history for quick calculations
   - Easy data export capabilities (future)
   - Category-based expense analysis (future)

## ✨ Features

### 🏠 Core Features

#### 📝 Expense Management
- **Quick Add**: Add expenses with amount, description, date, and time
- **Image Attachments**: Attach photos of bills/receipts via camera or gallery
- **Smart Categories**: Pre-defined suggestion chips (Food, Transport, Medicine, Groceries, etc.)
- **Edit & Delete**: Full CRUD operations with undo via recycle bin
- **Date/Time Picker**: Customizable date and time selection with auto-fill

#### 📊 Data Visualization
- **Dashboard Cards**: Real-time daily and monthly spending summaries
- **Interactive Charts**: Bar charts with three views:
  - **Daily**: Sunday to Saturday breakdown
  - **Weekly**: Current month's week-by-week analysis (Week 1-5)
  - **Monthly**: Last 12 months trend
- **Highest Spending**: Automatic detection and display of peak spending periods
- **Empty State**: Friendly guidance when no data exists

#### 🎨 User Interface
- **Material Design 3**: Modern, clean, and intuitive interface
- **Dark Mode**: Full dark theme support with proper color schemes
- **Bilingual**: Seamless Bengali ↔ English language switching
- **Animations**: Smooth transitions, card reveals, and button feedback
- **Responsive**: Optimized for various screen sizes

#### 🗑️ Recycle Bin
- **Soft Delete**: Deleted expenses move to recycle bin first
- **Restore**: Recover accidentally deleted expenses
- **Permanent Delete**: Final deletion with confirmation dialog
- **Badge Counter**: Visual indicator of items in recycle bin

#### 🧮 Built-in Calculator
- **Standard Operations**: +, −, ×, ÷, % (percentage)
- **Calculation History**: Auto-save last 50 calculations
- **Timestamp**: Each calculation recorded with date/time
- **Clear History**: Option to clear all history with confirmation
- **Smooth UI**: Animated button presses and result display

### 🔧 Technical Features

#### 💾 Data Persistence
- **Room Database**: SQLite-based local storage
- **Auto-increment IDs**: Unique identifiers for each expense
- **BLOB Storage**: Images stored as byte arrays in database
- **Soft Delete Flag**: `isDeleted` column for recycle bin functionality

#### 🎯 User Preferences
- **SharedPreferences**: Persistent theme and language settings
- **State Preservation**: Settings survive app restarts
- **Calculator History**: Stored in preferences as serialized strings

#### 📱 Permissions
- **Camera**: For capturing receipt photos (optional)
- **Storage**: For selecting images from gallery (optional)
- **Runtime Permissions**: Proper permission handling for Android 6.0+


## 🛠️ Technology Stack

### **Frontend**

| Technology | Purpose | Version |
|------------|---------|---------|
| **XML Layouts** | UI structure | - |
| **ConstraintLayout** | Responsive layouts | 2.1+ |
| **Material Design 3** | UI components | Latest |
| **RecyclerView** | List displays | 1.3+ |
| **CardView** | Material cards | 1.0+ |

### **Backend**

| Technology | Purpose | Version |
|------------|---------|---------|
| **Java** | Programming language | 11 |
| **Android SDK** | Platform framework | API 24+ (Android 7.0+) |
| **Room Database** | Local persistence | 2.6.1 |
| **SQLite** | Database engine | Built-in |

### **Libraries & Dependencies**

```gradle
// Chart Library
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'

// Room Database
implementation 'androidx.room:room-runtime:2.6.1'
annotationProcessor 'androidx.room:room-compiler:2.6.1'

// Material Components
implementation 'com.google.android.material:material:1.11.0'

// AndroidX Core
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

### **Development Tools**

| Tool | Purpose | Version |
|------|---------|---------|
| **Android Studio** | IDE | Hedgehog+ |
| **Gradle** | Build system | 8.0+ |
| **Git** | Version control | 2.x |
| **JDK** | Java compiler | 11+ |

### **Architecture**

```
├── Presentation Layer
│   ├── Activities (MainActivity, CalculatorActivity)
│   ├── Adapters (SpendingAdapter, RecycleBinAdapter)
│   └── XML Layouts
│
├── Data Layer
│   ├── Room Database (AppDatabase)
│   ├── DAO (SpendingDao)
│   ├── Entities (SpendingEntity)
│   └── SharedPreferences (User settings)
│
└── Utility Layer
    ├── Date/Time formatters
    ├── Image handling (Camera/Gallery)
    └── Animation helpers

### **Design Patterns**

- **Singleton Pattern**: Database instance
- **ViewHolder Pattern**: RecyclerView optimization
- **Observer Pattern**: UI updates on data changes
- **Factory Pattern**: Activity launchers

## 📦 Project Structure

AmarHisab/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/amarhisab/
│   │   │   │   ├── MainActivity.java           # Main expense screen
│   │   │   │   ├── CalculatorActivity.java     # Built-in calculator
│   │   │   │   ├── AppDatabase.java            # Room database
│   │   │   │   ├── SpendingEntity.java         # Data model
│   │   │   │   ├── SpendingDao.java            # Database operations
│   │   │   │   └── *Adapter.java               # RecyclerView adapters
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── activity_calculator.xml
│   │   │   │   │   ├── dialog_*.xml            # Dialog layouts
│   │   │   │   │   └── item_*.xml              # RecyclerView items
│   │   │   │   │
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml              # Color palette
│   │   │   │   │   ├── strings.xml             # Bilingual strings
│   │   │   │   │   └── themes.xml              # Material themes
│   │   │   │   │
│   │   │   │   └── drawable/                   # Icons & gradients
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── test/                               # Unit tests (future)
│   │
│   └── build.gradle                            # App dependencies
│
├── gradle/                                     # Gradle wrapper
├── build.gradle                                # Project-level build
├── settings.gradle                             # Project settings
└── README.md                                   # This file

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: Version 11 or higher
- **Android SDK**: API Level 24 (Android 7.0) minimum
- **Gradle**: 8.0 or higher (included in project)

## 📱 Usage Guide

### Adding an Expense

1. Tap the **"খরচ যোগ করুন"** (Add Spending) FAB button
2. Enter amount (৳)
3. Add description or select from quick suggestions
4. Optionally attach a receipt image
5. Adjust date/time if needed
6. Tap **"সংরক্ষণ করুন"** (Save)

### Viewing Charts

1. Scroll to the **"খরচের পরিসংখ্যান"** (Statistics) card
2. Toggle chart visibility with the dropdown icon
3. Switch between Daily, Weekly, Monthly views
4. View highest spending period in the highlighted card

### Using Calculator

1. Tap the calculator icon in the toolbar
2. Perform calculations as needed
3. View history by tapping the history icon
4. Clear history from the history dialog

### Changing Language

1. Tap the language toggle icon (🌐) in the toolbar
2. Interface switches between Bengali ↔ English instantly
3. Setting persists across app restarts

### Switching Theme

1. Tap the theme toggle icon (☀️/🌙) in the toolbar
2. App switches between Light ↔ Dark mode
3. App restarts automatically to apply theme


## 🎨 Design Philosophy

### Color Scheme

**Light Mode**:
- Primary: Sky Blue (`#64B5FF`)
- Secondary: Cyan (`#4FD9E8`)
- Error: Soft Red (`#FF9999`)
- Background: Light Gray (`#F5F7FA`)

**Dark Mode**:
- Primary: Sky Blue (`#64B5FF`)
- Secondary: Cyan (`#4FD9E8`)
- Error: Soft Red (`#FF9999`)
- Background: Deep Blue (`#0A1F3F`)

### Typography

- **Headers**: Bold, 18-24sp
- **Body**: Regular, 14-16sp
- **Captions**: Light, 11-13sp
- **Font**: System default (Roboto)

### Spacing & Elevation

- **Card Radius**: 16-24dp
- **Card Elevation**: 8-16dp
- **Padding**: 16-24dp (outer), 12-18dp (inner)
- **Margins**: 8-20dp between elements


## 🔄 Roadmap

### Version 1.0 (Current) ✅
- [x] Basic expense tracking
- [x] Daily/Monthly summaries
- [x] Chart visualization
- [x] Bilingual support
- [x] Dark mode
- [x] Recycle bin
- [x] Built-in calculator

### Version 1.5 (Planned) 🚧
- [ ] Category-based expense analysis
- [ ] Budget setting & alerts
- [ ] Export to CSV/PDF
- [ ] Monthly/Yearly reports
- [ ] Recurring expenses
- [ ] Search & filter

### Version 2.0 (Future) 🔮
- [ ] Cloud backup (optional)
- [ ] Multi-currency support
- [ ] Split expenses
- [ ] Income tracking
- [ ] Financial goals
- [ ] Widget support
- [ ] Wear OS companion app


## 🤝 Contributing

Contributions are welcome! Here's how you can help:

### Reporting Bugs

1. Check if the issue already exists
2. Create a new issue with:
   - Clear title and description
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots if applicable
   - Device & Android version

### Suggesting Features

1. Open an issue with the "enhancement" label
2. Describe the feature in detail
3. Explain why it would be useful
4. Include mockups if possible

### Pull Requests

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow standard Java conventions
- Use meaningful variable names
- Add comments for complex logic
- Keep functions focused and small
- Write in both Bengali and English for user-facing strings

**## 🙏 Acknowledgments**

- **MPAndroidChart**: Beautiful chart library by Philipp Jahoda
- **Material Design**: Google's design system for Android
- **Room Database**: Jetpack persistence library
- **Bengali Community**: For inspiration and feedback
- **Stack Overflow**: For countless solutions during development


**##Screenshots**


### 🏠 Home Screen
<img width="202" height="438" alt="image" src="https://github.com/user-attachments/assets/d523452f-014f-4d17-8f46-7b66a5c269cb" />

<img width="190" height="437" alt="image" src="https://github.com/user-attachments/assets/165a634d-956d-4b99-9a58-8bf5d6eba877" />


### 🧮 Calculator
<img width="200" height="440" alt="image" src="https://github.com/user-attachments/assets/b834322e-848b-4e1e-9fed-f4e916822505" />

<img width="199" height="442" alt="image" src="https://github.com/user-attachments/assets/6039dbe3-2a6e-44c0-8435-fe28b191fd5e" />


### 🗑️ Recycle Bin
<img width="199" height="418" alt="image" src="https://github.com/user-attachments/assets/d6a30394-5f48-4dca-a589-19364fa8f73a" />


