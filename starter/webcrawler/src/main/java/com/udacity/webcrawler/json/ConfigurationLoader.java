package com.udacity.webcrawler.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A static utility class that loads a JSON configuration file.
 */
public final class ConfigurationLoader {

  private final Path path;

  /**
   * Create a {@link ConfigurationLoader} that loads configuration from the given {@link Path}.
   */
  public ConfigurationLoader(Path path) {
    this.path = Objects.requireNonNull(path);
  }

  /**
   * Loads configuration from this {@link ConfigurationLoader}'s path
   *
   * @return the loaded {@link CrawlerConfiguration}.
   */
  public CrawlerConfiguration load() {
    try {
      return read(Files.newBufferedReader(path, StandardCharsets.UTF_8));
    } catch (IOException e) {
      System.out.println("File not found");
      throw new RuntimeException(e);
    }
  }

  /**
   * Loads crawler configuration from the given reader.
   *
   * @param reader a Reader pointing to a JSON string that contains crawler configuration.
   * @return a crawler configuration
   */
  public static CrawlerConfiguration read(Reader reader) {
    // This is here to get rid of the unused variable warning.
    try {
      Objects.requireNonNull(reader);
      var objectMapper = new ObjectMapper().findAndRegisterModules();
      var stringBuilder = new StringBuilder();
      char[] data = new char[10];
      while (reader.read(data) != -1) {
        stringBuilder.append(new String(data));
        data = new char[]{10};
      }
      String string = stringBuilder.toString();
      return objectMapper.readValue(string, CrawlerConfiguration.class);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }
}
