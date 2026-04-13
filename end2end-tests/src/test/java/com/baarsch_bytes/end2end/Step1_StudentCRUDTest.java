package com.baarsch_bytes.end2end;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class Step1_StudentCRUDTest {

        private static final String BASE_URL = resolveBaseUrl();
        private static final int WAIT_SEC = 20;

    private WebDriver driver;

        private static String resolveBaseUrl() {
                String containerUrl = "http://frontend:5173/students";
                String localUrl = "http://localhost:5173/students";
                return isHttpReachable(containerUrl) ? containerUrl : localUrl;
        }

        private static boolean isHttpReachable(String url) {
                try {
                        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(1000);
                        connection.setReadTimeout(1000);
                        int status = connection.getResponseCode();
                        return status >= 200 && status < 500;
                } catch (IOException ex) {
                        return false;
                }
        }

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    private WebDriverWait driverWait() {
        return new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));
    }

    /** Fill the new-student form and click Add Student. Pass null for gpa to leave blank. */
    private void fillAndSubmitStudent(String name, String major, String gpa) {
                WebElement nameInput = driver.findElement(By.id("new-student-name"));
                WebElement majorInput = driver.findElement(By.id("new-student-major"));
                WebElement gpaInput = driver.findElement(By.id("new-student-gpa"));

                nameInput.clear();
                majorInput.clear();
                gpaInput.clear();

                if (name != null && !name.isEmpty()) {
                        nameInput.sendKeys(name);
                        driverWait().until(d -> name.equals(d.findElement(By.id("new-student-name")).getAttribute("value")));
                }

                if (major != null && !major.isEmpty()) {
                        majorInput.sendKeys(major);
                        driverWait().until(d -> major.equals(d.findElement(By.id("new-student-major")).getAttribute("value")));
                }

                if (gpa != null && !gpa.isEmpty()) {
                        gpaInput.sendKeys(gpa);
                        driverWait().until(d -> gpa.equals(d.findElement(By.id("new-student-gpa")).getAttribute("value")));
                }

                WebElement addButton = driverWait().until(
                                ExpectedConditions.elementToBeClickable(By.id("add-student-button")));
                try {
                        addButton.click();
                } catch (ElementNotInteractableException e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addButton);
                }
    }

    private void takeScreenshot(String filename) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path dest = Paths.get("/tests/screenshots/" + filename);
            Files.createDirectories(dest.getParent());
            Files.copy(screenshot.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Screenshot failed: " + e.getMessage());
        }
    }

        private String findStudentIdByName(String name) {
                WebElement nameCell = driver.findElement(
                                By.xpath("//td[contains(@id,'student-name-') and text()='" + name + "']"));
                return nameCell.getAttribute("id").replace("student-name-", "");
        }

        private String createStudentAndGetId(String name, String major, String gpa) {
                driver.get(BASE_URL);
                driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));
                fillAndSubmitStudent(name, major, gpa);
                driverWait().until(ExpectedConditions.presenceOfElementLocated(
                                By.xpath("//td[contains(@id,'student-name-') and text()='" + name + "']")));
                return findStudentIdByName(name);
        }

    // SL1A1
    @Test
    @Order(1)
    public void testAddStudent() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));

        fillAndSubmitStudent("Batman", "Computer Science", "3.5");

        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'student-name-') and text()='Batman']")));

        takeScreenshot("SL1A1-add-student.png");

        WebElement nameCell = driver.findElement(
                By.xpath("//td[contains(@id,'student-name-') and text()='Batman']"));
        assertEquals("Batman", nameCell.getText());
    }

    // SL1A2
    @Test
    @Order(2)
    public void testAddStudentMissingGPA() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));

        fillAndSubmitStudent("Joshua", "Engineering", null);

        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'student-name-') and text()='Joshua']")));

        takeScreenshot("SL1A2-missing-gpa.png");

        WebElement nameCell = driver.findElement(
                By.xpath("//td[contains(@id,'student-name-') and text()='Joshua']"));
        assertEquals("Joshua", nameCell.getText());
    }

    // SL1A3
    @Test
    @Order(3)
    public void testAddStudentNameLowerBoundary() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));

        fillAndSubmitStudent("A", "Computer Science", "3.0");

        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'student-name-') and text()='A']")));

        takeScreenshot("SL1A3-name-lower-boundary.png");
        assertNotNull(driver.findElement(By.xpath("//td[contains(@id,'student-name-') and text()='A']")));
    }

    // SL1A4
    @Test
    @Order(4)
    public void testAddStudentNameUpperBoundary() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));

        String longName = "A".repeat(255);
        int before = driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size();

        fillAndSubmitStudent(longName, "Computer Science", "3.0");

        driverWait().until(d ->
                d.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size() == before + 1);

        takeScreenshot("SL1A4-name-upper-boundary.png");
        assertEquals(before + 1,
                driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size());
    }

    // SL1A5
    @Test
    @Order(5)
    public void testAddStudentMajorLowerBoundary() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));

        fillAndSubmitStudent("TestStudentA5", "A", "3.0");

        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'student-name-') and text()='TestStudentA5']")));

        takeScreenshot("SL1A5-major-lower-boundary.png");
        assertNotNull(driver.findElement(
                By.xpath("//td[contains(@id,'student-name-') and text()='TestStudentA5']")));
    }

    // SL1A6
    @Test
    @Order(6)
    public void testAddStudentMajorUpperBoundary() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));

        String longMajor = "A".repeat(255);
        int before = driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size();

        fillAndSubmitStudent("TestStudentA6", longMajor, "3.0");

        driverWait().until(d ->
                d.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size() == before + 1);

        takeScreenshot("SL1A6-major-upper-boundary.png");
        assertEquals(before + 1,
                driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size());
    }

    // SL1A7
    @Test
    @Order(7)
    public void testAddStudentGPAUpperBoundary() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));

        fillAndSubmitStudent("TestStudentA7", "Computer Science", "4.0");

        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'student-name-') and text()='TestStudentA7']")));

        takeScreenshot("SL1A7-gpa-upper-boundary.png");
        assertNotNull(driver.findElement(
                By.xpath("//td[contains(@id,'student-name-') and text()='TestStudentA7']")));
    }

    // SL1A8
    @Test
    @Order(8)
    public void testAddStudentGPALowerBoundary() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));

        fillAndSubmitStudent("TestStudentA8", "Computer Science", "0.0");

        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'student-name-') and text()='TestStudentA8']")));

        takeScreenshot("SL1A8-gpa-lower-boundary.png");
        assertNotNull(driver.findElement(
                By.xpath("//td[contains(@id,'student-name-') and text()='TestStudentA8']")));
    }

    // SL1B1
    @Test
    @Order(9)
    public void testAddStudentMissingName() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));

        int before = driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size();

        fillAndSubmitStudent("", "Engineering", "3.0");

        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("status-message")));

        takeScreenshot("SL1B1-missing-name.png");

        int after = driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size();
        assertEquals(before, after);
    }

    // SL1B2
    @Test
    @Order(10)
    public void testAddStudentMissingMajor() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));

        int before = driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size();

        fillAndSubmitStudent("NoMajorStudent", "", "3.0");

        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("status-message")));

        takeScreenshot("SL1B2-missing-major.png");

        int after = driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size();
        assertEquals(before, after);
    }

    // SL1B3
    @Test
    @Order(11)
    public void testAddStudentNameAboveBoundary() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));

        int before = driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size();

        fillAndSubmitStudent("A".repeat(256), "Computer Science", "3.0");

        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("status-message")));

        takeScreenshot("SL1B3-name-above-boundary.png");

        int after = driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size();
        assertEquals(before, after);
    }

    // SL1B4
    @Test
    @Order(12)
    public void testAddStudentMajorAboveBoundary() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));

        int before = driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size();

        fillAndSubmitStudent("TestStudentB4", "A".repeat(256), "3.0");

        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("status-message")));

        takeScreenshot("SL1B4-major-above-boundary.png");

        int after = driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size();
        assertEquals(before, after);
    }

    // SL1B5
    @Test
    @Order(13)
    public void testAddStudentGPAAboveMax() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));

        int before = driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size();

        fillAndSubmitStudent("TestStudentB5", "Computer Science", "4.1");

        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("status-message")));

        takeScreenshot("SL1B5-gpa-above-max.png");

        int after = driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size();
        assertEquals(before, after);
    }

    // SL1B6
    @Test
    @Order(14)
    public void testAddStudentGPABelowMin() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));

        int before = driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size();

        fillAndSubmitStudent("TestStudentB6", "Computer Science", "-0.1");

        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("status-message")));

        takeScreenshot("SL1B6-gpa-below-min.png");

        int after = driver.findElements(By.xpath("//tr[contains(@id,'student-row-')]")).size();
        assertEquals(before, after);
    }

    // SL2A1
    @Test
    @Order(15)
    public void testEditStudent() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'student-name-') and text()='Batman']")));

        WebElement nameCell = driver.findElement(
                By.xpath("//td[contains(@id,'student-name-') and text()='Batman']"));
        String studentId = nameCell.getAttribute("id").replace("student-name-", "");

        driver.findElement(By.xpath(
                "//tr[@id='student-row-" + studentId + "']//button[@id='edit-student-button']")).click();

        WebElement editName = driverWait().until(
                ExpectedConditions.visibilityOfElementLocated(By.id("edit-student-name")));

        editName.clear();
        editName.sendKeys("Aiden");

        driver.findElement(By.id("edit-student-save-button")).click();

        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'student-name-') and text()='Aiden']")));

        takeScreenshot("SL2A1-edit-student.png");

        assertEquals("Aiden", driver.findElement(
                By.xpath("//td[contains(@id,'student-name-') and text()='Aiden']")).getText());
    }

    // SL3A1
    @Test
    @Order(16)
    public void testDeleteStudent() {
        driver.get(BASE_URL);
        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'student-name-') and text()='Aiden']")));

        WebElement nameCell = driver.findElement(
                By.xpath("//td[contains(@id,'student-name-') and text()='Aiden']"));
        String studentId = nameCell.getAttribute("id").replace("student-name-", "");

        driver.findElement(By.xpath(
                "//tr[@id='student-row-" + studentId + "']//button[@id='delete-student-button']")).click();

        takeScreenshot("SL3A1-delete-student.png");

        driverWait().until(ExpectedConditions.invisibilityOfElementLocated(
                By.id("student-row-" + studentId)));

        assertTrue(driver.findElements(By.id("student-row-" + studentId)).isEmpty());
    }

    // SL2B1
    @Test
    @Order(17)
    public void testEditStudentNameBelowBoundary() {
        String originalName = "EditTargetNameLow-" + System.nanoTime();
        String studentId = createStudentAndGetId(originalName, "Physics", "3.0");

        driver.findElement(By.xpath(
                "//tr[@id='student-row-" + studentId + "']//button[@id='edit-student-button']")).click();

        WebElement editName = driverWait().until(
                ExpectedConditions.visibilityOfElementLocated(By.id("edit-student-name")));
        editName.clear();
        driver.findElement(By.id("edit-student-save-button")).click();

        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("status-message")));
        takeScreenshot("SL2B1-edit-student-name-below-boundary.png");

        assertEquals(originalName, driver.findElement(By.id("student-name-" + studentId)).getText());
    }

    // SL2B2
    @Test
    @Order(18)
    public void testEditStudentNameAboveBoundary() {
        String originalName = "EditTargetNameHigh-" + System.nanoTime();
        String studentId = createStudentAndGetId(originalName, "Physics", "3.0");

        driver.findElement(By.xpath(
                "//tr[@id='student-row-" + studentId + "']//button[@id='edit-student-button']")).click();

        WebElement editName = driverWait().until(
                ExpectedConditions.visibilityOfElementLocated(By.id("edit-student-name")));
        editName.clear();
        editName.sendKeys("A".repeat(256));
        driver.findElement(By.id("edit-student-save-button")).click();

        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("status-message")));
        takeScreenshot("SL2B2-edit-student-name-above-boundary.png");

        assertEquals(originalName, driver.findElement(By.id("student-name-" + studentId)).getText());
    }

    // SL2B3
    @Test
    @Order(19)
    public void testEditStudentMajorAboveBoundary() {
        String originalName = "EditTargetMajorHigh-" + System.nanoTime();
        String studentId = createStudentAndGetId(originalName, "Physics", "3.0");

        String majorBefore = driver.findElement(By.id("student-major-" + studentId)).getText();

        driver.findElement(By.xpath(
                "//tr[@id='student-row-" + studentId + "']//button[@id='edit-student-button']")).click();

        WebElement editMajor = driverWait().until(
                ExpectedConditions.visibilityOfElementLocated(By.id("edit-student-major")));
        editMajor.clear();
        editMajor.sendKeys("A".repeat(256));
        driver.findElement(By.id("edit-student-save-button")).click();

        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("status-message")));
        takeScreenshot("SL2B3-edit-student-major-above-boundary.png");

        assertEquals(majorBefore, driver.findElement(By.id("student-major-" + studentId)).getText());
    }

    // SL2B4
    @Test
    @Order(20)
    public void testEditStudentGPAAboveMax() {
        String originalName = "EditTargetGpaHigh-" + System.nanoTime();
        String studentId = createStudentAndGetId(originalName, "Physics", "3.0");

        String gpaBefore = driver.findElement(By.id("student-gpa-" + studentId)).getText();

        driver.findElement(By.xpath(
                "//tr[@id='student-row-" + studentId + "']//button[@id='edit-student-button']")).click();

        WebElement editGpa = driverWait().until(
                ExpectedConditions.visibilityOfElementLocated(By.id("edit-student-gpa")));
        editGpa.clear();
        editGpa.sendKeys("4.1");
        driver.findElement(By.id("edit-student-save-button")).click();

        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("status-message")));
        takeScreenshot("SL2B4-edit-student-gpa-above-max.png");

        assertEquals(gpaBefore, driver.findElement(By.id("student-gpa-" + studentId)).getText());
    }

    // SL2B5
    @Test
    @Order(21)
    public void testEditStudentGPABelowMin() {
        String originalName = "EditTargetGpaLow-" + System.nanoTime();
        String studentId = createStudentAndGetId(originalName, "Physics", "3.0");

        String gpaBefore = driver.findElement(By.id("student-gpa-" + studentId)).getText();

        driver.findElement(By.xpath(
                "//tr[@id='student-row-" + studentId + "']//button[@id='edit-student-button']")).click();

        WebElement editGpa = driverWait().until(
                ExpectedConditions.visibilityOfElementLocated(By.id("edit-student-gpa")));
        editGpa.clear();
        editGpa.sendKeys("-0.1");
        driver.findElement(By.id("edit-student-save-button")).click();

        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("status-message")));
        takeScreenshot("SL2B5-edit-student-gpa-below-min.png");

        assertEquals(gpaBefore, driver.findElement(By.id("student-gpa-" + studentId)).getText());
    }
}
