package ca.corefacility.bioinformatics.irida.ria.web.services;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import ca.corefacility.bioinformatics.irida.model.enums.ProjectMetadataRole;
import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplateField;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.MetadataRestriction;
import ca.corefacility.bioinformatics.irida.repositories.sample.MetadataEntryRepository;
import ca.corefacility.bioinformatics.irida.repositories.sample.MetadataRestrictionRepository;
import ca.corefacility.bioinformatics.irida.ria.web.samples.dto.*;
import ca.corefacility.bioinformatics.irida.service.sample.MetadataTemplateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ca.corefacility.bioinformatics.irida.model.assembly.GenomeAssembly;
import ca.corefacility.bioinformatics.irida.model.joins.impl.SampleGenomeAssemblyJoin;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.QCEntry;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sample.SampleSequencingObjectJoin;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.MetadataEntry;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.Fast5Object;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFilePair;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequencingObject;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SingleEndSequenceFile;
import ca.corefacility.bioinformatics.irida.ria.web.samples.dto.SampleGenomeAssemblyFileModel;
import ca.corefacility.bioinformatics.irida.ria.web.samples.dto.SampleSequencingObjectFileModel;
import ca.corefacility.bioinformatics.irida.security.permissions.sample.UpdateSamplePermission;
import ca.corefacility.bioinformatics.irida.service.GenomeAssemblyService;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.SequencingObjectService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;

import com.google.common.base.Strings;

/**
 * UI Service for samples
 */
@Component
public class UISampleService {
	private final SampleService sampleService;
	private final ProjectService projectService;
	private final UpdateSamplePermission updateSamplePermission;
	private final SequencingObjectService sequencingObjectService;
	private final GenomeAssemblyService genomeAssemblyService;
	private final MessageSource messageSource;
	private final UICartService cartService;
	private final MetadataTemplateService metadataTemplateService;
	private final MetadataEntryRepository metadataEntryRepository;
	private final MetadataRestrictionRepository metadataRestrictionRepository;

	@Autowired
	public UISampleService(SampleService sampleService, ProjectService projectService,
			UpdateSamplePermission updateSamplePermission, SequencingObjectService sequencingObjectService,
			GenomeAssemblyService genomeAssemblyService, MessageSource messageSource, UICartService cartService,
			MetadataTemplateService metadataTemplateService, MetadataEntryRepository metadataEntryRepository,
			MetadataRestrictionRepository metadataRestrictionRepository) {

		this.sampleService = sampleService;
		this.projectService = projectService;
		this.updateSamplePermission = updateSamplePermission;
		this.sequencingObjectService = sequencingObjectService;
		this.genomeAssemblyService = genomeAssemblyService;
		this.messageSource = messageSource;
		this.cartService = cartService;
		this.metadataTemplateService = metadataTemplateService;
		this.metadataEntryRepository = metadataEntryRepository;
		this.metadataRestrictionRepository = metadataRestrictionRepository;
	}

	/**
	 * Get full details for a {@link Sample}
	 *
	 * @param id Identifier for a {@link Sample}
	 * @return {@link SampleDetails}
	 */
	public SampleDetails getSampleDetails(Long id) {
		Sample sample = sampleService.read(id);
		Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		boolean isModifiable = updateSamplePermission.isAllowed(authentication, sample);

		return new SampleDetails(sample, isModifiable, cartService.isSampleInCart(id));
	}

	/**
	 * Get all the metadata for a {@link Sample}
	 *
	 * @param id        Identifier for a {@link Sample}
	 * @param projectId Identifier for a {@link Project}
	 * @return {@link SampleMetadata}
	 */
	public SampleMetadata getSampleMetadata(Long id, Long projectId) {
		Sample sample = sampleService.read(id);
		Set<MetadataEntry> metadataForSample = sampleService.getMetadataForSample(sample);

		List<SampleMetadataFieldEntry> metadata = metadataForSample.stream()
				.map(s -> new SampleMetadataFieldEntry(s.getField()
						.getId(), s.getField()
						.getLabel(), s.getValue(), s.getId(), getMetadataFieldRestriction(projectId, s.getField()
						.getId())))
				.sorted(Comparator.comparing(SampleMetadataFieldEntry::getMetadataTemplateField))
				.collect(Collectors.toList());

		return new SampleMetadata(metadata);
	}

