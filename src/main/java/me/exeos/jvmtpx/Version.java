package me.exeos.jvmtpx;

public enum Version {
    V1_2_2("1.2.2", 122, false),
    V1_3_0("1.3.0", 130, true);

    public final String stringVersion;
    public final int intVersion;
    public final boolean requiresPepper;

    Version(String stringVersion, int intVersion, boolean requiresPepper) {
        this.stringVersion = stringVersion;
        this.intVersion = intVersion;
        this.requiresPepper = requiresPepper;
    }
}
