### Core Structural Components

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                            Target ARCHITECTURE OVERVIEW                                │
└────────────────────────────────────────────────────────────────────────────────────────┘

              ┌────────────────────────────────────────────────────────┐
              │                   Downloader Engine                    │
              │  - Work Queue Manager (Priority Blocking Queue)        │
              │  - Lockless State Registry (32 Active Slots via Flat)  │
              └───────────────────────────┬────────────────────────────┘
                                          │
                  ┌───────────────────────┼───────────────────────┐
                  ▼                       ▼                       ▼
      ┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────────────┐
      │   HTTP/3 Pool Manager │ │   HTTP/2 Pool Manager │ │   HTTP/1 Pool Manager │
      │   - 4 UDP QUIC Conns  │ │   - 4 TCP Connections │ │   - 8 TCP Connections │
      │   - Max Mux: 8 / Conn │ │   - Max Mux: 2 / Conn │ │   - Max Mux: 1 / Conn │
      └───────────┬───────────┘ └───────────┬───────────┘ └───────────┬───────────┘
                  │                         │                         │
                  ▼                         ▼                         ▼
      ┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────────────┐
      │  Netty QUIC Pipeline  │ │  Netty TCP Pipeline   │ │  Netty TCP Pipeline   │
      │ [EpollDatagramChannel]│ │  [EpollSocketChannel] │ │  [EpollSocketChannel] │
      │  - UDP GRO Enabled    │ │  - AutoRead Throttled │ │  - 32MB Kernel Buffer │
      └───────────────────────┘ └───────────────────────┘ └───────────────────────┘

```

---

### Phase 1: Task Scheduling & Connection Leasings

```
[ Downloader Engine ]          [ HTTP/2 Pool Manager ]         [ Netty Epoll TCP Channel ]
          │                               │                              │
          │ 1. pollTask()                 │                              │
          ├──────────────────────────────►│                              │
          │                               │                              │
          │                               │ 2. Evaluate Primitive Slots  │
          │                               ├──────┐                       │
          │                               │      │ Active Conns < 4?     │
          │                               │◄─────┘ OR Streams/Conn < 2?  │
          │                               │                              │
          │                               │ [IF NO AVAILABLE CONNS]      │
          │                               │ 3. Bootstrap Native Socket   │
          │                               │    (Set SO_RCVBUF=32MB,      │
          │                               │     AdaptiveRecv 256K-16M)   │
          │                               ├─────────────────────────────►│
          │                               │                              │
          │                               │ 4. Initialize Child Pipeline │
          │                               │    (Add H2 Codec:            │
          │                               │     Max Window=2GB,          │
          │                               │     Max Frame=16MB)          │
          │                               ├─────────────────────────────►│
          │                               │                              │
          │ 5. Lease Acknowledged         │                              │
          │◄──────────────────────────────┤                              │
          │                               │                              │
          │ 6. Map Ingestion Context      │                              │
          ├──────┐                        │                              │
          │      │ Register Flat Int Map  │                              │
          │      │ to Panama Destination  │                              │
          │◄─────┘                        │                              │

```

---

### Phase 2: Multiplexed Inbound Pipeline Processing (Panama Zero-Allocation)

```
[ Remote Server ]             [ Netty Inbound Loop ]       [ IntObjectHashMap Router ]     [ Panama Ingestion Engine ]
        │                               │                               │                           │
        │ 1. Transmit H2 DATA Frame     │                               │                           │
        ├──────────────────────────────►│                               │                           │
        │    (Stream ID: 5, Size: 16MB) │                               │                           │
        │                               │ 2. Batch Extract Buffer       │                           │
        │                               │    (MaxMessagesPerRead=64)    │                           │
        │                               ├──────────────────────────────►│                           │
        │                               │                               │                           │
        │                               │                               │ 3. Flat L1-Cache Lookup   │
        │                               │                               ├──────┐                    │
        │                               │                               │      │ Locate Segment ID  │
        │                               │                               │◄─────┘                    │
        │                               │                               │                           │
        │                               │                               │ 4. Extract Raw Memory     │
        │                               │                               ├──────────────────────────►│
        │                               │                               │    (Wrap Address to       │
        │                               │                               │     MemorySegment,        │
        │                               │                               │     buf.retain())         │
        │                               │                               │                           │
        │ 5. Transmit End-Stream Frame  │                               │                           │
        ├──────────────────────────────►│                               │                           │
        │    (Stream ID: 5 + FIN Flag)  │                               │                           │
        │                               │ 6. Process End-Of-Stream      │                           │
        │                               ├──────────────────────────────►│                           │
        │                               │                               │                           │
        │                               │                               │ 7. Trigger Final Handshake│
        │                               │                               ├──────────────────────────►│
        │                               │                               │    (Release Native Pointer)
        │                               │                               │                           │
        │                               │                               │ 8. Free Array Slot        │
        │                               │                               ├──────────────────────────►│

