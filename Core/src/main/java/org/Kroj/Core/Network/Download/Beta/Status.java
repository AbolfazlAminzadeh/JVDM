package org.Kroj.Core.Network.Download.Beta;

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