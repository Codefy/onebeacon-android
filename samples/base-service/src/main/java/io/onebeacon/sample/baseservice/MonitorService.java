package io.onebeacon.sample.baseservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.onebeacon.api.Beacon;
import io.onebeacon.api.BeaconListener;
import io.onebeacon.api.Monitor;
import io.onebeacon.api.OneBeacon;
import io.onebeacon.api.ScanListener;
import io.onebeacon.api.ScanState;
import io.onebeacon.api.spec.Apple_iBeacon;
import io.onebeacon.cloud.api.AppBeacon;
import io.onebeacon.cloud.api.AppBeaconEvents;
import io.onebeacon.cloud.api.CloudListener;

/**
 * Monitor Service
 */
public class MonitorService extends Service
implements ScanListener, BeaconListener, CloudListener {

    /** Dummy binder that returns the actual service implementation for direct access to it
     * - only works from inside the same process! */
    class LocalServiceBinder extends Binder {
        MonitorService getService() {
            return MonitorService.this;
        }
    }

    private final LocalServiceBinder mBinder = new LocalServiceBinder();

    /** All ranged beacons **/
    private Set<Beacon> mBeacons = new HashSet<>();
    private boolean mServiceStarted = false;
    private Monitor mBeaconsMonitor = null;

    @Override
    public IBinder onBind(Intent intent) {
        log("onBind");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        log("onDestroy");

        // unregister from beacons API
        if (null != mBeaconsMonitor) {
            OneBeacon.getCloud().removeListener(this);

            mBeaconsMonitor.stop();
            mBeaconsMonitor = null;
        }

        mBeacons.clear();
        mServiceStarted = false;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand");

        if (!mServiceStarted) {
            startup();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void startup() {
        enableBeaconScan();
        mServiceStarted = true;
    }

    Collection<Beacon> getBeacons() {
        return mBeacons;
    }

    @Override
    public void onBeaconEvent(Beacon beacon, int flags) {
        if (beacon.getType() == Beacon.TYPE_APPLE_IBEACON) {
            Apple_iBeacon iBeacon = (Apple_iBeacon) beacon;
            int maj = iBeacon.getMajor();
            int min = iBeacon.getMinor();
            UUID uuid = iBeacon.getPrettyUUID();
        }
        if (flags == FLAG_REMOVED) {
            mBeacons.remove(beacon);
        }
        else {
            if (mBeacons.add(beacon)) {
                // new beacon
            } else {
                // updated beacon
            }
        }
    }

    @Override
    public void onScanStateChanged(int scanState, int flags) {
        if (ScanState.STATE_STOPPED == scanState) {
            // scan if off
            if (0 != (flags & ScanListener.FLAG_BLUETOOTH)) {
                // BT is off
            }
        }
        else {
            // scan started
        }
    }

    private void enableBeaconScan () {
        if (null == mBeaconsMonitor) {
            OneBeacon.init(this);
            OneBeacon.getCloud().addListener(this);

            mBeaconsMonitor = OneBeacon.createMonitor()
                .setBeaconListener(this)
                .setScanListener(this)
                .start(this);
        }
    }

    @Override
    public void onPrepareEvents(AppBeaconEvents appBeaconsEvent) {

    }

    @Override
    public void onAppBeaconEvent(AppBeacon appBeacon, int flags) {
        if (flags == FLAG_REMOVED) {

        }
        else {
            // app beacons changed range
        }
    }

    private void log(String msg) {
        Log.d("MonitorService", msg);
    }
}