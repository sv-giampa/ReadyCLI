/**
 *  Copyright 2020 Salvatore Giampà
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 **/

package readycli;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An option is an argument of a {@link Command} that can be optionally
 * specified on command.line. An option is useful to change the default values
 * of some parameters. These {@link Parameter parameters}, if present, succeed
 * the option name on the command-line. An option with no parameters is called a
 * flag. To create a new option, the {@link #create(String, String)} method must
 * be invoked.
 * 
 * @author Salvatore Giampa'
 *
 */
public final class Option implements Serializable {
	private static final long serialVersionUID = -1669795308156579822L;

	private String name;
	private ArrayList<String> aliases = new ArrayList<>();
	private String description;
	private ArrayList<Parameter> parameters = new ArrayList<>();

	// private constructor, use the static create() method to build a new option
	private Option() {}

	// private copy constructor
	private Option(Option toCopy) {
		this.name = toCopy.name;
		this.description = toCopy.description;
		this.parameters.addAll(toCopy.parameters);
		this.parameters.trimToSize();
		this.aliases.addAll(toCopy.aliases);
		this.aliases.trimToSize();
	}

	/**
	 * Gets the name of the option.
	 * 
	 * @return the name of the option as a string
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the description of the option.
	 * 
	 * @return the description of the option as a string
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the list of the option parameters, in the order they were specified
	 * during construction.
	 * 
	 * @return the list of parameters.
	 */
	public List<Parameter> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	/**
	 * Gets the list of the aliases of this option. the aliases are secondary or
	 * shortest names assigned to an option, to simplify its specification on
	 * command-line.
	 * 
	 * @return the list of aliases assigned to this option
	 */
	public List<String> getAliases() {
		return Collections.unmodifiableList(aliases);
	}

	/**
	 * Gets the default {@link OptionValues option values} of this option.
	 * 
	 * @return an {@link OptionValues} object
	 */
	OptionValues processDefaults() {
		Map<String, String> values = new HashMap<>();

		for (Parameter parameter : parameters)
			values.put(parameter.getName(), parameter.getDefaultValue());

		return new OptionValues(name, false, values);
	}

	/**
	 * Gets the {@link OptionValues option values} of this option built starting
	 * from the specified list of parameters.
	 * 
	 * @return an {@link OptionValues} object
	 */
	OptionValues process(List<String> parametersValues) {
		Map<String, String> values = new HashMap<>();
		for (int i = 0; i < parametersValues.size(); i++)
			values.put(parameters.get(i)
					.getName(), parametersValues.get(i));
		return new OptionValues(name, true, values);
	}

	/**
	 * Represents a parameter of an {@link Option}. An option parameter is composed
	 * by a name, a description and a default value, that is used in the case the
	 * option is not specified on the command-line.
	 * 
	 * @author Salvatore Giampa'
	 *
	 */
	public static final class Parameter {
		public String name;
		public String description;
		public String defaultValue;

		// private ocnstructor, can be instantiated internally only
		private Parameter(String name, String description, String defaultValue) {
			this.name = name;
			this.description = description;
			this.defaultValue = defaultValue;
		}

		/**
		 * Gets the name of the option parameter
		 * 
		 * @return the name as a string
		 */
		public String getName() {
			return name;
		}

		/**
		 * Gets the description of the option parameter
		 * 
		 * @return the description as a string
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Gets the default value assigned to this parameter.
		 * 
		 * @return the default string value
		 */
		public String getDefaultValue() {
			return defaultValue;
		}
	}

	/**
	 * Starts the creation of a new command-line option. An option can be specified
	 * on the command-line by specifying its name preceded by two consecutive minus
	 * ("--") characters.<br>
	 * <br>
	 * An option can be accessed from the {@link CommandContext} only through its
	 * name as it is specified during the construction of the option.
	 * 
	 * @param name        the name of the option
	 * @param description the description of the option
	 * @return An {@link Option.Builder} object
	 */
	public static Builder create(String name, String description) {
		return new Builder(name, description);
	}

	/**
	 * A builder entity used build new {@link Option options}.
	 * 
	 * @author Salvatore Giampa'
	 *
	 */
	public static class Builder {
		private Option option = new Option();

		private void checkName(String name) {
			final String NAME_REGEX = "[a-zA-Z][a-zA-Z0-9\\-]*";
			if (!name.matches(NAME_REGEX))
				throw new IllegalArgumentException("The name should match the following regex:" + " " + NAME_REGEX);
		}

		/**
		 * Starts the creation of a new command-line option. An option can be specified
		 * on the command-line by specifying its name preceded by two consecutive minus
		 * ("--") characters.<br>
		 * <br>
		 * An option can be accessed from the {@link CommandContext} only through its
		 * name as it is specified during the construction of the option.
		 * 
		 * @param name        the name of the option
		 * @param description the description of the option
		 */
		private Builder(String name, String description) {
			checkName(name);
			option.name = name;
			option.description = description;
		}

		/**
		 * Add an alias for the option. An option can be specified on the command-line
		 * through an alias by specifying the name of that alias preceded by a single
		 * minus ('-') character.<br>
		 * <br>
		 * For example, given an option "test":
		 * <ul>
		 * <li>the option name is "my-option";
		 * <li>an alias of the option is "myo";
		 * <li>another alias of the option is "o";
		 * </ul>
		 * The option can be specified on the command-line alternatively through:
		 * <ul>
		 * <li>--my-option followed by option parameters
		 * <li>-myo followed by option parameters
		 * <li>-o followed by option parameters
		 * </ul>
		 * 
		 * An option can be accessed from the {@link CommandContext} only through its
		 * name ("my-option", in the previous example) as it is specified during the
		 * construction of the option.
		 * 
		 * @param alias the name of the alias
		 * @return this {@link Option.Builder}
		 */
		public Builder addAlias(String alias) {
			return addAliases(alias);
		}

		/**
		 * Add aliases for the option. Invoking this method, is equivalent to invoke the
		 * {@link #addAlias(String)} method on each string passed to this one.
		 * 
		 * @param aliases the names of the aliases to add. If the array is empty, no
		 *                alias is added to the option and no exception is thrown.
		 * @return this {@link Option.Builder}
		 * @see #addAlias(String)
		 */
		public Builder addAliases(String... aliases) {
			for (String alias : aliases)
				checkName(alias);
			for (String alias : aliases)
				option.aliases.add(alias);
			return this;
		}

		/**
		 * Add a parameter to the option.
		 * 
		 * @param name         the name of the parameter
		 * @param description  the description of the parameter
		 * @param defaultValue the default value assumed by the parameter in the case
		 *                     the option is not specified on the command-line
		 * @return this {@link Option.Builder}
		 */
		public Builder addParameter(String name, String description, String defaultValue) {
			option.parameters.add(new Parameter(name, description, defaultValue));
			return this;
		}

		/**
		 * Definitely builds the option.
		 * 
		 * @return the new {@link Option} object
		 */
		public Option build() {
			return new Option(option);
		}
	}

}
