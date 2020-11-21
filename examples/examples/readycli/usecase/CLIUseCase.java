/**
 *  Copyright 2020 Salvatore Giamp�
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

package examples.readycli.usecase;

import readycli.CLI;
import readycli.Command;
import readycli.Option;

public class CLIUseCase {
	public static void main(String[] args) {
		CLI cli = CLI.create("CLIUseCase", "command> ");

		cli.setCommandPrompt("insert command> ")
				.addCommand(Command.forCLI("my-command", "A command for my CLI")
						.addOption(Option.create("opt", "An option of my-command")
								.addParameter("my-param", "A parameter for opt", "my default value")
								.build())
						.build(context -> {
							context.out.println("arguments: " + context.arguments);
							context.out.println("options: " + context.options);
							context.out.println("opt -> my-param=" + context.getOption("opt")
									.get("my-param"));
						})) // end of my-command command
				.addCommand(Command.forCLI("set-prompt", "Set a prompt for my CLI")
						.addRequiredArgument("new-prompt", "The new prompt to set")
						.build(context -> {
							String newPrompt = context.getArgument("new-prompt");
							cli.setCommandPrompt(newPrompt + "> ");
						})) // end of set-prompt command
				.addCommand(Command.forCLI("exit", "Exits the program")
						.build(context -> System.exit(0))); // end of exit command

		cli.execute(System.out, System.in);
	}
}
