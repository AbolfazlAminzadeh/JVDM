package org.kroj.Core.Network.Interface;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

// Problem, Streams are not available anymore in android
public class NetworkInterfaces {

    public static List<NetworkInterface> getInterfaces() {
        try {

            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();

            return Collections.list(nis).stream()
                    .filter(ni -> {
                        try {
                            return  !ni.isVirtual() &&
                                    !ni.isLoopback() &&
                                    !ni.isPointToPoint() &&
                                    !ni.getDisplayName().contains("vir") &&
                                    !ni.getDisplayName().contains("vnet") &&
                                    !ni.getDisplayName().contains("rndis") &&
                                    !ni.getDisplayName().contains("dummy") &&
                                    ni.getInetAddresses().hasMoreElements() &&
                                    ni.isUp();
                        } catch (SocketException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toCollection(ArrayList::new));
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getDevices() {
        return getInterfaces().stream()
                .map(NetworkInterface::getName)
                .collect(Collectors.toList());
    }
}
