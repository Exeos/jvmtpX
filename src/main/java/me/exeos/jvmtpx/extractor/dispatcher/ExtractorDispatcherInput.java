package me.exeos.jvmtpx.extractor.dispatcher;

import me.exeos.jvmtpx.Version;

public record ExtractorDispatcherInput(Version version, byte[] inputBytes, byte[] pepper) {
}
