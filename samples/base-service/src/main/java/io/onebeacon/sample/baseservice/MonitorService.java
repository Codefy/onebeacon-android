package io.onebeacon.sample.baseservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Collection;

import io.onebeacon.api.Beacon;
import io.onebeacon.api.BeaconsMonitor;

/**
 * Monitor Service
 */
public class MonitorService extends Service {
    /** Simple binder that returns the in-process service instance */
    class LocalServiceBinder extends Binder {
        MonitorService getService() {
            return MonitorService.this;
        }
    }

    private final LocalServiceBinder mBinder = new LocalServiceBinder();

    private boolean mServiceStarted = false;
    private BeaconsMonitor mBeaconsMonitor = null;

    @Override
    public IBinder onBind(Intent intent) {
        log("onBind");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand");

        if (!mServiceStarted) {
            if (null == mBeaconsMonitor) {
                // create and start a new beacons monitor, subclassing a few callbacks
                mBeaconsMonitor = new MyBeaconsMonitor(this);
            }
            mServiceStarted = true;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        log("onDestroy");

        // unregister from beacons API
        if (null != mBeaconsMonitor) {
            mBeaconsMonitor.close();
            mBeaconsMonitor = null;
        }

        mServiceStarted = false;

        super.onDestroy();
    }

    /** Return all the known beacons, for example to be bound to an adapter for displaying them **/
    public Collection<Beacon> getBeacons() {
        return mBeaconsMonitor.getBeacons();
    }

    private void log(String msg) {
        Log.d("MonitorService", msg);
    }
}