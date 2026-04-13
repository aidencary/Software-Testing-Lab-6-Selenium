package com.baarsch_bytes.end2end;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class Step2_CourseCRUDTest {

    //private static final String COURSE_URL = "http://frontend:5173/";
    //private static final String STUDENT_URL = "http://frontend:5173/students";
    private static final String COURSE_URL = "http://localhost:5173/";
    private static final String STUDENT_URL = "http://localhost:5173/students";
    private static final int WAIT_SEC = 10;

    private WebDriver driver;

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

    /** Fill the new-course form and click Add Course (no ID on button — use xpath). */
    private void fillAndSubmitCourse(String name, String instructor, String maxSize, String room) {
        driver.findElement(By.id("new-course-name")).sendKeys(name);
        driver.findElement(By.id("new-course-instructor")).sendKeys(instructor);
        driver.findElement(By.id("new-course-max-size")).sendKeys(maxSize);
        driver.findElement(By.id("new-course-room")).sendKeys(room);
        driver.findElement(By.xpath("//div[@id='new-course-fields']//button")).click();
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

    // CL1A1
    @Test
    @Order(1)
    public void testAddCourse() {
        driver.get(COURSE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-course-name")));

        fillAndSubmitCourse("Software Testing", "1", "5", "MCS 338");

        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'course-name-') and text()='Software Testing']")));

        takeScreenshot("CL1A1-add-course.png");

        assertEquals("Software Testing", driver.findElement(
                By.xpath("//td[contains(@id,'course-name-') and text()='Software Testing']")).getText());
    }

    // CL1A2
    @Test
    @Order(2)
    public void testAddCourseUpperNameBoundary() {
        driver.get(COURSE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-course-name")));

        String longName = "A".repeat(255);
        int before = driver.findElements(By.xpath("//tr[contains(@id,'course-row-')]")).size();

        fillAndSubmitCourse(longName, "1", "5", "MCS 101");

        driverWait().until(d ->
                d.findElements(By.xpath("//tr[contains(@id,'course-row-')]")).size() == before + 1);

        takeScreenshot("CL1A2-course-name-upper-boundary.png");
        assertEquals(before + 1,
                driver.findElements(By.xpath("//tr[contains(@id,'course-row-')]")).size());
    }

    // CL1A3
    @Test
    @Order(3)
    public void testAddCourseUpperRoomBoundary() {
        driver.get(COURSE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-course-name")));

        String longRoom = "A".repeat(255);
        int before = driver.findElements(By.xpath("//tr[contains(@id,'course-row-')]")).size();

        fillAndSubmitCourse("TestCourseRoom", "1", "5", longRoom);

        driverWait().until(d ->
                d.findElements(By.xpath("//tr[contains(@id,'course-row-')]")).size() == before + 1);

        takeScreenshot("CL1A3-course-room-upper-boundary.png");
        assertEquals(before + 1,
                driver.findElements(By.xpath("//tr[contains(@id,'course-row-')]")).size());
    }

    // CL1A4
    @Test
    @Order(4)
    public void testAddCourseSizeBoundary() {
        driver.get(COURSE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-course-name")));

        fillAndSubmitCourse("TestCourseSize1", "1", "1", "MCS 100");

        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'course-name-') and text()='TestCourseSize1']")));

        takeScreenshot("CL1A4-course-size-boundary.png");
        assertNotNull(driver.findElement(
                By.xpath("//td[contains(@id,'course-name-') and text()='TestCourseSize1']")));
    }

    // CL1B1
    @Test
    @Order(5)
    public void testAddCourseMissingName() {
        driver.get(COURSE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-course-name")));

        int before = driver.findElements(By.xpath("//tr[contains(@id,'course-row-')]")).size();

        fillAndSubmitCourse("", "1", "5", "MCS 338");

        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("status-message")));

        takeScreenshot("CL1B1-missing-name.png");

        int after = driver.findElements(By.xpath("//tr[contains(@id,'course-row-')]")).size();
        assertEquals(before, after);
    }

    // CL1B2
    @Test
    @Order(6)
    public void testAddCourseMissingRoom() {
        driver.get(COURSE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-course-name")));

        int before = driver.findElements(By.xpath("//tr[contains(@id,'course-row-')]")).size();

        fillAndSubmitCourse("TestCourseNoRoom", "1", "5", "");

        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("status-message")));

        takeScreenshot("CL1B2-missing-room.png");

        int after = driver.findElements(By.xpath("//tr[contains(@id,'course-row-')]")).size();
        assertEquals(before, after);
    }

    // CL1B3
    @Test
    @Order(7)
    public void testAddCourseSizeBelowBoundary() {
        driver.get(COURSE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-course-name")));

        int before = driver.findElements(By.xpath("//tr[contains(@id,'course-row-')]")).size();

        fillAndSubmitCourse("TestCourseSize0", "1", "0", "MCS 999");

        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("status-message")));

        takeScreenshot("CL1B3-size-below-boundary.png");

        int after = driver.findElements(By.xpath("//tr[contains(@id,'course-row-')]")).size();
        assertEquals(before, after);
    }

    // CL2A1
    @Test
    @Order(8)
    public void testEditCourse() {
        driver.get(COURSE_URL);
        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'course-name-') and text()='Software Testing']")));

        WebElement nameCell = driver.findElement(
                By.xpath("//td[contains(@id,'course-name-') and text()='Software Testing']"));
        String courseId = nameCell.getAttribute("id").replace("course-name-", "");

        driver.findElement(By.xpath(
                "//tr[@id='course-row-" + courseId + "']//button[@id='edit-course-button']")).click();

        WebElement editName = driverWait().until(
                ExpectedConditions.visibilityOfElementLocated(By.id("edit-course-name")));

        editName.clear();
        editName.sendKeys("CS2");

        driver.findElement(By.id("edit-course-save-button")).click();

        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'course-name-') and text()='CS2']")));

        takeScreenshot("CL2A1-edit-course.png");

        assertEquals("CS2", driver.findElement(
                By.xpath("//td[contains(@id,'course-name-') and text()='CS2']")).getText());
    }

    // CL3A1
    @Test
    @Order(9)
    public void testDeleteCourse() {
        driver.get(COURSE_URL);
        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'course-name-') and text()='CS2']")));

        WebElement nameCell = driver.findElement(
                By.xpath("//td[contains(@id,'course-name-') and text()='CS2']"));
        String courseId = nameCell.getAttribute("id").replace("course-name-", "");

        driver.findElement(By.xpath(
                "//tr[@id='course-row-" + courseId + "']//button[@id='delete-course-button']")).click();

        takeScreenshot("CL3A1-delete-course.png");

        driverWait().until(ExpectedConditions.invisibilityOfElementLocated(
                By.id("course-row-" + courseId)));

        assertTrue(driver.findElements(By.id("course-row-" + courseId)).isEmpty());
    }

    // CL4A1
    @Test
    @Order(10)
    public void testAddStudentToCourse() {
        // Add a test student
        driver.get(STUDENT_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-student-name")));
        driver.findElement(By.id("new-student-name")).sendKeys("TestStudentEnroll");
        driver.findElement(By.id("new-student-major")).sendKeys("Computer Science");
        driver.findElement(By.id("new-student-gpa")).sendKeys("3.0");
        driver.findElement(By.id("add-student-button")).click();
        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'student-name-') and text()='TestStudentEnroll']")));

        // Add a test course
        driver.get(COURSE_URL);
        driverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("new-course-name")));
        fillAndSubmitCourse("TestCourseEnroll", "1", "10", "MCS 200");
        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'course-name-') and text()='TestCourseEnroll']")));

        WebElement courseNameCell = driver.findElement(
                By.xpath("//td[contains(@id,'course-name-') and text()='TestCourseEnroll']"));
        String courseId = courseNameCell.getAttribute("id").replace("course-name-", "");

        int rosterBefore = Integer.parseInt(
                driver.findElement(By.id("course-roster-" + courseId)).getText());

        // Select TestStudentEnroll and add to course
        WebElement studentSelect = driver.findElement(By.xpath(
                "//tr[@id='course-row-" + courseId + "']//select[@id='select-student']"));
        new Select(studentSelect).selectByVisibleText("TestStudentEnroll");

        driver.findElement(By.xpath(
                "//tr[@id='course-row-" + courseId + "']//button[@id='add-student-button']")).click();

        final String finalCourseId = courseId;
        driverWait().until(d ->
                Integer.parseInt(d.findElement(By.id("course-roster-" + finalCourseId)).getText())
                        == rosterBefore + 1);

        takeScreenshot("CL4A1-add-student-to-course.png");

        assertEquals(rosterBefore + 1,
                Integer.parseInt(driver.findElement(By.id("course-roster-" + courseId)).getText()));
    }

    // CL4A2
    @Test
    @Order(11)
    public void testRemoveStudentFromCourse() {
        driver.get(COURSE_URL);
        driverWait().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(@id,'course-name-') and text()='TestCourseEnroll']")));

        WebElement courseNameCell = driver.findElement(
                By.xpath("//td[contains(@id,'course-name-') and text()='TestCourseEnroll']"));
        String courseId = courseNameCell.getAttribute("id").replace("course-name-", "");

        int rosterBefore = Integer.parseInt(
                driver.findElement(By.id("course-roster-" + courseId)).getText());

        // Click Edit to reveal remove-student controls
        driver.findElement(By.xpath(
                "//tr[@id='course-row-" + courseId + "']//button[@id='edit-course-button']")).click();

        WebElement removeSelect = driverWait().until(
                ExpectedConditions.visibilityOfElementLocated(By.id("remove-student-select")));
        new Select(removeSelect).selectByVisibleText("TestStudentEnroll");

        driver.findElement(By.id("remove-student-button")).click();

        final String finalCourseId = courseId;
        driverWait().until(d ->
                Integer.parseInt(d.findElement(By.id("course-roster-" + finalCourseId)).getText())
                        == rosterBefore - 1);

        takeScreenshot("CL4A2-remove-student-from-course.png");

        assertEquals(rosterBefore - 1,
                Integer.parseInt(driver.findElement(By.id("course-roster-" + courseId)).getText()));
    }
}
