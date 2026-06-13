package Network.SocketBind;

public class ChannelBinder {
    public static native void bindToDevice(int fd, String deviceName) throws RuntimeException;
}
