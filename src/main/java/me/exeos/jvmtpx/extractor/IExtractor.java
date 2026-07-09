package me.exeos.jvmtpx.extractor;

import java.io.IOException;

public interface IExtractor {

    ExtractorResult extract(byte[] input) throws IOException;
}
