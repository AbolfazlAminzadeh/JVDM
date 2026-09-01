package org.kroj.Core.Download.Beta;

public enum Status {
    Idle,
    Connecting,
    Connected,
    SendingGet,
    SentGet,
    Downloading,
    Downloaded,
    Failed
}