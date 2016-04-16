# OneBeacon Android SDK

## What is this?
An Android library that scans for Bluetooth Low Energy beacons, parses them into known entities, and provides interfaces to them to the application. It can recognize the following beacon formats:
- Eddystone: URL, UID, EID, Telemetry, encrypted TLM
- iBeacon, AltBeacon
- Estimote Nearable
- URIBeacon
- any other generic BLE devices.

Unlike other SDK's out there, it has the following benefits:
- it's not tied or locked-in to a specific beacon manufacturer or protocol
- supports rotating MAC addresses, for beacons that are identifiable otherwise (iBeacon, Eddystone UID, Nearable...)
- doesn't require any developer account (unless you plan to use the cloud features, ofcourse)
- uses optimized parsing, saving battery life: no memory allocations occur while scanning the same beacons over time, and native Android 5.0 BLE API is used when available.

For iBeacon devices, there's also the optional **onebeacon-android-cloud** library, which allows attaching generic data to a beacon and synchronizing it to the cloud, namespaced to your own application package. It can be used to make beacons a data input source in your apps without the need of a dedicated web-service.

## How do I get started?
Clone this repository and check out the samples. The library is provided as a local Maven repository and the samples are ready to build as they are.

## Quick API help
Check the base-service sample and examine the **onebeacon-android** classes in your favorite IDE. There is a JavaDoc archive right next to the library, in case it's not picked up automatically you can attach it yourself.

### Interfaces to beacon sub-types
  - **Beacon** - base entity, having address, RSSI, samples count, and potential generic data
    - **EddystoneTelemetry** / **EddystoneEncryptedTLM** - telemetry info
    - **Rangeable** - a beacon that supports ranging and distance estimation
      - all Eddystone frame types: ***EddystoneEID*** / ***EddystoneUID*** / ***EddystoneURL***
      - **Apple_iBeacon** - a rangeable that contains a UUID, a major ID and a minor ID
      - ***Nearable***

### Monitors and beacons
A Monitor is responsible for dispatching back beacon events to your caller. You just set the desired callbacks on it, and start it, handling the callbacks, seeing if you already know about that beacon, what has happened to it, and so on.
A wrapper around this is the ***BeaconsMonitor*** utility class, which will provide you with a set of detected beacons, and translate back beacon events to more discrete methods which you can override.
To create and start a ***BeaconsMonitor*** all you have to do is subclass it and instantiate it with the current context. It literally takes a single line of code in your app to make it beacon-aware!

### How to do "ranging" and "filtering" like on iOS?
Ranging is simply done by handling the ***onBeaconChangedRange*** event on your BeaconsMonitor subclass. Since it sends out a Rangeable entity, you can check its previous range and the current range to emulate iOS's entering/exiting zones.
Filtering is not relevant under Android, because the native LE scanner will find always find and report all detected BLE packets for iBeacon or other custom beacon types, and it is not so useful to try and filter at that level. Instead, you should just ignore the beacons that you are not interested in, based on either their type or their properties.

## Library design
The library consists of:
- a background service that has two roles:
  - provider: does background LE scanning, parses data of discovered packets, manages beacons lifecycle;
  - client: communicates with the best provider available, and back to the app through the public API
  Usually the client and server are in the same process, so the data flows directly from the service back to the application, but IPC is supported if there's more than one app installed, signed with the same key and having the same declared special permission value.
- an optimized packet parsing engine
- implementations of Parcelable beacon types, each exposing its own API through an interface.
- a simple but powerful API that allows easily creating a Beacons Monitor and simply receiving callbacks when they are found or something about them changes (like their computed Range, estimated distance, RSSI, etc)

## How much battery does the scanning eat?
The scanning power consumption is optimized natively when running on Android 5.0 or newer, and emulated on older Android versions by pausing the scan. The SDK offers the possibility to change the current scanning mode between:
- low power (background mode)
- balanced
- low latency (useful if app is in foreground and fast beacon input is needed)

## How do I use the cloud features?
You will need to create a developer account in our cloud console in order to use this library in your own apps. 
Please read the [Getting started wiki page](https://github.com/Codefy/onebeacon-android/wiki/Getting-started)

## Compatibility
The library is compatible with Android API 5 or newer. However it will only start the beacon scanning if the Android device supports this feature, which was introduced with Android 4.3, and if the hardware supports it.

## Changelog
1.0.26 (April 15th 2016)
- added support for Eddystone EID and encrypted TLM frames
