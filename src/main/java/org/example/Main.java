package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException {

        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("download.default_directory",  System.getProperty("user.dir")+ File.separator + "dc-mods");
        chromePrefs.put("safebrowsing.enabled", true);
        ChromeOptions options = new ChromeOptions();

        options.addArguments("headless");
        options.setExperimentalOption("prefs", chromePrefs);
        WebDriverManager.chromedriver().clearDriverCache().setup();
        WebDriverManager.chromedriver().clearResolutionCache().setup();
        // Setup WebDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver(options);

        // Read links from file
        List<String> links;
        try (Stream<String> lines = Files.lines(Paths.get("links.txt"))) {
            links = lines.collect(Collectors.toList());
        }

        for (String link : links) {
            System.out.println(link);
            try {
                // Open the link using Selenium
                driver.get(link + "/files/all?page=1&pageSize=1");

                driver.manage().window().setSize(new Dimension(1200, 1200)); // Set the width and height as needed

                // Extract the page body text
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // Wait for up to 10 seconds

                // Wait for the button to be clickable
                String buttonSelector = ".file-row-details";
                WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(buttonSelector)));
                button.click();

                String downloadSelector = "a.btn-cta.download-cta";
                WebElement download = driver.findElement(By.cssSelector(downloadSelector));
                download.click();

                // Wait for 10 seconds
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Close the browser
        driver.quit();
    }
}
