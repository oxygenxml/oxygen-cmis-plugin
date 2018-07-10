package com.oxygenxml.cmis.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.QueryStatement;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class ConnectionTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void test() {
    // default factory implementation
    SessionFactory factory = SessionFactoryImpl.newInstance();
    Map<String, String> parameters = new HashMap<String, String>();

    // user credentials
    parameters.put(SessionParameter.USER, "Otto");
    parameters.put(SessionParameter.PASSWORD, "****");

    // connection settings
    parameters.put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/atom11");
    parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
    parameters.put(SessionParameter.REPOSITORY_ID, "A1");

    // create session
    Session session = factory.createSession(parameters);


    QueryStatement qs = session.createQueryStatement("SELECT * FROM cmis:document");
    ItemIterable<QueryResult> results = qs.query();

    System.out.println("Has more items " + results.getHasMoreItems());

    StringBuilder b = new StringBuilder();

    for(QueryResult hit: results) {  
      for(PropertyData<?> property: hit.getProperties()) {

        String queryName = property.getQueryName();
        Object value = property.getFirstValue();

        b.append(queryName + ": " + value + "\n");
      }

      Assert.assertEquals("cmis:isImmutable: false\n" + 
          "cmis:objectTypeId: ComplexType\n" + 
          "cmis:versionLabel: null\n" + 
          "cmis:description: null\n" + 
          "cmis:createdBy: unknown\n" + 
          "cmis:checkinComment: null\n" + 
          "cmis:creationDate: java.util.GregorianCalendar[time=?,areFieldsSet=false,areAllFieldsSet=false,lenient=true,zone=sun.util.calendar.ZoneInfo[id=\"GMT\",offset=0,dstSavings=0,useDaylight=false,transitions=0,lastRule=null],firstDayOfWeek=1,minimalDaysInFirstWeek=1,ERA=?,YEAR=2018,MONTH=6,WEEK_OF_YEAR=?,WEEK_OF_MONTH=?,DAY_OF_MONTH=9,DAY_OF_YEAR=?,DAY_OF_WEEK=?,DAY_OF_WEEK_IN_MONTH=?,AM_PM=?,HOUR=?,HOUR_OF_DAY=10,MINUTE=52,SECOND=43,MILLISECOND=996,ZONE_OFFSET=?,DST_OFFSET=?]\n" + 
          "cmis:contentStreamFileName: data.txt\n" + 
          "cmis:isMajorVersion: true\n" + 
          "cmis:name: My_Document-1-0\n" + 
          "cmis:isLatestVersion: true\n" + 
          "cmis:lastModificationDate: java.util.GregorianCalendar[time=?,areFieldsSet=false,areAllFieldsSet=false,lenient=true,zone=sun.util.calendar.ZoneInfo[id=\"GMT\",offset=0,dstSavings=0,useDaylight=false,transitions=0,lastRule=null],firstDayOfWeek=1,minimalDaysInFirstWeek=1,ERA=?,YEAR=2018,MONTH=6,WEEK_OF_YEAR=?,WEEK_OF_MONTH=?,DAY_OF_MONTH=9,DAY_OF_YEAR=?,DAY_OF_WEEK=?,DAY_OF_WEEK_IN_MONTH=?,AM_PM=?,HOUR=?,HOUR_OF_DAY=10,MINUTE=52,SECOND=43,MILLISECOND=996,ZONE_OFFSET=?,DST_OFFSET=?]\n" + 
          "cmis:contentStreamLength: 33869\n" + 
          "cmis:objectId: 130\n" + 
          "cmis:lastModifiedBy: unknown\n" + 
          "cmis:secondaryObjectTypeIds: null\n" + 
          "cmis:contentStreamId: null\n" + 
          "cmis:contentStreamMimeType: text/plain\n" + 
          "cmis:baseTypeId: cmis:document\n" + 
          "cmis:changeToken: 1531133563996\n" + 
          "cmis:isPrivateWorkingCopy: false\n" + 
          "cmis:isVersionSeriesCheckedOut: false\n" + 
          "cmis:versionSeriesCheckedOutBy: null\n" + 
          "cmis:versionSeriesId: 130\n" + 
          "cmis:isLatestMajorVersion: true\n" + 
          "cmis:versionSeriesCheckedOutId: null\n" + 
          "", b.toString());
    }

  }

}
