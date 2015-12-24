package io.onebeacon.sample.liveusers;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

import io.onebeacon.api.Beacon;
import io.onebeacon.api.OneBeacon;
import io.onebeacon.api.Range;
import io.onebeacon.cloud.api.AppBeacon;
import io.onebeacon.cloud.api.AppBeaconEvents;
import io.onebeacon.cloud.api.CloudListener;

/**
 * Monitor Service
 */
public class MonitorService extends Service implements CloudListener {
    private static final String FIREBASE_URL = "https://fiery-torch-4066.firebaseio.com/";
    private Firebase mFirebase = null;
    private String mNickname = null;

    /** Dummy binder that returns the actual service implementation for direct access to it
     * - only works from inside the same process! */
    class LocalServiceBinder extends Binder {
        MonitorService getService() {
            return MonitorService.this;
        }
    }

    private final LocalServiceBinder mBinder = new LocalServiceBinder();

    private boolean mServiceStarted = false;
    private Firebase mFirebaseBeaconsRef = null;

    @Override
    public IBinder onBind(Intent intent) {
        log("onBind");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        log("onDestroy");

        // unregister from beacons API
        OneBeacon.getCloud().removeListener(this);

        mServiceStarted = false;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand");

        if (!mServiceStarted) {
            Firebase.setAndroidContext(this);
            if (null == mFirebaseBeaconsRef) {
                mFirebase = new Firebase(FIREBASE_URL);
                mFirebaseBeaconsRef = mFirebase.child("fire-beacons");
            }

            mFirebase.authAnonymously(new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    enableBeaconScan();
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    Toast.makeText(MonitorService.this, "Auth failed - " + firebaseError, Toast.LENGTH_LONG).show();
                }
            });

            mServiceStarted = true;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void enableBeaconScan () {
        OneBeacon.getCloud().addListener(this);
        OneBeacon.enableCloudServices(this);

        String userId = mFirebase.getAuth().getUid();
        mFirebase.child("buzz").child(userId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Vibrator vibratorService = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibratorService.vibrate(new long[]{
                        500, 500
                }, 0);

                Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();
                String nickname = (String) data.get("nickname");
                if (null == nickname) nickname = dataSnapshot.getKey();
                Toast.makeText(MonitorService.this, "BUZZ from " + nickname, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Vibrator vibratorService = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibratorService.cancel();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public void onPrepareEvents(AppBeaconEvents appBeaconsEvent) {

    }

    private Map<AppBeacon, FirebaseBeaconHolder> mLiveBeacons = new HashMap<>();

    @Override
    public void onAppBeaconEvent(AppBeacon appBeacon, int flags) {
        if (flags == Beacon.FLAG_REMOVED) {
            mLiveBeacons.remove(appBeacon);
        }
        else {
            // beacon changed range
            byte range = appBeacon.getBeacon().getRange();
            FirebaseBeaconHolder holder = mLiveBeacons.get(appBeacon);
            if (null != holder) {
                holder.update();
                if (range == 0) {
                    mLiveBeacons.remove(appBeacon);
                }
            } else {
                if (range > 0) {
                    holder = new FirebaseBeaconHolder(appBeacon);
                    mLiveBeacons.put(appBeacon, holder);
                    holder.update();
                }
            }
        }
    }

    public Map<AppBeacon, FirebaseBeaconHolder> getFirebaseBeacons() {
        return mLiveBeacons;
    }

    public FirebaseBeaconHolder getFirebaseBeacon(long beaconId) {
        for (FirebaseBeaconHolder holder : mLiveBeacons.values()) {
            if (holder.mAppBeacon.getId() == beaconId) {
                return holder;
            }
        }
        return null;
    }

    public void buzzUser(String user) {
        String userId = mFirebase.getAuth().getUid();
        Map<String, Object> values = new HashMap<>();
        if (mNickname != null) {
            values.put("nickname", mNickname);
        }
        mFirebase.child("buzz").child(user).child(userId).setValue(values);
    }

    public void unbuzzUser(String user) {
        String userId = mFirebase.getAuth().getUid();
        mFirebase.child("buzz").child(user).child(userId).removeValue();
    }

    public void setNickName(String nickname) {
        mNickname = nickname;
    }

    private void log(String msg) {
        Log.d("MonitorService", msg);
    }

    class FirebaseBeaconHolder {
        private Firebase mSelfUserRef;
        private AppBeacon mAppBeacon;
        private final Firebase mBeaconUsersRef;

        public FirebaseBeaconHolder(AppBeacon appBeacon) {
            mAppBeacon = appBeacon;
            String userId = mFirebase.getAuth().getUid();
            mBeaconUsersRef = mFirebaseBeaconsRef.child(String.valueOf(appBeacon.getId())).child("users");
            mSelfUserRef = mBeaconUsersRef.child(userId);
            mSelfUserRef.onDisconnect().removeValue();
        }

        public void update() {
            byte range = mAppBeacon.getBeacon().getRange();

            if (Range.UNKNOWN == range) {
                mSelfUserRef.removeValue();
            }
            else {
                Map<String, Object> values = new HashMap<>();
                if (mNickname != null) {
                    values.put("nickname", mNickname);
                }
                values.put("range", range);
                mSelfUserRef.setValue(values);
            }
        }

        Firebase getBeaconUsersRef() {
            return mBeaconUsersRef;
        }
    }
}