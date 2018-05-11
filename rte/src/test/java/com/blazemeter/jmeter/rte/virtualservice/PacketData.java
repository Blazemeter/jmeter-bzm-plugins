package com.blazemeter.jmeter.rte.virtualservice;

import com.google.common.io.BaseEncoding;

import java.util.Arrays;

public class PacketData {

  private final byte[] bytes;

  private PacketData(byte[] bytes) {
    this.bytes = bytes;
  }

  public String toString() {
    return BaseEncoding.base16().encode(bytes);
  }

  public static PacketData fromHexDump(String hexDump) {
    return new PacketData(BaseEncoding.base16().decode(hexDump.toUpperCase()));
  }

  public static PacketData fromBytes(byte[] bytes, int offset, int length) {
    return new PacketData(Arrays.copyOfRange(bytes, offset, offset + length));
  }

  public byte[] getBytes() {
    return bytes;
  }

}
