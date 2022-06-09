package ca.corefacility.bioinformatics.irida.ria.integration.components;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import ca.corefacility.bioinformatics.irida.ria.integration.pages.AbstractPage;

public class SampleDetailsViewer extends AbstractPage {
	@FindBy(className = "t-sample-details-modal")
	private WebElement modal;

	@FindBy(className = "t-concatenate-confirm-modal")
	private WebElement concatenateModal;

	@FindBy(className = "t-sample-details-name")
	private WebElement sampleName;

	@FindBy(className = "t-sample-details-project-name")
	private WebElement projectName;

	@FindBy(className = "t-sample-created-date")
	private WebElement createdDate;

	@FindBy(className = "t-sample-details-metadata-item")
	private List<WebElement> metadataFields;

	@FindBy(id="rc-tabs-0-tab-metadata")
	private WebElement metadataTabLink;

	@FindBy(id="rc-tabs-0-tab-files")
	private WebElement filesTabLink;

	@FindBy(id="rc-tabs-0-tab-analyses")
	private WebElement sampleAnalysesTabLink;

	@FindBy(className = "t-upload-sample-files")
	private List<WebElement> dragUploadList;

	@FindBy(className = "t-file-details")
	private List<WebElement> files;

	@FindBy(className = "t-add-new-metadata-btn")
	private List<WebElement> addNewMetadataBtn;

	@FindBy(className = "t-remove-file-btn")
	private List<WebElement> removeFileBtns;

	@FindBy(className = "t-concatenation-checkbox")
	private List<WebElement> concatenationCheckboxes;

	@FindBy(className = "t-concatenate-btn")
	private List<WebElement> concatenateBtn;

	@FindBy(className = "t-file-processing-status")
	private List<WebElement> processingStatuses;

	@FindBy(className = "t-concatenate-confirm")
	private List<WebElement> concatenateConfirmBtn;

	@FindBy(className = "t-download-file-btn")
	private List<WebElement> downloadFileBtns;

	@FindBy(className = "t-file-label")
	private List<WebElement> fileLabels;

	@FindBy(id="t-remove-originals-true")
	private WebElement removeOriginalRadioButton;

	@FindBy(className = "t-remove-file-confirm-btn")
	private List<WebElement> confirmBtns;

	@FindBy(className = "t-set-default-seq-obj-button")
	private List<WebElement> setDefaultSeqObjBtns;

	@FindBy(className = "t-default-seq-obj-tag")
	private List<WebElement> defaultSeqObjTags;

	@FindBy(className= "t-sample-analyses")
	private WebElement sampleAnalysesTable;

	@FindBy(className= "t-sample-analyses-search-input")
	private WebElement sampleAnalysesSearchInput;

	@FindBy(className = "ant-table-row")
	private List<WebElement> sampleAnalysesList;

	@FindBy(className = "t-add-sample-to-cart")
	private WebElement addSampleToCartBtn;

	@FindBy(className = "t-remove-sample-from-cart")
	private WebElement removeSampleFromCartBtn;


	public SampleDetailsViewer(WebDriver driver) {
		super(driver);
	}
	private String concatenatedFileName = "NewConcatenatedFile";

	public static SampleDetailsViewer getSampleDetails(WebDriver driver) {
		return PageFactory.initElements(driver, SampleDetailsViewer.class);
	}

	public String getSampleName() {
		return sampleName.getText();
	}

	public String getProjectName() {
		return projectName.getText();
	}

	public String getCreatedDateForSample() {
		return createdDate.getText();
	}

	public void closeDetails() {
		modal.findElement(By.className("ant-modal-close"))
				.click();
	}

	public int getNumberOfMetadataEntries() {
		return metadataFields.size();
	}

	public String getValueForMetadataField(String label) {
		for (WebElement field : metadataFields) {
			if (field.findElement(By.className("t-sample-details-metadata__field"))
					.getText()
					.equals(label)) {
				return field.findElement(By.className("t-sample-details-metadata__entry"))
						.getText();
			}
		}
		return null;
	}

	public void clickMetadataTabLink() {
		metadataTabLink.click();
		waitForTime(300);
	}

	public void clickFilesTabLink() {
		filesTabLink.click();
		waitForTime(300);
	}

	public void clickSampleAnalysesTabLink() {
		sampleAnalysesTabLink.click();
		waitForTime(300);
	}

	public boolean fileUploadVisible() {
		return dragUploadList.size() == 1;
	}

	public int numberOfFilesDisplayed() {
		if(files != null) {
			return files.size();
		}
		return 0;
	}

	public boolean addNewMetadataButtonVisible() {
		return addNewMetadataBtn.size() == 1;
	}

	public int removeFileButtonsVisible() {
		if(removeFileBtns != null) {
			return removeFileBtns.size();
		}
		return 0;
	}

	public int concatenationCheckboxesVisible() {

		if(concatenationCheckboxes != null) {
			return concatenationCheckboxes.size();
		}
		return 0;
	}

