package com.oxygenxml.cmis.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
   * The wrapped response.
   */
  private Response delegate;

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
        Collections.emptyMap(), 
        getMaterializedInputStream(delegate), 
        new ByteArrayInputStream(delegate.getErrorContent().getBytes(StandardCharsets.UTF_8)));
    this.delegate = delegate;
  }


  private static ByteArrayInputStream getMaterializedInputStream(Response delegate) throws IOException {
    try (InputStream stream = delegate.getStream()) {
      return new ByteArrayInputStream(IOUtil.readBytes(stream));
    }
  }
  
  @Override
  public String getCharset() {
    return delegate.getCharset();
  }
  @Override
  public String getContentDisposition() {
    return delegate.getContentDisposition();
  }
  
  @Override
  public String getContentEncoding() {
    return delegate.getContentEncoding();
  }
  
  @Override
  public BigInteger getContentLength() {
    return delegate.getContentLength();
  }
  
  @Override
  public BigInteger getContentLengthHeader() {
    return delegate.getContentLengthHeader();
  }
  @Override
  public String getContentLocactionHeader() {
    return delegate.getContentLocactionHeader();
  }
  @Override
  public String getContentTransferEncoding() {
    return delegate.getContentTransferEncoding();
  }
  @Override
  public String getContentTypeHeader() {
    return delegate.getContentTypeHeader();
  }
  @Override
  public String getErrorContent() {
    return delegate.getErrorContent();
  }
  @Override
  public String getHeader(String name) {
    return delegate.getHeader(name);
  }
  @Override
  public Map<String, List<String>> getHeaders() {
    return delegate.getHeaders();
  }
  @Override
  public String getLocactionHeader() {
    return delegate.getLocactionHeader();
  }
  @Override
  public int getResponseCode() {
    return delegate.getResponseCode();
  }
  @Override
  public String getResponseMessage() {
    return delegate.getResponseMessage();
  }
}
