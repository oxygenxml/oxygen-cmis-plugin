package com.oxygenxml.cmis.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.DefaultHttpInvoker;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpInvoker;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Output;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;

import lombok.extern.slf4j.Slf4j;
import ro.sync.net.protocol.http.HttpExceptionWithDetails;

/**
 * Invoker class that uses the Oxygen default protocols, but converts HttpExceptionWithDetails to
 * status codes.
 * 
 * @author cristi_talau
 */
@Slf4j
public class OxygenHttpInvoker implements HttpInvoker {
  
  /**
   * The original invoker.
   */
  private final HttpInvoker defaultInvoker = new DefaultHttpInvoker();

  @Override
  public Response invokeGET(UrlBuilder url, BindingSession session) {
    try {
      return defaultInvoker.invokeGET(url, session);
    } catch (CmisConnectionException e) {
      return handleConnectionException(e);
    }
  }

  /**
   * If the source exception was a {@link HttpExceptionWithDetails}, we return a Response with
   * the proper response code instead of an exception.
   * 
   * @param e The exception. 
   * @return The response.
   */
  private Response handleConnectionException(CmisConnectionException e) {
    if (e.getCause() instanceof HttpExceptionWithDetails) {
      HttpExceptionWithDetails httpEx = (HttpExceptionWithDetails) e.getCause();
      
      return new Response(httpEx.getReasonCode(), 
          httpEx.getMessage(), null, null, 
          new ByteArrayInputStream(httpEx.getReason().getBytes(StandardCharsets.UTF_8)));
    }

    throw e;
  }

  @Override
  public Response invokeGET(UrlBuilder url, BindingSession session, BigInteger offset, BigInteger length) {
    try {
      return defaultInvoker.invokeGET(url, session, offset, length);
    } catch (CmisConnectionException e) {
      return handleConnectionException(e);
    }
  }

  @Override
  public Response invokePOST(UrlBuilder url, String contentType, Output writer, BindingSession session) {
    try {
      return defaultInvoker.invokePOST(url, contentType, writer, session);
    } catch (CmisConnectionException e) {
      return handleConnectionException(e);
    }
  }

  @Override
  public Response invokePUT(UrlBuilder url, String contentType, Map<String, String> headers, Output writer,
      BindingSession session) {
    try {
      Response response = defaultInvoker.invokePUT(url, contentType, headers, writer, session);
      return new MaterializedResponse(response);
    } catch (CmisConnectionException e) {
      return handleConnectionException(e);
    } catch (IOException e) {
      log.debug("Error when materializing CMIS PUT response", e);
      return new Response(500, e.getMessage(), Collections.emptyMap(), 
          new ByteArrayInputStream(new byte[0]), 
          new ByteArrayInputStream(new byte[0]));
    }
  }

  @Override
  public Response invokeDELETE(UrlBuilder url, BindingSession session) {
    try {
      return defaultInvoker.invokeDELETE(url, session);
    } catch (CmisConnectionException e) {
      return handleConnectionException(e);
    }
  }
}
