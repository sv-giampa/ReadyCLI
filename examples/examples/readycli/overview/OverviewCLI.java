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

package examples.readycli.overview;

import java.util.Locale;

import readycli.CLI;
import readycli.Command;
import readycli.Option;

public class OverviewCLI {
	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		CLI cli = CLI.create("CLI Overview", "insert a command> ");

		cli.addCommand("my-command", Command.defaultDocs("my-command", "A command for my CLI")
				.addOption(Option.create("opt", "An option of my-command")
						.addAlias("o")
						.addParameter("my-param", "A parameter for --opt", "my default value")
						.build())
				.build(context -> { // build the "my-command" command
					context.out.println("arguments: " + context.arguments); // print required arguments
					context.out.println("options: " + context.options); // print options
				})) // end of "my-command" command
				.addCommand("set-prompt", Command.defaultDocs("set-prompt", "Set a prompt for my CLI")
						.addRequiredArgument("new-prompt", "The new prompt to set")
						.build(context -> { // build the "set-prompt" command
							String newPrompt = context.getArgument("new-prompt");
							// access the CLI itself and modifies it from this lambda
							cli.setCommandPrompt(newPrompt + "> ");
						})) // end of "set-prompt" command
				.addCommand("exit", Command.defaultDocs("exit", "Exits the program")
						.build(context -> System.exit(0))); // end of exit command

		cli.execute(); // executes the CLI on the standard I/O
	}
}
