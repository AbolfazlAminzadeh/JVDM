# JVDM (Java Vortex Download Manager)

JVDM is a high-performance, asynchronous network file and stream downloader built using **Java**, the **Netty framework**, and **GraalVM Native Image**. Engineered for extreme throughput and minimal system overhead, JVDM bypasses traditional HTTP client bottlenecks by implementing low-level socket optimizations, high-speed multi-interface routing, and a custom asynchronous DNS subsystem.

---

## 📖 About the Project

Traditional download managers often introduce significant network overhead due to blocking I/O, heavy memory usage, and generic runtime environments. JVDM addresses this by utilizing a modern, event-driven networking layer combined with low-level kernel optimizations.

By compiling directly into a native executable via GraalVM, JVDM completely eliminates JVM warm-up times, drastically lowers the memory (RSS) footprint, and provides an instant startup experience without requiring an external Java installation.

### Core Architecture Highlights

* **Asynchronous Engine:** Powered by Netty for streamlined, non-blocking network pipelining.
* **Multi-Interface Support:** Capable of binding and routing traffic across multiple network interfaces simultaneously to maximize aggregate bandwidth.
* **Vortex DNS Engine:** Outfitted with a custom, ultra-fast DNS resolver and a smart DNS caching layer to eliminate resolution latency before the download even starts.
* **GraalVM Native:** Compiled directly to a single standalone binary with zero runtime dependencies.
* **Zero-Copy Optimization:** Utilizes optimized native byte buffers to minimize CPU cycles during high-speed data transfers.

---

## ⚙️ How It Works

JVDM achieves high-efficiency data saturation through structured channel pipelining:

1. **Ultra-Fast Resolution:** The custom DNS engine resolves host domains concurrently using a smart cache, completely bypassing the OS blocking `getaddrinfo` bottlenecks.
2. **Multi-Interface Routing:** If multiple network connections are available, JVDM can bind sockets to separate interfaces to parallelize the pipe infrastructure.
3. **Asynchronous Pipelining:** Network data flows through non-blocking streaming channels. Netty event loops coordinate the inbound sockets, ensuring data chunks are processed smoothly.
4. **Backpressure Management:** Downloaded chunks are written directly to disk via an optimized pipeline, preventing system memory spikes even during ultra-high-speed network saturation.

---

## 🚀 How to Use

### Prerequisites

* **Native Version:** Completely standalone. No dependencies or Java installation required.
* **JAR Version:** Requires **Java 25** or later.

### Command Line Interface (CLI) Basic Usage

```bash
# Run the native binary (Linux/macOS)
./jvdm --url "https://example.com/video.mp4" --output ~/Downloads/video.mp4

# Run the native binary (Windows)
jvdm.exe --url "https://example.com/video.mp4" --output C:\Users\Name\Downloads\video.mp4

# Run using the fat JAR
java -jar JVDM.jar --url "https://example.com/video.mp4"

```

### Key Flags

* `-u`, `--url`: **(Required)** The target media, file, or streaming URL.
* `-o`, `--output`: Define the destination path and filename.
* `--interface`: Specify preferred network interfaces for binding (Optional).

---

## 🎯 Goals

* [x] **Zero JRE Dependency:** Package natively for effortless deployment on client environments.
* [x] **Maximized Throughput:** Fully saturate high-bandwidth network pipelines using an asynchronous architecture.
* [x] **Low-Latency DNS Subsystem:** Built-in caching and async resolution to outpace native OS resolvers.
* [ ] **Next-Gen Protocol Support:** Full implementation of HTTP/2 and HTTP/3 (QUIC), alongside legacy protocols like FTP.
* [ ] **Platform-Specific Extractors:** Dedicated downloader engines to natively parse and fetch media from platforms like YouTube, SoundCloud, Spotify, and more.
* [ ] **Aggressive Performance Optimization:** Continuous micro-benchmarking to squeeze out extra throughput and minimize latency at the socket level.

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

To compile the project and bundle all dependencies into a single runnable JAR file:

```bash
./gradlew shadowJar

```

The resulting JAR will be located in the `build/libs/` directory.

#### 3. Compile to Native Image via GraalVM

The project includes fully pre-configured cross-platform reachability metadata (`reachability-metadata.json`) ready for all major operating systems. Ahead-Of-Time (AOT) compilation runs out of the box with no extra tracing agent configuration needed.

Ensure your active environment points to your GraalVM distribution (`JAVA_HOME`), then execute:

```bash
./gradlew nativeCompile

```