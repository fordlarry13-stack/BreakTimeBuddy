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
    this.file = file;
  }

  @Override
  public InputStream in() throws IOException {
    if (!file.canRead())
      throw new IOException(String.format("Can not read file: %s", file));
    return new BufferedInputStream(new FileInputStream(file));
  }

  @Override
  public OutputStream out() throws IOException {
    file.createNewFile();
    if (!file.canWrite())
      throw new IOException(String.format("Can not write file: %s", file));
    return new BufferedOutputStream(new FileOutputStream(file));
  }
}
