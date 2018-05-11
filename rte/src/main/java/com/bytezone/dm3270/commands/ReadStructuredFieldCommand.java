package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.replyfield.AuxilliaryDevices;
import com.bytezone.dm3270.replyfield.CharacterSets;
import com.bytezone.dm3270.replyfield.Color;
import com.bytezone.dm3270.replyfield.DistributedDataManagement;
import com.bytezone.dm3270.replyfield.Highlight;
import com.bytezone.dm3270.replyfield.ImplicitPartition;
import com.bytezone.dm3270.replyfield.OEMAuxilliaryDevice;
import com.bytezone.dm3270.replyfield.QueryReplyField;
import com.bytezone.dm3270.replyfield.ReplyModes;
import com.bytezone.dm3270.replyfield.Summary;
import com.bytezone.dm3270.replyfield.UsableArea;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.structuredfields.DefaultStructuredField;
import com.bytezone.dm3270.structuredfields.QueryReplySF;
import com.bytezone.dm3270.structuredfields.StructuredField;
import com.bytezone.dm3270.utilities.Dm3270Utility;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class extracted from dm3270 source code but removing javafx dependencies, unnecessary code and
 * general refactor to comply with code style. Additionally, we replace existing standard output
 * with proper logging.
 */
public class ReadStructuredFieldCommand extends Command {

  private static final Logger LOG = LoggerFactory.getLogger(ReadStructuredFieldCommand.class);

  private static Map<String, String> clientNames = new HashMap<>();
  private static final String SEPARATOR =
      "\n-------------------------------------------------------------------------";

  private final List<StructuredField> structuredFields = new ArrayList<>();

  private String clientName = "";
  private String signature;
  private final List<QueryReplyField> replies = new ArrayList<>();
  private ScreenDimensions screenDimensions;

  static {
    clientNames.put("0BA60960D0116F016EBA4D14E610AA39", "Vista2");
    clientNames.put("12F0F4557FB72796E8A4398AA694255C", "Vista Model 2");
    clientNames.put("8EC3FF4989C2A3B7CB5B6B464CE6C24D", "Vista Model 3");
    clientNames.put("26ED6D641768FDF25889838F29248F07", "Vista Model 4");
    clientNames.put("93FCC5A3CC3515F167F995DE634B193F", "Vista Model 5");

    clientNames.put("53952DB14CBB53CD7C1E5AB1FDFDA193", "tn3270X");
    clientNames.put("19D8CA4B4B59357FBF37FB9B7F38EC21", "x3270");
    clientNames.put("F960E103861F3920FC3B8AF00D8B8601", "FreeHost");

    clientNames.put("C1F30DBA8306E1887C7EE2D976C6B24A", "dm3270 (old1)");
    clientNames.put("08997C53F68A969853867072174CD882", "dm3270 (old2)");
    clientNames.put("BD47AE1B606E2DF29C7D24DD128648A8", "dm3270 Model 2");
    clientNames.put("00235B1025AEAA11132E71EC16CD3B06", "dm3270 Model 5");
  }

  public ReadStructuredFieldCommand(TelnetState telnetState) {
    this(buildReply(telnetState));
  }

  private ReadStructuredFieldCommand(byte[] buffer) {
    this(buffer, 0, buffer.length);
  }

  public ReadStructuredFieldCommand(byte[] buffer, int offset, int length) {
    super(buffer, offset, length);

    assert data[0] == AIDCommand.AID_STRUCTURED_FIELD;

    int ptr = 1;
    int max = data.length;

    while (ptr < max) {
      int size = Dm3270Utility.unsignedShort(data, ptr) - 2;
      ptr += 2;

      switch (data[ptr]) {
        case StructuredField.QUERY_REPLY:
          QueryReplySF queryReply = new QueryReplySF(data, ptr, size);
          structuredFields.add(queryReply);
          replies.add(queryReply.getQueryReplyField());
          break;

        default:
          LOG.warn("Unknown Structured Field: {}", Dm3270Utility.toHex(data, ptr, 1, false));
          structuredFields.add(new DefaultStructuredField(data, ptr, size));
      }
      ptr += size;
    }

    if (replies.size() > 0) {
      clientName = getClientName(data);
      for (QueryReplyField reply : replies) {
        reply.addReplyFields(replies);         // allow each QRF to see all the others
        if (screenDimensions == null && reply instanceof UsableArea) {
          screenDimensions = ((UsableArea) reply).getScreenDimensions();
        }
      }
    }
  }

  private String getClientName(byte[] buffer) {
    try {
      byte[] digest = MessageDigest.getInstance("MD5").digest(buffer);
      signature = DatatypeConverter.printHexBinary(digest);
      String clientName = clientNames.get(signature);
      return clientName == null ? "Unknown" : clientName;
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return "Unknown";
  }

  private static byte[] buildReply(TelnetState telnetState) {
    Highlight highlight = new Highlight();
    Color color = new Color();

    ScreenDimensions screenDimensions = telnetState.getSecondary();
    ImplicitPartition partition =
        new ImplicitPartition(screenDimensions.rows, screenDimensions.columns);

    List<QueryReplyField> replyFields = new ArrayList<>();

    replyFields.add(new UsableArea(screenDimensions.rows, screenDimensions.columns));
    replyFields.add(new CharacterSets());
    replyFields.add(color);
    replyFields.add(highlight);
    replyFields.add(new ReplyModes());
    replyFields.add(new AuxilliaryDevices());
    replyFields.add(partition);
    replyFields.add(new OEMAuxilliaryDevice());
    replyFields.add(new DistributedDataManagement());

    Summary summary = new Summary(replyFields);      // adds itself to the list

    // calculate the size of the reply record
    int replyLength = 1;
    for (QueryReplyField reply : summary) {
      replyLength += reply.replySize();
    }

    // create the reply record buffer
    byte[] buffer = new byte[replyLength];

    int ptr = 0;
    buffer[ptr++] = AIDCommand.AID_STRUCTURED_FIELD;

    // fill buffer with reply components
    for (QueryReplyField reply : summary) {
      ptr = reply.packReply(buffer, ptr);
    }

    assert ptr == replyLength;

    return buffer;
  }

  @Override
  public void process(Screen screen) {
  }

  @Override
  public String getName() {
    return "Read SF";
  }

  @Override
  public String toString() {
    StringBuilder text =
        new StringBuilder(String.format("RSF (%d):", structuredFields.size()));

    if (replies.size() > 0) {
      text.append(String.format("%nChecksum     : %s", signature));
      text.append(String.format("%nClient name  : %s", clientName));
    }

    for (StructuredField sf : structuredFields) {
      text.append(SEPARATOR);
      text.append("\n");
      text.append(sf);
    }

    if (structuredFields.size() > 0) {
      text.append(SEPARATOR);
    }

    return text.toString();
  }

}
