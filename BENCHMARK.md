# Benchmark Results

This document compares the performance of `JVDM` against `aria2c` using `hyperfine`.

## Environment

* Tool: `hyperfine`
* aria2c flags: `-x 16` (16 connections)
* Multiple scenarios tested:

    * Local network (`192.168.1.50`)
    * Remote server (internet download)
    * Loopback (`127.0.0.1`)

---

## 1. Local Network Benchmark (`192.168.1.50`)

### Command

```bash
hyperfine './JVDM http://192.168.1.50/testfile.bin' \
          'aria2c -x 16 http://192.168.1.50/testfile.bin'
```

### Results

| Tool   | Mean Time | Std Dev  | Min     | Max     | User Time | System Time |
| ------ | --------- | -------- | ------- | ------- | --------- | ----------- |
| JVDM   | 1.171 s   | ±0.043 s | 1.123 s | 1.282 s | 0.636 s   | 2.200 s     |
| aria2c | 1.467 s   | ±0.038 s | 1.393 s | 1.545 s | 0.162 s   | 0.835 s     |

### Summary

**JVDM is ~1.25× faster than aria2c** on local network downloads.

---

## 2. Remote Download Benchmark (Internet)

### Command

```bash
hyperfine --warmup 1 --runs 3 \
'./JVDM https://dl2.soft98.ir/soft/i/Internet.Download.Manager.6.43.6.rar' \
'aria2c -x 16 https://dl2.soft98.ir/soft/i/Internet.Download.Manager.6.43.6.rar'
```

### Results

| Tool   | Mean Time | Std Dev  | Min     | Max      | User Time | System Time |
| ------ | --------- | -------- | ------- | -------- | --------- | ----------- |
| JVDM   | 10.476 s  | ±0.959 s | 9.373 s | 11.110 s | 1.378 s   | 0.305 s     |
| aria2c | 10.738 s  | ±1.996 s | 9.466 s | 13.038 s | 0.273 s   | 0.231 s     |

### Summary

**JVDM is ~1.03× faster than aria2c**, effectively similar performance under real internet conditions.

---

## 3. Loopback Benchmark (`127.0.0.1`)
#### Tested on NGINX server
### Command

```bash
hyperfine './JVDM http://127.0.0.1/testfile.bin' \
          'aria2c -x 16 http://127.0.0.1/testfile.bin'
```

### Results

| Tool   | Mean Time | Std Dev  | Min     | Max     | User Time | System Time |
|--------|-----------|----------|---------|---------|-----------|-------------|
| JVDM   | 1.072 s   | ±0.036 s | 1.010 s | 1.123 s | 0.637 s   | 2.090 s     |
| aria2c | 1.414 s   | ±0.067 s | 1.328 s | 1.571 s | 0.174 s   | 0.785 s     |

### Summary

**JVDM is ~1.32× faster than aria2c** on loopback interface.

---

## Overall Conclusion

* **JVDM consistently outperforms aria2c in local scenarios** (LAN & loopback).
* **Performance difference shrinks over the internet**, where network variability dominates.
* aria2c uses **less CPU/system resources**, while JVDM trades higher CPU usage for speed.

---

## Key Takeaways

* 🚀 Best performance gain: local transfers (up to **1.32× faster**)
* 🌐 Internet downloads: roughly equal performance
* ⚙️ Trade-off: higher CPU usage in JVDM vs lower resource usage in aria2c


