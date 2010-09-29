package com.google.ytd.model;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Blob;

/*
 * Represents the meta grouping of a set of photo entries.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class DataChunk {
  // Due to a bug in the Picasa resumable uploads API, we need to send data in a multiple of 256k.
  public static final int CHUNK_SIZE = 3 * 256 * 1024;
  
  @PrimaryKey
  @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private String id;
  
  private Blob data;
  
  private String photoEntryId;
  
  private Date created;
  
  private Integer index;
  
  public DataChunk(String photoEntryId, int index, byte[] bytes) {
    this.photoEntryId = photoEntryId;
    this.data = new Blob(bytes);
    this.index = new Integer(index);
    
    this.created = new Date();
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the data
   */
  public byte[] getData() {
    return data.getBytes();
  }

  /**
   * @return the created
   */
  public Date getCreated() {
    return created;
  }

  /**
   * @return the index
   */
  public int getIndex() {
    if (index == null) {
      index = new Integer(-1);
    }
    
    return index.intValue();
  }

  /**
   * @return the photoEntryId
   */
  public String getPhotoEntryId() {
    return photoEntryId;
  }
}