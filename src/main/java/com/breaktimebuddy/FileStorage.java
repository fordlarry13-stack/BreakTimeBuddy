package com.breaktimebuddy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileStorage implements Storage {
  private final File file;

  public FileStorage(File file) {
    if (file == null)
      throw new NullPointerException("file cannot be null");
    this.file = file;
  }

  @Override
  public InputStream in() throws IOException {
    if (file.isDirectory())
      throw new IOException("Cannot read from a directory: " + file);
    return new BufferedInputStream(new FileInputStream(file));
  }

  @Override
  public OutputStream out() throws IOException {
    if (file.isDirectory())
      throw new IOException("Cannot write to a directory: " + file);
    return new BufferedOutputStream(new FileOutputStream(file));
  }
}
