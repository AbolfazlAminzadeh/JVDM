package org.Kroj.Core.Network.SocketBind;

import java.net.Socket;

public interface SocketBind {

    void bindToCellular(Socket socket) throws Exception;
    void bindToWifi(Socket socket) throws Exception;

}
