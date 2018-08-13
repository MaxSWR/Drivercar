package com.cryptandroid.max.drivercar;

/**
 * Created by MAX on 11.08.2018.
 */

import android.app.Activity;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class BlueToothController {
    private static BluetoothAdapter bAdapter = null;
    private static ArrayList<BluetoothDevice> devices = null;
    private static BroadcastReceiver receiver = null;
    private static OutputStream out = null;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static BluetoothSocket btSocket = null;


    /**
     * проверка доступности адаптера
     */
    private static boolean checkBluetoothAdapter() {
        bAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bAdapter == null) {
            return false;
        }

        return true;
    }

    /**
     * проверка включенного адаптера
     */
    private static boolean checkBluetoothEnabled() {
        if (!checkBluetoothAdapter() || !bAdapter.isEnabled()) {
            return false;
        }

        return true;
    }

    /**
     * поиск устройств
     */
    public static boolean startSearchingDevices(BluetoothWindow activity) {
        devices = new ArrayList<>();

        if (!checkBluetoothAdapter()) {
            Toast.makeText(activity.getActivity(),"cant use adapter!", Toast.LENGTH_SHORT).show();
            activity.stopSearching("error");
            return false;
        }

        if (!checkBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, 0);
            return false;
        }

        bAdapter.startDiscovery();
        return true;
    }

    /**
     * выбор устройства
     */
    public static void chooseDevice(BluetoothWindow activity, int position) {
        bAdapter.cancelDiscovery();
        Toast.makeText(activity.getActivity(),"connect to " + devices.get(position).getName(), Toast.LENGTH_SHORT).show();
        BluetoothDevice device = bAdapter.getRemoteDevice(devices.get(position).getAddress());

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Toast.makeText(activity.getActivity(),e.getMessage() +"\n "+ e.getStackTrace().toString(), Toast.LENGTH_LONG).show();
        }

        bAdapter.cancelDiscovery();

        try {
            btSocket.connect();
        } catch (IOException e) {
            Toast.makeText(activity.getActivity(),e.getMessage() +"\n "+ e.getStackTrace().toString(), Toast.LENGTH_LONG).show();
            try {
                btSocket.close();
            } catch (IOException e2) {
                Toast.makeText(activity.getActivity(),e2.getMessage() +"\n "+ e2.getStackTrace().toString(), Toast.LENGTH_LONG).show();
            }
        }

        try {
            out = btSocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(activity.getActivity(),e.getMessage() +"\n "+ e.getStackTrace().toString(), Toast.LENGTH_LONG).show();
        }

        activity.stopSearching("connected");
    }


    public static BroadcastReceiver getBluetoothReceiver(final BluetoothWindow activity) {
        if (receiver != null) return receiver;

        receiver = new BroadcastReceiver(){

            public void onReceive(Context context, Intent intent){
                if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
                    BluetoothDevice device= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    devices.add(device);
                    activity.notifyDevices(device);
                }
            }
        };

        return  receiver;
    }

    /**
     * завершить работы с адаптером
     */
    public static void finishConnect() {
        if (bAdapter.isDiscovering())
            bAdapter.cancelDiscovery();

        if (btSocket != null)
            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        if (out != null)
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static void sendData(ControllerWindow activity, byte[] data) {
        if (checkBluetoothEnabled() && checkBluetoothAdapter() && out != null) {
            try {
                out.write(data);
            } catch (IOException e) {
                Toast.makeText(activity.getActivity(),"cant send data!!!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}
