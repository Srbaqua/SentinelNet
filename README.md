# SentinelAI 🔐  
### Mobile App Vulnerability Scanner

SentinelAI is an Android security analysis tool that scans installed applications and identifies potential privacy and security risks based on permission usage and network access patterns.

The system analyzes app permissions, detects suspicious combinations, assigns a security score, and provides mitigation recommendations to help users understand and improve their mobile security posture.

---

## 🚀 Features

### 🔎 App Security Scanner
Scans all installed applications on the device and collects metadata such as:
- App name
- Package name
- Requested permissions

---

### 📊 Security Score System
Each application receives a **security score (0–100)** based on the risk level of its permissions.

| Score Range | Risk Level |
|-------------|------------|
| 85 – 100 | Safe |
| 70 – 85 | Moderate Risk |
| 0 – 70 | High Risk |

---

### ⚠ Threat Detection
SentinelAI identifies suspicious applications based on:

- High-risk permission combinations
- Network access + sensitive permissions
- Excessive permission requests

Detected threats are displayed in the **Threat Center**.

---

### 🌐 Network Risk Detection
Apps that combine **internet access with sensitive permissions** are flagged.

Example:
Internet + Location
Internet + Contacts
Internet + Camera

These combinations may indicate potential data exposure risks.

---

### 📈 Security Dashboard
The home dashboard provides a complete overview of device security including:

- Privacy Score
- Total Apps Scanned
- High Risk Apps
- Threat Alerts
- Risk Distribution Charts

---

### 📊 Security Analytics
The Reports section provides deeper insights through visual analytics:

- Risk distribution charts
- Permission usage heatmap
- Top risky apps

---

### 🧠 Mitigation Advisor
For each detected risk, SentinelAI provides actionable security recommendations such as:

- Disable unnecessary permissions
- Restrict location access
- Review contacts or microphone permissions
- Avoid apps requesting excessive access

---

## 📱 Application Structure

The application is organized into four main sections:

| Section | Purpose |
|------|------|
| **Home** | Security overview and dashboard |
| **Apps** | Installed applications and security scores |
| **Threats** | Suspicious apps and detected vulnerabilities |
| **Reports** | Security analytics and charts |

---

## 🏗 System Architecture
Installed Apps ➡️
Permission Extraction ➡️
Risk Analysis Engine ➡️
Security Score Calculation ➡️
Threat Detection \
➡️ Security Dashboard + Reports

---

## ⚙️ Technologies Used

- **Android (Kotlin)**
- **Jetpack Compose** – Modern UI toolkit
- **Material Design 3**
- **Android PackageManager API**
- **Custom Risk Analysis Engine**
- **Data Visualization Components**

---

## 🧩 Key Components

### AppScanner
Extracts installed app metadata and permissions.

### RiskAnalyzer
Evaluates permission risk and calculates security scores.

### NetworkAnalyzer
Detects network-related security risks.

### SuspiciousAppDetector
Identifies suspicious applications based on risk patterns.

### MitigationAdvisor
Provides actionable recommendations for reducing security risks.


## 🔧 Installation

1. Clone the repository

```bash
git clone https://github.com/Srbaqua/SentinelNet.git
Open the project in Android Studio

Build and run on an Android device or emulator.
```
## 🤝 Contributing
Pull requests are welcome! If you have suggestions, feel free to open an issue. \
## 👤 Author

**Saurabh Chaudhary**
B.Tech CSE, NIT Hamirpur
