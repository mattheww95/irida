package ca.corefacility.bioinformatics.irida.ria.web.samples.dto;

public class NewProjectMetadataRestriction {
	private Long identifier;
	private String restriction;

	public NewProjectMetadataRestriction() {
	}

	public NewProjectMetadataRestriction(Long identifier, String restriction) {
		this.identifier = identifier;
		this.restriction = restriction;
	}

	public Long getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Long identifier) {
		this.identifier = identifier;
	}

	public String getRestriction() {
		return restriction;
	}

	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}
}
