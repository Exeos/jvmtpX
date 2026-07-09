package me.exeos.jvmtpx.extractor.dispatchor;

import me.exeos.jvmtpx.extractor.Version;

public record DispatcherInput(Version version, byte[] inputBytes, byte[] pepper) {
}
