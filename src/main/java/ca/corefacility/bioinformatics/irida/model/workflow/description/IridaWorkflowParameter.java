package ca.corefacility.bioinformatics.irida.model.workflow.description;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.*;

/**
 * Defines input parameters for a workflow which can be adjusted.
 * 
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class IridaWorkflowParameter {
	
	/**
	 * Can be passed as the value of a parameter instructing IRIDA to ignore the default value. 
	 */
	public static final String IGNORE_DEFAULT_VALUE = "";

	@XmlAttribute(name = "name")
	private String name;

	@XmlAttribute(name = "defaultValue")
	private String defaultValue;

	@XmlAttribute(name = "required")
	private Boolean required;

	@XmlElementWrapper(name = "dynamicSource")
	@XmlElements({
			@XmlElement(name = "galaxyToolDataTable", type = IridaWorkflowDynamicSourceGalaxy.class),
	})
	// In order to satisfy the @XMLElementWrapper annotation we need to store dynamicSources in a
	// collection type, but there should only be at most one <dynamicSource> element per <parameter> element
	// in the workflow definition .xml file
	private List<IridaWorkflowDynamicSourceGalaxy> dynamicSources;


	@XmlElement(name = "toolParameter")
	private List<IridaToolParameter> toolParameters;

	public IridaWorkflowParameter() {
	}

	/**
	 * Creates a new {@link IridaWorkflowParameter} which maps to the given tool
	 * parameters.
	 * 
	 * @param name
	 *            The name of the parameter.
	 *
	 * @param defaultValue
	 *            The default value of this parameter.
	 * @param toolParameters
	 *            The tool parameters corresponding to this named parameter.
	 */
	public IridaWorkflowParameter(String name, String defaultValue, List<IridaToolParameter> toolParameters) {
		checkNotNull(name, "name is null");
		checkNotNull(defaultValue, "defaultValue is null");
		checkNotNull(toolParameters, "toolParameters is null");
		checkArgument(toolParameters.size() > 0, "toolParameters has no elements");
		
		this.name = name;
		this.defaultValue = defaultValue;
		this.toolParameters = toolParameters;
		this.required = false;
	}

	/**
	 * Creates a new {@link IridaWorkflowParameter} which maps to the given tool
	 * parameters.
	 *
	 * @param name
	 *            The name of the parameter.
	 *
	 * @param required
	 *            The default value of this parameter.
	 * @param toolParameters
	 *            The tool parameters corresponding to this named parameter.
	 */
	public IridaWorkflowParameter(String name, Boolean required, List<IridaWorkflowDynamicSourceGalaxy> dynamicSources, List<IridaToolParameter> toolParameters ) {
		checkNotNull(name, "name is null");
		// checkNotNull(defaultValue, "defaultValue is null");
		checkNotNull(toolParameters, "toolParameters is null");
		checkArgument(toolParameters.size() > 0, "toolParameters has no elements");

		this.name = name;
		this.required = required;
		this.toolParameters = toolParameters;
		this.dynamicSources = dynamicSources;

	}

	/**
	 * Gets the name of this parameter.
	 * 
	 * @return The name of this parameter.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets a list of tools whose values are affected by this parameter.
	 * 
	 * @return A {@link List} of tools whose values are affected by this
	 *         parameter.
	 */
	public List<IridaToolParameter> getToolParameters() {
		return toolParameters;
	}

	/**
	 * Gets the default value for this parameter.
	 * 
	 * @return The default value for this parameter.
	 */
	public String getDefaultValue() throws NullPointerException {
		if (defaultValue == null) {
		 	throw new NullPointerException("defaultValue is null");
		} else {
			return defaultValue;
		}
	}

	/**
	 * Whether or not this parameter is required to be set manually
	 * before launching a pipeline.
	 *
	 * @return Boolean representing whether or not this parameter is required to be set manually
	 *         before launching a pipeline.
	 */
	public Boolean isRequired() {
		return required;
	}

	/**
	 * Gets the dynamic source for the parameter.
	 *
	 * @return The dynamic source for this parameter.
	 * @throws NullPointerException
	 */
	public IridaWorkflowDynamicSourceGalaxy getDynamicSource() throws NullPointerException {
		if (this.dynamicSources.get(0) == null) {
			throw new NullPointerException("dynamicSource is null");
		} else {
			return dynamicSources.get(0);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, defaultValue, toolParameters, required, dynamicSources);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		else if (obj instanceof IridaWorkflowParameter) {
			IridaWorkflowParameter other = (IridaWorkflowParameter) obj;

			return Objects.equals(name, other.name) && Objects.equals(defaultValue, other.defaultValue)
					&& Objects.equals(toolParameters, other.toolParameters)
					&& Objects.equals(required, other.required)
			        && Objects.equals(dynamicSources, other.dynamicSources);
		}

		return false;
	}

	@Override
	public String toString() {
		if (defaultValue != null) {
			return "IridaWorkflowParameter [name=" + name + ", defaultValue=" + defaultValue + "]";
		} else {
			return "IridaWorkflowParameter [name=" + name + ", dynamicSource=" + dynamicSources.get(0).toString() + "]";
		}
	}
}
