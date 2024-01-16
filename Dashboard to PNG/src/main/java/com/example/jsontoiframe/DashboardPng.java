package com.example.jsontoiframe;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

public class DashboardPng {

	public static void main(String[] args) {
		// Get the dashboard ID from the user
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Enter the dashboard ID: ");

		try {
			String dashboardId = reader.readLine();
			// Construct the API endpoint using the provided dashboard ID
			String apiEndpoint = "http://localhost:5601/api/kibana/dashboards/export?dashboard=" + dashboardId;
			// Run the curl command and get the JSON response
			String jsonResponse = runCurlCommand(apiEndpoint);
			// Convert the JSON response into an iframe URL
			String iframeUrl = convertJsonToIframeUrl(jsonResponse, dashboardId);
			// Print the iframe URL
			System.out.println("Generated iframe URL: " + iframeUrl);
			System.setProperty("webdriver.chrome.driver", "C:\\Users\\luxje\\Desktop\\chromedriver-win64\\chromedriver.exe");
			// Initialize ChromeDriver
			WebDriver driver = new ChromeDriver();
			// Navigate to the webpage you want to capture
			driver.get(iframeUrl);
			// Wait for the alert to be present
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
			WebElement dismissButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[@data-test-subj='dismissAlertButton']")));
			// Dismiss the alert
			dismissButton.click();
			try {
				// Wait for 18 seconds (18000 milliseconds) - adjusted based on your requirements
				Thread.sleep(18000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Scroll down the page using JavaScriptExecutor
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("window.scrollBy(0, 500);"); // You can adjust the scroll amount as needed
			// Take a screenshot
			File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			// Specify the file path where you want to save the screenshot
			String screenshotFilePath = generateScreenshotFilePath();
			// Save the screenshot with the dynamically generated file name
			FileUtils.copyFile(screenshot, new File(screenshotFilePath));

			try (InputStream input = new FileInputStream(screenshot);
				 OutputStream output = new FileOutputStream(screenshotFilePath)) {
				// Copy the screenshot to the specified file path
				byte[] buffer = new byte[1024];
				int bytesRead;
				while ((bytesRead = input.read(buffer)) != -1) {
					output.write(buffer, 0, bytesRead);
				}
				System.out.println("Screenshot saved successfully at: " + screenshotFilePath);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// Close the browser
				driver.quit();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String generateScreenshotFilePath() {
		// Create a timestamp to make the file name unique
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		// Specify the directory where you want to save the screenshots
		String directoryPath = "C:\\Users\\luxje\\Desktop\\";
		// Construct the dynamically generated file name
		String fileName = "NewDash_" + timeStamp + ".png";
		// Combine the directory path and file name to get the full file path
		return directoryPath + fileName;
	}

	private static String runCurlCommand(String apiEndpoint) throws IOException {
		// Build the curl command
		String curlCommand = "curl -X GET " + apiEndpoint;
		// Run the curl command and read the response
		Process process = Runtime.getRuntime().exec(curlCommand);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			StringBuilder response = new StringBuilder();
			String line;
			// Read the command output line by line
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			// Wait for the process to complete
			try {
				process.waitFor();
				// Check if the process exited successfully
				if (process.exitValue() == 0) {
					System.out.println("Curl command executed successfully.");
					return response.toString();
				} else {
					System.out.println("Error executing curl command. Exit code: " + process.exitValue());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static String convertJsonToIframeUrl(String jsonResponse, String dashboardId) {
		// Construct the iframe URL based on the provided structure and dashboardId
		return String.format("http://localhost:5601/app/dashboards#/view/%s", dashboardId);
	}
}
