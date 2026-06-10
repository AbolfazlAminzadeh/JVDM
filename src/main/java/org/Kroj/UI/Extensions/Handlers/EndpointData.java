package org.Kroj.UI.Extensions.Handlers;

public class EndpointData {

    private final byte status;
    private final String url;

    public EndpointData(byte status,String data) {
        this.status = status;
        this.url = data;
    }

    public byte getStatus() {
        return status;
    }
    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return status + ":" + url;
    }
}
