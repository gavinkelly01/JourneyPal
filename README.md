# 🌍 JourneyPal – Your Travel Safety Companion

**Created by Gavin Kelly @ National College of Ireland**  

---

## ✈️ What is JourneyPal?

JourneyPal is a safety-first Android travel app built to support and empower travelers especially those from marginalized communities. Whether you're navigating unfamiliar streets, concerned about privacy in accommodations, or just trying to stay informed abroad, JourneyPal brings together powerful tools to help you stay secure and confident.

---

## 🔑 Core Features

- **📸 Hidden Camera Scanner**  
  Scan for hidden surveillance devices using your phone's camera, with or without a flashlight. Powered by Camera2API and OpenCV.

- **📁 Encrypted Document Storage**  
  Securely store important files like passports and medical records on-device with AES-GCM encryption.

- **🗺️ Country Safety Map**  
  Browse safety ratings by country with filters for concerns like LGBTQ+ rights, gender-based safety, and more.

- **📘 Travel & Safety Guide**  
  Get tips on local customs, scams to avoid, and how to stay safe while traveling.

- **💱 Currency Exchange Tracker**  
  Check real-time exchange rates and compare them to local rates to avoid scams.

- **🌐 Live Translation (Text & Camera)**  
  Break language barriers using real-time translation via text input or camera recognition (DeepL integration).

- **🚨 Emergency SOS Button**  
  In case of emergency, send your live GPS location to preselected contacts.

- **📰 Local Alerts**  
  Receive real-time, location-specific alerts (e.g., travel disruptions, events, incidents).

- **🌤️ Weather Forecast**  
  Access local weather forecasts, including UV index, wind speed, humidity, and more.

- **🔐 Secure User Accounts**  
  Register/login with Firebase Auth and gain access to encrypted file storage.

---

## 🎯 Why I Built JourneyPal

As a frequent traveler and member of the LGBTQ+ community, I wanted a tool that makes travel safer, more informed, and less stressful. JourneyPal addresses real safety concerns and enhances digital privacy — something I couldn't find in a single, trustworthy app.

---

## ⚙️ Functional Requirements

### ✅ Key User Features

1. Hidden Camera Detection  
2. Encrypted File Storage  
3. Country Safety Map  
4. Safety and Travel Guide  
5. Currency Exchange Checker  
6. Real-Time Translator (text and camera)  
7. SOS Emergency Contact System  
8. Real-Time Local Alerts  
9. Secure User Registration & Login  
10. Weather Forecast & UV Index  

---

## 🛠️ Tech Stack

| Category             | Technology / Tools                    |
|----------------------|---------------------------------------|
| Development          | Android Studio (Kotlin, XML)          |
| UI/UX                | XML Layouts, Icons8                   |
| Authentication       | Firebase Authentication               |
| Backend & DB         | Firebase Firestore                    |
| File Encryption      | AES-GCM, Android Keystore, BCrypt     |
| Location Services    | FusedLocationProvider, Google Maps    |
| Translation          | DeepL API                             |
| Exchange Rates       | ExchangeRate-API                      |
| Alerts/News          | RapidAPI                              |
| Camera Detection     | Camera2API, OpenCV                    |
| Weather Data         | Open-Meteo API                        |
| Async/Background     | Kotlin Coroutines, Lifecycle Scopes   |
| API Dev / Hosting    | IntelliJ IDEA, Render Hosting         |
| Testing              | Android Emulator (Upside Down Cake)   |

---

## 🧠 How JourneyPal Works

1. Create a secure user account or log in to access encrypted storage.
2. Store sensitive documents using encrypted storage.
3. Scan your environment for hidden cameras.
4. Use live translation and currency exchange tools abroad.
5. Receive local alerts and weather updates.
6. Hit the SOS button in an emergency to share your GPS location.

---

## 🔒 Security Features

- **AES-GCM Encryption** for sensitive documents  
- **BCrypt Password Hashing**  
- **Firebase Authentication** for secure access  
- **GPS via FusedLocationProvider** ensures efficient and accurate location tracking

---

## 📌 Final Thoughts

JourneyPal is more than a travel app — it’s a mobile companion designed to make travel safer and more inclusive. By combining real-time data, privacy tools, and safety guidance, JourneyPal helps you explore with peace of mind.

---

