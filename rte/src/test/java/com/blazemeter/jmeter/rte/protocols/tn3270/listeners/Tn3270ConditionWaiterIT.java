package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.listeners.ConditionWaiterIT;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.Tn5250ConditionWaiter;
import com.bytezone.dm3270.display.Screen;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class Tn3270ConditionWaiterIT  extends ConditionWaiterIT<Tn3270ConditionWaiter<?>> {


  @Mock
  protected Screen screen;

  @Mock
  protected Tn3270Client client;

}
