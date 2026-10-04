package com.breaktimebuddy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A generic interface for storing data.
 */
public interface Storage {
  /**
   * Creates an input stream for loading data.
   *
   * @return an input stream for reading stored data
   * @throws IOException thrown if an I/O error occurs
   */
  InputStream in() throws IOException;

  /**
   * Creates an output stream for saving data.
   *
   * @return an output stream for writing data
   * @throws IOException thrown if an I/O error occurs
   */
  OutputStream out() throws IOException;
}
