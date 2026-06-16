package org.Kroj.Network;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;
import org.Kroj.Core.Network.SocketBind.SocketBind;
import org.Kroj.Core.Tools.NI.NetworkInterfaces;

import java.net.Socket;
// Not Ready Yet
public class AndroidSocketBinder implements SocketBind {

    private static final String TAG = "AndroidSocketBinder";
    private volatile Network cellularNetwork = null;
    private volatile Network wifiNetwork = null;

    public AndroidSocketBinder(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Log.e(TAG, "Connectivity Manager is unavailable");
            return;
        }

        NetworkRequest cellularRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        cm.requestNetwork(cellularRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                cellularNetwork = network;
                Log.i(TAG, "SimCard Detected!");
                NetworkInterfaces.getDevices().forEach(e -> Log.i(TAG, e));
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                cellularNetwork = null;
                Log.i(TAG, "SimCard Lost!");
                NetworkInterfaces.getDevices().forEach(e -> Log.i(TAG, e));
            }
        });

        NetworkRequest wifiRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        cm.requestNetwork(wifiRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                wifiNetwork = network;
                Log.i(TAG, "Wifi Detected!");
                NetworkInterfaces.getDevices().forEach(e -> Log.i(TAG, e));
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                wifiNetwork = null;
                Log.w(TAG, "Wifi Lost!");
                NetworkInterfaces.getDevices().forEach(e -> Log.i(TAG, e));
            }
        });
    }
    @Override
    public void bindToCellular(Socket socket) throws Exception {
        Network currentCellular = this.cellularNetwork;
        if (currentCellular != null) {
            currentCellular.bindSocket(socket);
            Log.d(TAG, "Socket Successfully Bind to SimCard!");
            NetworkInterfaces.getDevices().forEach(e -> Log.i(TAG, e));
        } else {
            throw new IllegalStateException("Socket Failed to bind simcard");
        }
    }

    @Override
    public void bindToWifi(Socket socket) throws Exception {
        Network currentWifi = this.wifiNetwork;
        if (currentWifi != null) {
            currentWifi.bindSocket(socket);
            Log.d(TAG, "Socket Successfully Bind To Wifi");
            NetworkInterfaces.getDevices().forEach(e -> Log.i(TAG, e));
        } else {
            throw new IllegalStateException("Socket Failed to bind wifi");
        }
    }
    public boolean isCellularAvailable() {
        return cellularNetwork != null;
    }

    public boolean isWifiAvailable() {
        return wifiNetwork != null;
    }
}