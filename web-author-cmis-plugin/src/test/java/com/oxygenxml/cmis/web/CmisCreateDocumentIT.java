package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.basic.io.IOUtil;

public class CmisCreateDocumentIT {

  @Rule
  public CmisAccessProvider cmisAccessProvider = new CmisAccessProvider();
  private ResourceController ctrl;

	@Before
	public void setUp() throws Exception {
    CMISAccess cmisAccess = cmisAccessProvider.getCmisAccess();
    ctrl = cmisAccess.createResourceController();
	}
	

	@Test
	public void testCreateDocumentMultipleThreads() throws Throwable {
    AtomicReference<Throwable> exc = new AtomicReference<>();
    int threadsNo = 100;
    CyclicBarrier barrier = new CyclicBarrier(threadsNo);
	  class CreateDocTask implements Runnable {
	    private int i;

      public CreateDocTask(int i) {
        this.i = i;
	    }

	    @Override
	    public void run() {
	      String name = "createDoc" + i + ".xml";
	      try {
	        Document document = null;
	        try {
	          try {
	            document = ctrl.createEmptyVersionedDocument(
	                ctrl.getRootFolder(), name, "plain/xml", VersioningState.MINOR);
	          } finally {
	            barrier.await();
	          }
	          assertEquals(name, document.getName());
	        } finally {
	          if (document != null) {
	            ctrl.deleteAllVersionsDocument(document);
	          }
	        }
	      } catch (Throwable e) {
	        exc.set(e);
	      }
	    }
	  }
	  
	  List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < threadsNo; i++) {
		  Thread thread = new Thread(new CreateDocTask(i));
		  thread.start();
      threads.add(thread);
		}
		
		for (Thread thread : threads) {
		  thread.join(5000);
    }
		if (exc.get() != null) {
		  throw exc.get();
		}
	}
	
	
  /**
   * <p><b>Description:</b> Test document creation using the URLConnection.</p>
   * <p><b>Bug ID:</b> WA-2428</p>
   *
   * @author cristi_talau
   *
   * @throws Exception
   */
  @Test
  public void testDocumentCreation() throws Exception {
    URL url = new URL("cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/new_file_WA_2428.xml");
    CmisURLConnection wConnection = cmisAccessProvider.createConnection(url);
    wConnection.setDoOutput(true);
    InputStream in = new ByteArrayInputStream("<root/>".getBytes(StandardCharsets.UTF_8));
    try (OutputStream outputStream = wConnection.getOutputStream()) {
      IOUtils.copy(in, outputStream);
    }
    
    
    CmisURLConnection rConnection = cmisAccessProvider.createConnection(url);
    try (InputStream inputStream = rConnection.getInputStream()) {
      byte[] bytes = IOUtil.readBytes(inputStream);
      assertEquals("<root/>", new String(bytes, StandardCharsets.UTF_8));
    }
  }

}
