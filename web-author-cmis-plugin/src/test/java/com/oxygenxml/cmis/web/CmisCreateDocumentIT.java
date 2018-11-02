package com.oxygenxml.cmis.web;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;

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
	            document = ctrl.createVersionedDocument(ctrl.getRootFolder(), 
	                name, "empty", "plain/xml", "VersionableType", VersioningState.MINOR);
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
}
