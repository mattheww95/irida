package ca.corefacility.bioinformatics.irida.ria.integration.pages.projects;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import ca.corefacility.bioinformatics.irida.ria.integration.utilities.Ajax;
import ca.corefacility.bioinformatics.irida.ria.integration.utilities.SortUtilities;

import com.google.common.base.Strings;

/**
 * <p>
 * Page Object to represent the project samples page.
 * </p>
 *
 * @author Josh Adam <josh.adam@phac-aspc.gc.ca>
 */
public class ProjectSamplesPage {
	public static final String DATE_FORMAT = "dd MMM YYYY";
	private static final String URL = "http://localhost:8080/projects/1/samples";
	private static int TIMEOUT_TIME = 100000;
	private WebDriver driver;

	public ProjectSamplesPage(WebDriver driver) {
		this.driver = driver;
	}

	public void goToPage() {
		driver.get(URL);
		waitForAjax();
	}

	/**
	 * The the h1 heading for the page
	 * 
	 * @return String value from within the h1 tag
	 */
	public String getTitle() {
		return driver.findElement(By.tagName("h1")).getText();
	}

	/**
	 * Get the number of displayed samples on the page.
	 * 
	 * @return integer value of displayed samples on the page.
	 */
	public int getDisplayedSampleCount() {
		return driver.findElements(By.cssSelector("tbody tr")).size();
	}

	/**
	 * Get the number of samples that contain files.
	 * 
	 * @return integer value of samples that contain files.
	 */
	public int getCountOfSamplesWithFiles() {
		return driver.findElements(By.cssSelector("td.details-control a")).size();
	}

	/**
	 * Get the number of selected inputs in the samples table body.
	 * 
	 * @return integer value of the number of selected inuts contained within
	 *         the tbody
	 */
	public int getSelectedSampleCount() {
		return driver.findElements(By.cssSelector("tbody input[type=\"checkbox\"]:checked")).size();
	}

	/**
	 * Checks to see if the selectAll checkbox is in an indeterminate state.
	 * 
	 * @return True if in an indeterminate state.
	 */
	public boolean isSelectAllInIndeterminateState() {
		String exists = driver.findElement(By.id("selectAll")).getAttribute("indeterminate");
		if (Strings.isNullOrEmpty(exists)) {
			return false;
		}
		return true;
	}

	/**
	 * Checks to see if the selectAll checkbox is in a checked state.
	 * 
	 * @return True if in a checked state.
	 */
	public boolean isSelectAllSelected() {
		return driver.findElement(By.id("selectAll")).getAttribute("checked").equals("true");
	}

	/**
	 * Checks to see if a files detail area is displayed
	 * 
	 * @return True if the file details area is displayed
	 */
	public boolean isFilesAreaDisplayed() {
		return driver.findElements(By.cssSelector("tbody tr.details + tr")).size() == 1;
	}

	/**
	 * Checks to see if the Sample Name column is sorted ascending
	 * 
	 * @return True if entire column is sorted ascending
	 */
	public boolean isSampleNameColumnSortedAsc() {
		List<String> list = driver.findElements(By.cssSelector("tbody td:nth-child(2)")).stream()
				.map(WebElement::getText).collect(Collectors.toList());
		return SortUtilities.isStringListSortedAsc(list);
	}

	/**
	 * Checks to see if the Sample Name column is sorted descending
	 *
	 * @return True if entire column is sorted descending
	 */
	public boolean isSampleNameColumnSortedDesc() {
		List<String> list = driver.findElements(By.cssSelector("tbody td:nth-child(2)")).stream()
				.map(WebElement::getText).collect(Collectors.toList());
		return SortUtilities.isStringListSortedDesc(list);
	}

	/**
	 * Checks to see if the Sample Added On column is sorted ascending
	 *
	 * @return True if entire column is sorted ascending
	 */
	public boolean isAddedOnDateColumnSortedAsc() {
		List<String> list = driver.findElements(By.cssSelector("tbody td:nth-child(4)")).stream()
				.map(WebElement::getText).collect(Collectors.toList());
		return SortUtilities.isDateSortedAsc(list, DATE_FORMAT);
	}

	/**
	 * Checks to see if the Sample Added On column is sorted descending
	 *
	 * @return True if entire column is sorted descending
	 */
	public boolean isAddedOnDateColumnSortedDesc() {
		List<String> list = driver.findElements(By.cssSelector("tbody td:nth-child(4)")).stream()
				.map(WebElement::getText).collect(Collectors.toList());
		return SortUtilities.isDateSortedDesc(list, DATE_FORMAT);
	}

	/**
	 * Checks to see if the is an input field named "editName" visible and that
	 * there is only one.
	 * 
	 * @return true if there is 1 visisble
	 */
	public boolean isRenameInputVisible() {
		return driver.findElements(By.id("editName")).size() == 1;
	}

