package org.kroj.Core.Network.Interface.Binder;

import org.kroj.Core.Exceptions.FailToBindSocketException;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

// Tested And Working Successfully :), No C Required More :))))))
public class PanamaBinder {

    private static final Linker linker = Linker.nativeLinker();
    private static final MethodHandle SetSocketOpt;
    static {
        try {
            // result (int) , socket file descriptor (int), option level (int), option name (int), option value (address (*)), length (int)
            // 0, 4, SOL_SOCKET, SO_BINDTODEVICE, "Mamad", 6
            SetSocketOpt = linker.downcallHandle(linker.defaultLookup().find("setsockopt").orElseThrow(), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void bindToDevice(int socketFileDescriptor, String deviceName) throws FailToBindSocketException {

        try (Arena arena = Arena.ofConfined()){
            MemorySegment memoryName = arena.allocateFrom(deviceName, StandardCharsets.UTF_8);
            // 1 = Socket Level Option
            // 25 = Bind To Device
            int result = (int) SetSocketOpt.invoke(socketFileDescriptor, 1, 25, memoryName, (int) memoryName.byteSize());
            if (result != 0) {
                throw new FailToBindSocketException("Failed to set device socket file descriptor, Error Code: "+result);
            }
        } catch (Throwable e) {
            throw new FailToBindSocketException(e);
        }

    }

}
