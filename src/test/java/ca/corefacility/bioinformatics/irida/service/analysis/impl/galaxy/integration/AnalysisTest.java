package ca.corefacility.bioinformatics.irida.service.analysis.impl.galaxy.integration;

import java.util.Set;

import ca.corefacility.bioinformatics.irida.model.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.Analysis;

public class AnalysisTest extends Analysis {

	public AnalysisTest(Set<SequenceFile> inputFiles) {
		super(inputFiles);
	}
}
