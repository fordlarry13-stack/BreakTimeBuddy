package com.breaktimebuddy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of {@link Storage} backed by a {@link File} instance.
 */
public class FileStorage implements Storage {
  private final File file;

  /**
   * Creates an instance backed by the {@link File} instance.
   *
   * @param file the file to use for storage
   * @throws IllegalArgumentException thrown when file is null
   */
  public FileStorage(File file) {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");
    this.file = file;
  }

  /** Creates an input stream for reading from the associated {@link File} instance. */
  @Override
  public InputStream in() throws IOException {
    if (file.isDirectory())
      throw new IOException("Cannot read from a directory: " + file);
    return new BufferedInputStream(new FileInputStream(file));
  }

  /** Creates an output stream for writing to the associated {@link File} instance. */
  @Override
  public OutputStream out() throws IOException {
    if (file.isDirectory())
      throw new IOException("Cannot write to a directory: " + file);
    return new BufferedOutputStream(new FileOutputStream(file));
  }
}