	/**
	 * Update a field within the sample details.
	 *
	 * @param id      {@link Long} identifier for the sample
	 * @param request {@link UpdateSampleAttributeRequest} details about which field to update
	 * @param locale  {@link Locale} for the currently logged in user
	 * @return message indicating update status
	 */
	public String updateSampleDetails(Long id, UpdateSampleAttributeRequest request, Locale locale) {
		try {
			String dateValue = null;

			Sample sample = sampleService.read(id);
			switch (request.getField()) {
			case "sampleName":
				sample.setSampleName(request.getValue());
				break;
			case "description":
				sample.setDescription(request.getValue());
				break;
			case "organism":
				sample.setOrganism(request.getValue());
				break;
			case "isolate":
				sample.setIsolate(request.getValue());
				break;
			case "strain":
				sample.setStrain(request.getValue());
				break;
			case "collectedBy":
				sample.setCollectedBy(request.getValue());
				break;
			case "collectionDate":
				Instant instant = Instant.parse(request.getValue());
				Date collectionDate = Date.from(instant);
				dateValue = new SimpleDateFormat("yyyy-MM-dd").format(collectionDate);
				sample.setCollectionDate(collectionDate);
				break;
			case "isolationSource":
				sample.setIsolationSource(request.getValue());
				break;
			case "geographicLocationName":
				sample.setGeographicLocationName(request.getValue());
				break;
			case "latitude":
				sample.setLatitude(request.getValue());
				break;
			case "longitude":
				sample.setLongitude(request.getValue());
				break;
			default:
				return messageSource.getMessage("server.sample.details.update.error",
						new Object[] { request.getField() }, locale);
			}
			sampleService.update(sample);
			String message;
			if (Strings.isNullOrEmpty(request.getValue())) {
				message = messageSource.getMessage("server.sample.details.removed.success",
						new Object[] { request.getField() }, locale);
			} else {
				String value = dateValue != null ? dateValue : request.getValue();
				message = messageSource.getMessage("server.sample.details.updated.success",
						new Object[] { request.getField(), value }, locale);
			}
			return message;
		} catch (ConstraintViolationException e) {
			throw new ConstraintViolationException(e.getConstraintViolations());
		}
	}

	/**
	 * Add metadata for the sample
	 *
	 * @param sampleId                 {@link Long} identifier for the sample
	 * @param addSampleMetadataRequest DTO containing sample metadata to add params
	 * @param locale                   {@link Locale} for the currently logged in user
	 * @return {@link AddSampleMetadataResponse} with added metadata field, entry, restriction, and response message
	 */
	public AddSampleMetadataResponse addSampleMetadata(Long sampleId, AddSampleMetadataRequest addSampleMetadataRequest,
			Locale locale) {
		Sample sample = sampleService.read(sampleId);
		ProjectMetadataRole metadataRole = ProjectMetadataRole.fromString(
				addSampleMetadataRequest.getMetadataRestriction());
		Project project = projectService.read(addSampleMetadataRequest.getProjectId());
		MetadataTemplateField templateField = new MetadataTemplateField(addSampleMetadataRequest.getMetadataField(),
				"text");

		MetadataTemplateField existingTemplateField = metadataTemplateService.readMetadataFieldByLabel(
				addSampleMetadataRequest.getMetadataField());

		if (existingTemplateField == null) {
			metadataTemplateService.saveMetadataField(templateField);
		}

		MetadataRestriction metadataRestriction = null;

		String message = "";
		MetadataEntry entry;
		Long entryId = null;
		String entryValue = "";
		Set<MetadataEntry> metadataEntrySet = new HashSet<>();
		MetadataTemplateField metadataTemplateField = null;

		if (!Strings.isNullOrEmpty(addSampleMetadataRequest.getMetadataEntry())) {
			entry = new MetadataEntry(addSampleMetadataRequest.getMetadataEntry(), "text", templateField);
			metadataEntrySet.add(entry);
			sampleService.mergeSampleMetadata(sample, metadataEntrySet);

			Optional<MetadataEntry> savedEntry = sampleService.getMetadataForSample(sample)
					.stream()
					.filter(s -> s.getField()
							.equals(templateField))
					.findFirst();

			if (savedEntry.isPresent()) {
				entryId = savedEntry.get()
						.getId();

				entryValue = savedEntry.get()
						.getValue();

				metadataTemplateField = savedEntry.get()
						.getField();

				metadataRestriction = metadataTemplateService.setMetadataRestriction(project, metadataTemplateField,
						metadataRole);
				metadataRestrictionRepository.save(metadataRestriction);
			}
			String metadataRestrictionString = messageSource.getMessage(
					"metadataRole." + metadataRestriction.getLevel(), new Object[] {}, locale);

			message = messageSource.getMessage("server.sample.metadata.add.success",
					new Object[] { addSampleMetadataRequest.getMetadataField(),
							addSampleMetadataRequest.getMetadataEntry(), metadataRestrictionString }, locale);
		}

		return new AddSampleMetadataResponse(metadataTemplateField.getId(), metadataTemplateField.getLabel(),
				entryValue, entryId, metadataRestriction.getLevel()
				.name(), message);

	}

