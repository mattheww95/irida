package ca.corefacility.bioinformatics.irida.ria.integration.pages.projects;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import ca.corefacility.bioinformatics.irida.ria.integration.pages.AbstractPage;

/**
 * Created by josh on 2016-10-07.
 */
public class ProjectSampleMetadataImportPage extends AbstractPage {
	@FindBy(css = ".t-metadata-uploader-dropzone input")
	WebElement dropzone;
	@FindBy(className = "t-metadata-uploader-file-button")
	WebElement fileBtn;
	@FindBy(css = "input[type=radio]")
	List<WebElement> headerRadios;
	@FindBy(className = "t-metadata-uploader-preview-button")
	WebElement previewBtn;
	@FindBy(className = "t-metadata-uploader-upload-button")
	WebElement uploadBtn;
	@FindBy(className = "t-metadata-uploader-review-table")
	WebElement reviewTable;
	@FindBy(css = "table > tbody > tr > td.t-metadata-uploader-new-column:empty")
	List<WebElement> updateRows;
	@FindBy(css = "table > tbody > tr > td.t-metadata-uploader-new-column > span")
	List<WebElement> newRows;
	@FindBy(css = "thead th")
	List<WebElement> headers;
	@FindBy(css = "tbody tr.ant-table-row")
	List<WebElement> rows;
	@FindBy(css = "div.ant-alert-error")
	WebElement validationAlert;
	@FindBy(css = "div.ant-result-success")
	WebElement successMessage;

	public ProjectSampleMetadataImportPage(WebDriver driver) {
		super(driver);
	}

	public static ProjectSampleMetadataImportPage goToPage(WebDriver driver) {
		get(driver, "projects/1/sample-metadata/upload/file");
		return PageFactory.initElements(driver, ProjectSampleMetadataImportPage.class);
	}

	public void uploadMetadataFile(String filePath) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		Path path = Paths.get(filePath);
		dropzone.sendKeys(path.toAbsolutePath().toString());
		wait.until(ExpectedConditions.visibilityOf(fileBtn));
	}

	public void goToReviewPage() {
		previewBtn.click();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		wait.until(ExpectedConditions.visibilityOf(reviewTable));
	}

	public void goToCompletePage() {
		uploadBtn.click();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		wait.until(ExpectedConditions.visibilityOf(successMessage));
	}

	public void selectSampleNameColumn() {
		headerRadios.get(3).click();
		goToReviewPage();
	}

	public String getValueForSelectedSampleNameColumn() {
		for (WebElement headerRadio : headerRadios) {
			boolean isSelected = headerRadio.isSelected();
			if (isSelected) {
				return headerRadio.getAttribute("value");
			}
		}
		return null;
	}

	public int getUpdateCount() {
		return updateRows.size();
	}

	public int getNewCount() {
		return newRows.size();
	}

	public List<String> getValuesForColumnByName(String column) {
		// Get the text from the headers
		List<String> headerText = headers.stream().map(WebElement::getText).collect(Collectors.toList());
		// Find which columns is the numbers
		int index = headerText.indexOf(column);
		return rows.stream()
				.map(row -> row.findElements(By.tagName("td")).get(index).getText())
				.collect(Collectors.toList());
	}

	public boolean isAlertDisplayed() {
		return validationAlert.isDisplayed();
	}

	public boolean isSuccessDisplayed() {
		return successMessage.isDisplayed();
	}
}
