package io.onebeacon.sample.liveusers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.onebeacon.cloud.api.AppBeacon;

public class MainFragment extends Fragment implements AdapterView.OnItemClickListener {
    private Map<AppBeacon, MonitorService.FirebaseBeaconHolder> mBeacons = null;
    private List<AppBeacon> mList;
    private Map<AppBeacon, BeaconUsers> mBeaconUsers = new HashMap<>();
    private BeaconsAdapter mAdapter = null;
    private MainActivity mMainActivity = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainActivity = (MainActivity) getActivity();
        return inflater.inflate(R.layout.frag_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        if (null == mAdapter) {
            mAdapter = new BeaconsAdapter();
        }
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
        refresh();
    }

    public void refresh() {
        mBeacons = mMainActivity.getLiveBeacons();
        if (null != mBeacons) {
            mList = new ArrayList<>(mBeacons.keySet());
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mMainActivity.openBeaconFragment(mList.get(position));
    }

    private class BeaconsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return null == mList ? 0 : mList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup viewGroup) {
            if (null == convertView) {
                convertView = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_2, viewGroup, false);
            }

            AppBeacon appBeacon = mList.get(pos);
            BeaconUsers beaconUsers = mBeaconUsers.get(appBeacon);
            if (null == beaconUsers) {
                MonitorService.FirebaseBeaconHolder beaconRef = mBeacons.get(appBeacon);
                beaconUsers = new BeaconUsers(beaconRef.getBeaconUsersRef());
                mBeaconUsers.put(appBeacon, beaconUsers);
            }

            ((TextView) convertView.findViewById(android.R.id.text1)).setText(
                    "Beacon " + appBeacon.getId());

            ((TextView) convertView.findViewById(android.R.id.text2)).setText(
                    beaconUsers.mUsersRange.size() + " active users");

            return convertView;
        }
    }

    private class BeaconUsers {
        private final TreeMap<String, Object> mUsersRange = new TreeMap<>();

        public BeaconUsers(Firebase beaconUsersRef) {
            beaconUsersRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String key = dataSnapshot.getKey();
                    mUsersRange.put(key, dataSnapshot.getValue());
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    mUsersRange.put(dataSnapshot.getKey(), dataSnapshot.getValue());
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    mUsersRange.remove(dataSnapshot.getKey());
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }
}