	/**
	 * Remove metadata from the sample
	 *
	 * @param projectId       The project id
	 * @param metadataField   The metadata field
	 * @param metadataEntryId The metadata entry id
	 * @param locale          {@link Locale} for the currently logged in user
	 * @return message indicating deletion status
	 */
	public String removeSampleMetadata(Long projectId, String metadataField, Long metadataEntryId, Locale locale) {
		Project project = projectService.read(projectId);
		List<Sample> sampleList = sampleService.getSamplesForProject(project)
				.stream()
				.map((s) -> s.getObject())
				.collect(Collectors.toList());
		MetadataTemplateField metadataTemplateField = metadataTemplateService.readMetadataFieldByLabel(metadataField);
		Long fieldUsageCount = metadataEntryRepository.getMetadataEntriesCountBySamplesAndField(metadataTemplateField,
				sampleList);
		metadataEntryRepository.deleteById(metadataEntryId);
		MetadataRestriction restrictionToDelete = metadataTemplateService.getMetadataRestrictionForFieldAndProject(
				project, metadataTemplateField);
		/*
		 Only delete the restriction on the field if there is only one place
		 where the field is in use within the project
		 */
		if (fieldUsageCount == 1) {
			metadataRestrictionRepository.delete(restrictionToDelete);
		}

		return messageSource.getMessage("server.sample.metadata.remove.success", new Object[] { metadataField },
				locale);
	}

	/**
	 * Update metadata for the sample
	 *
	 * @param sampleId                    The sample identifier
	 * @param updateSampleMetadataRequest DTO containing sample metadata update params
	 * @param locale                      {@link Locale} for the currently logged in user
	 * @return message indicating update status
	 */
	public String updateSampleMetadata(Long sampleId, UpdateSampleMetadataRequest updateSampleMetadataRequest,
			Locale locale) {
		Sample sample = sampleService.read(sampleId);
		Project project = projectService.read(updateSampleMetadataRequest.getProjectId());
		boolean sampleUpdated = false;
		MetadataTemplateField metadataTemplateField = metadataTemplateService.readMetadataFieldByLabel(
				updateSampleMetadataRequest.getMetadataField());
		MetadataTemplateField existingFieldWithLabel = metadataTemplateService.readMetadataField(
				updateSampleMetadataRequest.getMetadataFieldId());
		Set<MetadataEntry> metadataEntrySet = new HashSet<>();

		ProjectMetadataRole projectMetadataRole = ProjectMetadataRole.fromString(
				updateSampleMetadataRequest.getMetadataRestriction());

		// Update the metadata field and project metadata role
		if (metadataTemplateField == null) {
			if (existingFieldWithLabel == null) {
				metadataTemplateField = new MetadataTemplateField(updateSampleMetadataRequest.getMetadataField(),
						"text");
			} else {
				metadataTemplateField = existingFieldWithLabel;
				metadataTemplateField.setLabel(updateSampleMetadataRequest.getMetadataField());
			}
			metadataTemplateService.updateMetadataField(metadataTemplateField);
		} else {
			ProjectMetadataRole roleFromUpdateRequest = projectMetadataRole;
			projectMetadataRole = getMetadataFieldRestriction(project.getId(), metadataTemplateField.getId());

			/*
			 We want to only set the role from the update request if it
			 is different than the current metadata role for the field
			 */
			if (projectMetadataRole != null && !projectMetadataRole.equals(roleFromUpdateRequest)) {
				projectMetadataRole = roleFromUpdateRequest;
			}
		}

		// Update the metadata entry
		MetadataEntry prevEntry = metadataEntryRepository.getMetadataEntryBySampleAndField(metadataTemplateField,
				sample);
		boolean fieldOrValUpdated = false;
		if (prevEntry != null) {
			if (!prevEntry.getField()
					.equals(metadataTemplateField)) {
				prevEntry.setField(metadataTemplateField);
				fieldOrValUpdated = true;
			}
			if (!prevEntry.getValue()
					.equals(updateSampleMetadataRequest.getMetadataEntry())) {
				prevEntry.setValue(updateSampleMetadataRequest.getMetadataEntry());
				fieldOrValUpdated = true;
			}

			if (fieldOrValUpdated) {
				metadataEntrySet.add(prevEntry);
			}
		} else {
			MetadataEntry entry = new MetadataEntry(updateSampleMetadataRequest.getMetadataEntry(), "text",
					metadataTemplateField);
			metadataEntrySet.add(entry);
			fieldOrValUpdated = true;
		}

		//Only merge if changes were made to field or value
		if (fieldOrValUpdated) {
			sampleService.mergeSampleMetadata(sample, metadataEntrySet);
		}

		/*
		 Get the metadata restriction for the field and update if there is no previous
		 restriction on the field or a user modifies the restriction for the field
		 */
		MetadataRestriction currentRestriction = metadataTemplateService.getMetadataRestrictionForFieldAndProject(
				project, metadataTemplateField);

		if (currentRestriction == null) {
			metadataTemplateService.setMetadataRestriction(project, metadataTemplateField, projectMetadataRole);
			sampleUpdated = true;
		} else {
			if (!currentRestriction.getLevel()
					.equals(projectMetadataRole)) {
				currentRestriction.setLevel(projectMetadataRole);
				sampleUpdated = true;
				metadataRestrictionRepository.save(currentRestriction);
			}
		}

		// Delete existing field/entry/restriction if user updates field to an existing field label
		MetadataTemplateField fieldToDelete = metadataTemplateService.readMetadataField(
				updateSampleMetadataRequest.getMetadataFieldId());
		if (fieldToDelete != null && !fieldToDelete.getLabel()
				.equals(metadataTemplateField.getLabel())) {
			MetadataEntry metadataEntryToDelete = metadataEntryRepository.getMetadataEntryById(
					updateSampleMetadataRequest.getMetadataEntryId());
			MetadataRestriction restrictionToDelete = metadataTemplateService.getMetadataRestrictionForFieldAndProject(
					project, fieldToDelete);
			metadataRestrictionRepository.delete(restrictionToDelete);
			metadataEntryRepository.delete(metadataEntryToDelete);
			metadataTemplateService.deleteMetadataField(fieldToDelete);
		}

		// If sample metadata was updated then update the sample modified date
		if (sampleUpdated) {
			sample.setModifiedDate(new Date());
		}

		return messageSource.getMessage("server.sample.metadata.update.success",
				new Object[] { updateSampleMetadataRequest.getMetadataField() }, locale);
	}

