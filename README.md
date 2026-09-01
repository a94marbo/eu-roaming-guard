# 🛡️ EU Roaming Guard

An automated Android utility designed to prevent unexpected "bill shock" by instantly managing Mobile Data Roaming based on the connected Mobile Network Operator's country code (MCC).

---

## 📖 The Problem & What This App Does

Under European Union regulations ("Roam Like at Home"), mobile data used within EU/EEA member states is included in domestic plans at no extra charge. However, non-EU European countries (such as **Switzerland**, **Andorra**, or **Monaco**) and border areas are **not** covered by EU roaming caps, often resulting in exorbitant data roaming fees.

**Typical Scenario:**
> You are skiing in the Italian Alps (Cervinia / Zermatt border). Your phone switches from an Italian carrier (TIM/Vodafone Italy – MCC `222`) to a Swiss carrier (Swisscom/Salt – MCC `228`). Without warning, background data synchronization incurs massive roaming charges.

**The Solution:**
**EU Roaming Guard** runs a lightweight background monitoring service that listens to cell tower handovers. 
- When connected to an **allowed EU/EEA carrier**, Data Roaming is automatically **enabled**.
- The second your device handovers to a **non-whitelisted carrier (e.g., Switzerland)**, Data Roaming is instantly **disabled** at the system level before data leaks occur.

---

## ✨ Features

- ⚡ **Instant Roaming Toggling**: Switches global data roaming ON/OFF in real time upon cell tower handover.
- 🇪🇺 **EU/EEA Defaults**: Pre-configured with all 30 EU & EEA member state Mobile Country Codes (MCC).
- ✏️ **Editable Whitelist**: Add custom countries/MCCs or toggle existing ones ON/OFF.
- 🔋 **Battery Efficient**: Uses native Android `TelephonyCallback` / `ServiceStateListener` event triggers instead of battery-draining continuous polling.
- 🎨 **Modern Material 3 UI**: Real-time status display showing connected carrier name, MCC, and roaming state.

---

## 🛠️ How to Compile

### Method 1: Automatic Build via GitHub Actions (Recommended)
This repository includes a pre-configured GitHub Actions workflow (`.github/workflows/build.yml`).
1. Push this repository to GitHub.
2. Go to the **Actions** tab on your GitHub repository.
3. Click the latest workflow run and download the **`EURoamingGuard-APK`** artifact.

---

### Method 2: Command Line (Gradle)

**Prerequisites:** JDK 17+ and Android SDK installed.

- **Linux / macOS (Bash):**
  ```bash
  chmod +x gradlew
  ./gradlew assembleDebug.
