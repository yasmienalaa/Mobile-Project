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

## Important Limitations

Android restricts Wi-Fi scanning and access to some network details. Actual behavior depends on the Android version, device hardware, granted permissions, and system settings. Scanning may require location-related permissions and enabled location services; newer Android versions also have additional nearby-device permission rules depending on the API used.

**ARP spoofing cannot be reliably confirmed by a normal, unrooted Android app using public APIs alone.** DNS changes are also not automatically malicious: VPNs, private DNS, DHCP, and network administrators can legitimately affect DNS configuration. The app will therefore treat these checks as experimental and report uncertainty rather than claiming confirmed attacks.

## Project Status

🚧 **Under development** — features and implementation details may change during development.

## Educational Purpose

WiFi Security Auditor is intended for **learning, awareness, and authorized network assessment only**. It does not attempt to attack networks or bypass their security.