	/**
	 * Get the sequence files associated with a sample
	 *
	 * @param sampleId  Identifier for a sample
	 * @param projectId Identifier for the project the sample belong to
	 * @return All the sequencing files associated with the sample
	 */
	public SampleFiles getSampleFiles(Long sampleId, Long projectId) {
		Sample sample = sampleService.read(sampleId);
		// get the project if available
		Project project = null;
		if (projectId != null) {
			project = projectService.read(projectId);
		}

		List<SampleSequencingObjectFileModel> filePairs = getPairedSequenceFilesForSample(sample, project);
		List<SampleSequencingObjectFileModel> singles = getSingleEndSequenceFilesForSample(sample, project);
		List<SampleSequencingObjectFileModel> fast5 = getFast5FilesForSample(sample);
		List<SampleGenomeAssemblyFileModel> genomeAssemblies = getGenomeAssembliesForSample(sample);

		return new SampleFiles(singles, filePairs, fast5, genomeAssemblies);
	}

	/**
	 * Remove a sequencing object linked to a {@link Sample}
	 *
	 * @param sampleId           Identifier for a sample
	 * @param sequencingObjectId Identifier for the sequencingObject
	 * @param locale             {@link Locale} for the currently logged in user
	 * @return {@link String} explaining to the user the results of the delete.
	 */
	public String deleteSequencingObjectFromSample(Long sampleId, Long sequencingObjectId, Locale locale) {
		Sample sample = sampleService.read(sampleId);
		SequencingObject sequencingObject = sequencingObjectService.read(sequencingObjectId);

		try {
			sampleService.removeSequencingObjectFromSample(sample, sequencingObject);
			return messageSource.getMessage("server.SampleFiles.removeSequencingObjectSuccess", new Object[] {},
					locale);
		} catch (Exception e) {
			return messageSource.getMessage("samples.files.remove.error", new Object[] { sequencingObject.getLabel() },
					locale);
		}
	}

