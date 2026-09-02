# 🛡️ EU Roaming Guard

Very early in development; just tested it on my own device, an A56. However should work on more OEMs; see below.

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
- 🇪🇺  **EU/EEA Defaults**: Pre-configured with all 30 EU & EEA member state Mobile Country Codes (MCC).
- ✏️ **Editable Whitelist**: Add custom countries/MCCs or toggle existing ones ON/OFF.
- 🔋 **Battery Efficient**: Uses native Android `TelephonyCallback` / `ServiceStateListener` event triggers instead of battery-draining continuous polling.

---

## Manufacturer Support:
- Samsung (One UI): data_roaming1, data_roaming2, data_roaming_0/1
- Google Pixel,
- Motorola, 
- Sony (Stock Android/AOSP): Settings.Global.DATA_ROAMING, dynamic data_roaming
- Xiaomi, Redmi, POCO (HyperOS / MIUI): data_roaming_slot1/2, data_roaming_mode
- OnePlus, OPPO, Realme (OxygenOS / ColorOS): oppo_data_roaming, data_roaming_sub1/2
- Huawei, Honor (EMUI / MagicOS): hw_data_roaming, hw_data_roaming_sim1/2
- Dual-SIM & eSIM: Automatically reads and updates all active SIM cards via SubscriptionManager.

## 📲 Installation & Setup
1. Transfer and install app-debug.apk on your Android device.
2. Open the app and grant the runtime permissions (Phone and Location are required by Android to read cell tower MCC codes).

---

### 🔑 One-Time Permission Setup (ADB)
Android restricts third-party apps from toggling system settings like Data Roaming without the WRITE_SECURE_SETTINGS permission. You only need to grant this permission once (it survives device reboots).

Choose one of the methods below to grant permission:

#### Option A: USB Cable (Linux / macOS / Windows Bash)
1. Enable Developer Options (Settings > About Phone > tap "Build Number" 7 times).
2. Enable USB Debugging inside Developer Options.
3. Connect your phone to your computer via USB.
4. Copy the ADB command string from the EU Roaming Guard
- **Run:**
  ```bash
  adb shell pm grant com.example.euroamingguard android.permission.WRITE_SECURE_SETTINGS

#### Option B: Wireless ADB from PC (No USB Cable)
1. Connect your phone and computer to the same Wi-Fi network.
2. On your phone, go to Developer Options > Wireless Debugging and turn it ON.
3. Tap "Pair device with pairing code" to view your IP, port, and 6-digit code.
4. On your PC terminal,
- **pair the device:**
  ```bash
  adb pair <PHONE_IP>:<PAIRING_PORT>
"# Enter the 6-digit code when prompted"

5. Connect to the device:
- **Run:**
  ```bash
  adb connect <PHONE_IP>:<CONNECT_PORT>
  
6. Grant the permission:
- **Run:**
  ```bash
  adb shell pm grant com.example.euroamingguard android.permission.WRITE_SECURE_SETTINGS

#### Option C: Directly on Phone without PC (Android 11+)
You can grant the permission entirely on your device using any ADB local shell app.
Using ADB app:
1. Install an ADB app from Google Play or GitHub.
2. Open Wireless Debugging in Settings > Developer Options in split-screen mode alongside LADB.
3. Tap "Pair device with pairing code" and enter the port and pairing code into ADB.
4. Copy the ADB command string from the EU Roaming Guard
- **Run:**
  ```bash
  pm grant com.example.euroamingguard android.permission.WRITE_SECURE_SETTINGS

Using Termux (with android-tools):
- **Run:**
  ```bash
  pkg install android-tools
  adb pair localhost:<PAIRING_PORT>
  adb connect localhost:<CONNECT_PORT>
  adb shell pm grant com.example.euroamingguard android.permission.WRITE_SECURE_SETTINGS

#### Option D: Rooted Devices
If your device is rooted with Magisk / KernelSU / APatch, open any terminal emulator on the device and run:
Copy the ADB command string from the EU Roaming Guard
- **Run:**
  ```bash
  su -c pm grant com.example.euroamingguard android.permission.WRITE_SECURE_SETTINGS

---

##  Verification
To verify that the permission has been granted:
- **Run:**
  ```bash
  adb shell dumpsys package com.example.euroamingguard | grep WRITE_SECURE_SETTINGS

Expected output: android.permission.WRITE_SECURE_SETTINGS: granted=true

---

## 📋 Default Pre-configured MCC List
Country	MCC	Default State	Country	MCC	Default State

🇦🇹 Austria	232	✅ Allowed	🇮🇹 Italy	222	✅ Allowed
🇧🇪 Belgium	206	✅ Allowed	🇱🇻 Latvia	247	✅ Allowed
🇧🇬 Bulgaria	284	✅ Allowed	🇱🇮 Liechtenstein (EEA)	295	✅ Allowed
🇭🇷 Croatia	219	✅ Allowed	🇱🇹 Lithuania	246	✅ Allowed
🇨🇾 Cyprus	280	✅ Allowed	🇱🇺 Luxembourg	270	✅ Allowed
🇨🇿 Czechia	230	✅ Allowed	🇲🇹 Malta	278	✅ Allowed
🇩🇰 Denmark	238	✅ Allowed	🇳🇱 Netherlands	204	✅ Allowed
🇪🇪 Estonia	248	✅ Allowed	🇳🇴 Norway (EEA)	242	✅ Allowed
🇫🇮 Finland	244	✅ Allowed	🇵🇱 Poland	260	✅ Allowed
🇫🇷 France	208	✅ Allowed	🇵🇹 Portugal	268	✅ Allowed
🇩🇪 Germany	262	✅ Allowed	🇷🇴 Romania	226	✅ Allowed
🇬🇷 Greece	202	✅ Allowed	🇸🇰 Slovakia	231	✅ Allowed
🇭🇺 Hungary	216	✅ Allowed	🇸🇮 Slovenia	293	✅ Allowed
🇮🇸 Iceland (EEA)	274	✅ Allowed	🇪🇸 Spain	214	✅ Allowed
🇮🇪 Ireland	272	✅ Allowed	🇸🇪 Sweden	240	✅ Allowed
🇨🇭 Switzerland	228	❌ Blocked	🇬🇧 United Kingdom	234 / 235	❌ Blocked
🇦🇩 Andorra	213	❌ Blocked	🇲🇨 Monaco	212	❌ Blocked

##  License
This project is open source and available under the MIT License.
