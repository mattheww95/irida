package ca.corefacility.bioinformatics.irida.ria.web.projects;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.corefacility.bioinformatics.irida.model.workflow.analysis.ProjectSampleAnalysisOutputInfo;
import ca.corefacility.bioinformatics.irida.ria.web.services.UIProjectAnalysesService;

/**
 * Controller for handling all ajax requests for Project Single Sample Analyses Outputs.
 */
@RestController
@RequestMapping("/ajax/projects/analyses-outputs")
public class ProjectAnalysesAjaxController {

	private UIProjectAnalysesService uiProjectAnalysesService;

	@Autowired
	public ProjectAnalysesAjaxController(UIProjectAnalysesService uiProjectAnalysesService) {
		this.uiProjectAnalysesService = uiProjectAnalysesService;
	}

	/**
	 * Get all the shared single sample analysis outputs for the project
	 *
	 * @param projectId {@link ca.corefacility.bioinformatics.irida.model.project.Project} id
	 * @return a response containing a list of filtered {@link ProjectSampleAnalysisOutputInfo} shated single sample analysis outputs
	 */
	@GetMapping("/shared")
	public ResponseEntity<List<ProjectSampleAnalysisOutputInfo>> getSharedSingleSampleOutputs(
			@RequestParam Long projectId) {
		return ResponseEntity.ok(uiProjectAnalysesService.getSharedSingleSampleOutputs(projectId));
	}

	/**
	 * Get all the automated single sample analysis outputs for the project
	 *
	 * @param projectId {@link ca.corefacility.bioinformatics.irida.model.project.Project} id
	 * @return a response containing a list of filtered {@link ProjectSampleAnalysisOutputInfo} automated single sample analysis outputs
	 */
	@GetMapping("/automated")
	public ResponseEntity<List<ProjectSampleAnalysisOutputInfo>> getAutomatedSingleSampleOutputs(@RequestParam Long projectId) {
		return ResponseEntity.ok(uiProjectAnalysesService.getAutomatedSingleSampleOutputs(projectId));
	}

}
