package me.exeos.jvmtpx;

import me.exeos.jvmtpx.extractor.dispatcher.ExtractorDispatcherInput;
import me.exeos.jvmtpx.packer.dispatcher.PackerDispatcherInput;

import java.util.Optional;

public record CLIInput(Optional<ExtractorDispatcherInput> dispatcherInput, Optional<PackerDispatcherInput> packerInput) {
}
