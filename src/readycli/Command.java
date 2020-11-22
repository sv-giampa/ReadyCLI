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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Represents a command to be typed in a command line. The command can be
 * included in a {@link CLI} or can be a stand-alone command that represents a
 * Java class with an executable main() method. In this case the command can be
 * used to parse command line arguments passed to the main() method.
 * 
 * <h2>Command components</h2>
 * 
 * A command is mainly composed by:<br>
 * <ul>
 * <li>A name: identifies the command, expecially in a {@link CLI};
 * <li>A description: is used for command documentation; it tells what the
 * command will do when invoked;
 * <li>A usage string: shows how to use the command;
 * <li>Required arguments: are the arguments necessary for command execution
 * (e.g. the source and the destination file names for a copy command).
 * <li>Options: optional parameters or flags that have a changeable default
 * values (e.g. a parameter that indicates the name of a log file, or a flag
 * that tells if the source file must be deleted after a copy)
 * <li>Sub-commands: are commands nested in the current one. Invoking a
 * sub-command is equivalent to invoke another independent command with the
 * simple difference that its first level name is the current one.
 * <li>Documentation option: is the optional flag that shows the documentation
 * of the command. The documentation option can have more than one alias. An
 * alias for the documentation option is a simple name string that can be typed
 * by the user as first argument of the command to show its documentation.
 * </ul>
 * 
 * <h2>Usage String</h2>
 * 
 * The usage string show how to use the command. For example: given a class
 * my.example.Main with a main() method that implements the command, the usage
 * string is an information for the user that tells how to run the program,
 * showing that the user must run the java command, followed by a class-path and
 * by the canonical name of the class that contains the main method. In this
 * case the usage string can be <br>
 * <code> java -cp MyExample/classes my.example.Main </code><br>
 * If the program is designed to run from a jar file, the usage string can
 * specify to run the program as <br>
 * <code>java -jar myjarfile.jar</code><br>
 * Alternatively, the usage string can be an empty string, so that documentation
 * shows only the expected arguments.<br>
 * If the command is implemented in a CLI, the usage string can be just equal to
 * the name of the command. <br>
 * The usage string can be an empty string. <br>
 * Some of these cases have some default solution, given by the creator methods
 * {@link #forMain(String, String)} and {@link #forCLI(String, String)}.
 * 
 * <h2>Required Arguments</h2>
 * 
 * All the required arguments must be typed just as first arguments for the
 * command, in the same order they are specified during command construction and
 * in the documentation.<br>
 * 
 * <h2>Options</h2>
 * 
 * For details about the options see {@link Option}.
 * 
 * <h2>Sub-commands</h2>
 * 
 * A sub-command is just a command that can be used by typing its name after the
 * name of the current one. The sub-command will have its own required
 * arguments, options, sub-commands and documentation option.
 * 
 * <h2>Documentation option</h2>
 * 
 * As sayd above, the documentation option can have one or more aliases, to be
 * typed by the final user of the software. If the developer does not indicate a
 * documentation option alias during the command construction, the command will
 * not have documentation option available. The developer should always indicate
 * one or more documentation option aliases using the
 * {@link Command.Builder#addDocumentationAlias(String)} method or
 * {@link Command.Builder#addDocumentationAliases(String...)} method.<br>
 * <br>
 * 
 * 
 * @author Salvatore Giampa'
 *
 */
public final class Command implements Serializable {
	private static final long serialVersionUID = -4623869993281952660L;
	private CommandExecutor commandExecutor;
	private Command parent;
	private String usageString;
	private String name;
	private String description;

	private ArrayList<RequiredArgument> requiredArguments = new ArrayList<>();
	private Map<String, Option> options = new TreeMap<>();
	private Map<String, Command> subCommands = new TreeMap<>();
	private Set<String> documentationAliases = new TreeSet<>();

	// private constructor, use the Command.Builder class to create new commands
	private Command() {}

	/**
	 * Creates a new command with the given name, description, usage string and
	 * document option aliases. The name of the command identifies it and allows a
	 * {@link CLI} to address it when the user types it in.<br>
	 * <br>
	 * See {@link Command} for more details about usage strings.
	 * 
	 * @param name        the name of the command
	 * @param description the human-readable description of the command
	 * @param usageString the string that specify how to run the command
	 * @param docAliases  the aliases for the documentation option
	 * @return a new {@link Command.Builder}
	 * @see Command
	 */
	public static Builder create(String name, String description, String usageString, String... docAliases) {
		return new Builder(name, description, usageString).addDocumentationAliases(docAliases);
	}

	/**
	 * Creates a new command, well suitable to parse main() method command-line
	 * arguments, as the {@link #create(String, String, String, String...) create()}
	 * method does. This method sets the usage string to an empty one.<br>
	 * The new command will have the default documentation option aliases: '?',
	 * '--help', '-h'. <br>
	 * <br>
	 * See {@link Command} for more details about usage strings and documentation
	 * option.
	 * 
	 * @param name        the name of the command
	 * @param description the human-readable description of the command
	 * @return a new {@link Command.Builder}
	 * @see Command
	 */
	public static Builder forMain(String name, String description) {
		return create(name, description, "", "?", "--help", "-h"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	/**
	 * Creates a new command well suitable for CLIs, as the
	 * {@link #create(String, String, String, String...) create()} method does. This
	 * method sets the usage string to be equal to the command name.<br>
	 * The new command will have the default documentation option aliases: '?',
	 * '--help', '-h'. <br>
	 * See {@link Command} for more details about usage strings and documentation
	 * option.
	 * 
	 * @param name        the name of the command
	 * @param description the human-readable description of the command
	 * @return a new {@link Command.Builder}
	 * @see Command
	 */
	public static Builder forCLI(String name, String description) {
		return create(name, description, name, "?", "--help", "-h"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Gets the name of the command.
	 * 
	 * @return the name as string.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the command description.
	 * 
	 * @return the description as string.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the list of all the required arguments of this command.
	 * 
	 * @return an unmodifiable list.
	 */
	public List<RequiredArgument> getRequiredArguments() {
		return Collections.unmodifiableList(requiredArguments);
	}

	/**
	 * Gets a map of all the options of this command.
	 * 
	 * @return an unmodifiable map that associates the names of the options (keys)
	 *         to the options themselves (values).
	 */
	public Map<String, Option> getOptions() {
		return Collections.unmodifiableMap(options);
	}

	/**
	 * Gets a map of all the sub-commands of first level of this command.
	 * 
	 * @return an unmodifiable map that associates the names of the sub-commands
	 *         (keys) to the sub-commands themselves (values).
	 */
	public Map<String, Command> getSubCommands() {
		return Collections.unmodifiableMap(subCommands);
	}

	public Set<String> getDocumentationAliases() {
		return documentationAliases;
	}

	/**
	 * Executes the command on a list of arguments encoded as a string command-line,
	 * as {@link #execute(String, PrintStream, InputStream)}, and uses the standard
	 * in/output to interact with the user.
	 * 
	 * @param arguments the command-line arguments for this command as a full string
	 * @return the {@link ExitCause exit cause}
	 * @see #execute(String, PrintStream, InputStream)
	 */
	public ExitCause execute(String arguments) {
		return execute(arguments, System.out, System.in);
	}

	/**
	 * Executes the command on a list of arguments encoded as a string
	 * command-line.<br>
	 * <br>
	 * This method automatically splits the command-line taking care of single quote
	 * ('), double quote (") and escaped characters, as a UNIX-like operative system
	 * does, then it passes the result list of arguments to the
	 * {@link #execute(List, PrintStream, InputStream)} method<br>
	 * <br>
	 * Single quotes (') and double quotes (") characters can be escaped preceding
	 * them with a backslash (\) character: \", \'.<br>
	 * The backslash (\) character can be escaped itself in the same way: \\.<br>
	 * <br>
	 * Note that: the first token of the passed command-line is parsed as an
	 * argument of the command and not as the name of the command itself. If the
	 * command-line comprises the name of this command, the caller should separate
	 * the name of this command from its arguments before invoking this method.
	 *
	 * @param arguments the command-line arguments for this command as a full string
	 * @param output    the output stream to write messages for the user to.
	 * @param input     the input stream used to read the user input.
	 * @return the {@link ExitCause exit cause}
	 */
	public ExitCause execute(String arguments, PrintStream output, InputStream input) {
		return execute(parseArgumentsLine(arguments), output, input);
	}

	/**
	 * Executes the command on an array of arguments already tokenized, using the
	 * standard {@link System#in I}/{@link System#out O} to interact with the user.
	 * This method is useful to be invoked on the arguments received by the
	 * <code>main()</code> method.<br>
	 * <br>
	 * For more details, please refer to
	 * {@link #execute(List, PrintStream, InputStream)};
	 *
	 * @param args the command-line arguments for this command as an array of
	 *             strings
	 * @return the {@link ExitCause exit cause}
	 */
	public ExitCause execute(String[] args) {
		return execute(args, System.out, System.in);
	}

	/**
	 * Executes the command on an array of arguments already tokenized. This method
	 * is useful to be invoked on the arguments received by the <code>main()</code>
	 * method.<br>
	 * <br>
	 * For more details, please refer to
	 * {@link #execute(List, PrintStream, InputStream)};
	 *
	 * @param args   the command-line arguments for this command as an array of
	 *               strings
	 * @param output the output stream to write messages for the user to.
	 * @param input  the in stream used to read the user in.
	 * @return the {@link ExitCause exit cause}
	 */
	public ExitCause execute(String[] args, PrintStream output, InputStream input) {
		return execute(Arrays.asList(args), output, input);
	}

	/**
	 * Executes the command on a list of arguments already tokenized, using the
	 * standard {@link System#in I}/{@link System#out O} to interact with the
	 * user.<br>
	 * <br>
	 * For more details, please refer to
	 * {@link #execute(List, PrintStream, InputStream)};
	 * 
	 * @param arguments the list of arguments
	 * @return the {@link ExitCause exit cause}
	 * @see #execute(List, PrintStream, InputStream)
	 */
	public ExitCause execute(List<String> arguments) {
		return execute(arguments, System.out, System.in);
	}

	/**
	 * Executes the command on a list of arguments already tokenized.<br>
	 * <br>
	 * 
	 * Note that: the first token is parsed as an argument of the command and not as
	 * the name of the command itself. The caller should separate the name of this
	 * command from its arguments before invoking this method.
	 *
	 * @param arguments the command-line arguments already for this command as a
	 *                  list of strings
	 * @param output    the output stream to write messages for the user to.
	 * @param input     the in stream used to read the user in.
	 * @return the {@link ExitCause exit cause}
	 */
	public ExitCause execute(List<String> arguments, PrintStream output, InputStream input) {

		if (arguments.size() > 0 && documentationAliases.contains(arguments.get(0))) // $NON-NLS-1$
			return printDocumentation(output);

		// sub-command
		if (arguments.size() > 0 && subCommands.containsKey(arguments.get(0)))
			return subCommands.get(arguments.get(0))
					.execute(arguments.subList(1, arguments.size()), output, input);

		Map<String, String> args = new HashMap<>();
		Map<String, OptionValues> opts = new HashMap<>();

		Set<Option> foundOptions = new HashSet<>();

		int argIndex = 0;
		for (int index = 0; index < arguments.size(); index++) {
			String key = arguments.get(index);

			if (options.containsKey(key)) {
				// option
				Option option = options.get(key);
				int nParams = option.getParameters()
						.size();

				int start = index + 1;
				int end = start + nParams;

				if (end > arguments.size()) {
					int expectedParamIndex = arguments.size() - start + 1;
					Option.Parameter expectedParam = option.getParameters()
							.get(expectedParamIndex);
					output.printf(Messages.getString("Command.7"), expectedParam.getName(), //$NON-NLS-1$
							option.getName());
					output.println();
					output.println();
					output.printf(Messages.getString("Command.3"), //$NON-NLS-1$
							documentationAliases);
					output.println();
					return ExitCause.ERROR_EXPECTED_OPTION_PARAMETER;
				}

				List<String> values = arguments.subList(start, end);
				opts.put(option.getName(), option.process(values));
				foundOptions.add(option);
				index = end - 1;
			} else if (argIndex < requiredArguments.size()) {
				// required argument
				String value = arguments.get(index);
				args.put(requiredArguments.get(argIndex++)
						.getName(), value);
			} else {
				// unknown option
				output.printf(Messages.getString("Command.8"), key, index); //$NON-NLS-1$ //$NON-NLS-2$
				output.println();
				output.println();
				output.printf(Messages.getString("Command.3"), //$NON-NLS-1$
						documentationAliases);
				output.println();
				return ExitCause.ERROR_UNEXPECTED_OPTION;
			}
		}

		if (argIndex < requiredArguments.size()) {
			output.println(Messages.getString("Command.6") + ": " + requiredArguments.get(argIndex) //$NON-NLS-1$//$NON-NLS-2$
					.getName());
			output.println();
			output.printf(Messages.getString("Command.3"), //$NON-NLS-1$
					documentationAliases);
			output.println();
			return ExitCause.ERROR_EXPECTED_ARGUMENT;
		}

		// process default values for unspecified options
		Set<Option> processDefaults = new HashSet<>(options.values());
		processDefaults.removeAll(foundOptions); // removes all the specified options

		for (Option option : processDefaults)
			opts.put(option.getName(), option.processDefaults());

		if (commandExecutor == null) {
			output.println(Messages.getString("Command.9")); //$NON-NLS-1$
			return ExitCause.ERROR_COMMAND_NOT_IMPLEMENTED;
		}

		try {
			CommandContext commandContext = new CommandContext(args, opts, output, input);
			commandExecutor.process(commandContext);
		} catch (Exception e) {
			e.printStackTrace();
			return ExitCause.ERROR_COMMAND_EXECUTOR;
		}
		return ExitCause.SUCCESS;
	}

	/**
	 * Specify the cause of the exit from command execution. The command already
	 * reports all the errors on the output stream passed to the
	 * {@link Command#execute(List, PrintStream, InputStream)} method.
	 *
	 * @author Salvatore Giampa'
	 *
	 */
	public static enum ExitCause {
		/**
		 * The command successfully terminated
		 */
		SUCCESS(0),

		/**
		 * The help flag was specified
		 */
		HELP_FLAG(0),

		/**
		 * Terminated in error, because an expected required argument was not specified
		 */
		ERROR_EXPECTED_ARGUMENT(-1),

		/**
		 * Terminated in error, because an expected option parameter was not specified
		 */
		ERROR_EXPECTED_OPTION_PARAMETER(-2),

		/**
		 * Terminated in error, because an unexpected option name was specified
		 */
		ERROR_UNEXPECTED_OPTION(-3),

		/**
		 * Terminated in error, because the command was not implemented (i.e. the
		 * command commandExecutor was set to null)
		 */
		ERROR_COMMAND_NOT_IMPLEMENTED(-4),

		/**
		 * Terminated in error, because the command commandExecutor thrown an exception
		 */
		ERROR_COMMAND_EXECUTOR(-5);

		private int exitCode;

		private ExitCause(int exitCode) {
			this.exitCode = exitCode;
		}

		public int getExitCode() {
			return exitCode;
		}
	}

	/**
	 * Shows the help of the command, printing the command name, the description,
	 * the usage, the required arguments, the options and the sub-commands, with
	 * relative descriptions.
	 * 
	 * @param output the output stream where the help must be printed to
	 * @return {@link ExitCause#HELP_FLAG}
	 */
	private ExitCause printDocumentation(PrintStream output) {
		if (commandExecutor == null && subCommands.isEmpty()) {
			output.println(Messages.getString("Command.10")); //$NON-NLS-1$
		} else {
			// create full command string from parent commands
			LinkedList<String> usageList = new LinkedList<>();
			Command current = this;
			usageList.addFirst(current.usageString);
			while (current.parent != null) {
				current = current.parent;
				usageList.addFirst(current.usageString);
			}
			StringBuilder sb = new StringBuilder();
			sb.append(usageList.removeFirst());
			while (!usageList.isEmpty())
				sb.append(' ')
						.append(usageList.removeFirst());
			String fullCommand = sb.toString();

			// print command description
			output.println(Messages.getString("Command.2")); //$NON-NLS-1$
			output.print('\t' + name + ' ' + '-' + ' ');
			output.println(description);

			// print usage
			String usageMsg = Messages.getString("Command.11"); //$NON-NLS-1$

			output.println(usageMsg);
			if (commandExecutor != null) {
				output.printf("\t%s", fullCommand); // $NON-NLS-2$ //$NON-NLS-1$
				for (RequiredArgument reqArg : requiredArguments)
					output.printf(" <%s>", reqArg.getName()); //$NON-NLS-1$

				if (!options.isEmpty()) {
					Set<Option> alreadyPrinted = new HashSet<>();
					for (Entry<String, Option> entry : options.entrySet()) {
						Option option = entry.getValue();
						if (!alreadyPrinted.contains(option)) {
							output.printf(" [--%s", option.getName()); //$NON-NLS-1$

							for (Option.Parameter param : option.getParameters())
								output.printf(" <%s>", param.getName()); //$NON-NLS-1$
							output.printf("]", option.getName()); //$NON-NLS-1$
							alreadyPrinted.add(option);
						}
					}
				}
				output.println();
			} else {
				output.println(Messages.getString("Command.13")); //$NON-NLS-1$
			}

			if (subCommands.size() > 0) {
				String aSubCommandMsg = Messages.getString("Command.14"); //$NON-NLS-1$
				String subCommandArgumentsMsg = Messages.getString("Command.15"); //$NON-NLS-1$
				output.printf("%s \t %s {<%s> <%s ...>}", usageMsg, fullCommand, aSubCommandMsg, //$NON-NLS-1$
						subCommandArgumentsMsg);
				output.println();
			}

			// print required arguments and their descriptions
			if (commandExecutor != null && requiredArguments.size() > 0) {
				output.println();
				output.println(Messages.getString("Command.17")); //$NON-NLS-1$
				int index = 1;
				for (RequiredArgument reqArg : requiredArguments) {
					output.printf("\t(%s)\t<%s>:  %s\n", index, reqArg.getName(), reqArg.getDescription()); //$NON-NLS-1$
					index++;
				}
			}

			// print options, their descriptions, their parameters and the
			// descriptions of their parameters

			output.println();
			output.println(Messages.getString("Command.18")); //$NON-NLS-1$

			if (documentationAliases.size() > 0) {
				output.print('\t');
				boolean firstDocAlias = true;
				for (String alias : documentationAliases) {
					if (!firstDocAlias) {
						output.print(", "); //$NON-NLS-1$
					}
					firstDocAlias = false;
					output.printf("%s", alias); //$NON-NLS-1$
				}
				output.printf(":  %s\n\n", Messages.getString("Command.26")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (commandExecutor != null && options.size() > 0) {
				Set<Option> alreadyPrinted = new HashSet<>();
				for (Option option : options.values())
					if (!alreadyPrinted.contains(option)) {
						output.printf("\t--%s", option.getName()); //$NON-NLS-1$
						for (String alias : option.getAliases())
							output.printf(", -%s", alias); //$NON-NLS-1$
						output.printf(":  %s\n\n", option.getDescription()); //$NON-NLS-1$

						if (!option.getParameters()
								.isEmpty()) {
							output.println(Messages.getString("Command.22")); //$NON-NLS-1$
							int i = 1;
							for (Option.Parameter param : option.getParameters())
								output.printf("\t\t(%s)\t%s:  %s (deafult value: \"%s\")\n", i++, param.getName(), //$NON-NLS-1$
										param.getDescription(), param.getDefaultValue());
						}
						output.println();
						alreadyPrinted.add(option);
					}
			}

			// print sub-commands and their descriptions but not their arguments
			if (subCommands.size() > 0) {
				output.println();
				output.println(Messages.getString("Command.23")); //$NON-NLS-1$
				for (Entry<String, Command> entry : subCommands.entrySet()) {
					Command subCommand = entry.getValue();
					output.printf("\t%s: %s", subCommand.getName(), subCommand.getDescription()); //$NON-NLS-1$
					output.printf(Messages.getString("Command.1"), //$NON-NLS-1$
							subCommand.getDocumentationAliases());
					output.println();
				}
				output.println();
				output.println(Messages.getString("Command.0")); //$NON-NLS-1$
			}
			output.println();

		}
		return ExitCause.HELP_FLAG;
	}

	/**
	 * Parse the arguments of from a string.
	 * 
	 * @param argumentsLine the string containing the arguments.
	 * @return the tokens parsed from the given string.
	 */
	private static List<String> parseArgumentsLine(String argumentsLine) {
		final int NEW_TOKEN = 0;
		final int CONTINUE = 1;
		final int SINGLE_QUOTES = 2;
		final int DOUBLE_QUOTES = 3;

		List<String> tokens = new LinkedList<String>();
		StringBuilder currentToken = new StringBuilder();
		boolean escapeCharFound = false;

		int state = NEW_TOKEN; // initial state
		for (int index = 0; index < argumentsLine.length(); index++) {
			char c = argumentsLine.charAt(index);
			if (escapeCharFound) {
				escapeCharFound = false;
				currentToken.append(c);
			} else {
				switch (state) {
				case NEW_TOKEN:
				case CONTINUE:
					switch (c) {
					case '\\':
						escapeCharFound = true;
						state = CONTINUE;
						break;
					case '\'':
						state = SINGLE_QUOTES;
						break;
					case '"':
						state = DOUBLE_QUOTES;
						break;
					default:
						if (!Character.isWhitespace(c)) {
							currentToken.append(c);
							state = CONTINUE;
						} else if (state == CONTINUE) {
							tokens.add(currentToken.toString());
							currentToken.setLength(0);
							state = NEW_TOKEN;
						}
					}
					break;
				case SINGLE_QUOTES:
					switch (c) {
					case '\'':
						state = CONTINUE;
						break;
					default:
						currentToken.append(c);
					}
					break;
				case DOUBLE_QUOTES:
					switch (c) {
					case '"':
						state = CONTINUE;
						break;
					case '\\':
						index++;
						char next = argumentsLine.charAt(index);
						if (next == '"' || next == '\\') {
							currentToken.append(next);
						} else {
							currentToken.append(c);
							currentToken.append(next);
						}
						break;
					default:
						currentToken.append(c);
					}
					break;
				}
			}
		}
		if (escapeCharFound) {
			currentToken.append('\\');
			tokens.add(currentToken.toString());
		} else if (state != NEW_TOKEN) {
			tokens.add(currentToken.toString());
		}

		return tokens;
	}

	/**
	 * Class used to build a new {@link Command command}. To instantiate this class
	 * use the {@link Command#create(String, String, String, String...)} method.
	 * 
	 * @author Salvatore Giampa'
	 *
	 */
	public static class Builder {
		private Command command = new Command();

		/**
		 * Checks if the given name is already assigned to a required argument, option
		 * or sub-command.
		 * 
		 * @param name the name to check
		 */
		private void checkName(String name) {
			if (command.requiredArguments.stream()
					.anyMatch(arg -> arg.getName()
							.equals(name))
					|| command.options.containsKey(name) || command.subCommands.containsKey(name)
					|| command.documentationAliases.contains(name))
				throw new IllegalStateException(String.format("The name or alias \"%s\" is already assigned.", name)); //$NON-NLS-1$
		}

		// checks if the builder can be used
		private void checkState() {
			if (command == null)
				throw new IllegalArgumentException("The command has been already built."); //$NON-NLS-1$
		}

		private Builder(String name, String description, String usageString) {
			final String NAME_REGEX = "[a-zA-Z][a-zA-Z0-9\\-]*"; //$NON-NLS-1$
			if (!name.matches(NAME_REGEX))
				throw new IllegalArgumentException(
						"The command name must match the following regular expression: " + NAME_REGEX); //$NON-NLS-1$
			command.name = name;
			command.description = description;
			command.usageString = usageString;
		}

		/**
		 * Adds a new alias for the option that shows the documentation. This option
		 * must be found on the first element of the arguments list. In other cases it
		 * is interpreted as an argument for the command execution.
		 * 
		 * @param alias the alias for the documentation command
		 * @return this same {@link Builder builder}
		 * @throws IllegalStateException if the given name is already assigned to a
		 *                               required argument, option, sub-command,
		 *                               documentation alias or if the command has been
		 *                               already built.
		 * @see #addDocumentationAliases(String...)
		 */
		public Builder addDocumentationAlias(String alias) {
			checkState();
			checkName(alias);
			command.documentationAliases.add(alias);
			return this;
		}

		/**
		 * Adds new aliases for the documentation option, that shows the documentation.
		 * This option must be found on the first element of the arguments list. In
		 * other cases it is interpreted as an argument for the command execution.
		 * 
		 * @param aliases the aliases for the documentation option.
		 * @return this same {@link Builder builder}
		 * @throws IllegalStateException if the given name is already assigned to a
		 *                               required argument, option, sub-command,
		 *                               documentation alias or if the command has been
		 *                               already built.
		 * @see #addDocumentationAlias(String)
		 */
		public Builder addDocumentationAliases(String... aliases) {
			checkState();
			Arrays.asList(aliases)
					.forEach(alias -> addDocumentationAlias(alias));
			return this;
		}

		/**
		 * Adds a required argument to the command. This method maintains the order of
		 * addiction of the required arguments. That order is then used to receive
		 * arguments on the command-line.
		 * 
		 * @param name        the name of the argument.
		 * @param description the description of the argument.
		 * @return this same {@link Builder builder}
		 * @throws IllegalStateException if the given name is already assigned to a
		 *                               required argument, option, sub-command,
		 *                               documentation alias or if the command has been
		 *                               already built.
		 */
		public Builder addRequiredArgument(String name, String description) throws IllegalStateException {
			checkState();
			checkName(name);
			command.requiredArguments.add(new RequiredArgument(name, description));
			return this;
		}

		/**
		 * Adds a flag to the command. A flag is a normal {@link Option option} with no
		 * parameters. The flag boolean value is accessed by using a
		 * {@link CommandContext} as a normal {@link Option}, and using the method
		 * {@link OptionValues#getFlag()}. This method is used to automate the creation
		 * of an option with no parameters and invoking it has the same effect of
		 * invoking the {@link #addOption(Option)} on a new option with no
		 * parameters:<br>
		 * <br>
		 * <code>addOption(Option.create(name, description).build());</code>
		 * 
		 * @param name        the name of the flag.
		 * @param description the description of the flag.
		 * @return this same {@link Builder builder}
		 * @throws IllegalStateException if the given name is already assigned to a
		 *                               required argument, sub-command, documentation
		 *                               alias or if the command has been already built.
		 */
		public Builder addFlag(String name, String description) throws IllegalStateException {
			checkState();
			checkName(name);
			return addOption(Option.create(name, description)
					.build());
		}

		/**
		 * Adds an {@link Option option} to the {@link Command command}. An
		 * {@link Option} has a name that starts with a lowercase or a capital letter.
		 * The name specified at the option construction is then used to access it from
		 * the {@link CommandContext}. The option can be specified on the command-line
		 * by using "--" followed by the option name or "-" followed by an
		 * {@link Option#getAliases() option alias}.
		 * 
		 * @param option the {@link Option option} to add.
		 * @return this same {@link Builder builder}
		 * @throws IllegalStateException if the name of the given option is already
		 *                               assigned to a required argument, option,
		 *                               sub-command, documentation alias or if the
		 *                               command has been already built.
		 */
		public Builder addOption(Option option) throws IllegalStateException {
			checkState();
			checkName("--" + option.getName()); //$NON-NLS-1$
			for (String alias : option.getAliases())
				checkName("-" + alias); //$NON-NLS-1$
			command.options.put("--" + option.getName(), option); //$NON-NLS-1$
			for (String alias : option.getAliases())
				command.options.put("-" + alias, option); //$NON-NLS-1$
			return this;
		}

		/**
		 * Adds a {@link Command sub-command} that can be invoked by specifying it as
		 * the first argument of the current command. Basically, This method allows for
		 * nested commands definition. The added sub-command can expose other
		 * sub-commands that are sub-commands of second level respect to the current
		 * command, and these can expose other sub-commands that are sub-commands of
		 * third level respect to the current command, and so on.
		 * 
		 * @param subCommand the {@link Command sub-command} to add.
		 * @return this same {@link Builder builder}
		 * @throws IllegalStateException if the name of the given sub-command is already
		 *                               assigned to a required argument, option,
		 *                               sub-command, documentation alias or if the
		 *                               command has been already built.
		 */
		public Builder addSubCommand(Command subCommand) throws IllegalStateException {
			checkState();
			checkName(subCommand.name);
			subCommand.parent = command;
			command.subCommands.put(subCommand.getName(), subCommand);
			return this;
		}

		/**
		 * Builds the new command. To build a command, a {@link CommandExecutor} is
		 * required. The {@link CommandExecutor} receives the arguments and the options
		 * parsed on the command-line in a {@link CommandContext} object and it
		 * represents the actions of the command. If the given {@link CommandExecutor}
		 * is null, then invoking the command without giving a sub-command will output
		 * an error and the {@link Command#execute(List, PrintStream, InputStream)
		 * execute} method of the command will return the
		 * {@link ExitCause#ERROR_COMMAND_NOT_IMPLEMENTED} value. Executing the command
		 * documentation, indicating '?' as first argument, will return the list of
		 * sub-commands only, ignoring arguments and options.<br>
		 * <br>
		 * Hint: a {@link CommandExecutor} can just collect the values of the arguments
		 * and the options in an opportune data-structure or in the fields of an object
		 * that can be queried by the application, doing the type casting or the parsing
		 * of these string values received from the command-line.
		 * 
		 * @param commandExecutor the {@link CommandExecutor} that does the actions
		 *                        associated to this command. It can be null.
		 * @return the new {@link Command command}.
		 */
		public Command build(CommandExecutor commandExecutor) {
			checkState();
			command.requiredArguments.trimToSize();
			command.commandExecutor = commandExecutor;
			command.documentationAliases = Collections.unmodifiableSet(command.documentationAliases);

			Command result = command;
			command = null;
			return result;
		}
	}
}
