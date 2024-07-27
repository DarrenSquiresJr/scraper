package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
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

public class Main
{
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";
    private static final String DOWNLOAD_DIRECTORY = System.getProperty("user.dir") + File.separator + "dc-mods";
    private static final String DOWNLOAD_ERROR_LOG_PATH = System.getProperty("user.dir") + File.separator + "dc-mods" + File.separator + "error.log";
    private static final boolean HEADLESS_ENABLED = true;
    private static final int MIN_WAIT_TIME_IN_MS = 1500;
    private static final int MAX_WAIT_TIME_IN_MS = 3500;
    private static final int SET_WAIT_TIME_IN_MS = 5500;

    private static final List<String> erroredDownloads = new ArrayList<>();

    public static void main(String[] args) throws IOException
    {
        clearWebDriverCache();
        try (Stream<String> lines = Files.lines(Paths.get("links.txt")))
        {
            lines.forEach((modLink) -> {

                if (modLink.startsWith("https://www.curseforge.com"))
                {
                    downloadFromCurseForge(modLink);
                } else if (modLink.startsWith("https://modrinth.com"))
                {
                    downloadFromModrinth(modLink);
                } else
                {
                    System.out.println("Unrecognized mod link: " + modLink);
                }
            });
        }
        if (!erroredDownloads.isEmpty())
        {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(DOWNLOAD_ERROR_LOG_PATH))) {
                for (String line : erroredDownloads) {
                    writer.write(line);
                    writer.newLine(); // Add a new line after each string
                }
                System.out.println("File written successfully!");
            } catch (IOException exception) {
                System.out.println("Error writing log file: " + exception.getMessage());
            }
        }
    }

    private static void downloadFromCurseForge(String modLink)
    {
        String completeModLink = modLink + "/files/all?page=1&pageSize=1";
        Path sortedDirectory;

        if (modLink.contains("/mc-mods/"))
        {
            completeModLink += "&gameVersionTypeId=4&version=1.21";
            sortedDirectory = Paths.get(DOWNLOAD_DIRECTORY + File.separator + "mods");
        } else if (modLink.contains("/texture-packs/"))
        {
            sortedDirectory = Paths.get(DOWNLOAD_DIRECTORY + File.separator + "texture-packs");
        } else if (modLink.contains("/shaders/"))
        {
            sortedDirectory = Paths.get(DOWNLOAD_DIRECTORY + File.separator + "shaders");
        } else
        {
            sortedDirectory = Paths.get(DOWNLOAD_DIRECTORY + File.separator + "unknown");
        }
        WebDriver driver = createWebDriver(String.valueOf(sortedDirectory));

        System.out.println("Downloading from curseforge.com: " + completeModLink);

        try
        {
            driver.get(completeModLink);

            // Extract the page body text
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // Wait for up to 10 seconds

            // Wait for the button to be clickable
            String buttonSelector = ".file-row-details";
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(buttonSelector)));
            button.click();

            String downloadSelector = "a.btn-cta.download-cta";
            WebElement download = driver.findElement(By.cssSelector(downloadSelector));

            System.out.println("Found download: " + download.getAttribute("href"));
            download.click();

            waitSetTime();
            System.out.println("Finished waiting for download... Download should start now.");
        } catch (Exception exception)
        {
            erroredDownloads.add(modLink);
        }

        driver.quit();
    }

    private static void waitRandomTime()
    {
        Random random = new Random();
        int randomNumber = random.nextInt(MAX_WAIT_TIME_IN_MS - MIN_WAIT_TIME_IN_MS + 1) + MIN_WAIT_TIME_IN_MS;

        try
        {
            Thread.sleep(randomNumber);
        } catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void waitSetTime()
    {
        try
        {
            Thread.sleep(SET_WAIT_TIME_IN_MS);
        } catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void downloadFromModrinth(String modLink)
    {
        String completeModLink = modLink + "/versions?g=1.21&l=fabric";

        Path sortedDirectory;

        if (modLink.contains("/mod/"))
        {
            sortedDirectory = Paths.get(DOWNLOAD_DIRECTORY + File.separator + "mods");
        } else if (modLink.contains("/plugin/"))
        {
            sortedDirectory = Paths.get(DOWNLOAD_DIRECTORY + File.separator + "plugins");
        } else if (modLink.contains("/datapack/"))
        {
            sortedDirectory = Paths.get(DOWNLOAD_DIRECTORY + File.separator + "datapack");
        } else
        {
            sortedDirectory = Paths.get(DOWNLOAD_DIRECTORY + File.separator + "unknown");
        }

        WebDriver driver = createWebDriver(String.valueOf(sortedDirectory));

        System.out.println("Downloading from modrinth.com: " + completeModLink);

        try {
            driver.get(completeModLink);

            String downloadSelector = ".download-button";
            WebElement download = driver.findElements(By.cssSelector(downloadSelector)).getFirst();

            System.out.println("Found download: " + download.getAttribute("href"));
            download.click();

            System.out.println("Download started... Waiting random amount of time before continuing");
            waitSetTime();
        }
        catch (Exception exception)
        {
            System.out.println("Download failed: " + exception.getMessage());
            erroredDownloads.add(modLink);
        }
        driver.quit();
    }

    private static WebDriver createWebDriver(String downloadDirectory)
    {
        WebDriverManager.chromedriver().setup();
        return new ChromeDriver(createChromeOptions(downloadDirectory));
    }

    private static void clearWebDriverCache()
    {
        WebDriverManager.chromedriver().clearDriverCache().setup();
        WebDriverManager.chromedriver().clearResolutionCache().setup();
    }

    private static ChromeOptions createChromeOptions(String downloadDirectory)
    {
        ChromeOptions options = new ChromeOptions();

        if (HEADLESS_ENABLED)
        {
            options.addArguments("headless");
            options.addArguments("user-agent=" + USER_AGENT);
            options.addArguments("window-size=1920,1080");
        }

        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("download.default_directory", downloadDirectory);
        chromePrefs.put("safebrowsing.enabled", true); //if false downloads will be blocked

        options.setExperimentalOption("prefs", chromePrefs);

        return options;
    }

}