	/**
	 * Remove a genome assembly linked to a {@link Sample}
	 *
	 * @param sampleId         Identifier for a sample
	 * @param genomeAssemblyId Identifier for the GenomeAssembly
	 * @param locale           {@link Locale} for the currently logged in user
	 * @return {@link String} explaining to the user the results of the delete.
	 */
	public String deleteGenomeAssemblyFromSample(Long sampleId, Long genomeAssemblyId, Locale locale) {
		Sample sample = sampleService.read(sampleId);
		GenomeAssembly genomeAssembly = genomeAssemblyService.getGenomeAssemblyForSample(sample, genomeAssemblyId);

		try {
			genomeAssemblyService.removeGenomeAssemblyFromSample(sample, genomeAssemblyId);
			return messageSource.getMessage("server.SampleFiles.removeGenomeAssemblySuccess", new Object[] {},
					locale);
		} catch (Exception e) {
			return messageSource.getMessage("samples.files.remove.error", new Object[] { genomeAssembly.getLabel() }, locale);
		}
	}

	/**
	 * Download a GenomeAssembly file
	 *
	 * @param sampleId         Identifier for a sample
	 * @param genomeAssemblyId Identifier for the genome assembly
	 * @param response         {@link HttpServletResponse}
	 */
	public void downloadAssembly(Long sampleId, Long genomeAssemblyId, HttpServletResponse response)
			throws IOException {
		Sample sample = sampleService.read(sampleId);
		GenomeAssembly genomeAssembly = genomeAssemblyService.getGenomeAssemblyForSample(sample, genomeAssemblyId);

		Path path = genomeAssembly.getFile();
		response.setHeader("Content-Disposition",
				"attachment; filename=\"" + genomeAssembly.getLabel() + "\"");
		Files.copy(path, response.getOutputStream());
		response.flushBuffer();
	}

	/**
	 * Get a list of paired end sequence files for a sample
	 *
	 * @param sample  the {@link Sample} to get the files for.
	 * @param project the {@link Project} the sample belongs to
	 * @return list of paired end sequence files
	 */
	public List<SampleSequencingObjectFileModel> getPairedSequenceFilesForSample(Sample sample, Project project) {
		Collection<SampleSequencingObjectJoin> filePairJoins = sequencingObjectService.getSequencesForSampleOfType(
				sample, SequenceFilePair.class);
		// add project to qc entries and filter any unavailable entries
		List<SampleSequencingObjectFileModel> filePairs = new ArrayList<>();
		for (SampleSequencingObjectJoin join : filePairJoins) {
			SequencingObject obj = join.getObject();
			enhanceQcEntries(obj, project);
			SequenceFilePair sfp = (SequenceFilePair) obj;
			String firstFileSize = sfp.getForwardSequenceFile()
					.getFileSize();
			String secondFileSize = sfp.getReverseSequenceFile()
					.getFileSize();

			filePairs.add(new SampleSequencingObjectFileModel(obj, firstFileSize, secondFileSize));
		}

		return filePairs;
	}

	/**
	 * Get a list of single end sequence files for a sample
	 *
	 * @param sample  the {@link Sample} to get the files for.
	 * @param project the {@link Project} the sample belongs to
	 * @return list of single end sequence files
	 */
	public List<SampleSequencingObjectFileModel> getSingleEndSequenceFilesForSample(Sample sample, Project project) {
		Collection<SampleSequencingObjectJoin> singleFileJoins = sequencingObjectService.getSequencesForSampleOfType(
				sample, SingleEndSequenceFile.class);

		List<SampleSequencingObjectFileModel> singles = new ArrayList<>();
		for (SampleSequencingObjectJoin join : singleFileJoins) {
			SequencingObject obj = join.getObject();
			enhanceQcEntries(obj, project);
			SingleEndSequenceFile sf = (SingleEndSequenceFile) obj;
			String fileSize = sf.getSequenceFile()
					.getFileSize();
			singles.add(new SampleSequencingObjectFileModel(obj, fileSize, null));
		}

		return singles;
	}

	/**
	 * Get a list of fast5 sequence files for a sample
	 *
	 * @param sample the {@link Sample} to get the files for.
	 * @return list of fast5 sequence files
	 */
	public List<SampleSequencingObjectFileModel> getFast5FilesForSample(Sample sample) {
		Collection<SampleSequencingObjectJoin> fast5FileJoins = sequencingObjectService.getSequencesForSampleOfType(
				sample, Fast5Object.class);

		List<SampleSequencingObjectFileModel> fast5Files = new ArrayList<>();
		for (SampleSequencingObjectJoin join : fast5FileJoins) {
			SequencingObject obj = join.getObject();
			Fast5Object f5 = (Fast5Object) obj;
			String fileSize = f5.getFile()
					.getFileSize();
			fast5Files.add(new SampleSequencingObjectFileModel(obj, fileSize, null));
		}
		return fast5Files;
	}

