package com.oxygenxml.cmis.selenium;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.chemistry.opencmis.client.api.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

public class WebAuthorCmisPlugin {

	private static WebDriver driver;
	private static Document document;

	private static final String WA_LINK 	  = "http://localhost:8081/oxygen-xml-web-author/app/"
											  + "oxygen.html?url=cmis%3A%2F%2Fhttp%253A%252F%252Flo"
											  + "calhost%253A8080%252FB%252Fatom11%2FA1%2FConcept.dita";

	private static final String DOC_URL		  = "cmis://http%3A%2F%2Flocalhost%3A8080%2FB%2Fatom11/A1/Concept.dita";
	private static final String FIREFOX_PATH  = "C:\\Silence\\FirefoxPortableESR\\firefox.exe";
	private static final String SERVER_URL 	  = "http://localhost:8080/B/atom11";
	
	private static final String USERNAME 	  = "admin";
	private static final String PASSWORD 	  = "";
	
	@Before
	public void setUp() throws MalformedURLException {
		CmisURLConnection connection = new CmisURLConnection(new URL(SERVER_URL), 
				new CMISAccess(), new UserCredentials(USERNAME, PASSWORD));
		
		document = (Document) connection.getCMISObject(DOC_URL);
		
		DesiredCapabilities caps = DesiredCapabilities.firefox();
		caps.setJavascriptEnabled(true);

		File pathToBinary = new File(FIREFOX_PATH);
		FirefoxBinary ffBinary = new FirefoxBinary(pathToBinary);
		FirefoxProfile firefoxProfile = new FirefoxProfile();
		driver = new FirefoxDriver(ffBinary, firefoxProfile, caps);

		driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);		
		
		driver.get(WA_LINK);

		WebElement logininput = driver.findElement(By.cssSelector("#cmis-name"));
		logininput.sendKeys("admin");
		logininput.sendKeys(Keys.ENTER);
	}

	@Test
	public void testCheckOutAllVersButtons() throws Exception {
		if(document.isVersionSeriesCheckedOut()) {
			document.cancelCheckOut();;
		}
		
		Thread.sleep(6000);
		
		WebElement actionsButton = driver.findElement(By.cssSelector("div[name='cmis-actions']"));
		actionsButton.click();

		WebElement checkOutButton = driver.findElement(By.cssSelector("div[name='cmisCheckOut.link']"));
		checkOutButton.click();
		
		Thread.sleep(2000);
		actionsButton.click();

		WebElement allButton = driver.findElement(By.cssSelector("div[name='listOldVersion.link']"));
		allButton.click();
	}

	@Test
	public void testCheckInCancelCheckOutButtons() throws InterruptedException {
		if(!document.isVersionSeriesCheckedOut()) {
			document = document.getObjectOfLatestVersion(false);
			document.checkOut();
		}
		
		Thread.sleep(6000);

		WebElement actionsButton = driver.findElement(By.cssSelector("div[name='cmis-actions']"));
		actionsButton.click();

		WebElement checkInButton = driver.findElement(By.cssSelector("div[name='cmisCheckIn.link']"));
		checkInButton.click();
		
		Thread.sleep(2000);
		actionsButton.click();

		WebElement cancelButton = driver.findElement(By.cssSelector("div[name='cancelCmisCheckOut.link']"));
		cancelButton.click();
	}

	@Test
	public void testSaveButtonWhenDocIsCheckOut() throws InterruptedException {
		if(!document.isVersionSeriesCheckedOut()) {
			document = document.getObjectOfLatestVersion(false);
			document.checkOut();
		}
		
		Thread.sleep(6000);
		
		WebElement form = driver.findElement(By.cssSelector("#editor-frame"));
		form.sendKeys("Some text for test");
		
		Thread.sleep(2000);
		
		WebElement saveButton = driver.findElement(By.cssSelector(".Save24_light"));
		saveButton.click();
	}
	
	@After
	public void after() {
		driver.close();
		
		if(document.isVersionSeriesCheckedOut()) {
			document.cancelCheckOut();
		}
	}
}
