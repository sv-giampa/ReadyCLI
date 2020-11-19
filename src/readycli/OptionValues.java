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
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * An instance of this class represents a particular assignment of the
 * parameters of an {@link Option}. Therefore, an instance of this class, allows
 * to access the actual values of the parameters of an {@link Option} by their
 * names, through the {@link #get(String)} method, and allows to know if the
 * option has been specified on the command-line, through the {@link #getFlag()}
 * method.
 * 
 * @author Salvatore Giampa'
 *
 */
public final class OptionValues implements Serializable {
	private static final long serialVersionUID = -2376506734130179329L;
	private String optionName;
	private boolean flag;
	private Map<String, String> parameters = new TreeMap<>();

	/**
	 * Package constructor. It is used to instantiate {@link OptionValues} objects
	 * during command parsing.
	 * 
	 * @param optionName the name of the represented {@link Option}
	 * @param flag       true if the option has been specified on the command-line,
	 *                   false otherwise
	 * @param parameters mapping of the parameter names to their actual values, that
	 *                   can be default values, too.
	 */
	OptionValues(String optionName, boolean flag, Map<String, String> parameters) {
		this.optionName = optionName;
		this.flag = flag;
		this.parameters.putAll(parameters);
		this.parameters = Collections.unmodifiableMap(this.parameters);
	}

	/**
	 * Gets the option name.
	 * 
	 * @return the name of the option
	 */
	public String getOptionName() {
		return optionName;
	}

	/**
	 * Allows to know if the option was specified on the command-line. It is useful
	 * for flags (i.e. parameterless options) to obtain their boolean values.
	 * 
	 * @return true if this option was specified, false otherwise.
	 */
	public boolean getFlag() {
		return flag;
	}

	/**
	 * Gets the map of all parameters values of the option.
	 * 
	 * @return an unmodifiable map of all parameters values
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Gets the {@link String} value of the specified parameter.
	 * 
	 * @param name the name of the parameter to get the value of
	 * @return the value associated to the given parameter name
	 */
	public String get(String name) {
		return parameters.get(name);
	}

	@Override
	public String toString() {
		return "{flag=" + flag + ", parameters=" + parameters + "}";
	}

}
