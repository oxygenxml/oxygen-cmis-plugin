package com.oxygenxml.cmis.selenium;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebAuthorCmisPlugin {

	static WebDriver driver;

	@Before
	public void setUp() throws MalformedURLException {
		DesiredCapabilities caps = DesiredCapabilities.firefox();
		caps.setJavascriptEnabled(true);

		File pathToBinary = new File("C:\\Silence\\FirefoxPortableESR\\firefox.exe");
		FirefoxBinary ffBinary = new FirefoxBinary(pathToBinary);
		FirefoxProfile firefoxProfile = new FirefoxProfile();
		driver = new FirefoxDriver(ffBinary, firefoxProfile, caps);

		driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
		
		driver.get("http://localhost:8081/oxygen-xml-web-author/app/"
				+ "oxygen.html?url=cmis%3A%2F%2Fhttp%253A%252F%252Flo"
				+ "calhost%253A8080%252FB%252Fatom11%2FA1%2FConcept.dita");
	}

	@Test
	public void testCheckOutButton() throws Exception {
		WebDriverWait wait = new WebDriverWait(driver, 15);

		WebElement logininput = driver.findElement(By.cssSelector("#cmis-name"));
		logininput.sendKeys("admin");
		logininput.sendKeys(Keys.ENTER);

		Thread.sleep(3000);

		WebElement actionsButton = driver
				.findElement(By.xpath("/div[@class=\"goog-inline-block goog-toolbar-menu-button-caption\"]"));
		actionsButton.click();

		WebElement checkOutButton = driver.findElement(By.name("cmisCheckOut.link"));
		checkOutButton.click();

	}

	@After
	public void after() {
		driver.close();
	}
}
