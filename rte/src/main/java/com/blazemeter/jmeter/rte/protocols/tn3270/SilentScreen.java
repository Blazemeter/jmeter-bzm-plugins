package com.blazemeter.jmeter.rte.protocols.tn3270;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.streams.TelnetState;

public class SilentScreen extends Screen {

  private boolean soundAlarm = false;

  SilentScreen(ScreenDimensions defaultScreenDimensions, ScreenDimensions alternateScreenDimensions,
      TelnetState telnetState) {
    super(defaultScreenDimensions, alternateScreenDimensions, telnetState);
  }

  @Override
  public void soundAlarm() {
    soundAlarm = true;
  }

  public boolean resetAlarm() {
    boolean ret = soundAlarm;
    soundAlarm = false;
    return ret;
  }
}
