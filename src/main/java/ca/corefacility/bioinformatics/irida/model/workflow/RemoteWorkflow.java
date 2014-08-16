package ca.corefacility.bioinformatics.irida.model.workflow;

/**
 * A reference to a workflow in a remote execution manager.
 * @author Aaron Petkau <aaron.petkau@phac-aspc.gc.ca>
 *
 */
public interface RemoteWorkflow {
	
	/**
	 * Gets the id of this remote workflow.
	 * @return The id of this remote workflow.
	 */
	public String getWorkflowId();
	
	/**
	 * Gets the checksum of this remote workflow.
	 * @return The checksum of this remote workflow.
	 */
	public String getWorkflowChecksum();
}
