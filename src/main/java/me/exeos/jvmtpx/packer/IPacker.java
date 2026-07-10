package me.exeos.jvmtpx.packer;

import java.io.IOException;
import java.util.Map;

public interface IPacker {

    byte[] pack(Map<String, byte[]> platformBinaries) throws IOException;
}