	public boolean concatenationButtonVisible() {
		return concatenateBtn.size() == 1;
	}

	public void selectFilesToConcatenate(int maxCheckboxesToSelect) {
		List<WebElement> checkboxes = modal.findElements(By.className("t-concatenation-checkbox"));
		if(checkboxes.size() > 0 && maxCheckboxesToSelect < checkboxes.size()) {
			checkboxes = checkboxes.subList(0,maxCheckboxesToSelect);
			for(WebElement element : checkboxes) {
				element.click();
			}
		}
	}

	public void clickConcatenateBtn() {
		concatenateBtn.get(0).click();
		waitForTime(500);
	}

	public int processingStatusesCount() {
			return processingStatuses.size();
	}

	public int singleEndFileCount() {
		return concatenateModal.findElements(By.className("t-single-end-file")).size();
	}

	public void  enterFileName() {
		concatenateModal.findElement(By.id("t-concat-new-file-name")).sendKeys(concatenatedFileName);
	}

	public void  enterFileName(String filename) {
		concatenateModal.findElement(By.id("t-concat-new-file-name")).sendKeys(filename);
	}

	public void clickConcatenateConfirmBtn() {
		concatenateConfirmBtn.get(0).click();
		WebDriverWait wait = new WebDriverWait(driver, 20);
		wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.className("ant-notification"))));
		waitForTime(500);
	}

	public int downloadFileButtonsVisible() {
		if(downloadFileBtns != null) {
			return downloadFileBtns.size();
		}
		return 0;
	}

	public boolean correctFileNamesDisplayedAdmin(List<String> existingFileNames) {
		boolean correctNames = true;

		for (WebElement fileLabel : fileLabels) {
			String text = fileLabel.getAttribute("innerHTML");
			if (!existingFileNames.contains(text) ) {
				if(!text
						.equals(concatenatedFileName + ".fastq")){
					correctNames = false;
				}
			}
		}
		return correctNames;
	}

	public boolean correctFileNamesDisplayedAdmin(List<String> existingFileNames, String nameOfConcatenatedFile) {
		boolean correctNames = true;

		for (WebElement fileLabel : fileLabels) {
			String text = fileLabel.getAttribute("innerHTML");
			if (!existingFileNames.contains(text) ) {
				if(!text
						.equals(nameOfConcatenatedFile + ".fastq")){
					correctNames = false;
				}
			}
		}
		return correctNames;
	}

	public boolean correctFileNamesDisplayedUser(List<String> existingFileNames) {
		boolean correctNames = true;

		for (WebElement fileLabel : fileLabels) {
			String text = fileLabel.getAttribute("innerHTML");
			if (!existingFileNames.contains(text)) {
				correctNames = false;
			}
		}
		return correctNames;
	}

	public void clickRemoveOriginalsRadioButton() {
		concatenateModal.findElement(By.className("t-remove-originals-true")).click();
		waitForTime(500);
	}

	public void removeFile(int index) {
		removeFileBtns.get(index).click();
		waitForTime(500);
		confirmBtns.get(0).click();
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.className("ant-notification"))));
	}

	public boolean setSetDefaultSeqObjButtonsVisible() {
		return setDefaultSeqObjBtns.size() > 0;
	}

	public int defaultSeqObjTagCount() {
		return defaultSeqObjTags.size();
	}

	public void updateDefaultSequencingObjectForSample() {
		setDefaultSeqObjBtns.get(0).click();
		WebDriverWait wait = new WebDriverWait(driver, 20);
		wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.className("ant-notification"))));
	}

	public boolean searchSampleAnalysesInputVisible() {
		return sampleAnalysesSearchInput.isDisplayed();
	}

	public boolean sampleAnalysesTableVisible() {
		return sampleAnalysesTable.isDisplayed();
	}

	public int numberOfSampleAnalysesVisible() {
		return sampleAnalysesList.size();
	}

	public int filterSampleAnalyses(String searchString) {
		WebElement searchInput = sampleAnalysesSearchInput.findElement(By.className("ant-input"));
		searchInput.sendKeys(searchString);
		waitForTime(500);
		return sampleAnalysesList.size();
	}

	public void clearSampleAnalysesFilter() {
		WebElement searchInput = sampleAnalysesSearchInput.findElement(By.className("ant-input"));
		searchInput.sendKeys(Keys.CONTROL + "a", Keys.DELETE);
		waitForTime(500);
	}

	public boolean isAddSampleToCartButtonVisible() {
		try {
			return addSampleToCartBtn.isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isRemoveSampleFromCartButtonVisible() {
		try {
			return removeSampleFromCartBtn.isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}

	public void clickAddSampleToCartButton() {
		addSampleToCartBtn.click();
		waitForTime(500);
	}

	public void clickRemoveSampleFromCartButton() {
		removeSampleFromCartBtn.click();
		waitForTime(500);
	}

}