    public String getSampleName() {
        return driver.findElement(By.cssSelector("tbody tr:nth-child(1) td:nth-child(2)")).getText();
    }

	/**
	 * Test to show that the noty message was shown.
	 * 
	 * @return True if the message area is present.
	 */
    public boolean successMessageShown() {
        return driver.findElements(By.cssSelector(".noty_message")).size() > 0;
    }

	/**
	 * Test to see fi the qtip message is shown.
	 * 
	 * @return True if the qtip message is shown.
	 */
	public boolean hasErrorMessage() {
        return driver.findElements(By.className("qtip-content")).size() == 1;
    }

    /**
	 * Test to see if there is a formatting message displayed.
	 * 
	 * @return True if the words "space character" is shown.
	 */
	public boolean hasFormattingMessage() {
        return driver.findElement(By.className("qtip-content")).getText().contains("space character");
    }

    /**
	 * Check to see if the delete button is disabled
	 * 
	 * @return Return true if the button is disabled
	 */
	public boolean isDeleteBtnDisabled() {
		return driver.findElement(By.id("deleteBtn")).getAttribute("disabled").equals("true");
	}

	/**
	 * Check to see if the table empty row is displayed
	 * 
	 * @return True if the table empty row is displayed.
	 */
	public boolean isTableEmptyRowShown() {
		return driver.findElements(By.className("dataTables_empty")).size() == 1;
	}

    public boolean isCombineSamplesModalOpen() {
        return driver.findElements(By.cssSelector(".noty_bar h2")).size() == 1;
    }

    public String getSampleNameForRow(int rowNum) {
        return driver.findElement(By.cssSelector("tbody tr:nth-child(" + rowNum + ") .name")).getText();
    }

    public boolean isMergeErrorDisplayed() {
        return driver.findElements(By.id("merge-error")).size() == 1;
    }

	/**************************************************************************************
	 * EVENTS
	 **************************************************************************************/

	/**
	 * Click on the Select All Checkbox
	 */
	public void clickSelectAllCheckbox() {
		driver.findElement(By.id("selectAll")).click();
	}

	/**
	 * Click on the first checkbox in the tbody.
	 */
	public void clickFirstSampleCheckbox() {
		driver.findElement(By.cssSelector("tbody > tr input[type=\"checkbox\"]")).click();
	}

	/**
	 * Open the row that contains files.
	 */
	public void openFilesView() {
		driver.findElement(By.cssSelector("td.details-control a")).click();
	}

	/**
	 * Click the table header for the samples name. This will enable sorting by
	 * that column.
	 */
	public void clickSampleNameHeader() {
		driver.findElement(By.cssSelector("thead th:nth-child(2)")).click();
		waitForAjax();
	}

	/**
	 * Click the table header for the samples added on date.. This will enable
	 * sorting by that column.
	 */
	public void clickCreatedDateHeader() {
		driver.findElement(By.cssSelector("thead th:nth-child(4)")).click();
		waitForAjax();
	}

	/**
	 * Display the sample edit
	 */
	public void clickEditFirstSample() {
		driver.findElement(By.cssSelector("tbody tr:nth-child(1) button.edit")).click();
	}

	public void selectFirstSample() {
		driver.findElement(By.cssSelector("tbody tr:nth-child(1) input[type=\"checkbox\"]")).click();
	}

	/**
	 * Rename a sample
	 */
	public void sendRenameSample(String name) {
        WebElement el = driver.findElement(By.id("editName"));
        el.sendKeys(name);
        el.sendKeys(Keys.ENTER);
        waitForAjax();
    }

    public void clickOnEditCancel() {
        driver.findElement(By.cssSelector("button.cancel")).click();
    }

	public void clickDeleteSamples() {
		driver.findElement(By.id("deleteBtn")).click();
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT_TIME);
		wait.until(ExpectedConditions.elementToBeClickable(By.id("button-0")));
		driver.findElement(By.id("button-0")).click();
		waitForAjax();
	}

    public void clickFirstThreeCheckboxes() {
        List<WebElement> checkboxes = driver.findElements(By.cssSelector("tbody input[type=\"checkbox\"]"));
        for (int i = 0; i < 3; i++) {
            checkboxes.get(i).click();
        }
    }

    public void clickCombineSamples() {
        driver.findElement(By.id("combineBtn")).click();
        try {
            (new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By
                    .className("noty_message")));
        } catch (NoSuchElementException e) {
            // Nothing to do, it will die on the next test.
        }
    }

    public void selectFirstNameInCombineSamples(String name) {
        WebElement el = driver.findElement(By.className("select2-choice"));
        el.click();
        el.sendKeys(name);
        el.sendKeys(Keys.TAB);
        driver.findElement(By.cssSelector(".noty_buttons .btn-primary")).click();
        waitForAjax();
    }

	private void waitForAjax() {
		Wait<WebDriver> wait = new WebDriverWait(driver, 60);
		wait.until(Ajax.waitForAjax(60000));
	}
}
