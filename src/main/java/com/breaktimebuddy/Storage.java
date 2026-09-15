package com.breaktimebuddy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Storage {
  public InputStream in() throws IOException;

  public OutputStream out() throws IOException;
}
