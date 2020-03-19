package com.oxygenxml.cmis.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;

import ro.sync.basic.io.IOUtil;

/**
 * Response wrapper that consumes the input stream at the beginning.
 * 
 * When using WebDAVUrlConnection for a PUT request, the input stream needs to be closed 
 * so that the underlying connection is released to the HTTP Client implementation. Since
 * Apache Chemsitry does not close the input stream we make sure to close it ourself.
 * 
 * @author cristi_talau
 */
public class MaterializedResponse extends Response {

  /**
   * Constructor.
   * 
   * @param delegate The wrapped response.
   * 
   * @throws IOException If the input stream could not be closed.
   */
  public MaterializedResponse(Response delegate) throws IOException {
    super(delegate.getResponseCode(), 
        delegate.getResponseMessage(), 
        delegate.getHeaders(), 
        getMaterializedInputStream(delegate), 
        getMaterialzedErrorStream(delegate));
  }

  /**
   * Force the error stream into memory.
   * @param delegate The delegate response.
   * @return the error stream.
   * @throws IOException If reading the error stream fails.
   */
  private static ByteArrayInputStream getMaterialzedErrorStream(Response delegate) {
    String errorContent = delegate.getErrorContent();
    if (errorContent == null) {
      errorContent = "";
    }
    byte[] errorContentBytes = errorContent.getBytes(StandardCharsets.UTF_8);
    return new ByteArrayInputStream(errorContentBytes);
  }

  /**
   * Force the input stream into memory.
   * @param delegate The delegate response.
   * @return the input stream.
   * @throws IOException If reading the input stream fails.
   */
  private static ByteArrayInputStream getMaterializedInputStream(Response delegate) throws IOException {
    try (InputStream stream = delegate.getStream()) {
      return new ByteArrayInputStream(IOUtil.readBytes(stream));
    }
  }
}
