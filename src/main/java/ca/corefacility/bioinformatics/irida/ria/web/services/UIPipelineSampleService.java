package ca.corefacility.bioinformatics.irida.ria.web.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.ria.web.samples.dto.SampleSequencingObjectFileModel;
import ca.corefacility.bioinformatics.irida.ria.web.launchPipeline.dtos.LaunchSample;

/**
 * UI Service for all things related to samples within the pipeline
 */
@Component
public class UIPipelineSampleService {
	private final UISampleService sampleService;
	private final UICartService cartService;

	@Autowired
	public UIPipelineSampleService(UISampleService sampleService, UICartService cartService) {
		this.sampleService = sampleService;
		this.cartService = cartService;
	}

	/**
	 * Get a list of the samples that are in the cart and get their associated sequence files that
	 * can be used on the current pipeline
	 *
	 * @param paired  Whether paired end files can be run on the current pipeline
	 * @param singles Whether single end files can be run on the current pipeline
	 * @return list of samples containing their associated sequencing data
	 */
	public List<LaunchSample> getPipelineSamples(boolean paired, boolean singles) {
		Map<Project, List<Sample>> cart = cartService.getFullCart();
		List<LaunchSample> samples = new ArrayList<>();
		cart.forEach((project, projectSamples) -> {
			for (Sample sample : projectSamples) {
				LaunchSample launchSample = new LaunchSample(sample, project, sample.getDefaultSequencingObject());
				List<SampleSequencingObjectFileModel> files = new ArrayList<>();
				if (paired) {
					files.addAll(sampleService.getPairedSequenceFilesForSample(sample, project, null));
				}
				if (singles) {
					files.addAll(sampleService.getSingleEndSequenceFilesForSample(sample, project, null));
				}
				launchSample.setFiles(files);
				samples.add(launchSample);
			}
		});
		return samples;
	}
}
