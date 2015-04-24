package io.onebeacon.sample.liveusers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
import java.util.TreeMap;

public class BeaconFragment extends Fragment {
    private BeaconUsers mBeaconUsers = null;
    private List<String> mUsersList;

    private UsersAdapter mAdapter = null;
    private MainActivity mMainActivity = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainActivity = (MainActivity) getActivity();
        return inflater.inflate(R.layout.frag_main, container, false);
    }

    private String mBuzzedUser = null;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ListView listView = (ListView) view.findViewById(android.R.id.list);
        if (null == mAdapter) {
            mAdapter = new UsersAdapter();
        }
        listView.setAdapter(mAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String user = mUsersList.get(position);
                if (user.equals(mBuzzedUser)) {
                    mMainActivity.getService().unbuzzUser(mBuzzedUser);
                    mBuzzedUser = null;
                    listView.clearChoices();
                    mAdapter.notifyDataSetChanged();
                }
                else {
                    mBuzzedUser = user;
                    mMainActivity.getService().buzzUser(mBuzzedUser);
                }
            }
        });
        refresh();
    }

    public void refresh() {
        long beaconId = getArguments().getLong("id");
        MonitorService.FirebaseBeaconHolder beaconHolder = mMainActivity.getLiveBeacon(beaconId);
        if (null != beaconHolder) {
            mBeaconUsers = new BeaconUsers(beaconHolder.getBeaconUsersRef());
            mAdapter.notifyDataSetChanged();
        }
    }

    private class UsersAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return null == mUsersList ? 0 : mUsersList.size();
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
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            if (null == convertView) {
                convertView = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_activated_2, viewGroup, false);
            }

//            if (null == beaconUsers) {
//                MonitorService.FirebaseBeaconHolder beaconRef = mBeacons.get(appBeacon);
//                mBeaconUsers.put(appBeacon, beaconUsers);
//            }
//
            String userId = mUsersList.get(position);
            HashMap<String, Object> values = (HashMap<String, Object>) mBeaconUsers.mUsersInfo.get(userId);

            String nickname = (String) values.get("nickname");
            if (null == nickname) nickname = userId;

            ((TextView) convertView.findViewById(android.R.id.text1)).setText(nickname);

            ((TextView) convertView.findViewById(android.R.id.text2)).setText(
                    "Range: " + values.get("range"));

            return convertView;
        }
    }

    private class BeaconUsers {
        private final TreeMap<String, Object> mUsersInfo = new TreeMap<>();

        public BeaconUsers(Firebase beaconUsersRef) {
            beaconUsersRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String key = dataSnapshot.getKey();
                    mUsersInfo.put(key, dataSnapshot.getValue());
                    mUsersList = new ArrayList<>(mBeaconUsers.mUsersInfo.keySet());
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    mUsersInfo.put(dataSnapshot.getKey(), dataSnapshot.getValue());
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    mUsersInfo.remove(dataSnapshot.getKey());
                    mUsersList = new ArrayList<>(mBeaconUsers.mUsersInfo.keySet());
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