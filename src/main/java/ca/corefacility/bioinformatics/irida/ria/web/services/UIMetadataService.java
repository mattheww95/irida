package ca.corefacility.bioinformatics.irida.ria.web.services;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import ca.corefacility.bioinformatics.irida.model.enums.ProjectRole;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectMetadataTemplateJoin;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplate;
import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplateField;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.MetadataRestriction;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.ui.SelectOption;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.metadata.dto.ProjectMetadataField;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.sample.MetadataTemplateService;

/**
 * Service for Metadata Templates in the user interface
 */
@Component
public class UIMetadataService {
	private final ProjectService projectService;
	private final MetadataTemplateService templateService;
	private final MessageSource messageSource;

	@Autowired
	public UIMetadataService(ProjectService projectService, MetadataTemplateService templateService,
			MessageSource messageSource) {
		this.projectService = projectService;
		this.templateService = templateService;
		this.messageSource = messageSource;
	}

	/**
	 * Get a list of {@link MetadataTemplate} for a specific {@link Project}
	 *
	 * @param projectId Identifier for a {@link Project}
	 * @return {@link List} of {@link MetadataTemplate}
	 */
	public List<MetadataTemplate> getProjectMetadataTemplates(Long projectId) {
		Project project = projectService.read(projectId);
		List<ProjectMetadataTemplateJoin> joins = templateService.getMetadataTemplatesForProject(project);
		return joins.stream()
				.map(ProjectMetadataTemplateJoin::getObject)
				.collect(Collectors.toList());
	}

	/**
	 * Create a new {@link MetadataTemplate} within a {@link Project}
	 *
	 * @param template   Details about the {@link MetadataTemplate} to create
	 * @param projectId Identifier for the {@link Project} to add them template to
	 * @return {@link MetadataTemplate}
	 */
	public MetadataTemplate createMetadataTemplate(MetadataTemplate template, Long projectId) {
		Project project = projectService.read(projectId);
		ProjectMetadataTemplateJoin join = templateService.createMetadataTemplateInProject(template, project);
		return join.getObject();
	}

	/**
	 * Update details within a {@link MetadataTemplate}
	 *
	 * @param template Updated {@link MetadataTemplate} to save
	 * @param locale   Current users {@link Locale}
	 * @return text to display to the user about the result of the update
	 * @throws Exception if there is an error updating the template
	 */
	public String updateMetadataTemplate(MetadataTemplate template, Locale locale) throws Exception {
		try {
			templateService.updateMetadataTemplateInProject(template);
			return messageSource.getMessage("server.MetadataTemplateManager.update-success", new Object[] {}, locale);
		} catch (Exception e) {
			throw new Exception(
					messageSource.getMessage("server.MetadataTemplateManager.update-error", new Object[] {}, locale));
		}
	}

	/**
	 * Remove a {@link MetadataTemplate} from a {@link Project}
	 *
	 * @param templateId Identifier for a {@link MetadataTemplate} to remove
	 * @param projectId  Identifier for a {@link Project}
	 * @param locale     Current users {@link Locale}
	 * @return text to display to the user about the result of removing the template
	 * @throws Exception if there is an error deleting the template
	 */
	public String deleteMetadataTemplate(Long templateId, Long projectId, Locale locale) throws Exception {
		try {
			Project project = projectService.read(projectId);
			templateService.deleteMetadataTemplateFromProject(project, templateId);
			return messageSource.getMessage("server.MetadataTemplateManager.remove-success", new Object[] {}, locale);
		} catch (Exception e) {
			throw new Exception(messageSource.getMessage("server.MetadataTemplateManager.remove-error",
					new Object[] {}, locale));
		}
	}

	/**
	 * Get all {@link MetadataTemplateField}s belonging to a {@link Project}
	 *
	 * @param projectId Identifier for a {@link Project}
	 * @return List of {@link MetadataTemplateField}
	 */
	public List<ProjectMetadataField> getMetadataFieldsForProject(Long projectId) {
		Project project = projectService.read(projectId);
		List<MetadataTemplateField> fields = templateService.getMetadataFieldsForProject(project);
		return fields.stream()
				.map(field -> {
					MetadataRestriction restriction = templateService.getMetadataRestrictionForFieldAndProject(project,
							field);
					String level = restriction == null ?
							"PROJECT_USER" :
							restriction.getLevel()
									.toString();
					return new ProjectMetadataField(field, level);
				})
				.collect(Collectors.toList());
	}

	public List<SelectOption> getMetadataFieldRestrictions(Locale locale) {
		return Arrays.stream(ProjectRole.values())
				.map(role -> new SelectOption(role.toString(),
						messageSource.getMessage("projectRole." + role, new Object[] {}, locale)))
				.collect(Collectors.toList());
	}

	public String updateMetadataProjectField(Long projectId, String fieldKey, ProjectRole newRole) {
		Project project = projectService.read(projectId);
		MetadataTemplateField field = templateService.readMetadataFieldByKey(fieldKey);
		MetadataRestriction restriction = new MetadataRestriction(project, field, newRole);
		templateService.addMetadataRestriction(restriction);
		return "__ SUCCESS __";
	}
}
