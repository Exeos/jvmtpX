# jvmtpX

Extracts platform-specific binaries from JVMTP binary bundle files, and packs platform binaries back into a JVMTP binary bundle file.

## Usage

### Extract

```sh
java -jar jvmtpx.jar extract/e <version> <path/to/jvmtp.binary> [pepper]
```

### Pack

```sh
java -jar jvmtpx.jar pack/p <version> [<path/to/platform.binary>]...
```

## Arguments

### Extract mode

| Argument                 | Description                                                                                           |
|--------------------------|-------------------------------------------------------------------------------------------------------|
| `extract` / `e`          | Run extractor mode.                                                                                   |
| `<version>`              | JVMTP version (string or int), e.g. `1.3.0` or `130`.                                                 |
| `<path/to/jvmtp.binary>` | Path to the JVMTP binary bundle file.                                                                 |
| `[pepper]`               | Optional comma-separated `byte[]`; required for versions that require pepper. Example: `"10,-7,1,0"`. |

### Pack mode

| Argument                         | Description                                                       |
|----------------------------------|-------------------------------------------------------------------|
| `pack` / `p`                     | Run packer mode.                                                  |
| `<version>`                      | JVMTP version (string or int), e.g. `1.3.0` or `130`.             |
| `[<path/to/platform.binary>]...` | One or more platform binary files to include in the packed bunde. |

## Platform filename for packing

When using `pack`, input filenames must match the platform keys expected by JVMTP.

`jvmtpX` uses the input file's **name** (`.` replaced with `/`) as the platform key.  
This mirrors extract mode behavior in reverse:

- **extract** writes filenames by using JVMTP platform keys and replacing `/` with `.`
- **pack** reads the filename and replaces `.` with `/` (effectively converted back to JVMTP platform keys)

So in practice, **files produced by `extract` are the expected naming format for `pack`.**

## Output

### Extract
Each extracted entry is written into the current working directory using the entry key with `/` replaced by `.`.

### Pack
Creates a packet file in the current working directory named like:

`jvmtpx-packet-<version>_<hash>`

Example:

`jvmtpx-packet-1.3.0_123456789`

## Supported Versions

| Version     | Requires Pepper |
|-------------|-----------------|
| 1.2.2 (122) | No              |
| 1.3.0 (130) | Yes             |