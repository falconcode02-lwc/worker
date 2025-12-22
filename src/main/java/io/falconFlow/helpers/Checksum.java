package io.falconFlow.helpers;

import java.security.MessageDigest;
import java.util.HexFormat;

public class Checksum {

  public static String checksum(byte[] data, Algorithm algorithm) throws Exception {
    MessageDigest md = MessageDigest.getInstance(String.valueOf(algorithm).replace("_", "-"));
    byte[] digest = md.digest(data);
    return HexFormat.of().formatHex(digest); // Java 17+ (or use manual conversion)
  }

  public enum Algorithm {
    MD5,
    SHA_256
  }
}
