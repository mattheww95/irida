package ca.corefacility.bioinformatics.irida.ria.web.samples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import ca.corefacility.bioinformatics.irida.exceptions.EntityExistsException;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplateField;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.MetadataEntry;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.MetadataRestriction;
import ca.corefacility.bioinformatics.irida.ria.web.samples.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import ca.corefacility.bioinformatics.irida.model.assembly.UploadedAssembly;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.Fast5Object;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFilePair;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SingleEndSequenceFile;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.ajax.AjaxErrorResponse;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.ajax.AjaxResponse;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.ajax.AjaxSuccessResponse;
import ca.corefacility.bioinformatics.irida.ria.web.services.UISampleService;
import ca.corefacility.bioinformatics.irida.service.GenomeAssemblyService;
import ca.corefacility.bioinformatics.irida.service.SequencingObjectService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;


/**
 * Controller for asynchronous requests for a {@link Sample}
 */
@RestController
@RequestMapping("/ajax/samples")
public class SamplesAjaxController {
	private final SampleService sampleService;
	private final SequencingObjectService sequencingObjectService;
	private final GenomeAssemblyService genomeAssemblyService;
	private final UISampleService uiSampleService;
	private final MessageSource messageSource;

	@Autowired
	public SamplesAjaxController(SampleService sampleService, SequencingObjectService sequencingObjectService,
			GenomeAssemblyService genomeAssemblyService, UISampleService uiSampleService, MessageSource messageSource) {
		this.sampleService = sampleService;
		this.sequencingObjectService = sequencingObjectService;
		this.genomeAssemblyService = genomeAssemblyService;
		this.uiSampleService = uiSampleService;
		this.messageSource = messageSource;
	}

	/**
	 * Upload {@link SequenceFile}'s to a sample
	 *
	 * @param sampleId The {@link Sample} id to upload to
	 * @param request  The current request which contains {@link MultipartFile}
	 * @param locale   The locale for the currently logged in user
	 * @return {@link ResponseEntity} containing the message for the user on the status of the action
	 */
	@RequestMapping(value = { "/{sampleId}/sequenceFiles/upload" }, method = RequestMethod.POST)
	public ResponseEntity<String> uploadSequenceFiles(@PathVariable Long sampleId, MultipartHttpServletRequest request,
			Locale locale) {
		Sample sample = sampleService.read(sampleId);

		Iterator<String> fileNames = request.getFileNames();
		List<MultipartFile> files = new ArrayList<>();
		while (fileNames.hasNext()) {
			files.add(request.getFile(fileNames.next()));
		}

		SamplePairer samplePairer = new SamplePairer(files);
		final Map<String, List<MultipartFile>> pairedFiles = samplePairer.getPairedFiles(files);
		final List<MultipartFile> singleFiles = samplePairer.getSingleFiles(files);

		try {
			for (String key : pairedFiles.keySet()) {
				List<MultipartFile> list = pairedFiles.get(key);
				createSequenceFilePairsInSample(list, sample);
			}

			for (MultipartFile file : singleFiles) {
				createSequenceFileInSample(file, sample);
			}

			return ResponseEntity.ok(messageSource.getMessage("server.SampleFileUploader.success",
					new Object[] { sample.getSampleName() }, locale));
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("");
		}
	}

	/**
	 * Upload fast5 files to the given sample
	 *
	 * @param sampleId the ID of the sample to upload to
	 * @param request  The current request which contains {@link MultipartFile}
	 * @param locale   The locale for the currently logged in user
	 * @return {@link ResponseEntity} containing the message for the user on the status of the action
	 */
	@RequestMapping(value = "/{sampleId}/fast5/upload", method = RequestMethod.POST)
	public ResponseEntity<String> uploadFast5Files(@PathVariable Long sampleId, MultipartHttpServletRequest request,
			Locale locale) {
		Sample sample = sampleService.read(sampleId);
		Iterator<String> fileNames = request.getFileNames();
		List<MultipartFile> files = new ArrayList<>();
		while (fileNames.hasNext()) {
			files.add(request.getFile(fileNames.next()));
		}

		try {
			for (MultipartFile file : files) {
				createFast5FileInSample(file, sample);
			}
			return ResponseEntity.ok(messageSource.getMessage("server.SampleFileUploader.success",
					new Object[]{sample.getSampleName()}, locale));
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("");
		}
	}

