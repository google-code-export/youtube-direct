package com.google.ytd.dao;

public interface DataChunkDao {
  public byte[] getBytes(String photoEntryId, long startByte);
  public void deleteChunks(String photoEntryId);
}
