package com.blazemeter.jmeter.http2.sampler.gui;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class BlazemeterLabsLogo extends JLabel {

  private static final Logger LOG = LoggingManager.getLoggerForClass();

  private static final ImageIcon DEFAULT_ICON = new ImageIcon(
      BlazemeterLabsLogo.class.getResource("/blazemeter-labs-logo.png"));
  private static final ImageIcon DARCULA_ICON = new ImageIcon(
      BlazemeterLabsLogo.class.getResource("/blazemeter-labs-light-logo.png"));

  public BlazemeterLabsLogo() {
    super(DEFAULT_ICON);
    setBrowseOnClick("https://github.com/Blazemeter/jmeter-bzm-plugins");
  }

  @Override
  public void paint(Graphics g) {
    setIcon("Darcula".equals(UIManager.getLookAndFeel().getID()) ? DARCULA_ICON : DEFAULT_ICON);
    super.paint(g);
  }

  private void setBrowseOnClick(String url) {
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent mouseEvent) {
        if (Desktop.isDesktopSupported()) {
          try {
            Desktop.getDesktop().browse(new URI(url));
          } catch (IOException | URISyntaxException exception) {
            LOG.error("Problem when accessing repository", exception);
          }
        }
      }

      @Override
      public void mousePressed(MouseEvent mouseEvent) {
        //no action
      }

      @Override
      public void mouseReleased(MouseEvent mouseEvent) {
        //no action
      }

      @Override
      public void mouseEntered(MouseEvent mouseEvent) {
        //no action
      }

      @Override
      public void mouseExited(MouseEvent mouseEvent) {
        //no action
      }

    });
  }

}
