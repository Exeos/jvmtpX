package me.exeos.jvmtpx.packer.dispatcher;

import me.exeos.jvmtpx.Version;

import java.util.Map;

public record PackerDispatcherInput(Version version, Map<String, byte[]> platformBinaries) {
}
