QuizApp – Android Quiz Application with Local SQLite Database
Overview
QuizApp is a self-contained Android application developed using Kotlin in Android Studio. It allows users to take quizzes, view results, and explore additional app features like maps, contact forms, and dashboards — all powered by a local SQLite database.
🚀 Key Features
- Multiple-choice quiz functionality
- Result calculation and display
- Local SQLite database for storing quiz data
- Google Maps integration
- Contact Us form
- Dashboard & additional activities
- Clean and simple UI using Material Design
🧑‍💻 Tech Stack
Language        : Kotlin
IDE             : Android Studio
Local Storage   : SQLite (via DBHelper.kt)
UI Components   : Material Design, XML layouts
Navigation      : Android Activities & Intents

1. Clone the Repository:
git clone https://github.com/Sreeram2710/QuizApp.git

2. Open in Android Studio:
- File → Open → Select the QuizApp project folder

3. Run the App:
- Connect an Android device or launch an emulator
- Click the green Run ▶️ button in Android Studio

4. Permissions:
- Allow necessary permissions (e.g., location for maps) during runtime
🗄️ Local Database (SQLite)
- All quiz questions, answers, and user data are stored in the local SQLite database
- Managed through DBHelper.kt
- No internet required — app runs completely offline
🧪 Testing
- Manual testing on emulator and physical Android devices
- Data persistence via SQLite
- Activity navigation flow and form validation tested
🧩 Future Enhancements
- Sync with a remote ASP.NET Core backend (optional)
- Leaderboard and scoring history
- Cloud backup using Firebase
- Multi-language support
- Email or export quiz results
🤝 Contribution
Pull requests are welcome! For suggestions or improvements, please open an issue first.
📄 License
This project is licensed under the MIT License.
👨‍💻 Developed By
SRIRAM MADHAMANCHI
Postgraduate IT Student | Android & .NET Developer
📍 Auckland, New Zealand

