#include <errno.h>
#include <jni.h>
#include <string.h>
#include <sys/socket.h>
#include <net/if.h>
#include "org_Kroj_Core_Network_SocketBind_ChannelBinder.h"

JNIEXPORT void JNICALL Java_org_Kroj_Core_Network_SocketBind_ChannelBinder_bindToDevice(JNIEnv *env, jclass this,jint fd, jstring device) {
    const char *dev = (*env)->GetStringUTFChars(env, device, 0);
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    strncpy(ifr.ifr_name, dev, IFNAMSIZ - 1);

    if (setsockopt(fd, SOL_SOCKET, SO_BINDTODEVICE,
                   (char *)&ifr, sizeof(ifr)) == -1) {
        jclass exClass = (*env)->FindClass(env, "java/lang/RuntimeException");
        (*env)->ThrowNew(env, exClass, strerror(errno));
        return;
    }
    (*env)->ReleaseStringUTFChars(env, device, dev);
}
