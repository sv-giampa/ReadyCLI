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

/**
 * Represents a required argument of a command. This class is used internally by
 * the {@link Command} class.
 * 
 * @author Salvatore Giampa'
 *
 */
final class RequiredArgument implements Serializable {
	private static final long serialVersionUID = -553819628678014753L;
	private String name;
	private String description;

	RequiredArgument(String name, String description) {
		final String NAME_REGEX = "[a-zA-Z][a-zA-Z0-9\\-]*";
		if (!name.matches(NAME_REGEX))
			throw new IllegalArgumentException("The name should match the following regex:" + " " + NAME_REGEX);
		this.name = name;
		this.description = description;
	}

	/**
	 * Gets the name of the {@link RequiredArgument}
	 * 
	 * @return the name as a string.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the description of the {@link RequiredArgument}
	 * 
	 * @return the description as a string.
	 */
	public String getDescription() {
		return description;
	}
}
