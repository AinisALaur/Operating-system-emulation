import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.example.ChannelDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class HddFormatTest {
  @TempDir File tempDir;

  @Test
  void testValidSingleBlock() throws IOException {
    File hdd = writeHdd("----\nabcd\n$RJG\nHALT\n$END\n----\n");
    assertDoesNotThrow(() -> ChannelDevice.validateHddFormat(hdd));
  }

  @Test
  void testValidMultipleBlocksSharedSeparator() throws IOException {
    File hdd = writeHdd("----\nabcd\n$RJG\nHALT\n$END\n----\nwxyz\n$RJG\nADDX\n$END\n----\n");
    assertDoesNotThrow(() -> ChannelDevice.validateHddFormat(hdd));
  }

  @Test
  void testValidMultipleBlocksDoubleSeparator() throws IOException {
    File hdd = writeHdd("----\nabcd\n$RJG\nHALT\n$END\n----\n----\nwxyz\n$RJG\nADDX\n$END\n----\n");
    assertDoesNotThrow(() -> ChannelDevice.validateHddFormat(hdd));
  }

  @Test
  void testRejectsShortFilename() throws IOException {
    File hdd = writeHdd("----\nabc\n$RJG\nHALT\n$END\n----\n");
    assertThrows(IllegalArgumentException.class, () -> ChannelDevice.validateHddFormat(hdd));
  }

  @Test
  void testRejectsMissingProgramStart() throws IOException {
    File hdd = writeHdd("----\nabcd\nHALT\n$END\n----\n");
    assertThrows(IllegalArgumentException.class, () -> ChannelDevice.validateHddFormat(hdd));
  }

  @Test
  void testRejectsMissingClosingSeparator() throws IOException {
    File hdd = writeHdd("----\nabcd\n$RJG\nHALT\n$END\n");
    assertThrows(IllegalArgumentException.class, () -> ChannelDevice.validateHddFormat(hdd));
  }

  @Test
  void testRejectsInvalidProgramLineLength() throws IOException {
    File hdd = writeHdd("----\nabcd\n$RJG\nHELLO\n$END\n----\n");
    assertThrows(IllegalArgumentException.class, () -> ChannelDevice.validateHddFormat(hdd));
  }

  @Test
  void testSelectedBlockWorksWhenAnotherBlockIsCorrupt() throws IOException {
    File hdd =
        writeHdd(
            "----\n"
                + "abcd\n$RJG\n$00$\n0008\nabcd\nefgh\n$50$\nLD00\nSWAP\nP500\nHALT\n"
                + "----\n----\n"
                + "efgh\n$RJG\n$00$\n0008\nabcd\nefgh\n$50$\nLD00\nSWAP\nP500\nHALT\n$END\n"
                + "----\n");

    assertDoesNotThrow(() -> ChannelDevice.validateHddFormat(hdd, "efgh"));
  }

  @Test
  void testSelectedCorruptBlockIsRejected() throws IOException {
    File hdd = writeHdd("----\nabcd\n$RJG\nHALT\n----\n" + "----\nefgh\n$RJG\nHALT\n$END\n----\n");

    assertThrows(
        IllegalArgumentException.class, () -> ChannelDevice.validateHddFormat(hdd, "abcd"));
  }

  private File writeHdd(String contents) throws IOException {
    File hdd = new File(tempDir, "hdd.txt");
    Files.writeString(hdd.toPath(), contents);
    return hdd;
  }
}