	/**
	 * Upload assemblies to the given sample
	 *
	 * @param sampleId the ID of the sample to upload to
	 * @param request  The current request which contains {@link MultipartFile}
	 * @param locale   The locale for the currently logged in user
	 * @return {@link ResponseEntity} containing the message for the user on the status of the action
	 */
	@RequestMapping(value = { "/{sampleId}/assemblies/upload" }, method = RequestMethod.POST)
	public ResponseEntity<String> uploadAssemblies(@PathVariable Long sampleId, MultipartHttpServletRequest request,
			Locale locale) {
		Sample sample = sampleService.read(sampleId);
		Iterator<String> fileNames = request.getFileNames();
		List<MultipartFile> files = new ArrayList<>();
		while (fileNames.hasNext()) {
			files.add(request.getFile(fileNames.next()));
		}

		try {
			for (MultipartFile file : files) {
				Path temp = Files.createTempDirectory(null);
				Path target = temp.resolve(file.getOriginalFilename());
				file.transferTo(target.toFile());
				UploadedAssembly uploadedAssembly = new UploadedAssembly(target);

				genomeAssemblyService.createAssemblyInSample(sample, uploadedAssembly);
			}
			return ResponseEntity.ok()
					.body(messageSource.getMessage("server.SampleFileUploader.success",
							new Object[] { sample.getSampleName() }, locale));
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(messageSource.getMessage("server.SampleFileUploader.error",
							new Object[] { sample.getSampleName() }, locale));
		}
	}

	/**
	 * Get {@link Sample} details for a specific sample.
	 *
	 * @param id {@link Long} identifier for a sample.
	 * @return {@link SampleDetails} for the {@link Sample}
	 */
	@GetMapping(value = "/{id}/details", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SampleDetails> getSampleDetails(@PathVariable Long id) {
		return ResponseEntity.ok(uiSampleService.getSampleDetails(id));
	}

	/**
	 * Get {@link Sample} metadata for a specific sample.
	 *
	 * @param id {@link Long} identifier for a sample.
	 * @return {@link SampleMetadata} for the {@link Sample}
	 */
	@GetMapping(value = "/{id}/metadata")
	public ResponseEntity<SampleMetadata> getSampleMetadata(@PathVariable Long id) {
		return ResponseEntity.ok(uiSampleService.getSampleMetadata(id));
	}

	/**
	 * Get {@link MetadataRestriction} for metadata field
	 *
	 * @param projectId               Identifier for {@link Project}
	 * @param metadataTemplateFieldId Identifier for {@link MetadataTemplateField}
	 * @return {@link MetadataRestriction}
	 */
	@GetMapping(value = "/metadata-field-restriction")
	public ResponseEntity<MetadataRestriction> getMetadataFieldRestriction(@RequestParam Long projectId,
			@RequestParam Long metadataTemplateFieldId) {
		return ResponseEntity.ok(uiSampleService.getMetadataFieldRestriction(projectId, metadataTemplateFieldId));
	}

	/**
	 * Update a field within the sample details.
	 *
	 * @param id      {@link Long} identifier for the sample
	 * @param request {@link UpdateSampleAttributeRequest} details about which field to update
	 * @param locale  {@link Locale} for the currently logged in user
	 * @return {@link ResponseEntity} explaining to the user the results of the update.
	 */
	@PutMapping(value = "/{id}/details")
	public ResponseEntity<AjaxResponse> updateSampleDetails(@PathVariable Long id,
			@RequestBody UpdateSampleAttributeRequest request, Locale locale) {
		try {
			return ResponseEntity.ok(new AjaxSuccessResponse(uiSampleService.updateSampleDetails(id, request, locale)));
		} catch (ConstraintViolationException e) {
			String constraintViolations = "";
			for (ConstraintViolation a : e.getConstraintViolations()) {
				constraintViolations += a.getMessage() + "\n";
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new AjaxErrorResponse(constraintViolations));
		}
	}

	/**
	 * Add a metadata field and entry to {@link Sample}
	 *
	 * @param id                      {@link Long} identifier for the sample
	 * @param projectId               {@link Long} project identifier
	 * @param metadataField           The metadata field label
	 * @param metadataEntry           The metadata field value
	 * @param metadataFieldPermission The metadata permission to set for the field
	 * @param locale                  {@link Locale} for the currently logged in user
	 * @return {@link ResponseEntity} explaining to the user the results of the update.
	 */
	@PutMapping(value = "/{id}/metadata")
	public ResponseEntity<AjaxResponse> addSampleMetadata(@PathVariable Long id, @RequestParam Long projectId,
			@RequestParam String metadataField, @RequestParam String metadataEntry,
			@RequestParam String metadataFieldPermission, Locale locale) {
		return ResponseEntity.ok(new AjaxSuccessResponse(
				uiSampleService.addSampleMetadata(id, projectId, metadataField, metadataEntry, metadataFieldPermission,
						locale)));
	}

	/**
	 * Remove metadata field and entry from {@link Sample}
	 *
	 * @param metadataField   The metadata field
	 * @param metadataEntryId The metadata entry identifier
	 * @param locale          {@link Locale} for the currently logged in user
	 * @return {@link ResponseEntity} explaining to the user the results of the deletion.
	 */
	@DeleteMapping(value = "/metadata")
	public ResponseEntity<AjaxResponse> removeSampleMetadata(@RequestParam String metadataField,
			@RequestParam Long metadataEntryId, Locale locale) {
		return ResponseEntity.ok(
				new AjaxSuccessResponse(uiSampleService.removeSampleMetadata(metadataField, metadataEntryId, locale)));
	}

	/**
	 * Update a metadata field entry for {@link Sample}
	 *
	 * @param id                  The sample identifier
	 * @param projectId           The project identifier
	 * @param metadataFieldId     The {@link MetadataTemplateField} identifier
	 * @param metadataField       The metadata field label
	 * @param metadataEntryId     The {@link MetadataEntry} identifier
	 * @param metadataEntry       The metadata field value
	 * @param metadataRestriction The restriction for the metadata field
	 * @param locale              {@link Locale} for the currently logged in user
	 * @return {@link ResponseEntity} explaining to the user the results of the update.
	 */
	@PutMapping(value = "/{id}/metadata/update")
	public ResponseEntity<AjaxResponse> updateSampleMetadata(@PathVariable Long id, @RequestParam Long projectId,
			@RequestParam Long metadataFieldId, @RequestParam String metadataField, @RequestParam Long metadataEntryId,
			@RequestParam String metadataEntry, @RequestParam String metadataRestriction, Locale locale) {
		try {
			String responseMessage = uiSampleService.updateSampleMetadata(id, projectId, metadataFieldId, metadataField,
					metadataEntryId, metadataEntry, metadataRestriction, locale);
			return ResponseEntity.ok(new AjaxSuccessResponse(responseMessage));
		} catch (EntityExistsException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new AjaxErrorResponse(e.getMessage()));
		}
	}

