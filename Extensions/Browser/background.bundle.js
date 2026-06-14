(() => {
  var __create = Object.create;
  var __defProp = Object.defineProperty;
  var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
  var __getOwnPropNames = Object.getOwnPropertyNames;
  var __getProtoOf = Object.getPrototypeOf;
  var __hasOwnProp = Object.prototype.hasOwnProperty;
  var __commonJS = (cb, mod) => function __require() {
    return mod || (0, cb[__getOwnPropNames(cb)[0]])((mod = { exports: {} }).exports, mod), mod.exports;
  };
  var __copyProps = (to, from, except, desc) => {
    if (from && typeof from === "object" || typeof from === "function") {
      for (let key of __getOwnPropNames(from))
        if (!__hasOwnProp.call(to, key) && key !== except)
          __defProp(to, key, { get: () => from[key], enumerable: !(desc = __getOwnPropDesc(from, key)) || desc.enumerable });
    }
    return to;
  };
  var __toESM = (mod, isNodeMode, target) => (target = mod != null ? __create(__getProtoOf(mod)) : {}, __copyProps(
    // If the importer is in node compatibility mode or this is not an ESM
    // file that has been converted to a CommonJS file using a Babel-
    // compatible transform (i.e. "__esModule" has not been set), then set
    // "default" to the CommonJS "module.exports" for node compatibility.
    isNodeMode || !mod || !mod.__esModule ? __defProp(target, "default", { value: mod, enumerable: true }) : target,
    mod
  ));

  // ../../../../../node_modules/msgpack-lite/lib/buffer-global.js
  var require_buffer_global = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/buffer-global.js"(exports, module) {
      module.exports = c("undefined" !== typeof Buffer && Buffer) || c(exports.Buffer) || c("undefined" !== typeof window && window.Buffer) || exports.Buffer;
      function c(B) {
        return B && B.isBuffer && B;
      }
    }
  });

  // ../../../../../node_modules/isarray/index.js
  var require_isarray = __commonJS({
    "../../../../../node_modules/isarray/index.js"(exports, module) {
      var toString = {}.toString;
      module.exports = Array.isArray || function(arr) {
        return toString.call(arr) == "[object Array]";
      };
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/bufferish-array.js
  var require_bufferish_array = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/bufferish-array.js"(exports, module) {
      var Bufferish = require_bufferish();
      var exports = module.exports = alloc(0);
      exports.alloc = alloc;
      exports.concat = Bufferish.concat;
      exports.from = from;
      function alloc(size) {
        return new Array(size);
      }
      function from(value) {
        if (!Bufferish.isBuffer(value) && Bufferish.isView(value)) {
          value = Bufferish.Uint8Array.from(value);
        } else if (Bufferish.isArrayBuffer(value)) {
          value = new Uint8Array(value);
        } else if (typeof value === "string") {
          return Bufferish.from.call(exports, value);
        } else if (typeof value === "number") {
          throw new TypeError('"value" argument must not be a number');
        }
        return Array.prototype.slice.call(value);
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/bufferish-buffer.js
  var require_bufferish_buffer = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/bufferish-buffer.js"(exports, module) {
      var Bufferish = require_bufferish();
      var Buffer2 = Bufferish.global;
      var exports = module.exports = Bufferish.hasBuffer ? alloc(0) : [];
      exports.alloc = Bufferish.hasBuffer && Buffer2.alloc || alloc;
      exports.concat = Bufferish.concat;
      exports.from = from;
      function alloc(size) {
        return new Buffer2(size);
      }
      function from(value) {
        if (!Bufferish.isBuffer(value) && Bufferish.isView(value)) {
          value = Bufferish.Uint8Array.from(value);
        } else if (Bufferish.isArrayBuffer(value)) {
          value = new Uint8Array(value);
        } else if (typeof value === "string") {
          return Bufferish.from.call(exports, value);
        } else if (typeof value === "number") {
          throw new TypeError('"value" argument must not be a number');
        }
        if (Buffer2.from && Buffer2.from.length !== 1) {
          return Buffer2.from(value);
        } else {
          return new Buffer2(value);
        }
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/bufferish-uint8array.js
  var require_bufferish_uint8array = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/bufferish-uint8array.js"(exports, module) {
      var Bufferish = require_bufferish();
      var exports = module.exports = Bufferish.hasArrayBuffer ? alloc(0) : [];
      exports.alloc = alloc;
      exports.concat = Bufferish.concat;
      exports.from = from;
      function alloc(size) {
        return new Uint8Array(size);
      }
      function from(value) {
        if (Bufferish.isView(value)) {
          var byteOffset = value.byteOffset;
          var byteLength = value.byteLength;
          value = value.buffer;
          if (value.byteLength !== byteLength) {
            if (value.slice) {
              value = value.slice(byteOffset, byteOffset + byteLength);
            } else {
              value = new Uint8Array(value);
              if (value.byteLength !== byteLength) {
                value = Array.prototype.slice.call(value, byteOffset, byteOffset + byteLength);
              }
            }
          }
        } else if (typeof value === "string") {
          return Bufferish.from.call(exports, value);
        } else if (typeof value === "number") {
          throw new TypeError('"value" argument must not be a number');
        }
        return new Uint8Array(value);
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/buffer-lite.js
  var require_buffer_lite = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/buffer-lite.js"(exports) {
      exports.copy = copy;
      exports.toString = toString;
      exports.write = write;
      function write(string, offset) {
        var buffer = this;
        var index = offset || (offset |= 0);
        var length = string.length;
        var chr = 0;
        var i = 0;
        while (i < length) {
          chr = string.charCodeAt(i++);
          if (chr < 128) {
            buffer[index++] = chr;
          } else if (chr < 2048) {
            buffer[index++] = 192 | chr >>> 6;
            buffer[index++] = 128 | chr & 63;
          } else if (chr < 55296 || chr > 57343) {
            buffer[index++] = 224 | chr >>> 12;
            buffer[index++] = 128 | chr >>> 6 & 63;
            buffer[index++] = 128 | chr & 63;
          } else {
            chr = (chr - 55296 << 10 | string.charCodeAt(i++) - 56320) + 65536;
            buffer[index++] = 240 | chr >>> 18;
            buffer[index++] = 128 | chr >>> 12 & 63;
            buffer[index++] = 128 | chr >>> 6 & 63;
            buffer[index++] = 128 | chr & 63;
          }
        }
        return index - offset;
      }
      function toString(encoding, start, end) {
        var buffer = this;
        var index = start | 0;
        if (!end) end = buffer.length;
        var string = "";
        var chr = 0;
        while (index < end) {
          chr = buffer[index++];
          if (chr < 128) {
            string += String.fromCharCode(chr);
            continue;
          }
          if ((chr & 224) === 192) {
            chr = (chr & 31) << 6 | buffer[index++] & 63;
          } else if ((chr & 240) === 224) {
            chr = (chr & 15) << 12 | (buffer[index++] & 63) << 6 | buffer[index++] & 63;
          } else if ((chr & 248) === 240) {
            chr = (chr & 7) << 18 | (buffer[index++] & 63) << 12 | (buffer[index++] & 63) << 6 | buffer[index++] & 63;
          }
          if (chr >= 65536) {
            chr -= 65536;
            string += String.fromCharCode((chr >>> 10) + 55296, (chr & 1023) + 56320);
          } else {
            string += String.fromCharCode(chr);
          }
        }
        return string;
      }
      function copy(target, targetStart, start, end) {
        var i;
        if (!start) start = 0;
        if (!end && end !== 0) end = this.length;
        if (!targetStart) targetStart = 0;
        var len = end - start;
        if (target === this && start < targetStart && targetStart < end) {
          for (i = len - 1; i >= 0; i--) {
            target[i + targetStart] = this[i + start];
          }
        } else {
          for (i = 0; i < len; i++) {
            target[i + targetStart] = this[i + start];
          }
        }
        return len;
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/bufferish-proto.js
  var require_bufferish_proto = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/bufferish-proto.js"(exports) {
      var BufferLite = require_buffer_lite();
      exports.copy = copy;
      exports.slice = slice;
      exports.toString = toString;
      exports.write = gen("write");
      var Bufferish = require_bufferish();
      var Buffer2 = Bufferish.global;
      var isBufferShim = Bufferish.hasBuffer && "TYPED_ARRAY_SUPPORT" in Buffer2;
      var brokenTypedArray = isBufferShim && !Buffer2.TYPED_ARRAY_SUPPORT;
      function copy(target, targetStart, start, end) {
        var thisIsBuffer = Bufferish.isBuffer(this);
        var targetIsBuffer = Bufferish.isBuffer(target);
        if (thisIsBuffer && targetIsBuffer) {
          return this.copy(target, targetStart, start, end);
        } else if (!brokenTypedArray && !thisIsBuffer && !targetIsBuffer && Bufferish.isView(this) && Bufferish.isView(target)) {
          var buffer = start || end != null ? slice.call(this, start, end) : this;
          target.set(buffer, targetStart);
          return buffer.length;
        } else {
          return BufferLite.copy.call(this, target, targetStart, start, end);
        }
      }
      function slice(start, end) {
        var f = this.slice || !brokenTypedArray && this.subarray;
        if (f) return f.call(this, start, end);
        var target = Bufferish.alloc.call(this, end - start);
        copy.call(this, target, 0, start, end);
        return target;
      }
      function toString(encoding, start, end) {
        var f = !isBufferShim && Bufferish.isBuffer(this) ? this.toString : BufferLite.toString;
        return f.apply(this, arguments);
      }
      function gen(method) {
        return wrap;
        function wrap() {
          var f = this[method] || BufferLite[method];
          return f.apply(this, arguments);
        }
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/bufferish.js
  var require_bufferish = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/bufferish.js"(exports) {
      var Buffer2 = exports.global = require_buffer_global();
      var hasBuffer = exports.hasBuffer = Buffer2 && !!Buffer2.isBuffer;
      var hasArrayBuffer = exports.hasArrayBuffer = "undefined" !== typeof ArrayBuffer;
      var isArray = exports.isArray = require_isarray();
      exports.isArrayBuffer = hasArrayBuffer ? isArrayBuffer : _false;
      var isBuffer = exports.isBuffer = hasBuffer ? Buffer2.isBuffer : _false;
      var isView = exports.isView = hasArrayBuffer ? ArrayBuffer.isView || _is("ArrayBuffer", "buffer") : _false;
      exports.alloc = alloc;
      exports.concat = concat;
      exports.from = from;
      var BufferArray = exports.Array = require_bufferish_array();
      var BufferBuffer = exports.Buffer = require_bufferish_buffer();
      var BufferUint8Array = exports.Uint8Array = require_bufferish_uint8array();
      var BufferProto = exports.prototype = require_bufferish_proto();
      function from(value) {
        if (typeof value === "string") {
          return fromString.call(this, value);
        } else {
          return auto(this).from(value);
        }
      }
      function alloc(size) {
        return auto(this).alloc(size);
      }
      function concat(list, length) {
        if (!length) {
          length = 0;
          Array.prototype.forEach.call(list, dryrun);
        }
        var ref = this !== exports && this || list[0];
        var result = alloc.call(ref, length);
        var offset = 0;
        Array.prototype.forEach.call(list, append);
        return result;
        function dryrun(buffer) {
          length += buffer.length;
        }
        function append(buffer) {
          offset += BufferProto.copy.call(buffer, result, offset);
        }
      }
      var _isArrayBuffer = _is("ArrayBuffer");
      function isArrayBuffer(value) {
        return value instanceof ArrayBuffer || _isArrayBuffer(value);
      }
      function fromString(value) {
        var expected = value.length * 3;
        var that = alloc.call(this, expected);
        var actual = BufferProto.write.call(that, value);
        if (expected !== actual) {
          that = BufferProto.slice.call(that, 0, actual);
        }
        return that;
      }
      function auto(that) {
        return isBuffer(that) ? BufferBuffer : isView(that) ? BufferUint8Array : isArray(that) ? BufferArray : hasBuffer ? BufferBuffer : hasArrayBuffer ? BufferUint8Array : BufferArray;
      }
      function _false() {
        return false;
      }
      function _is(name, key) {
        name = "[object " + name + "]";
        return function(value) {
          return value != null && {}.toString.call(key ? value[key] : value) === name;
        };
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/ext-buffer.js
  var require_ext_buffer = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/ext-buffer.js"(exports) {
      exports.ExtBuffer = ExtBuffer;
      var Bufferish = require_bufferish();
      function ExtBuffer(buffer, type) {
        if (!(this instanceof ExtBuffer)) return new ExtBuffer(buffer, type);
        this.buffer = Bufferish.from(buffer);
        this.type = type;
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/ext-packer.js
  var require_ext_packer = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/ext-packer.js"(exports) {
      exports.setExtPackers = setExtPackers;
      var Bufferish = require_bufferish();
      var Buffer2 = Bufferish.global;
      var packTypedArray = Bufferish.Uint8Array.from;
      var _encode;
      var ERROR_COLUMNS = { name: 1, message: 1, stack: 1, columnNumber: 1, fileName: 1, lineNumber: 1 };
      function setExtPackers(codec) {
        codec.addExtPacker(14, Error, [packError, encode]);
        codec.addExtPacker(1, EvalError, [packError, encode]);
        codec.addExtPacker(2, RangeError, [packError, encode]);
        codec.addExtPacker(3, ReferenceError, [packError, encode]);
        codec.addExtPacker(4, SyntaxError, [packError, encode]);
        codec.addExtPacker(5, TypeError, [packError, encode]);
        codec.addExtPacker(6, URIError, [packError, encode]);
        codec.addExtPacker(10, RegExp, [packRegExp, encode]);
        codec.addExtPacker(11, Boolean, [packValueOf, encode]);
        codec.addExtPacker(12, String, [packValueOf, encode]);
        codec.addExtPacker(13, Date, [Number, encode]);
        codec.addExtPacker(15, Number, [packValueOf, encode]);
        if ("undefined" !== typeof Uint8Array) {
          codec.addExtPacker(17, Int8Array, packTypedArray);
          codec.addExtPacker(18, Uint8Array, packTypedArray);
          codec.addExtPacker(19, Int16Array, packTypedArray);
          codec.addExtPacker(20, Uint16Array, packTypedArray);
          codec.addExtPacker(21, Int32Array, packTypedArray);
          codec.addExtPacker(22, Uint32Array, packTypedArray);
          codec.addExtPacker(23, Float32Array, packTypedArray);
          if ("undefined" !== typeof Float64Array) {
            codec.addExtPacker(24, Float64Array, packTypedArray);
          }
          if ("undefined" !== typeof Uint8ClampedArray) {
            codec.addExtPacker(25, Uint8ClampedArray, packTypedArray);
          }
          codec.addExtPacker(26, ArrayBuffer, packTypedArray);
          codec.addExtPacker(29, DataView, packTypedArray);
        }
        if (Bufferish.hasBuffer) {
          codec.addExtPacker(27, Buffer2, Bufferish.from);
        }
      }
      function encode(input) {
        if (!_encode) _encode = require_encode().encode;
        return _encode(input);
      }
      function packValueOf(value) {
        return value.valueOf();
      }
      function packRegExp(value) {
        value = RegExp.prototype.toString.call(value).split("/");
        value.shift();
        var out = [value.pop()];
        out.unshift(value.join("/"));
        return out;
      }
      function packError(value) {
        var out = {};
        for (var key in ERROR_COLUMNS) {
          out[key] = value[key];
        }
        return out;
      }
    }
  });

  // ../../../../../node_modules/int64-buffer/int64-buffer.js
  var require_int64_buffer = __commonJS({
    "../../../../../node_modules/int64-buffer/int64-buffer.js"(exports) {
      var Uint64BE;
      var Int64BE;
      var Uint64LE;
      var Int64LE;
      !(function(exports2) {
        var UNDEFINED = "undefined";
        var BUFFER = UNDEFINED !== typeof Buffer && Buffer;
        var UINT8ARRAY = UNDEFINED !== typeof Uint8Array && Uint8Array;
        var ARRAYBUFFER = UNDEFINED !== typeof ArrayBuffer && ArrayBuffer;
        var ZERO = [0, 0, 0, 0, 0, 0, 0, 0];
        var isArray = Array.isArray || _isArray;
        var BIT32 = 4294967296;
        var BIT24 = 16777216;
        var storage;
        Uint64BE = factory("Uint64BE", true, true);
        Int64BE = factory("Int64BE", true, false);
        Uint64LE = factory("Uint64LE", false, true);
        Int64LE = factory("Int64LE", false, false);
        function factory(name, bigendian, unsigned) {
          var posH = bigendian ? 0 : 4;
          var posL = bigendian ? 4 : 0;
          var pos0 = bigendian ? 0 : 3;
          var pos1 = bigendian ? 1 : 2;
          var pos2 = bigendian ? 2 : 1;
          var pos3 = bigendian ? 3 : 0;
          var fromPositive = bigendian ? fromPositiveBE : fromPositiveLE;
          var fromNegative = bigendian ? fromNegativeBE : fromNegativeLE;
          var proto = Int64.prototype;
          var isName = "is" + name;
          var _isInt64 = "_" + isName;
          proto.buffer = void 0;
          proto.offset = 0;
          proto[_isInt64] = true;
          proto.toNumber = toNumber;
          proto.toString = toString;
          proto.toJSON = toNumber;
          proto.toArray = toArray;
          if (BUFFER) proto.toBuffer = toBuffer;
          if (UINT8ARRAY) proto.toArrayBuffer = toArrayBuffer;
          Int64[isName] = isInt64;
          exports2[name] = Int64;
          return Int64;
          function Int64(buffer, offset, value, raddix) {
            if (!(this instanceof Int64)) return new Int64(buffer, offset, value, raddix);
            return init(this, buffer, offset, value, raddix);
          }
          function isInt64(b) {
            return !!(b && b[_isInt64]);
          }
          function init(that, buffer, offset, value, raddix) {
            if (UINT8ARRAY && ARRAYBUFFER) {
              if (buffer instanceof ARRAYBUFFER) buffer = new UINT8ARRAY(buffer);
              if (value instanceof ARRAYBUFFER) value = new UINT8ARRAY(value);
            }
            if (!buffer && !offset && !value && !storage) {
              that.buffer = newArray(ZERO, 0);
              return;
            }
            if (!isValidBuffer(buffer, offset)) {
              var _storage = storage || Array;
              raddix = offset;
              value = buffer;
              offset = 0;
              buffer = new _storage(8);
            }
            that.buffer = buffer;
            that.offset = offset |= 0;
            if (UNDEFINED === typeof value) return;
            if ("string" === typeof value) {
              fromString(buffer, offset, value, raddix || 10);
            } else if (isValidBuffer(value, raddix)) {
              fromArray(buffer, offset, value, raddix);
            } else if ("number" === typeof raddix) {
              writeInt32(buffer, offset + posH, value);
              writeInt32(buffer, offset + posL, raddix);
            } else if (value > 0) {
              fromPositive(buffer, offset, value);
            } else if (value < 0) {
              fromNegative(buffer, offset, value);
            } else {
              fromArray(buffer, offset, ZERO, 0);
            }
          }
          function fromString(buffer, offset, str, raddix) {
            var pos = 0;
            var len = str.length;
            var high = 0;
            var low = 0;
            if (str[0] === "-") pos++;
            var sign = pos;
            while (pos < len) {
              var chr = parseInt(str[pos++], raddix);
              if (!(chr >= 0)) break;
              low = low * raddix + chr;
              high = high * raddix + Math.floor(low / BIT32);
              low %= BIT32;
            }
            if (sign) {
              high = ~high;
              if (low) {
                low = BIT32 - low;
              } else {
                high++;
              }
            }
            writeInt32(buffer, offset + posH, high);
            writeInt32(buffer, offset + posL, low);
          }
          function toNumber() {
            var buffer = this.buffer;
            var offset = this.offset;
            var high = readInt32(buffer, offset + posH);
            var low = readInt32(buffer, offset + posL);
            if (!unsigned) high |= 0;
            return high ? high * BIT32 + low : low;
          }
          function toString(radix) {
            var buffer = this.buffer;
            var offset = this.offset;
            var high = readInt32(buffer, offset + posH);
            var low = readInt32(buffer, offset + posL);
            var str = "";
            var sign = !unsigned && high & 2147483648;
            if (sign) {
              high = ~high;
              low = BIT32 - low;
            }
            radix = radix || 10;
            while (1) {
              var mod = high % radix * BIT32 + low;
              high = Math.floor(high / radix);
              low = Math.floor(mod / radix);
              str = (mod % radix).toString(radix) + str;
              if (!high && !low) break;
            }
            if (sign) {
              str = "-" + str;
            }
            return str;
          }
          function writeInt32(buffer, offset, value) {
            buffer[offset + pos3] = value & 255;
            value = value >> 8;
            buffer[offset + pos2] = value & 255;
            value = value >> 8;
            buffer[offset + pos1] = value & 255;
            value = value >> 8;
            buffer[offset + pos0] = value & 255;
          }
          function readInt32(buffer, offset) {
            return buffer[offset + pos0] * BIT24 + (buffer[offset + pos1] << 16) + (buffer[offset + pos2] << 8) + buffer[offset + pos3];
          }
        }
        function toArray(raw) {
          var buffer = this.buffer;
          var offset = this.offset;
          storage = null;
          if (raw !== false && offset === 0 && buffer.length === 8 && isArray(buffer)) return buffer;
          return newArray(buffer, offset);
        }
        function toBuffer(raw) {
          var buffer = this.buffer;
          var offset = this.offset;
          storage = BUFFER;
          if (raw !== false && offset === 0 && buffer.length === 8 && Buffer.isBuffer(buffer)) return buffer;
          var dest = new BUFFER(8);
          fromArray(dest, 0, buffer, offset);
          return dest;
        }
        function toArrayBuffer(raw) {
          var buffer = this.buffer;
          var offset = this.offset;
          var arrbuf = buffer.buffer;
          storage = UINT8ARRAY;
          if (raw !== false && offset === 0 && arrbuf instanceof ARRAYBUFFER && arrbuf.byteLength === 8) return arrbuf;
          var dest = new UINT8ARRAY(8);
          fromArray(dest, 0, buffer, offset);
          return dest.buffer;
        }
        function isValidBuffer(buffer, offset) {
          var len = buffer && buffer.length;
          offset |= 0;
          return len && offset + 8 <= len && "string" !== typeof buffer[offset];
        }
        function fromArray(destbuf, destoff, srcbuf, srcoff) {
          destoff |= 0;
          srcoff |= 0;
          for (var i = 0; i < 8; i++) {
            destbuf[destoff++] = srcbuf[srcoff++] & 255;
          }
        }
        function newArray(buffer, offset) {
          return Array.prototype.slice.call(buffer, offset, offset + 8);
        }
        function fromPositiveBE(buffer, offset, value) {
          var pos = offset + 8;
          while (pos > offset) {
            buffer[--pos] = value & 255;
            value /= 256;
          }
        }
        function fromNegativeBE(buffer, offset, value) {
          var pos = offset + 8;
          value++;
          while (pos > offset) {
            buffer[--pos] = -value & 255 ^ 255;
            value /= 256;
          }
        }
        function fromPositiveLE(buffer, offset, value) {
          var end = offset + 8;
          while (offset < end) {
            buffer[offset++] = value & 255;
            value /= 256;
          }
        }
        function fromNegativeLE(buffer, offset, value) {
          var end = offset + 8;
          value++;
          while (offset < end) {
            buffer[offset++] = -value & 255 ^ 255;
            value /= 256;
          }
        }
        function _isArray(val) {
          return !!val && "[object Array]" == Object.prototype.toString.call(val);
        }
      })(typeof exports === "object" && typeof exports.nodeName !== "string" ? exports : exports || {});
    }
  });

  // ../../../../../node_modules/ieee754/index.js
  var require_ieee754 = __commonJS({
    "../../../../../node_modules/ieee754/index.js"(exports) {
      exports.read = function(buffer, offset, isLE, mLen, nBytes) {
        var e, m;
        var eLen = nBytes * 8 - mLen - 1;
        var eMax = (1 << eLen) - 1;
        var eBias = eMax >> 1;
        var nBits = -7;
        var i = isLE ? nBytes - 1 : 0;
        var d = isLE ? -1 : 1;
        var s = buffer[offset + i];
        i += d;
        e = s & (1 << -nBits) - 1;
        s >>= -nBits;
        nBits += eLen;
        for (; nBits > 0; e = e * 256 + buffer[offset + i], i += d, nBits -= 8) {
        }
        m = e & (1 << -nBits) - 1;
        e >>= -nBits;
        nBits += mLen;
        for (; nBits > 0; m = m * 256 + buffer[offset + i], i += d, nBits -= 8) {
        }
        if (e === 0) {
          e = 1 - eBias;
        } else if (e === eMax) {
          return m ? NaN : (s ? -1 : 1) * Infinity;
        } else {
          m = m + Math.pow(2, mLen);
          e = e - eBias;
        }
        return (s ? -1 : 1) * m * Math.pow(2, e - mLen);
      };
      exports.write = function(buffer, value, offset, isLE, mLen, nBytes) {
        var e, m, c;
        var eLen = nBytes * 8 - mLen - 1;
        var eMax = (1 << eLen) - 1;
        var eBias = eMax >> 1;
        var rt = mLen === 23 ? Math.pow(2, -24) - Math.pow(2, -77) : 0;
        var i = isLE ? 0 : nBytes - 1;
        var d = isLE ? 1 : -1;
        var s = value < 0 || value === 0 && 1 / value < 0 ? 1 : 0;
        value = Math.abs(value);
        if (isNaN(value) || value === Infinity) {
          m = isNaN(value) ? 1 : 0;
          e = eMax;
        } else {
          e = Math.floor(Math.log(value) / Math.LN2);
          if (value * (c = Math.pow(2, -e)) < 1) {
            e--;
            c *= 2;
          }
          if (e + eBias >= 1) {
            value += rt / c;
          } else {
            value += rt * Math.pow(2, 1 - eBias);
          }
          if (value * c >= 2) {
            e++;
            c /= 2;
          }
          if (e + eBias >= eMax) {
            m = 0;
            e = eMax;
          } else if (e + eBias >= 1) {
            m = (value * c - 1) * Math.pow(2, mLen);
            e = e + eBias;
          } else {
            m = value * Math.pow(2, eBias - 1) * Math.pow(2, mLen);
            e = 0;
          }
        }
        for (; mLen >= 8; buffer[offset + i] = m & 255, i += d, m /= 256, mLen -= 8) {
        }
        e = e << mLen | m;
        eLen += mLen;
        for (; eLen > 0; buffer[offset + i] = e & 255, i += d, e /= 256, eLen -= 8) {
        }
        buffer[offset + i - d] |= s * 128;
      };
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/write-uint8.js
  var require_write_uint8 = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/write-uint8.js"(exports) {
      var constant = exports.uint8 = new Array(256);
      for (i = 0; i <= 255; i++) {
        constant[i] = write0(i);
      }
      var i;
      function write0(type) {
        return function(encoder) {
          var offset = encoder.reserve(1);
          encoder.buffer[offset] = type;
        };
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/write-token.js
  var require_write_token = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/write-token.js"(exports) {
      var ieee754 = require_ieee754();
      var Int64Buffer = require_int64_buffer();
      var Uint64BE = Int64Buffer.Uint64BE;
      var Int64BE = Int64Buffer.Int64BE;
      var uint8 = require_write_uint8().uint8;
      var Bufferish = require_bufferish();
      var Buffer2 = Bufferish.global;
      var IS_BUFFER_SHIM = Bufferish.hasBuffer && "TYPED_ARRAY_SUPPORT" in Buffer2;
      var NO_TYPED_ARRAY = IS_BUFFER_SHIM && !Buffer2.TYPED_ARRAY_SUPPORT;
      var Buffer_prototype = Bufferish.hasBuffer && Buffer2.prototype || {};
      exports.getWriteToken = getWriteToken;
      function getWriteToken(options) {
        if (options && options.uint8array) {
          return init_uint8array();
        } else if (NO_TYPED_ARRAY || Bufferish.hasBuffer && options && options.safe) {
          return init_safe();
        } else {
          return init_token();
        }
      }
      function init_uint8array() {
        var token = init_token();
        token[202] = writeN(202, 4, writeFloatBE);
        token[203] = writeN(203, 8, writeDoubleBE);
        return token;
      }
      function init_token() {
        var token = uint8.slice();
        token[196] = write1(196);
        token[197] = write2(197);
        token[198] = write4(198);
        token[199] = write1(199);
        token[200] = write2(200);
        token[201] = write4(201);
        token[202] = writeN(202, 4, Buffer_prototype.writeFloatBE || writeFloatBE, true);
        token[203] = writeN(203, 8, Buffer_prototype.writeDoubleBE || writeDoubleBE, true);
        token[204] = write1(204);
        token[205] = write2(205);
        token[206] = write4(206);
        token[207] = writeN(207, 8, writeUInt64BE);
        token[208] = write1(208);
        token[209] = write2(209);
        token[210] = write4(210);
        token[211] = writeN(211, 8, writeInt64BE);
        token[217] = write1(217);
        token[218] = write2(218);
        token[219] = write4(219);
        token[220] = write2(220);
        token[221] = write4(221);
        token[222] = write2(222);
        token[223] = write4(223);
        return token;
      }
      function init_safe() {
        var token = uint8.slice();
        token[196] = writeN(196, 1, Buffer2.prototype.writeUInt8);
        token[197] = writeN(197, 2, Buffer2.prototype.writeUInt16BE);
        token[198] = writeN(198, 4, Buffer2.prototype.writeUInt32BE);
        token[199] = writeN(199, 1, Buffer2.prototype.writeUInt8);
        token[200] = writeN(200, 2, Buffer2.prototype.writeUInt16BE);
        token[201] = writeN(201, 4, Buffer2.prototype.writeUInt32BE);
        token[202] = writeN(202, 4, Buffer2.prototype.writeFloatBE);
        token[203] = writeN(203, 8, Buffer2.prototype.writeDoubleBE);
        token[204] = writeN(204, 1, Buffer2.prototype.writeUInt8);
        token[205] = writeN(205, 2, Buffer2.prototype.writeUInt16BE);
        token[206] = writeN(206, 4, Buffer2.prototype.writeUInt32BE);
        token[207] = writeN(207, 8, writeUInt64BE);
        token[208] = writeN(208, 1, Buffer2.prototype.writeInt8);
        token[209] = writeN(209, 2, Buffer2.prototype.writeInt16BE);
        token[210] = writeN(210, 4, Buffer2.prototype.writeInt32BE);
        token[211] = writeN(211, 8, writeInt64BE);
        token[217] = writeN(217, 1, Buffer2.prototype.writeUInt8);
        token[218] = writeN(218, 2, Buffer2.prototype.writeUInt16BE);
        token[219] = writeN(219, 4, Buffer2.prototype.writeUInt32BE);
        token[220] = writeN(220, 2, Buffer2.prototype.writeUInt16BE);
        token[221] = writeN(221, 4, Buffer2.prototype.writeUInt32BE);
        token[222] = writeN(222, 2, Buffer2.prototype.writeUInt16BE);
        token[223] = writeN(223, 4, Buffer2.prototype.writeUInt32BE);
        return token;
      }
      function write1(type) {
        return function(encoder, value) {
          var offset = encoder.reserve(2);
          var buffer = encoder.buffer;
          buffer[offset++] = type;
          buffer[offset] = value;
        };
      }
      function write2(type) {
        return function(encoder, value) {
          var offset = encoder.reserve(3);
          var buffer = encoder.buffer;
          buffer[offset++] = type;
          buffer[offset++] = value >>> 8;
          buffer[offset] = value;
        };
      }
      function write4(type) {
        return function(encoder, value) {
          var offset = encoder.reserve(5);
          var buffer = encoder.buffer;
          buffer[offset++] = type;
          buffer[offset++] = value >>> 24;
          buffer[offset++] = value >>> 16;
          buffer[offset++] = value >>> 8;
          buffer[offset] = value;
        };
      }
      function writeN(type, len, method, noAssert) {
        return function(encoder, value) {
          var offset = encoder.reserve(len + 1);
          encoder.buffer[offset++] = type;
          method.call(encoder.buffer, value, offset, noAssert);
        };
      }
      function writeUInt64BE(value, offset) {
        new Uint64BE(this, offset, value);
      }
      function writeInt64BE(value, offset) {
        new Int64BE(this, offset, value);
      }
      function writeFloatBE(value, offset) {
        ieee754.write(this, value, offset, false, 23, 4);
      }
      function writeDoubleBE(value, offset) {
        ieee754.write(this, value, offset, false, 52, 8);
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/write-type.js
  var require_write_type = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/write-type.js"(exports) {
      var IS_ARRAY = require_isarray();
      var Int64Buffer = require_int64_buffer();
      var Uint64BE = Int64Buffer.Uint64BE;
      var Int64BE = Int64Buffer.Int64BE;
      var Bufferish = require_bufferish();
      var BufferProto = require_bufferish_proto();
      var WriteToken = require_write_token();
      var uint8 = require_write_uint8().uint8;
      var ExtBuffer = require_ext_buffer().ExtBuffer;
      var HAS_UINT8ARRAY = "undefined" !== typeof Uint8Array;
      var HAS_MAP = "undefined" !== typeof Map;
      var extmap = [];
      extmap[1] = 212;
      extmap[2] = 213;
      extmap[4] = 214;
      extmap[8] = 215;
      extmap[16] = 216;
      exports.getWriteType = getWriteType;
      function getWriteType(options) {
        var token = WriteToken.getWriteToken(options);
        var useraw = options && options.useraw;
        var binarraybuffer = HAS_UINT8ARRAY && options && options.binarraybuffer;
        var isBuffer = binarraybuffer ? Bufferish.isArrayBuffer : Bufferish.isBuffer;
        var bin = binarraybuffer ? bin_arraybuffer : bin_buffer;
        var usemap = HAS_MAP && options && options.usemap;
        var map = usemap ? map_to_map : obj_to_map;
        var writeType = {
          "boolean": bool,
          "function": nil,
          "number": number,
          "object": useraw ? object_raw : object,
          "string": _string(useraw ? raw_head_size : str_head_size),
          "symbol": nil,
          "undefined": nil
        };
        return writeType;
        function bool(encoder, value) {
          var type = value ? 195 : 194;
          token[type](encoder, value);
        }
        function number(encoder, value) {
          var ivalue = value | 0;
          var type;
          if (value !== ivalue) {
            type = 203;
            token[type](encoder, value);
            return;
          } else if (-32 <= ivalue && ivalue <= 127) {
            type = ivalue & 255;
          } else if (0 <= ivalue) {
            type = ivalue <= 255 ? 204 : ivalue <= 65535 ? 205 : 206;
          } else {
            type = -128 <= ivalue ? 208 : -32768 <= ivalue ? 209 : 210;
          }
          token[type](encoder, ivalue);
        }
        function uint64(encoder, value) {
          var type = 207;
          token[type](encoder, value.toArray());
        }
        function int64(encoder, value) {
          var type = 211;
          token[type](encoder, value.toArray());
        }
        function str_head_size(length) {
          return length < 32 ? 1 : length <= 255 ? 2 : length <= 65535 ? 3 : 5;
        }
        function raw_head_size(length) {
          return length < 32 ? 1 : length <= 65535 ? 3 : 5;
        }
        function _string(head_size) {
          return string;
          function string(encoder, value) {
            var length = value.length;
            var maxsize = 5 + length * 3;
            encoder.offset = encoder.reserve(maxsize);
            var buffer = encoder.buffer;
            var expected = head_size(length);
            var start = encoder.offset + expected;
            length = BufferProto.write.call(buffer, value, start);
            var actual = head_size(length);
            if (expected !== actual) {
              var targetStart = start + actual - expected;
              var end = start + length;
              BufferProto.copy.call(buffer, buffer, targetStart, start, end);
            }
            var type = actual === 1 ? 160 + length : actual <= 3 ? 215 + actual : 219;
            token[type](encoder, length);
            encoder.offset += length;
          }
        }
        function object(encoder, value) {
          if (value === null) return nil(encoder, value);
          if (isBuffer(value)) return bin(encoder, value);
          if (IS_ARRAY(value)) return array(encoder, value);
          if (Uint64BE.isUint64BE(value)) return uint64(encoder, value);
          if (Int64BE.isInt64BE(value)) return int64(encoder, value);
          var packer = encoder.codec.getExtPacker(value);
          if (packer) value = packer(value);
          if (value instanceof ExtBuffer) return ext(encoder, value);
          map(encoder, value);
        }
        function object_raw(encoder, value) {
          if (isBuffer(value)) return raw(encoder, value);
          object(encoder, value);
        }
        function nil(encoder, value) {
          var type = 192;
          token[type](encoder, value);
        }
        function array(encoder, value) {
          var length = value.length;
          var type = length < 16 ? 144 + length : length <= 65535 ? 220 : 221;
          token[type](encoder, length);
          var encode = encoder.codec.encode;
          for (var i = 0; i < length; i++) {
            encode(encoder, value[i]);
          }
        }
        function bin_buffer(encoder, value) {
          var length = value.length;
          var type = length < 255 ? 196 : length <= 65535 ? 197 : 198;
          token[type](encoder, length);
          encoder.send(value);
        }
        function bin_arraybuffer(encoder, value) {
          bin_buffer(encoder, new Uint8Array(value));
        }
        function ext(encoder, value) {
          var buffer = value.buffer;
          var length = buffer.length;
          var type = extmap[length] || (length < 255 ? 199 : length <= 65535 ? 200 : 201);
          token[type](encoder, length);
          uint8[value.type](encoder);
          encoder.send(buffer);
        }
        function obj_to_map(encoder, value) {
          var keys = Object.keys(value);
          var length = keys.length;
          var type = length < 16 ? 128 + length : length <= 65535 ? 222 : 223;
          token[type](encoder, length);
          var encode = encoder.codec.encode;
          keys.forEach(function(key) {
            encode(encoder, key);
            encode(encoder, value[key]);
          });
        }
        function map_to_map(encoder, value) {
          if (!(value instanceof Map)) return obj_to_map(encoder, value);
          var length = value.size;
          var type = length < 16 ? 128 + length : length <= 65535 ? 222 : 223;
          token[type](encoder, length);
          var encode = encoder.codec.encode;
          value.forEach(function(val, key, m) {
            encode(encoder, key);
            encode(encoder, val);
          });
        }
        function raw(encoder, value) {
          var length = value.length;
          var type = length < 32 ? 160 + length : length <= 65535 ? 218 : 219;
          token[type](encoder, length);
          encoder.send(value);
        }
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/codec-base.js
  var require_codec_base = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/codec-base.js"(exports) {
      var IS_ARRAY = require_isarray();
      exports.createCodec = createCodec;
      exports.install = install;
      exports.filter = filter;
      var Bufferish = require_bufferish();
      function Codec(options) {
        if (!(this instanceof Codec)) return new Codec(options);
        this.options = options;
        this.init();
      }
      Codec.prototype.init = function() {
        var options = this.options;
        if (options && options.uint8array) {
          this.bufferish = Bufferish.Uint8Array;
        }
        return this;
      };
      function install(props) {
        for (var key in props) {
          Codec.prototype[key] = add(Codec.prototype[key], props[key]);
        }
      }
      function add(a, b) {
        return a && b ? ab : a || b;
        function ab() {
          a.apply(this, arguments);
          return b.apply(this, arguments);
        }
      }
      function join(filters) {
        filters = filters.slice();
        return function(value) {
          return filters.reduce(iterator, value);
        };
        function iterator(value, filter2) {
          return filter2(value);
        }
      }
      function filter(filter2) {
        return IS_ARRAY(filter2) ? join(filter2) : filter2;
      }
      function createCodec(options) {
        return new Codec(options);
      }
      exports.preset = createCodec({ preset: true });
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/write-core.js
  var require_write_core = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/write-core.js"(exports) {
      var ExtBuffer = require_ext_buffer().ExtBuffer;
      var ExtPacker = require_ext_packer();
      var WriteType = require_write_type();
      var CodecBase = require_codec_base();
      CodecBase.install({
        addExtPacker,
        getExtPacker,
        init
      });
      exports.preset = init.call(CodecBase.preset);
      function getEncoder(options) {
        var writeType = WriteType.getWriteType(options);
        return encode;
        function encode(encoder, value) {
          var func = writeType[typeof value];
          if (!func) throw new Error('Unsupported type "' + typeof value + '": ' + value);
          func(encoder, value);
        }
      }
      function init() {
        var options = this.options;
        this.encode = getEncoder(options);
        if (options && options.preset) {
          ExtPacker.setExtPackers(this);
        }
        return this;
      }
      function addExtPacker(etype, Class, packer) {
        packer = CodecBase.filter(packer);
        var name = Class.name;
        if (name && name !== "Object") {
          var packers = this.extPackers || (this.extPackers = {});
          packers[name] = extPacker;
        } else {
          var list = this.extEncoderList || (this.extEncoderList = []);
          list.unshift([Class, extPacker]);
        }
        function extPacker(value) {
          if (packer) value = packer(value);
          return new ExtBuffer(value, etype);
        }
      }
      function getExtPacker(value) {
        var packers = this.extPackers || (this.extPackers = {});
        var c = value.constructor;
        var e = c && c.name && packers[c.name];
        if (e) return e;
        var list = this.extEncoderList || (this.extEncoderList = []);
        var len = list.length;
        for (var i = 0; i < len; i++) {
          var pair = list[i];
          if (c === pair[0]) return pair[1];
        }
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/flex-buffer.js
  var require_flex_buffer = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/flex-buffer.js"(exports) {
      exports.FlexDecoder = FlexDecoder;
      exports.FlexEncoder = FlexEncoder;
      var Bufferish = require_bufferish();
      var MIN_BUFFER_SIZE = 2048;
      var MAX_BUFFER_SIZE = 65536;
      var BUFFER_SHORTAGE = "BUFFER_SHORTAGE";
      function FlexDecoder() {
        if (!(this instanceof FlexDecoder)) return new FlexDecoder();
      }
      function FlexEncoder() {
        if (!(this instanceof FlexEncoder)) return new FlexEncoder();
      }
      FlexDecoder.mixin = mixinFactory(getDecoderMethods());
      FlexDecoder.mixin(FlexDecoder.prototype);
      FlexEncoder.mixin = mixinFactory(getEncoderMethods());
      FlexEncoder.mixin(FlexEncoder.prototype);
      function getDecoderMethods() {
        return {
          bufferish: Bufferish,
          write: write2,
          fetch,
          flush,
          push,
          pull,
          read,
          reserve,
          offset: 0
        };
        function write2(chunk) {
          var prev = this.offset ? Bufferish.prototype.slice.call(this.buffer, this.offset) : this.buffer;
          this.buffer = prev ? chunk ? this.bufferish.concat([prev, chunk]) : prev : chunk;
          this.offset = 0;
        }
        function flush() {
          while (this.offset < this.buffer.length) {
            var start = this.offset;
            var value;
            try {
              value = this.fetch();
            } catch (e) {
              if (e && e.message != BUFFER_SHORTAGE) throw e;
              this.offset = start;
              break;
            }
            this.push(value);
          }
        }
        function reserve(length) {
          var start = this.offset;
          var end = start + length;
          if (end > this.buffer.length) throw new Error(BUFFER_SHORTAGE);
          this.offset = end;
          return start;
        }
      }
      function getEncoderMethods() {
        return {
          bufferish: Bufferish,
          write,
          fetch: fetch2,
          flush,
          push,
          pull: pull2,
          read,
          reserve,
          send,
          maxBufferSize: MAX_BUFFER_SIZE,
          minBufferSize: MIN_BUFFER_SIZE,
          offset: 0,
          start: 0
        };
        function fetch2() {
          var start = this.start;
          if (start < this.offset) {
            var end = this.start = this.offset;
            return Bufferish.prototype.slice.call(this.buffer, start, end);
          }
        }
        function flush() {
          while (this.start < this.offset) {
            var value = this.fetch();
            if (value) this.push(value);
          }
        }
        function pull2() {
          var buffers = this.buffers || (this.buffers = []);
          var chunk = buffers.length > 1 ? this.bufferish.concat(buffers) : buffers[0];
          buffers.length = 0;
          return chunk;
        }
        function reserve(length) {
          var req = length | 0;
          if (this.buffer) {
            var size = this.buffer.length;
            var start = this.offset | 0;
            var end = start + req;
            if (end < size) {
              this.offset = end;
              return start;
            }
            this.flush();
            length = Math.max(length, Math.min(size * 2, this.maxBufferSize));
          }
          length = Math.max(length, this.minBufferSize);
          this.buffer = this.bufferish.alloc(length);
          this.start = 0;
          this.offset = req;
          return 0;
        }
        function send(buffer) {
          var length = buffer.length;
          if (length > this.minBufferSize) {
            this.flush();
            this.push(buffer);
          } else {
            var offset = this.reserve(length);
            Bufferish.prototype.copy.call(buffer, this.buffer, offset);
          }
        }
      }
      function write() {
        throw new Error("method not implemented: write()");
      }
      function fetch() {
        throw new Error("method not implemented: fetch()");
      }
      function read() {
        var length = this.buffers && this.buffers.length;
        if (!length) return this.fetch();
        this.flush();
        return this.pull();
      }
      function push(chunk) {
        var buffers = this.buffers || (this.buffers = []);
        buffers.push(chunk);
      }
      function pull() {
        var buffers = this.buffers || (this.buffers = []);
        return buffers.shift();
      }
      function mixinFactory(source) {
        return mixin;
        function mixin(target) {
          for (var key in source) {
            target[key] = source[key];
          }
          return target;
        }
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/encode-buffer.js
  var require_encode_buffer = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/encode-buffer.js"(exports) {
      exports.EncodeBuffer = EncodeBuffer;
      var preset = require_write_core().preset;
      var FlexEncoder = require_flex_buffer().FlexEncoder;
      FlexEncoder.mixin(EncodeBuffer.prototype);
      function EncodeBuffer(options) {
        if (!(this instanceof EncodeBuffer)) return new EncodeBuffer(options);
        if (options) {
          this.options = options;
          if (options.codec) {
            var codec = this.codec = options.codec;
            if (codec.bufferish) this.bufferish = codec.bufferish;
          }
        }
      }
      EncodeBuffer.prototype.codec = preset;
      EncodeBuffer.prototype.write = function(input) {
        this.codec.encode(this, input);
      };
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/encode.js
  var require_encode = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/encode.js"(exports) {
      exports.encode = encode;
      var EncodeBuffer = require_encode_buffer().EncodeBuffer;
      function encode(input, options) {
        var encoder = new EncodeBuffer(options);
        encoder.write(input);
        return encoder.read();
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/ext-unpacker.js
  var require_ext_unpacker = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/ext-unpacker.js"(exports) {
      exports.setExtUnpackers = setExtUnpackers;
      var Bufferish = require_bufferish();
      var Buffer2 = Bufferish.global;
      var _decode;
      var ERROR_COLUMNS = { name: 1, message: 1, stack: 1, columnNumber: 1, fileName: 1, lineNumber: 1 };
      function setExtUnpackers(codec) {
        codec.addExtUnpacker(14, [decode, unpackError(Error)]);
        codec.addExtUnpacker(1, [decode, unpackError(EvalError)]);
        codec.addExtUnpacker(2, [decode, unpackError(RangeError)]);
        codec.addExtUnpacker(3, [decode, unpackError(ReferenceError)]);
        codec.addExtUnpacker(4, [decode, unpackError(SyntaxError)]);
        codec.addExtUnpacker(5, [decode, unpackError(TypeError)]);
        codec.addExtUnpacker(6, [decode, unpackError(URIError)]);
        codec.addExtUnpacker(10, [decode, unpackRegExp]);
        codec.addExtUnpacker(11, [decode, unpackClass(Boolean)]);
        codec.addExtUnpacker(12, [decode, unpackClass(String)]);
        codec.addExtUnpacker(13, [decode, unpackClass(Date)]);
        codec.addExtUnpacker(15, [decode, unpackClass(Number)]);
        if ("undefined" !== typeof Uint8Array) {
          codec.addExtUnpacker(17, unpackClass(Int8Array));
          codec.addExtUnpacker(18, unpackClass(Uint8Array));
          codec.addExtUnpacker(19, [unpackArrayBuffer, unpackClass(Int16Array)]);
          codec.addExtUnpacker(20, [unpackArrayBuffer, unpackClass(Uint16Array)]);
          codec.addExtUnpacker(21, [unpackArrayBuffer, unpackClass(Int32Array)]);
          codec.addExtUnpacker(22, [unpackArrayBuffer, unpackClass(Uint32Array)]);
          codec.addExtUnpacker(23, [unpackArrayBuffer, unpackClass(Float32Array)]);
          if ("undefined" !== typeof Float64Array) {
            codec.addExtUnpacker(24, [unpackArrayBuffer, unpackClass(Float64Array)]);
          }
          if ("undefined" !== typeof Uint8ClampedArray) {
            codec.addExtUnpacker(25, unpackClass(Uint8ClampedArray));
          }
          codec.addExtUnpacker(26, unpackArrayBuffer);
          codec.addExtUnpacker(29, [unpackArrayBuffer, unpackClass(DataView)]);
        }
        if (Bufferish.hasBuffer) {
          codec.addExtUnpacker(27, unpackClass(Buffer2));
        }
      }
      function decode(input) {
        if (!_decode) _decode = require_decode().decode;
        return _decode(input);
      }
      function unpackRegExp(value) {
        return RegExp.apply(null, value);
      }
      function unpackError(Class) {
        return function(value) {
          var out = new Class();
          for (var key in ERROR_COLUMNS) {
            out[key] = value[key];
          }
          return out;
        };
      }
      function unpackClass(Class) {
        return function(value) {
          return new Class(value);
        };
      }
      function unpackArrayBuffer(value) {
        return new Uint8Array(value).buffer;
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/read-format.js
  var require_read_format = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/read-format.js"(exports) {
      var ieee754 = require_ieee754();
      var Int64Buffer = require_int64_buffer();
      var Uint64BE = Int64Buffer.Uint64BE;
      var Int64BE = Int64Buffer.Int64BE;
      exports.getReadFormat = getReadFormat;
      exports.readUint8 = uint8;
      var Bufferish = require_bufferish();
      var BufferProto = require_bufferish_proto();
      var HAS_MAP = "undefined" !== typeof Map;
      var NO_ASSERT = true;
      function getReadFormat(options) {
        var binarraybuffer = Bufferish.hasArrayBuffer && options && options.binarraybuffer;
        var int64 = options && options.int64;
        var usemap = HAS_MAP && options && options.usemap;
        var readFormat = {
          map: usemap ? map_to_map : map_to_obj,
          array,
          str,
          bin: binarraybuffer ? bin_arraybuffer : bin_buffer,
          ext,
          uint8,
          uint16,
          uint32,
          uint64: read(8, int64 ? readUInt64BE_int64 : readUInt64BE),
          int8,
          int16,
          int32,
          int64: read(8, int64 ? readInt64BE_int64 : readInt64BE),
          float32: read(4, readFloatBE),
          float64: read(8, readDoubleBE)
        };
        return readFormat;
      }
      function map_to_obj(decoder, len) {
        var value = {};
        var i;
        var k = new Array(len);
        var v = new Array(len);
        var decode = decoder.codec.decode;
        for (i = 0; i < len; i++) {
          k[i] = decode(decoder);
          v[i] = decode(decoder);
        }
        for (i = 0; i < len; i++) {
          value[k[i]] = v[i];
        }
        return value;
      }
      function map_to_map(decoder, len) {
        var value = /* @__PURE__ */ new Map();
        var i;
        var k = new Array(len);
        var v = new Array(len);
        var decode = decoder.codec.decode;
        for (i = 0; i < len; i++) {
          k[i] = decode(decoder);
          v[i] = decode(decoder);
        }
        for (i = 0; i < len; i++) {
          value.set(k[i], v[i]);
        }
        return value;
      }
      function array(decoder, len) {
        var value = new Array(len);
        var decode = decoder.codec.decode;
        for (var i = 0; i < len; i++) {
          value[i] = decode(decoder);
        }
        return value;
      }
      function str(decoder, len) {
        var start = decoder.reserve(len);
        var end = start + len;
        return BufferProto.toString.call(decoder.buffer, "utf-8", start, end);
      }
      function bin_buffer(decoder, len) {
        var start = decoder.reserve(len);
        var end = start + len;
        var buf = BufferProto.slice.call(decoder.buffer, start, end);
        return Bufferish.from(buf);
      }
      function bin_arraybuffer(decoder, len) {
        var start = decoder.reserve(len);
        var end = start + len;
        var buf = BufferProto.slice.call(decoder.buffer, start, end);
        return Bufferish.Uint8Array.from(buf).buffer;
      }
      function ext(decoder, len) {
        var start = decoder.reserve(len + 1);
        var type = decoder.buffer[start++];
        var end = start + len;
        var unpack = decoder.codec.getExtUnpacker(type);
        if (!unpack) throw new Error("Invalid ext type: " + (type ? "0x" + type.toString(16) : type));
        var buf = BufferProto.slice.call(decoder.buffer, start, end);
        return unpack(buf);
      }
      function uint8(decoder) {
        var start = decoder.reserve(1);
        return decoder.buffer[start];
      }
      function int8(decoder) {
        var start = decoder.reserve(1);
        var value = decoder.buffer[start];
        return value & 128 ? value - 256 : value;
      }
      function uint16(decoder) {
        var start = decoder.reserve(2);
        var buffer = decoder.buffer;
        return buffer[start++] << 8 | buffer[start];
      }
      function int16(decoder) {
        var start = decoder.reserve(2);
        var buffer = decoder.buffer;
        var value = buffer[start++] << 8 | buffer[start];
        return value & 32768 ? value - 65536 : value;
      }
      function uint32(decoder) {
        var start = decoder.reserve(4);
        var buffer = decoder.buffer;
        return buffer[start++] * 16777216 + (buffer[start++] << 16) + (buffer[start++] << 8) + buffer[start];
      }
      function int32(decoder) {
        var start = decoder.reserve(4);
        var buffer = decoder.buffer;
        return buffer[start++] << 24 | buffer[start++] << 16 | buffer[start++] << 8 | buffer[start];
      }
      function read(len, method) {
        return function(decoder) {
          var start = decoder.reserve(len);
          return method.call(decoder.buffer, start, NO_ASSERT);
        };
      }
      function readUInt64BE(start) {
        return new Uint64BE(this, start).toNumber();
      }
      function readInt64BE(start) {
        return new Int64BE(this, start).toNumber();
      }
      function readUInt64BE_int64(start) {
        return new Uint64BE(this, start);
      }
      function readInt64BE_int64(start) {
        return new Int64BE(this, start);
      }
      function readFloatBE(start) {
        return ieee754.read(this, start, false, 23, 4);
      }
      function readDoubleBE(start) {
        return ieee754.read(this, start, false, 52, 8);
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/read-token.js
  var require_read_token = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/read-token.js"(exports) {
      var ReadFormat = require_read_format();
      exports.getReadToken = getReadToken;
      function getReadToken(options) {
        var format = ReadFormat.getReadFormat(options);
        if (options && options.useraw) {
          return init_useraw(format);
        } else {
          return init_token(format);
        }
      }
      function init_token(format) {
        var i;
        var token = new Array(256);
        for (i = 0; i <= 127; i++) {
          token[i] = constant(i);
        }
        for (i = 128; i <= 143; i++) {
          token[i] = fix(i - 128, format.map);
        }
        for (i = 144; i <= 159; i++) {
          token[i] = fix(i - 144, format.array);
        }
        for (i = 160; i <= 191; i++) {
          token[i] = fix(i - 160, format.str);
        }
        token[192] = constant(null);
        token[193] = null;
        token[194] = constant(false);
        token[195] = constant(true);
        token[196] = flex(format.uint8, format.bin);
        token[197] = flex(format.uint16, format.bin);
        token[198] = flex(format.uint32, format.bin);
        token[199] = flex(format.uint8, format.ext);
        token[200] = flex(format.uint16, format.ext);
        token[201] = flex(format.uint32, format.ext);
        token[202] = format.float32;
        token[203] = format.float64;
        token[204] = format.uint8;
        token[205] = format.uint16;
        token[206] = format.uint32;
        token[207] = format.uint64;
        token[208] = format.int8;
        token[209] = format.int16;
        token[210] = format.int32;
        token[211] = format.int64;
        token[212] = fix(1, format.ext);
        token[213] = fix(2, format.ext);
        token[214] = fix(4, format.ext);
        token[215] = fix(8, format.ext);
        token[216] = fix(16, format.ext);
        token[217] = flex(format.uint8, format.str);
        token[218] = flex(format.uint16, format.str);
        token[219] = flex(format.uint32, format.str);
        token[220] = flex(format.uint16, format.array);
        token[221] = flex(format.uint32, format.array);
        token[222] = flex(format.uint16, format.map);
        token[223] = flex(format.uint32, format.map);
        for (i = 224; i <= 255; i++) {
          token[i] = constant(i - 256);
        }
        return token;
      }
      function init_useraw(format) {
        var i;
        var token = init_token(format).slice();
        token[217] = token[196];
        token[218] = token[197];
        token[219] = token[198];
        for (i = 160; i <= 191; i++) {
          token[i] = fix(i - 160, format.bin);
        }
        return token;
      }
      function constant(value) {
        return function() {
          return value;
        };
      }
      function flex(lenFunc, decodeFunc) {
        return function(decoder) {
          var len = lenFunc(decoder);
          return decodeFunc(decoder, len);
        };
      }
      function fix(len, method) {
        return function(decoder) {
          return method(decoder, len);
        };
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/read-core.js
  var require_read_core = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/read-core.js"(exports) {
      var ExtBuffer = require_ext_buffer().ExtBuffer;
      var ExtUnpacker = require_ext_unpacker();
      var readUint8 = require_read_format().readUint8;
      var ReadToken = require_read_token();
      var CodecBase = require_codec_base();
      CodecBase.install({
        addExtUnpacker,
        getExtUnpacker,
        init
      });
      exports.preset = init.call(CodecBase.preset);
      function getDecoder(options) {
        var readToken = ReadToken.getReadToken(options);
        return decode;
        function decode(decoder) {
          var type = readUint8(decoder);
          var func = readToken[type];
          if (!func) throw new Error("Invalid type: " + (type ? "0x" + type.toString(16) : type));
          return func(decoder);
        }
      }
      function init() {
        var options = this.options;
        this.decode = getDecoder(options);
        if (options && options.preset) {
          ExtUnpacker.setExtUnpackers(this);
        }
        return this;
      }
      function addExtUnpacker(etype, unpacker) {
        var unpackers = this.extUnpackers || (this.extUnpackers = []);
        unpackers[etype] = CodecBase.filter(unpacker);
      }
      function getExtUnpacker(type) {
        var unpackers = this.extUnpackers || (this.extUnpackers = []);
        return unpackers[type] || extUnpacker;
        function extUnpacker(buffer) {
          return new ExtBuffer(buffer, type);
        }
      }
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/decode-buffer.js
  var require_decode_buffer = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/decode-buffer.js"(exports) {
      exports.DecodeBuffer = DecodeBuffer;
      var preset = require_read_core().preset;
      var FlexDecoder = require_flex_buffer().FlexDecoder;
      FlexDecoder.mixin(DecodeBuffer.prototype);
      function DecodeBuffer(options) {
        if (!(this instanceof DecodeBuffer)) return new DecodeBuffer(options);
        if (options) {
          this.options = options;
          if (options.codec) {
            var codec = this.codec = options.codec;
            if (codec.bufferish) this.bufferish = codec.bufferish;
          }
        }
      }
      DecodeBuffer.prototype.codec = preset;
      DecodeBuffer.prototype.fetch = function() {
        return this.codec.decode(this);
      };
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/decode.js
  var require_decode = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/decode.js"(exports) {
      exports.decode = decode;
      var DecodeBuffer = require_decode_buffer().DecodeBuffer;
      function decode(input, options) {
        var decoder = new DecodeBuffer(options);
        decoder.write(input);
        return decoder.read();
      }
    }
  });

  // ../../../../../node_modules/event-lite/event-lite.js
  var require_event_lite = __commonJS({
    "../../../../../node_modules/event-lite/event-lite.js"(exports, module) {
      function EventLite() {
        if (!(this instanceof EventLite)) return new EventLite();
      }
      (function(EventLite2) {
        if ("undefined" !== typeof module) module.exports = EventLite2;
        var LISTENERS = "listeners";
        var methods = {
          on,
          once,
          off,
          emit
        };
        mixin(EventLite2.prototype);
        EventLite2.mixin = mixin;
        function mixin(target) {
          for (var key in methods) {
            target[key] = methods[key];
          }
          return target;
        }
        function on(type, func) {
          getListeners(this, type).push(func);
          return this;
        }
        function once(type, func) {
          var that = this;
          wrap.originalListener = func;
          getListeners(that, type).push(wrap);
          return that;
          function wrap() {
            off.call(that, type, wrap);
            func.apply(this, arguments);
          }
        }
        function off(type, func) {
          var that = this;
          var listners;
          if (!arguments.length) {
            delete that[LISTENERS];
          } else if (!func) {
            listners = that[LISTENERS];
            if (listners) {
              delete listners[type];
              if (!Object.keys(listners).length) return off.call(that);
            }
          } else {
            listners = getListeners(that, type, true);
            if (listners) {
              listners = listners.filter(ne);
              if (!listners.length) return off.call(that, type);
              that[LISTENERS][type] = listners;
            }
          }
          return that;
          function ne(test) {
            return test !== func && test.originalListener !== func;
          }
        }
        function emit(type, value) {
          var that = this;
          var listeners = getListeners(that, type, true);
          if (!listeners) return false;
          var arglen = arguments.length;
          if (arglen === 1) {
            listeners.forEach(zeroarg);
          } else if (arglen === 2) {
            listeners.forEach(onearg);
          } else {
            var args = Array.prototype.slice.call(arguments, 1);
            listeners.forEach(moreargs);
          }
          return !!listeners.length;
          function zeroarg(func) {
            func.call(that);
          }
          function onearg(func) {
            func.call(that, value);
          }
          function moreargs(func) {
            func.apply(that, args);
          }
        }
        function getListeners(that, type, readonly) {
          if (readonly && !that[LISTENERS]) return;
          var listeners = that[LISTENERS] || (that[LISTENERS] = {});
          return listeners[type] || (listeners[type] = []);
        }
      })(EventLite);
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/encoder.js
  var require_encoder = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/encoder.js"(exports) {
      exports.Encoder = Encoder;
      var EventLite = require_event_lite();
      var EncodeBuffer = require_encode_buffer().EncodeBuffer;
      function Encoder(options) {
        if (!(this instanceof Encoder)) return new Encoder(options);
        EncodeBuffer.call(this, options);
      }
      Encoder.prototype = new EncodeBuffer();
      EventLite.mixin(Encoder.prototype);
      Encoder.prototype.encode = function(chunk) {
        this.write(chunk);
        this.emit("data", this.read());
      };
      Encoder.prototype.end = function(chunk) {
        if (arguments.length) this.encode(chunk);
        this.flush();
        this.emit("end");
      };
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/decoder.js
  var require_decoder = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/decoder.js"(exports) {
      exports.Decoder = Decoder;
      var EventLite = require_event_lite();
      var DecodeBuffer = require_decode_buffer().DecodeBuffer;
      function Decoder(options) {
        if (!(this instanceof Decoder)) return new Decoder(options);
        DecodeBuffer.call(this, options);
      }
      Decoder.prototype = new DecodeBuffer();
      EventLite.mixin(Decoder.prototype);
      Decoder.prototype.decode = function(chunk) {
        if (arguments.length) this.write(chunk);
        this.flush();
      };
      Decoder.prototype.push = function(chunk) {
        this.emit("data", chunk);
      };
      Decoder.prototype.end = function(chunk) {
        this.decode(chunk);
        this.emit("end");
      };
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/ext.js
  var require_ext = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/ext.js"(exports) {
      require_read_core();
      require_write_core();
      exports.createCodec = require_codec_base().createCodec;
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/codec.js
  var require_codec = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/codec.js"(exports) {
      require_read_core();
      require_write_core();
      exports.codec = {
        preset: require_codec_base().preset
      };
    }
  });

  // ../../../../../node_modules/msgpack-lite/lib/browser.js
  var require_browser = __commonJS({
    "../../../../../node_modules/msgpack-lite/lib/browser.js"(exports) {
      exports.encode = require_encode().encode;
      exports.decode = require_decode().decode;
      exports.Encoder = require_encoder().Encoder;
      exports.Decoder = require_decoder().Decoder;
      exports.createCodec = require_ext().createCodec;
      exports.codec = require_codec().codec;
    }
  });

  // background.js
  var import_msgpack_lite = __toESM(require_browser());
  var WEBSOCKET_URL = "ws://127.0.0.1:20506/ws";
  var ws = null;
  var retryCount = 0;
  var isConnecting = false;
  var MAX_RETRYS = 50;
  var RETRY_DELAY = 5 * 1e3 * 60;
  chrome.runtime.onInstalled.addListener(() => {
    chrome.contextMenus.create({
      id: "send-for-jvdm",
      title: "Connecting to server...",
      contexts: ["link", "video", "audio"]
    });
    connectWebSocket();
  });
  chrome.contextMenus.onClicked.addListener((info, tab) => {
    if (info.menuItemId === "send-for-jvdm") {
      if (ws && ws.readyState === WebSocket.OPEN) {
        const downloadUrl = info.linkUrl || info.srcUrl;
        if (downloadUrl) {
          triggerDownload(downloadUrl);
        }
      } else {
        retryCount = 0;
        if (!isConnecting) {
          connectWebSocket();
        }
      }
    }
  });
  chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  });
  function triggerDownload(url) {
    sendLinks([
      [0, url]
    ]);
  }
  function updateContextMenu(title) {
    chrome.contextMenus.update("send-for-jvdm", { title });
  }
  function connectWebSocket() {
    isConnecting = true;
    updateContextMenu("Connecting to server...");
    ws = new WebSocket(WEBSOCKET_URL);
    ws.addEventListener("open", (lis) => {
      console.log("Connected!");
      retryCount = 0;
      isConnecting = false;
      updateContextMenu("Force JVDM to download");
    });
    ws.addEventListener("message", (lis) => {
      console.log(lis.data);
    });
    ws.addEventListener("error", (lis) => {
      console.error("Server Is Offline!");
    });
    ws.addEventListener("close", (lis) => {
      isConnecting = false;
      if (retryCount < MAX_RETRYS) {
        updateContextMenu(`Retry connection (${retryCount + 1}/${MAX_RETRYS})`);
        retryCount++;
        setTimeout(() => {
          if (!isConnecting && (!ws || ws.readyState !== WebSocket.OPEN)) {
            connectWebSocket();
          }
        }, RETRY_DELAY);
      } else {
        updateContextMenu("Connection failed. Click to retry connection");
      }
    });
  }
  function sendLinks(list) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      const encoded = import_msgpack_lite.default.encode(list);
      ws.send(encoded);
    }
  }
})();
/*! Bundled license information:

ieee754/index.js:
  (*! ieee754. BSD-3-Clause License. Feross Aboukhadijeh <https://feross.org/opensource> *)

event-lite/event-lite.js:
  (**
   * event-lite.js - Light-weight EventEmitter (less than 1KB when gzipped)
   *
   * @copyright Yusuke Kawasaki
   * @license MIT
   * @constructor
   * @see https://github.com/kawanet/event-lite
   * @see http://kawanet.github.io/event-lite/EventLite.html
   * @example
   * var EventLite = require("event-lite");
   *
   * function MyClass() {...}             // your class
   *
   * EventLite.mixin(MyClass.prototype);  // import event methods
   *
   * var obj = new MyClass();
   * obj.on("foo", function() {...});     // add event listener
   * obj.once("bar", function() {...});   // add one-time event listener
   * obj.emit("foo");                     // dispatch event
   * obj.emit("bar");                     // dispatch another event
   * obj.off("foo");                      // remove event listener
   *)
*/
