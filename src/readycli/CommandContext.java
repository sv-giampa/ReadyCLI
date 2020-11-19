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

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;

public final class CommandContext {

	/**
	 * Contains all values of all arguments. This is an unmodifiable map.<br>
	 * <ul>
	 * <li>Key: argument name;
	 * <li>Value: argument value;
	 * </ul>
	 */
	public final Map<String, String> arguments;

	/**
	 * Contains all values of all options. This is an unmodifiable map.<br>
	 * <ul>
	 * <li>Key: option name;
	 * <li>Value: option values;
	 * </ul>
	 */
	public final Map<String, OptionValues> options;

	/**
	 * The output stream used to interact with the user.
	 */
	public final PrintStream out;

	/**
	 * The input stream used to interact with the user.
	 */
	public final InputStream in;

	CommandContext(Map<String, String> arguments, Map<String, OptionValues> options, PrintStream output,
			InputStream input) {
		super();
		this.arguments = Collections.unmodifiableMap(arguments);
		this.options = Collections.unmodifiableMap(options);
		this.out = output;
		this.in = input;
	}

	/**
	 * Gets the value of an argument. <br>
	 * Invoking this method, has the same effect of the following code:<br>
	 * <code>context.arguments.get(name);</code>
	 * 
	 * @param name the name of the argument
	 * @return the string value of the argument
	 */
	public String getArgument(String name) {
		return arguments.get(name);
	}

	/**
	 * Gets the parameter values of an option.<br>
	 * Invoking this method, has the same effect of the following code:<br>
	 * <code>context.options.get(name);</code>
	 * 
	 * @param name the name of the option
	 * @return an {@link OptionValues} object containing the values of the option
	 */
	public OptionValues getOption(String name) {
		return options.get(name);
	}
}
