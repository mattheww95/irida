package ca.corefacility.bioinformatics.irida.ria.web.sessionAttrs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;

/**
 * Session object to hold samples that are currently in the cart.
 */
public class Cart extends HashMap<Project, HashSet<Sample>> {

	/**
	 * Get the total number of samples in the cart
	 *
	 * @return Total samples from all projects in the cart
	 */
	public int getNumberOfSamplesInCart() {
		return this.values()
				.stream()
				.reduce(0, (total, samples) -> total + samples.size(), Integer::sum);
	}

	/**
	 * Remove a specific sample from the cart.
	 *
	 * @param project to remove the sample from
	 * @param sample the sample to remove.
	 * @return Total samples from all project in the cart
	 */
	public int removeSample(Project project, Sample sample) {
		this.get(project)
				.remove(sample);
		return this.getNumberOfSamplesInCart();
	}

	/**
	 * Remove all samples from a specific project
	 *
	 * @param project to remove from cart
	 * @return Total samples from all projects in the cart
	 */
	public int removeProject(Project project) {
		this.remove(project);
		return getNumberOfSamplesInCart();
	}

	/**
	 * Get all the identifiers for projects in the cart
	 *
	 * @return Set of identifiers for projects in the cart
	 */
	public Set<Long> getProjectIdsInCart() {
		return this.keySet().stream().map(Project::getId).collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * Get all the identifiers for samples belonging to a specific project in the cart.
	 *
	 * @param project to get the samples for
	 * @return Set of identifiers for samples belonging to a project in the cart
	 */
	public Set<Sample> getSamplesForProjectInCart(Project project) {
		return this.get(project);
	}

	/**
	 * Find if a sample is currently in the cart.  If it is get the project that it belongs to.
	 *
	 * @param sample - the sample to check the cart for.
	 * @return identifier for the project the sample is in if the sample is in the cart
	 */
	public Long isSampleInCart(Sample sample) {
		for (Project project : this.keySet()) {
			if (this.get(project)
					.contains(sample)) {
				return project.getId();
			}
		}
		return null;
	}
}
