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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Represents a CLI (Command-Line Interface). A CLI object can be created
 * through the {@link #create(String, String)} method. A CLI object is
 * responsible of interacting with the user on the given out and in streams. A
 * CLI object allows to add new commands and to remove existing ones. Moreover,
 * a CLI object allows to show an application title when it starts and a command
 * prompt, that can be modified at runtime. A CLI object is not active and it
 * must be executed through the {@link #execute(PrintStream, InputStream)}
 * method.
 * 
 * @author Salvatore Giampa'
 *
 */
public final class CLI implements Serializable {
	private static final long serialVersionUID = 5401195475462129855L;
	private String title;
	private String commandPrompt;
	private Map<String, Command> commands = new TreeMap<>();

	/**
	 * Creates a new {@link CLI} object with the given application title and command
	 * prompt.
	 * 
	 * @param title         the title to show when the {@link #CLI(String, String)}
	 *                      starts (e.g. the application name and the version)
	 * @param commandPrompt the prompt to show before starting to listen for
	 *                      commands (e.g. the current directory path)
	 * @return the new {@link CLI} object
	 */
	public static CLI create(String title, String commandPrompt) {
		return new CLI(title, commandPrompt);
	}

	/**
	 * Creates a new {@link CLI} object with a null application title and a null
	 * command prompt.
	 * 
	 * @return the new {@link CLI} object
	 */
	public static CLI create() {
		return new CLI(null, null);
	}

	/**
	 * Creates a new {@link CLI} object with a null application title and a null
	 * command prompt.
	 * 
	 * @param title the title to show when the {@link #CLI(String, String)} starts
	 *              (e.g. the application name and the version)
	 * @return the new {@link CLI} object
	 */
	public static CLI create(String title) {
		return new CLI(title, null);
	}

	/**
	 * Creates a new {@link CLI} object with the given application title and command
	 * prompt.
	 * 
	 * @param title         the title to show when the {@link #CLI(String, String)}
	 *                      starts (e.g. the application name and the version)
	 * @param commandPrompt the prompt to show just before starting to listen for
	 *                      commands (e.g. the current directory path)
	 */
	private CLI(String title, String commandPrompt) {
		this.title = title;
		this.commandPrompt = commandPrompt;
	}

	/**
	 * Adds a command to the {@link CLI}.
	 * 
	 * @param command the {@link Command command} to add
	 * @return this same {@link CLI} object
	 */
	public synchronized CLI addCommand(Command command) {
		if (commands.containsKey(command.getName()))
			throw new IllegalStateException(String.format("Command name \"%s\" already assigned.", command.getName())); //$NON-NLS-1$
		commands.put(command.getName(), command);
		return this;
	}

	/**
	 * Removes a command to the {@link CLI}.
	 * 
	 * @param name the name of the command to remove
	 * @return this same {@link CLI} object
	 */
	public synchronized CLI removeCommand(String name) {
		commands.remove(name);
		return this;
	}

	/**
	 * Sets a new command prompt to show just before the {@link CLI} starts to
	 * listen for the next command.
	 * 
	 * @param commandPrompt the new command prompt
	 * @return this same {@link CLI} object
	 */
	public synchronized CLI setCommandPrompt(String commandPrompt) {
		this.commandPrompt = commandPrompt;
		return this;
	}

	/**
	 * Gets the current command prompt that is shown just before the {@link CLI}
	 * starts to listen for the next command.
	 * 
	 * @return the current command prompt as string
	 */
	public String getCommandPrompt() {
		return commandPrompt;
	}

	/**
	 * Sets the {@link CLI} title (e.g. the application name and the version).
	 * 
	 * @param title the title to show when the {@link CLI} starts.
	 * @return this same {@link CLI} object
	 */
	public CLI setTitle(String title) {
		this.title = title;
		return this;
	}

	/**
	 * Gets the current title.
	 * 
	 * @return the current title as string
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Lists the available commands of this {@link CLI} on the given out stream.
	 * 
	 * @param output the out stream on which the list of command must be wrote.
	 */
	public void printHelp(PrintStream output) {
		output.println(Messages.getString("CLI.1")); //$NON-NLS-1$
		for (Entry<String, Command> entry : commands.entrySet()) {
			Command command = entry.getValue();

			output.printf(Messages.getString("CLI.2"), command.getName(), command.getDescription(), //$NON-NLS-1$
					command.getDocumentationAliases());
		}
		output.println(Messages.getString("CLI.3")); //$NON-NLS-1$
	}

	/**
	 * Starts executing the {@link CLI}, as the
	 * {@link #execute(PrintStream, InputStream)} method does, but on the standard
	 * {@link System#in I}/{@link System#out O}. <br>
	 * <br>
	 * See {@link #execute(PrintStream, InputStream)} for more details.
	 * 
	 * @see #execute(PrintStream, InputStream)
	 */
	public void execute() {
		execute(System.out, System.in);
	}

	/**
	 * Starts executing the {@link CLI} until the current thread is interrupted.
	 * When this method is invoked, the {@link CLI} starts writing the title on the
	 * specified out stream. Then the {@link CLI} writes a short message to show to
	 * the user how to list the available commands. Finally the {@link CLI} writes
	 * the command prompt on the out stream and starts listening for new commands on
	 * the given in stream.
	 * 
	 * @param output the out stream on which the user can see CLI messages (can be
	 *               the standard out {@link System#out})
	 * @param input  the in stream on which the user writes the commands (can be the
	 *               standard in {@link System#in})
	 */
	public void execute(PrintStream output, InputStream input) {
		Scanner in = new Scanner(input);
		output.println(title);
		output.println(Messages.getString("CLI.4")); //$NON-NLS-1$
		while (!Thread.currentThread()
				.isInterrupted()) {
			if (commandPrompt != null)
				output.print(commandPrompt);
			if (!in.hasNextLine())
				break;
			String commandLine = in.nextLine();
			if (!"".equals(commandLine)) { //$NON-NLS-1$
				Scanner lineScanner = new Scanner(commandLine);
				String commandName = lineScanner.next();
				String commandArgs = ""; //$NON-NLS-1$
				if (lineScanner.hasNextLine())
					commandArgs = lineScanner.nextLine();
				lineScanner.close();

				if (commandName.equals("?")) { //$NON-NLS-1$
					printHelp(output);
				} else {
					Command command = null;
					synchronized (this) {
						command = commands.get(commandName);
					}
					if (command != null) {
						command.execute(commandArgs, output, input);
					} else {
						output.println(Messages.getString("CLI.5") + commandName); //$NON-NLS-1$
					}
				}
			}
			output.println();
		}
		in.close();
	}
}
