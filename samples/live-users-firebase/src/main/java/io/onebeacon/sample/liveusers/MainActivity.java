package io.onebeacon.sample.liveusers;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.widget.EditText;

import java.util.Map;

import io.onebeacon.api.OneBeacon;
import io.onebeacon.api.ScanStrategy;
import io.onebeacon.cloud.api.AppBeacon;

public class MainActivity extends ActionBarActivity implements ServiceConnection {
    private MonitorService mService = null;
    private AlertDialog mNicknameDialog = null;
    private MainFragment mFragment;
    private Handler mHandler;

    Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            mFragment.refresh();
            mHandler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
        if (null == savedInstanceState) {
            mFragment = new MainFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, mFragment)
                    .commit();
        }

        if (!bindService(new Intent(this, MonitorService.class), this, BIND_AUTO_CREATE)) {
            setTitle("Bind failed! Manifest?");
        }
    }

    @Override
    protected void onDestroy() {
        OneBeacon.setScanStrategy(ScanStrategy.LOW_POWER);
        if (null != mService) {
            // optionally stop the service if running in background is not desired
//            stopService(new Intent(this, MonitorService.class));
            unbindService(this);
            mService = null;
        }
        if (null != mNicknameDialog) {
            mNicknameDialog.dismiss();
            mNicknameDialog = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        OneBeacon.setScanStrategy(ScanStrategy.LOW_LATENCY);

        mHandler.post(mRefreshRunnable);
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(mRefreshRunnable);
        OneBeacon.setScanStrategy(ScanStrategy.BALANCED);
        super.onPause();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = ((MonitorService.LocalServiceBinder) service).getService();
        startService(new Intent(this, MonitorService.class));

        final EditText view = new EditText(this);
        view.setHint("Enter your nickname...");

        mNicknameDialog = new AlertDialog.Builder(this)
                .setTitle("Nickname?")
                .setIcon(android.R.drawable.presence_online)
                .setCancelable(false)
                .setView(view)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nickname = view.getEditableText().toString();
                        mService.setNickName(nickname);
                    }
                })
                .show();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

    public Map<AppBeacon, MonitorService.FirebaseBeaconHolder> getLiveBeacons() {
        return null == mService ? null : mService.getFirebaseBeacons();
    }

    public void openBeaconFragment(AppBeacon appBeacon) {
        Fragment fragment = new BeaconFragment();
        Bundle args = new Bundle();
        args.putLong("id", appBeacon.getId());

        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.content, fragment)
            .addToBackStack(null)
            .commit();
    }

    public MonitorService.FirebaseBeaconHolder getLiveBeacon(long beaconId) {
        return null == mService ? null : mService.getFirebaseBeacon(beaconId);
    }

    public MonitorService getService() {
        return mService;
    }
}