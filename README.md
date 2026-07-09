# jvmtpX

Extracts platform-specific binaries from JVMTP binary bundle files.

## Usage

```sh
java -jar jvmtpX.jar <path/to/bundle> <version> [pepper]
```

### Arguments

| Argument           | Description                                                                                                                                   |
|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `<path/to/bundle>` | Path to the JVMTP binary bundle file to extract.                                                                                              |
| `<version>`        | The JVMTP version the bundle was created with.                                                                                                |
| `[pepper]`         | Optional. Required for some versions. A comma-separated `byte[]`, for example: `"106,118,109,116,112,45,114,99,-79,-98,55,92,-46,64,-89,31"`. |

## Supported Versions

| Version     | Requires Pepper |
|-------------|-----------------|
| 1.2.2 (122) | No              |
| 1.3.0 (130) | Yes             |
