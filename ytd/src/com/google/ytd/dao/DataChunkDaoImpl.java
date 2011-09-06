package com.google.ytd.dao;

import com.google.inject.Inject;
import com.google.ytd.model.DataChunk;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

public class DataChunkDaoImpl implements DataChunkDao {
  private static final Logger LOG = Logger.getLogger(DataChunkDaoImpl.class.getName());

  @Inject
  private PersistenceManagerFactory pmf;

  @Inject
  public DataChunkDaoImpl(PersistenceManagerFactory pmf) {
    this.pmf = pmf;
  }

  @Override
  @SuppressWarnings("unchecked")
  public byte[] getBytes(String photoEntryId, long startByte) {
    PersistenceManager pm = pmf.getPersistenceManager();
    byte[] bytes = null;

    // We explicitly want to truncate via the int cast.
    int index = (int) (startByte / DataChunk.CHUNK_SIZE);

    try {
      Query query = pm.newQuery(DataChunk.class);
      query.setFilter("photoEntryId == photoEntryIdParam && index == indexParam");
      query.declareParameters("String photoEntryIdParam, int indexParam");

      List<DataChunk> results = (List<DataChunk>) query.execute(photoEntryId, index);
      if (results.size() > 0) {
        DataChunk dataChunk = results.get(0);
        bytes = dataChunk.getData();

        int startIndex = (int) startByte - (index * DataChunk.CHUNK_SIZE);
        if (startIndex != 0) {
          bytes = Arrays.copyOfRange(bytes, startIndex, bytes.length);
        }
      }
    } finally {
      pm.close();
    }

    return bytes;
  }

  @Override
  public void deleteChunks(String photoEntryId) {
    LOG.info(String.format("Attempting to delete all DataChunks with photoEntryId '%s'...",
        photoEntryId));
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      Query query = pm.newQuery(DataChunk.class);
      query.setFilter("photoEntryId == photoEntryIdParam");
      query.declareParameters("String photoEntryIdParam");
      long itemsDeleted = query.deletePersistentAll(photoEntryId);

      LOG.info(String.format("... %d DataChunk(s) deleted successfully.", itemsDeleted));
    } finally {
      pm.close();
    }
  }
}
