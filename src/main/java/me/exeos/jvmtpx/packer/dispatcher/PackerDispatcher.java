package me.exeos.jvmtpx.packer.dispatcher;

import me.exeos.jvmtpx.packer.impl.Packer122;
import me.exeos.jvmtpx.packer.impl.Packer130;

import java.io.IOException;
import java.util.Optional;

public class PackerDispatcher {

    public static Optional<byte[]> dispatch(PackerDispatcherInput input) {
        byte[] output = null;

        try {
            output = switch (input.version()) {
                case V1_2_2 -> new Packer122().pack(input.platformBinaries());
                case V1_3_0 -> new Packer130(input.pepper()).pack(input.platformBinaries());
            };
        } catch (IOException e) {
            System.out.println("Failed to pack platform binaries");
        }

        return output == null ? Optional.empty() : Optional.of(output);
    }
}
