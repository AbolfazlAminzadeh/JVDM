---

# JVDM (Java Video Download Manager)

JVDM is a high-performance, asynchronous network video downloader built using **Java**, the **Netty framework**, and **GraalVM Native Image**. Engineered for efficiency and minimal system overhead, JVDM bypasses heavy, conventional HTTP client bottlenecks by implementing low-level socket optimizations and pipelined data transfers to maximize network throughput.

---

## 📖 About the Project

Traditional download managers often introduce significant overhead due to heavy memory usage and runtime environments. JVDM addresses this by utilizing a modern, event-driven networking layer. By compiling directly into a native executable via GraalVM, JVDM eliminates JVM warm-up times, drastically lowers the memory (RSS) footprint, and provides an instant startup experience directly on your operating system without needing an external runtime.

### Core Architecture Highlights

* **Asynchronous Engine:** Powered by Netty for streamlined, event-driven network pipelining.
* **GraalVM Native:** Compiled directly to a single standalone binary with zero reliance on a pre-installed Java Runtime Environment (JRE).
* **Zero-Copy Optimization:** Utilizes optimized byte buffers to minimize CPU cycles during high-speed data transfers.
* **Cross-Platform Compilation:** Native binaries targeting Windows, Linux, and macOS.
* **Multi Internet Adaptor Support:** High Compatibility with binding networks to sockets, speed boost, no device waste. 

---

## ⚙️ How It Works

JVDM achieves high-efficiency data saturation through structured channel pipelining:

1. **Stream Analysis:** Upon receiving a target URL, JVDM probes the host server to verify file capabilities and establish the optimum byte-range pipeline.
2. **Asynchronous Pipelining:** The network data flows through non-blocking streaming channels. Netty event loops coordinate the inbound sockets, ensuring data chunks are processed smoothly.
3. **Backpressure Management:** Downloaded chunks are written directly to disk via an optimized pipeline, preventing system memory spikes even during ultra-high-speed network saturation.

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

### Key Flags (CLI Version)

* `-u`, `--url`: **(Required)** The direct video streaming or file URL.
* `-o`, `--output`: Define the destination path and filename.

---

## 🎯 Goals

* [x] **Zero JRE Dependency:** Package natively for effortless deployment on client environments.
* [x] **Maximized Throughput:** Fully saturate high-bandwidth network pipelines using an asynchronous architecture.
* [ ] **HTTP V3 Support:** Insane Speed, Quic Download Support, Best for high throughput and low latency.
* [ ] **HTTP V2 Support:** Multiplexing to increase download speed, even more.
* [ ] **FTP And Other Protocols:** For a good multi network interface download manager, we need this.

---

## 🛠️ For Developers

### Setup Requirements

* **JDK 21+** (GraalVM CE or Oracle GraalVM recommended)
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

Ensure your active environment points to your GraalVM distribution (`JAVA_HOME`), then execute:

```bash
./gradlew nativeCompile

```

> 💡 **AOT Reflection Configuration Note:** > If you are working on the UI layer or introducing dynamic class loading, ensure that your reflection targets are accurately declared in the schema configuration file (`reachability-metadata.json`) under `META-INF/native-image/` to ensure flawless Ahead-Of-Time (AOT) compilation.