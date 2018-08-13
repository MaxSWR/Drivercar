package com.cryptandroid.max.drivercar;

import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by MAX on 11.08.2018.
 */

public class BluetoothWindow extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener{
    private TextView status;
    private Button search;
    private Button stop;
    private ListView list;
    private ProgressBar load;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> names;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bluetooth, null);
        status = (TextView)view.findViewById(R.id.status);
        search = (Button)view.findViewById(R.id.search_btn);
        search.setOnClickListener(this);
        stop = (Button)view.findViewById(R.id.stop_btn);
        stop.setOnClickListener(this);
        list = (ListView)view.findViewById(R.id.device_list);
        list.setOnItemClickListener(this);
        names = new ArrayList<>();
        adapter = new  ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, names);
        list.setAdapter(adapter);
        load = (ProgressBar) view.findViewById(R.id.load);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(BlueToothController.getBluetoothReceiver(this), filter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(BlueToothController.getBluetoothReceiver(this));
        //BlueToothController.finishConnect();
    }

    /**
     * обновление списка устройств
     */
    public void notifyDevices(BluetoothDevice device) {
        names.add(device.getName());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_btn:
            if (BlueToothController.startSearchingDevices(this)) {
                names.clear();
                load.setVisibility(View.VISIBLE);
                status.setText("searching");
            }
                break;

            case R.id.stop_btn:
                stopSearching("stoped");
                BlueToothController.finishConnect();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        BlueToothController.chooseDevice(this, i);
    }

    public void stopSearching(String statusText) {
        load.setVisibility(View.INVISIBLE);
        status.setText(statusText);
    }
}
