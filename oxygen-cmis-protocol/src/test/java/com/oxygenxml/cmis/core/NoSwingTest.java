package com.oxygenxml.cmis.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import ro.sync.basic.io.IOUtil;

public class NoSwingTest {
  
  /**
   * <p><b>Description:</b> Test that no swing class is used in this package.</p>
   *
   * @author cristi_talau
   *
   * @throws Exception
   */
  @Test
  public void testNoSwing() throws Exception {
    File sourceFolder = new File("oxygen-cmis-protocol/src/main/java/");
    if (!sourceFolder.exists()) {
      sourceFolder = new File("src/main/java/");
    }
    List<File> javaFiles = Files.walk(sourceFolder.toPath())
        .map(Path::toFile)
        .filter(file -> file.getPath().endsWith(".java"))
        .collect(Collectors.toList());

    assertFalse("There should be at least one java file", javaFiles.isEmpty());
    
    javaFiles.forEach(f -> {
      try {
        String content = IOUtil.readFile(f);
        assertFalse("File " + f + " contains swing reference.", content.contains("swing"));
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });
  }
}
