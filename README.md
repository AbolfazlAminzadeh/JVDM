# JVDM (Java Vortex Download Manager)

## 📖 About the Project

### Core Abilities

* **Asynchronous:** Using Netty, Non-blocking io with highly optimizations
* **Multi-Interface Support:** Capability to use how much internet device you have (VPN, LTE, Modem, etc.)
* **VeryFast DNS Resolver:** A highly optimized dns cache to prevent syscalls and reducing overhead
* **GraalVM Native:** Compiled directly for each os to make the user feels better
* **Zero-Copy:** Feel free to use it, no OOM kill, no Memory Leaks :) 

---

## ⚙️ How Works?

JVDM is Netty based so data passthrough pipelines:

1. **⚡Ultra-Fast DNS Resolving:** `getaddrinfo`?, resolve 1 time, use it many times, resolve as fastest you can (parallel request)
2. **🤹Multi-Interface Routing:** JVDM uses all available network devices, it can bypass Linux's `iproute` so its working fine :)
3. **⏳Asynchronous Pipes:** Data transfer over non-blocking stream channels of netty, this means there is no bottleneck for receiving data, no wait, no blocking = more speed
4. **🚦Backpressure Managing:** Slow Disk? No worry, JVDM waits until your disk handle it

---
## 📊 Benchmark

Want to see performance results and comparisons?

👉 [View Benchmark Results](BENCHMARK.md)

---
## 🚀 How to Use

### Requirements

* **Native Version:** Not Complete Tested Yet, currently Linux - Windows 10/11 are working good
* **JAR Version:** Requires **Java 25** or later, Use only if native versions didn't work, you cant get maximum speed with jar file (you need it to warm it up)

---

## 🎯 Goals

* [x] **Maximize Throughput:** Make internet usage at maximum device can have
* [x] **Low-Latency DNS Subsystem:** No overhead for waiting 50~100MS for a download
* [ ] **HTTP2/3 Support**: Lower Latency, More Speed, Lower Connections, Maximum speed :::)
* [ ] **Other Protocols**: Torrent, FTP, SFTP, etc. There is many protocols that we want to add
* [ ] **Huge Amount of platform support:** Bundle Everything inside JVDM, Download From YT,SoundCloud,Spotify,Repositories Possible :)
* [ ] **API Support:** Other Applications can send a request to redirect download and maximize download speed, even with live progress response :::)

---

## 🛠️ For Developers

### Setup Requirements

* **JDK 25+** (GraalVM CE or Oracle GraalVM recommended)
* **Gradle** (or use the included wrapper `./gradlew`)
* Native build tools for your specific platform (`gcc`, `zlib-devel`, `glibc-devel` for Linux; Visual Studio Build Tools for Windows).

### How to Build

#### 1. Clone the repository

```bash
git clone https://github.com/AbolfazlAminzadeh/JVDM.git
cd JVDM
```

#### 2. Build the Fat JAR

To compile and bundle everything inside a jar file:

```bash
./gradlew shadowJar
```

The resulting JAR will be located in the `build/libs/` directory.

#### 3. Compile to Native Image via GraalVM

You don't need to pass insane nightmare, I already passed and filled the `metadata.json` file for you, its preconfigured so feel free to use it.

#### Important: Ensure your os environment table have (`JAVA_HOME`) set to graalvm path, then execute:

```bash
./gradlew nativeCompile
```