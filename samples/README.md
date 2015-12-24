# OneBeacon Android SDK samples

## Setup
Building the samples requires Android SDK and is done through Gradle build system. You can also use Android Studio to **import** the project and compile it using the IDE.
Note that the **OneBeacon** library is referenced in the **build.gradle** file from a local repository, relative to the sample directory. If you plan to create your own app to use the library you should modify your build script accordingly.

### Sample 1: base-service
Example of a simple background service that detects beacons. This sample depends on the **onebeacon-android** library, without any need to create or declare an application key in your app manifest. Consists of three components:
- **MonitorService** - a simple Android **Service** that creates a new **MyBeaconsMonitor** when it (re)starts;
- **MainActivity** - binds and starts the **MonitorService**, and also changes the background scanning strategy depending on the activity state:
  - onResume: ScanStrategy.LOW_LATENCY;
  - onPause: ScanStrategy.BALANCED;
  - onDestroy: ScanStrategy.LOW_POWER;
- **MyBeaconsMonitor** - a custom subclass of the SDK's **BeaconsMonitor** utility class. The superclass (provided by the SDK) starts the internal beacon scanning service, manages the beacon events coming from the OneBeacon service, and calls some protected methods which subclasses can override:
  - onBeaconAdded (Beacon beacon) - called when a new beacon is detected
  - onBeaconChanged***Property*** (Beacon beacon) - called when a beacon changes some property


### Sample 2: live-users-firebase
This simple app demonstrates the use of a basic OneBeacon cloud API feature: mapping of found beacons to unique entities in the current application's namespace. Because the sample requires accessing the OneBeacon cloud functionality it depends on the **onebeacon-android-cloud** library, and declares an appKey in its **AndroidManifest.xml** file.

The sample also uses Firebase to display a list of realtime users near each beacon around, with their current proximity, and buzz-ing them by tapping on their name. Tapping again stops the buzz.
