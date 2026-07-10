package me.exeos.jvmtpx.extractor;

import java.util.Map;

public record ExtractorResult(byte[] blob, Map<String, long[]> platformOffsets) {
}