	/**
	 * Get sequencing files associated with a sample
	 * @param id Identifier for a sample
	 * @param projectId Identifier for a project
	 * @return All sequencing files associated with a sample.
	 */
	@GetMapping("/{id}/files")
	public ResponseEntity<AjaxResponse> getFilesForSample(@PathVariable Long id,
			@RequestParam(required = false) Long projectId) {
		return ResponseEntity.ok(uiSampleService.getSampleFiles(id, projectId));
	}

	/**
	 * Get a list of all {@link Sample} identifiers within a specific project
	 *
	 * @param projectId Identifier for a Project
	 * @return {@link List} of {@link Sample} identifiers
	 */
	@GetMapping("/identifiers")
	public List<Long> getSampleIdsForProject(@RequestParam Long projectId) {
		return uiSampleService.getSampleIdsForProject(projectId);
	}

	/**
	 * Share / Move samples between projects
	 *
	 * @param request {@link ShareSamplesRequest} details about the samples to share
	 * @param locale  current users {@link Locale}
	 * @return Outcome of the share/move
	 */
	@PostMapping("/share")
	public ResponseEntity<AjaxResponse> shareSamplesWithProject(@RequestBody ShareSamplesRequest request,
			Locale locale) {
		try {
			uiSampleService.shareSamplesWithProject(request, locale);
			return ResponseEntity.ok(new AjaxSuccessResponse(""));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new AjaxErrorResponse(e.getLocalizedMessage()));
		}
	}

	/**
	 * Create {@link SequenceFile}'s then add them as {@link SequenceFilePair}
	 * to a {@link Sample}
	 *
	 * @param pair   {@link List} of {@link MultipartFile}
	 * @param sample {@link Sample} to add the pair to.
	 * @throws IOException Exception thrown if there is an error handling the file.
	 */
	private void createSequenceFilePairsInSample(List<MultipartFile> pair, Sample sample) throws IOException {
		SequenceFile firstFile = createSequenceFile(pair.get(0));
		SequenceFile secondFile = createSequenceFile(pair.get(1));
		sequencingObjectService.createSequencingObjectInSample(new SequenceFilePair(firstFile, secondFile), sample);
	}

	/**
	 * Create a {@link SequenceFile} and add it to a {@link Sample}
	 *
	 * @param file   {@link MultipartFile}
	 * @param sample {@link Sample} to add the file to.
	 * @throws IOException Exception thrown if there is an error handling the file.
	 */
	private void createSequenceFileInSample(MultipartFile file, Sample sample) throws IOException {
		SequenceFile sequenceFile = createSequenceFile(file);
		sequencingObjectService.createSequencingObjectInSample(new SingleEndSequenceFile(sequenceFile), sample);
	}

	/**
	 * Create a {@link Fast5Object} and add it to a {@link Sample}
	 *
	 * @param file   {@link MultipartFile}
	 * @param sample {@link Sample} to add the file to.
	 * @throws IOException Exception thrown if there is an error handling the file.
	 */
	private void createFast5FileInSample(MultipartFile file, Sample sample) throws IOException {
		SequenceFile sequenceFile = createSequenceFile(file);
		sequencingObjectService.createSequencingObjectInSample(new Fast5Object(sequenceFile), sample);
	}

	/**
	 * Private method to move the sequence file into the correct directory and
	 * create the {@link SequenceFile} object.
	 *
	 * @param file {@link MultipartFile} sequence file uploaded.
	 * @return {@link SequenceFile}
	 * @throws IOException Exception thrown if there is an error handling the file.
	 */
	private SequenceFile createSequenceFile(MultipartFile file) throws IOException {
		Path temp = Files.createTempDirectory(null);
		Path target = temp.resolve(file.getOriginalFilename());
		file.transferTo(target.toFile());
		return new SequenceFile(target);
	}
}