	/**
	 * Get any genome assemblies that are available for a sample
	 *
	 * @param sample the {@link Sample} to get the assemblies for
	 * @return a list of genome assembly files
	 */
	public List<SampleGenomeAssemblyFileModel> getGenomeAssembliesForSample(Sample sample) {
		Collection<SampleGenomeAssemblyJoin> genomeAssemblyJoins = genomeAssemblyService.getAssembliesForSample(sample);
		List<SampleGenomeAssemblyFileModel> assemblyFiles = new ArrayList<>();
		for (SampleGenomeAssemblyJoin join : genomeAssemblyJoins) {
			GenomeAssembly obj = join.getObject();
			String fileSize = obj.getFileSize();
			assemblyFiles.add(new SampleGenomeAssemblyFileModel(obj, fileSize));
		}

		return assemblyFiles;
	}

	/**
	 * Adds the {@link Project} to any {@link QCEntry} within a
	 * {@link SequencingObject}. If the {@link QCEntry} reports as
	 * {@link QCEntry.QCEntryStatus#UNAVAILABLE} after being enhanced it is removed from
	 * the list
	 *
	 * @param obj     the {@link SequencingObject} to enhance
	 * @param project the {@link Project} to add
	 */
	private void enhanceQcEntries(SequencingObject obj, Project project) {
		Set<QCEntry> availableEntries = new HashSet<>();
		if (obj.getQcEntries() != null) {
			for (QCEntry q : obj.getQcEntries()) {
				q.addProjectSettings(project);
				if (!q.getStatus()
						.equals(QCEntry.QCEntryStatus.UNAVAILABLE)) {
					availableEntries.add(q);
				}
			}
		}

		obj.setQcEntries(availableEntries);
	}

	/**
	 * Get a list of all {@link Sample} identifiers within a specific project
	 *
	 * @param projectId Identifier for a {@link Project}
	 * @return {@link List} of {@link Sample} identifiers
	 */
	public List<Long> getSampleIdsForProject(Long projectId) {
		Project project = projectService.read(projectId);
		List<Sample> samples = sampleService.getSamplesForProjectShallow(project);
		return samples.stream()
				.map(Sample::getId)
				.collect(Collectors.toUnmodifiableList());
	}

	/**
	 * Share / Move samples with another project
	 *
	 * @param request Request containing the details of the move
	 * @param locale  current users {@link Locale}
	 * @throws Exception if project or samples cannot be found
	 */
	public void shareSamplesWithProject(ShareSamplesRequest request, Locale locale) throws Exception {
		Project currentProject = projectService.read(request.getCurrentId());
		Project targetProject = projectService.read(request.getTargetId());

		List<Sample> samples = (List<Sample>) sampleService.readMultiple(request.getSampleIds());
		if (request.getRemove()) {
			try {
				projectService.moveSamples(currentProject, targetProject, samples);
			} catch (Exception e) {
				throw new Exception(messageSource.getMessage("server.ShareSamples.move-error",
						new Object[] { targetProject.getLabel() }, locale));
			}
		} else {
			try {
				projectService.shareSamples(currentProject, targetProject, samples, !request.getLocked());
			} catch (Exception e) {
				throw new Exception(messageSource.getMessage("server.ShareSamples.copy-error",
						new Object[] { targetProject.getLabel() }, locale));
			}
		}
	}

	/**
	 * Get {@link MetadataRestriction} for metadata field
	 *
	 * @param projectId               Identifier for {@link Project}
	 * @param metadataTemplateFieldId Identifier for {@link MetadataTemplateField}
	 * @return {@link MetadataRestriction}
	 */
	private ProjectMetadataRole getMetadataFieldRestriction(Long projectId, Long metadataTemplateFieldId) {
		Project project = projectService.read(projectId);
		MetadataTemplateField metadataTemplateField = metadataTemplateService.readMetadataField(
				metadataTemplateFieldId);
		MetadataRestriction metadataRestriction = metadataRestrictionRepository.getRestrictionForFieldAndProject(
				project, metadataTemplateField);
		if (metadataRestriction != null) {
			return metadataRestriction.getLevel();
		}

		return null;
	}
}
