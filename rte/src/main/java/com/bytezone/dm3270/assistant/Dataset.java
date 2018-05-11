package com.bytezone.dm3270.assistant;

/**
 * Class extracted from dm3270 source code but removing javafx dependencies, unnecessary code and
 * general refactor to comply with code style.
 */
public class Dataset {

  private String datasetName;

  private String volume;
  private String device;
  private String dsorg;
  private String recfm;
  private String catalog;
  private String created;
  private String expires;
  private String referredDate;

  private int tracks;
  private int extents;
  private int percentUsed;
  private int lrecl;
  private int blksize;

  public Dataset(String name) {
    datasetName = name;
  }

  public void setVolume(String volume) {
    this.volume = volume;
  }

  public String getVolume() {
    return volume;
  }

  public void setDevice(String device) {
    this.device = device;
  }

  public String getDevice() {
    return device;
  }

  public void setDsorg(String dsorg) {
    this.dsorg = dsorg;
  }

  public void setRecfm(String recfm) {
    this.recfm = recfm;
  }

  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  public void setCreated(String created) {
    this.created = created;
  }

  public String getCreated() {
    return created;
  }

  public void setExpires(String expires) {
    this.expires = expires;
  }

  public void setReferredDate(String referredDate) {
    this.referredDate = referredDate;
  }

  public void setTracks(int tracks) {
    this.tracks = tracks;
  }

  public void setExtents(int extents) {
    this.extents = extents;
  }

  public void setPercentUsed(int percentUsed) {
    this.percentUsed = percentUsed;
  }

  public void setLrecl(int lrecl) {
    this.lrecl = lrecl;
  }

  public void setBlksize(int blksize) {
    this.blksize = blksize;
  }

  @Override
  public String toString() {
    return String.format("Name ............ %s%n", datasetName)
        + String.format("Volume .......... %s%n", volume)
        + String.format("Device .......... %s%n", device)
        + String.format("DSORG ........... %s%n", dsorg)
        + String.format("RECFM ........... %s%n", recfm)
        + String.format("Catalog ......... %s%n", catalog)
        + String.format("Created ......... %s%n", created)
        + String.format("Expires ......... %s%n", expires)
        + String.format("Referred ........ %s%n", referredDate)
        + String.format("Tracks .......... %s%n", tracks)
        + String.format("Extents ......... %s%n", extents)
        + String.format("Percent used .... %s%n", percentUsed)
        + String.format("LRECL ........... %s%n", lrecl)
        + String.format("BLKSIZE ......... %s  ", blksize);
  }

}
