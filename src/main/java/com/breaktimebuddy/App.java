package com.breaktimebuddy;

/**
 * Delegates application startup to {@link Main} to defer JavaFX initialization until after the
 * distributable package is loaded.
 */
public class App {

  public App() {}

  /**
   * Delegates to {@link Main#main(String[])}.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    Main.main(args);
  }
}