```

---

### Detailed Architecture Matrix

| Attribute | HTTP/3 Connection Layer | HTTP/2 Connection Layer | HTTP/1.x Connection Layer |
| --- | --- | --- | --- |
| **Underlying Transport** | UDP / QUIC via Native `EpollDatagramChannel` | TCP via Native `EpollSocketChannel` | TCP via Native `EpollSocketChannel` |
| **Max Physical Sockets** | 4 UDP Connection Channels | 4 TCP Connection Channels | 8 TCP Connection Channels |
| **Multiplex Ratio (Mux)** | 8 Streams per UDP Channel | 2 Streams per TCP Channel | 1 Stream per TCP Channel |
| **Total Parallel Tasks** | 32 concurrent downloads | 8 concurrent downloads | 8 concurrent downloads |
| **Stream Lifecycle** | Controlled via explicit QUIC `FIN` stream bits. | Signaled by `isEndStream()` flags on payload frames. | Determined via distinct `LastHttpContent` object frames. |
| **Routing Mechanism** | Flat Array Cache (Size 8) matching `QuicStreamChannel` handles. | Non-boxed primitive `IntObjectHashMap` using Integer `Stream ID`. | Unique Netty `ChannelId` directly mapped to task reference. |
| **Memory Blueprint** | Zero-copy handoff of `Http3DataFrame` to memory address segment. | Zero-copy handoff of `Http2DataFrame` to memory address segment. | Zero-copy handoff of `HttpContent` chunks to memory address segment. |
| **Kernel Parameters** | `EpollChannelOption.UDP_GRO` enabled; Recv Buffer: 32MB. | `ChannelOption.SO_RCVBUF`: 32MB; `TCP_NODELAY`: true. | `ChannelOption.SO_RCVBUF`: 32MB; `TCP_NODELAY`: true. |
| **Flow Control Windows** | Connection: 128MB, Stream: 32MB via native QUIC config. | Connection: 2GB, Stream: 2GB via `initialWindowSize`. | None (Regulated entirely by kernel TCP slide window). |
| **Handoff Mechanism** | Project Panama `MemorySegment.ofAddress().reinterpret()`. | Project Panama `MemorySegment.ofAddress().reinterpret()`. | Project Panama `MemorySegment.ofAddress().reinterpret()`. |

---

### Structural Pipeline Configurations

#### HTTP/3 Pipeline Topology

1. **`Http3ServerConnectionHandler`**: Translates incoming native UDP datagram layers into logical, protocol-compliant HTTP/3 frame types.
2. **`UDP_GRO_Coalescer`**: Merges segments up to 64KB at the kernel-to-transport interface layer to bypass event-loop thread waking cycles.
3. **`MultiplexedStreamRouter` (Child Sub-Channel Handler)**: Intercepts raw data frames, extracts `ByteBuf.memoryAddress()`, wraps the pointer into an un-managed Panama `MemorySegment`, and invokes the ingestion layer instantly on the same EventLoop thread.

#### HTTP/2 Pipeline Topology

1. **`Http2FrameCodec`**: Parsed via native BoringSSL (`netty-tcnative`) vector instructions. Configured with an explosive `maxFrameSize` of 16,777,215 bytes and `initialWindowSize` of 2,147,483,647.
2. **`Http2MultiplexHandler`**: Spawns low-overhead virtual pipeline execution loops for every active context stream.
3. **`MultiplexedStreamRouter` (Registered as Child Handler)**: Performs non-boxing lookup using primitive `IntObjectHashMap`. Extracts raw off-heap buffer addresses directly to the Panama ingestion interface, executing `buf.retain()` and releasing it only after processing completes.

#### HTTP/1.x Pipeline Topology

1. **`HttpClientCodec`**: Parses high-speed raw stream frames without payload pooling restrictions.
2. **`HttpContentDecompressor`**: Decompresses streaming components directly on the wire when necessary.
3. **`SequentialChannelRouter`**: Intercepts sequential `HttpContent` and `LastHttpContent` objects, exposing raw un-managed allocations to Panama memory pipelines without cross-thread handoffs.

---

### Host Kernel Configuration Script (`/etc/sysctl.conf`)

To ensure the OS can keep up with this network profile, apply these settings to maximize the system socket limits:

```ini
# Force kernel to allocate up to 64MB for network sockets
net.core.rmem_max=67108864
net.core.wmem_max=67108864

# Force maximum auto-tuning TCP read buffer spacing (Min, Initial, Max)
net.ipv4.tcp_rmem=4096 87380 67108864
net.ipv4.tcp_wmem=4096 65536 67108864

# Enable TCP BBR Congestion Control to protect throughput against latency spikes
net.core.default_qdisc=fq
net.ipv4.tcp_congestion_control=bbr

# Maximize the network interface card's backpressure queue size
net.core.netdev_max_backlog=10000

# Optimize allocation limits for multi-gigabit workloads
net.ipv4.tcp_max_syn_backlog=8192

```