# WiFi Security Auditor

**WiFi Security Auditor** is an Android application developed in **Kotlin** for a Mobile Computing course project. It helps users explore nearby Wi-Fi networks, understand their basic security settings, and inspect potential risks on the network they are currently using.

## Project Overview

The app combines Wi-Fi discovery with basic network-security checks in a mobile-friendly interface. Its goal is to make common wireless security indicators easier to understand, including encryption standards, signal strength, and signs of suspicious network behavior.

## Planned Features

### 1. Nearby Wi-Fi Network Scanner
- Discover nearby Wi-Fi networks, subject to Android permissions and scanning restrictions.
- Display available network details, such as SSID, signal strength (RSSI), and advertised security capabilities.
- Identify common security types, including **Open**, **WEP**, **WPA/WPA2**, and **WPA3** when exposed by the Android APIs.
- Highlight potentially weak or unencrypted networks.

### 2. Connected Network Information
- Show basic information about the current Wi-Fi connection, where permitted.
- Present the network's security and signal indicators in a readable format.

### 3. Basic Security Checks (Experimental)
- **ARP spoofing indicators:** Investigate changes or inconsistencies in local network information that *may* suggest suspicious activity, where technically accessible.
- **DNS configuration checks:** Inspect available DNS settings and optionally compare them with a user-defined trusted baseline to flag unexpected changes.
- Display findings as **informational warnings**, not definitive proof of an attack.

### 4. Security Dashboard
- Summarize scan results and security observations.
- Provide simple explanations and practical recommendations for safer Wi-Fi usage.

## Tech Stack

- **Language:** Kotlin
- **Platform:** Android
- **IDE:** Android Studio
- **Networking:** Android Wi-Fi and Connectivity APIs
- **UI:** Android UI components (final UI framework to be decided)

## Mobile Computing Concepts

This project demonstrates:
- Wireless network discovery and connectivity
- Mobile operating-system permissions and privacy restrictions
- Access to network metadata through mobile APIs
- Context-aware security feedback for mobile users
