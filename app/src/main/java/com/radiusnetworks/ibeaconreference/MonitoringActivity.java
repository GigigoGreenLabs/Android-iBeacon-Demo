package com.radiusnetworks.ibeaconreference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;


public class MonitoringActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "MonitoringActivity";

    private ListView list = null;
    private BeaconAdapter adapter = null;
    private ArrayList<BeaconWrapper> arrayBeaconWrapper = new ArrayList<BeaconWrapper>();


    private LayoutInflater inflater;

    private BeaconServiceUtility beaconUtill = null;
    private BeaconManager iBeaconManager = BeaconManager.getInstanceForApplication(this);
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        beaconUtill = new BeaconServiceUtility(this);
        list = (ListView) findViewById(R.id.list);
        adapter = new BeaconAdapter();
        list.setAdapter(adapter);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //add your ibeacon Spec
        addBaconSpecToBeaconManager();

        checkPermissionsAndroidM();

    }

    //region Check COARSE Permission this is 4 background recognition
    private void checkPermissionsAndroidM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(23)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }

                });
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

//endregion

    private void addBaconSpecToBeaconManager() {
        //ojito
//
//		public static final String ALTBEACON_LAYOUT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
//		public static final String EDDYSTONE_TLM_LAYOUT = "x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15";
//		public static final String EDDYSTONE_UID_LAYOUT = "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19";
//		public static final String EDDYSTONE_URL_LAYOUT = "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-21v";
//		public static final String URI_BEACON_LAYOUT = "s:0-1=fed8,m:2-2=00,p:3-3:-41,i:4-21v";

        //todo NO HAY Q TENER AQUI EL APPLE_BACON_SPEC, es una constante ver arriba
        //https://beaconlayout.wordpress.com/

        //apple IBEacon+TLM
        BeaconParser beaconParser = new BeaconParser().setBeaconLayout(BuildConfig.APPLE_BACON_SPEC);
        beaconParser.addExtraDataParser(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        iBeaconManager.getBeaconParsers().add(beaconParser);
        iBeaconManager.getBeaconParsers().add(beaconParser);

        //EDDYSTONE UID+TLM
        beaconParser = new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT);
        beaconParser.addExtraDataParser(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        iBeaconManager.getBeaconParsers().add(beaconParser);
        //EDDYSTONE URL +TLM
        beaconParser = new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT);
        beaconParser.addExtraDataParser(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        iBeaconManager.getBeaconParsers().add(beaconParser);

//add tlm too
        beaconParser = new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT);
        iBeaconManager.getBeaconParsers().add(beaconParser);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        beaconUtill.onStart(iBeaconManager, this);
    }

    @Override
    protected void onStop() {
        beaconUtill.onStop(iBeaconManager, this);
        super.onStop();
    }

    @Override
    public void onBeaconServiceConnect() {

        iBeaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                //region `parsing Eddystone fields
                ArrayList<BeaconWrapper> beaconsWrappers = new ArrayList<BeaconWrapper>();
                for (Beacon beacon : beacons) {
                    BeaconWrapper aux = new BeaconWrapper(
                            BeaconType.getBeaconType(beacon.getBeaconTypeCode()),
                            beacon,
                            BeaconType.getBckColorByBeaconType(getApplicationContext(), beacon.getBeaconTypeCode())
                    );
                    beaconsWrappers.add(aux);
                }
                //endregion

//beaconType


                Collections.sort(beaconsWrappers, new Comparator<BeaconWrapper>() {
                    @Override
                    public int compare(BeaconWrapper b1, BeaconWrapper b2) {
                        int result;
                        result = orderByType(b1.getBeaconType(), b2.getBeaconType());
                        return result;
                    }

                    private int orderByType(String val1, String val2) {
                        if (val1 == BeaconType.IBEACON && val2 != BeaconType.IBEACON)
                            return 1;
                        if (val1 == BeaconType.EDDY_STONE_UID && val2 != BeaconType.EDDY_STONE_UID)
                            return -1;
                        if (val1 == BeaconType.EDDY_STONE_URL && val2 != BeaconType.EDDY_STONE_URL)
                            return -1;

                        return 0;
                    }


                });
                Collections.sort(beaconsWrappers, new Comparator<BeaconWrapper>() {
                    @Override
                    public int compare(BeaconWrapper b1, BeaconWrapper b2) {
                        int result;
                        result = groupValues(b1.getBeacon().getBluetoothAddress(), b2.getBeacon().getBluetoothAddress());
                        return result;
                    }

                    private int groupValues(String val1, String val2) {
                        if (val1.equals(val2))
                            return 1;
                        return -1;
                    }
                });

                //todo lo de arriba sobra
                arrayBeaconWrapper.clear();
                arrayBeaconWrapper.addAll(beaconsWrappers);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }

        });

        iBeaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.e("BeaconDetactorService", "didEnterRegion");
                // logStatus("I just saw an iBeacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.e("BeaconDetactorService", "didExitRegion");
                // logStatus("I no longer see an iBeacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.e("BeaconDetactorService", "didDetermineStateForRegion");
                // logStatus("I have just switched from seeing/not seeing iBeacons: " + state);
            }

        });

        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private boolean isEddyStone(Beacon beacon) {
        if (beacon.getServiceUuid() == 0xfeaa) //00UID eddystone
            return true;
        else
            return false;
    }


    private class BeaconAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (arrayBeaconWrapper != null && arrayBeaconWrapper.size() > 0)
                return arrayBeaconWrapper.size();
            else
                return 0;
        }

        @Override
        public BeaconWrapper getItem(int arg0) {
            return arrayBeaconWrapper.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                ViewHolder holder;

                if (convertView != null) {
                    holder = (ViewHolder) convertView.getTag();
                } else {
                    holder = new ViewHolder(convertView = inflater.inflate(R.layout.tupple_monitoring, null));
                }

                BeaconWrapper beaconWrapperItem = arrayBeaconWrapper.get(position);
                holder.lytContainer.setBackgroundColor(beaconWrapperItem.getBeaconColorBack());
                holder.BEACON_TYPE.setText(beaconWrapperItem.getBeaconType());

                Beacon beaconItem = beaconWrapperItem.beacon;

                switch (beaconWrapperItem.getBeaconType()) {
                    case BeaconType.IBEACON:
                        showIBeaconViewItem(holder, beaconItem);
                        break;
                    case BeaconType.EDDY_STONE_UID:
                        showEddyStoneUIDViewItem(holder, beaconItem);
                        break;
                    case BeaconType.EDDY_STONE_URL:
                        showEddyStoneURLViewItem(holder, beaconItem);
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return convertView;
        }

        private void showEddyStoneTLMData(ViewHolder holder, Beacon beaconItem) {

            //If have TLM Data
            if (beaconItem.getExtraDataFields().size() > 0) {
                long telemetryVersion = beaconItem.getExtraDataFields().get(0);
                long batteryMilliVolts = beaconItem.getExtraDataFields().get(1);
                long temperature = beaconItem.getExtraDataFields().get(2);
                long pduCount = beaconItem.getExtraDataFields().get(3);
                long uptime = beaconItem.getExtraDataFields().get(4);

                holder.beacon_tlm.setText("Vs: " + telemetryVersion +
                        "\nTemperature: " + convertToDecimalDegrees(temperature) + " º" +
                        "  BattLevel: " + batteryMilliVolts + " mV" +
                        "\n has transmitted: " + pduCount + " advertisements." +
                        "\n has been up for : " + uptime + " seconds");
            } else
                holder.beacon_tlm.setVisibility(View.GONE);

        }

        private String convertToDecimalDegrees(long temperature) {
            String temperatureSTR = "N/S";
            float tempF = 0.0f;
            if (temperature > 0)
                tempF = temperature / 256f;

            if (tempF != 0.0f)
                temperatureSTR = tempF + "";


            return temperatureSTR;
        }

        private String formatDistanceDecimals(double distance) {
            return String.format("%1$.3f", distance);
        }

        private void showEddyStoneUIDViewItem(ViewHolder holder, Beacon beaconItem) {

            holder.beacon_tlm.setVisibility(View.VISIBLE);
            holder.beacon_minor.setVisibility(View.GONE);
            holder.beacon_proximity.setVisibility(View.GONE);

            //eddystone only have 2 ids namespace(uuid, sesgado) y instance id(like major)
            holder.beacon_uuid.setText("NamespaceID: " + beaconItem.getId1());
            holder.beacon_major.setText("InstanceID: " + beaconItem.getId2());
            holder.beacon_rssi.setText("Rssi: " + beaconItem.getRssi());
            holder.beacon_txpower.setText("TxPower: " + beaconItem.getTxPower());
            holder.beacon_range.setText("DistanceOLD: " + formatDistanceDecimals(beaconItem.getDistance())+"m\nDistanceNEW: " + formatDistanceDecimals(BeaconType.calculateDistanceWithRefRSSI0(beaconItem.getTxPower(), beaconItem.getRssi()))+"m"); //todo esto no es correcto

            showEddyStoneTLMData(holder, beaconItem);

            holder.BEACON_MAC.setText("MAC: " + beaconItem.getBluetoothAddress());
            holder.BEACON_BTName.setText("Name: " + beaconItem.getBluetoothName());

        }

        private void showEddyStoneURLViewItem(ViewHolder holder, Beacon beaconItem) {


            holder.beacon_tlm.setVisibility(View.VISIBLE);
            holder.beacon_minor.setVisibility(View.GONE);
            holder.beacon_proximity.setVisibility(View.GONE);

            String url = UrlBeaconUrlCompressor.uncompress(beaconItem.getId1().toByteArray());

            //eddystone only have 2 ids namespace(uuid, sesgado) y instance id(like major)
            holder.beacon_uuid.setText("URL: " + url);
            holder.beacon_major.setText("Compress_URL: " + beaconItem.getId1());

            holder.beacon_rssi.setText("Rssi: " + beaconItem.getRssi());
            holder.beacon_txpower.setText("TxPower: " + beaconItem.getTxPower());
            holder.beacon_range.setText("DistanceOLD: " + formatDistanceDecimals(beaconItem.getDistance())+"m\nDistanceNEW: " + formatDistanceDecimals(BeaconType.calculateDistanceWithRefRSSI0(beaconItem.getTxPower(), beaconItem.getRssi()))+"m"); //todo esto no es correcto

            showEddyStoneTLMData(holder, beaconItem);

            holder.BEACON_MAC.setText("MAC: " + beaconItem.getBluetoothAddress());
            holder.BEACON_BTName.setText("Name: " + beaconItem.getBluetoothName());
        }

        private void showIBeaconViewItem(ViewHolder holder, Beacon beaconItem) {

            holder.beacon_minor.setVisibility(View.VISIBLE);
            holder.beacon_proximity.setVisibility(View.GONE);
            holder.beacon_tlm.setVisibility(View.GONE);

            holder.beacon_uuid.setText("UUID: " + beaconItem.getId1());
            holder.beacon_major.setText("Major: " + beaconItem.getId2());
            holder.beacon_minor.setText("Minor: " + beaconItem.getId3());
            //holder.beacon_proximity.setText("Proximity: " + "N/A");
            holder.beacon_rssi.setText("Rssi: " + beaconItem.getRssi());
            holder.beacon_txpower.setText("TxPower: " + beaconItem.getTxPower());
            holder.beacon_range.setText("Distance: " + formatDistanceDecimals(beaconItem.getDistance()));

            holder.BEACON_MAC.setText("MAC: " + beaconItem.getBluetoothAddress());
            holder.BEACON_BTName.setText("Name: " + beaconItem.getBluetoothName());
        }

        private class ViewHolder {
            private TextView beacon_uuid;
            private TextView beacon_major;
            private TextView beacon_minor;
            private TextView beacon_proximity;
            private TextView beacon_rssi;
            private TextView beacon_txpower;
            private TextView beacon_range;
            private TextView beacon_tlm;

            private TextView BEACON_MAC;
            private TextView BEACON_BTName;
            private TextView BEACON_TYPE;

            private RelativeLayout lytContainer;

            public ViewHolder(View view) {
                beacon_uuid = (TextView) view.findViewById(R.id.BEACON_uuid);
                beacon_major = (TextView) view.findViewById(R.id.BEACON_major);
                beacon_minor = (TextView) view.findViewById(R.id.BEACON_minor);
                beacon_proximity = (TextView) view.findViewById(R.id.BEACON_proximity);
                beacon_rssi = (TextView) view.findViewById(R.id.BEACON_rssi);
                beacon_txpower = (TextView) view.findViewById(R.id.BEACON_txpower);
                beacon_range = (TextView) view.findViewById(R.id.BEACON_range);
                beacon_tlm = (TextView) view.findViewById(R.id.BEACON_TLM);
                BEACON_MAC = (TextView) view.findViewById(R.id.BEACON_MAC);
                BEACON_BTName = (TextView) view.findViewById(R.id.BEACON_BTName);
                BEACON_TYPE = (TextView) view.findViewById(R.id.BEACON_TYPE);
                lytContainer = (RelativeLayout) view.findViewById(R.id.lytContainer);

                view.setTag(this);
            }
        }

    }

}