# OneBeacon Android library

## What is it?
This is a client library that scans for Bluetooth beacons and integrates with the OneBeacon cloud platform.

## How do I use it?
Download the project, which includes samples and the local Android library repository to get started.
You will need to create a developer account in our cloud console in order to use this library in your own apps. 
Please read the [Getting started wiki page](../wiki/Getting-started)

## How does it work?
The OneBeacon library scans for low energy Bluetooth devices. It can find:
- iBeacon / AltBeacon devices
- URI beacons
- - other BLE devices, however since they cannot be parsed only the MAC address, RSSI, and name will be available.

## How much battery does the scanning eat?
The scanning power consumption is optimized natively when running on Android 5.0 or newer, and emulated on older Android versions by pausing the scan. The SDK offers the possibility to change the current scanning mode between:
- low power (background mode)
- balanced
- low latency (useful if app is in foreground and fast beacon input is needed)

## Compatibility
The library is compatible with Android API 5 or newer. However it will only start the beacon scanning if the Android device supports this feature, which was introduced with Android 4.3, and if the hardware supports it.
