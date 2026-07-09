package me.exeos.jvmtpx.extractor;

import java.io.IOException;
import java.util.Map;

public interface IExtractor {

    Map<String, long[]> extract(byte[] input) throws IOException;
}
