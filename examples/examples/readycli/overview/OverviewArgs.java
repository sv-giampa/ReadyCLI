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

import readycli.Command;
import readycli.Option;

public class OverviewArgs {
	public static void main(String[] args) {
		Command.forCLI("command-name", "command-description")
				.addRequiredArgument("file-name", "in file name") // a required argument
				.addFlag("myflag", "an example flag") // an optional flag (true if specified, false otherwise)
				.addOption(Option.create("myoption", "an example option") // an option
						.addParameter("p1", "parameter 1 of --myoption", "default-value-of-p1")
						.addParameter("p2", "parameter 2 of --myoption", "myoption-p2-default")
						.build())
				.addSubCommand(Command.forCLI("my-sub-command", "an example sub-command")
						.addRequiredArgument("text-file", "a required argument of my-sub-command")
						.build(context -> {
							String textFile = context.getArgument("text-file");
							// ... do something ...
						}))
				.build(context -> { // defines the code that can read the received arguments and build the command
					String fileName = context.getArgument("file-name"); // read the required argument
					boolean flag = context.getOption("myflag")
							.getFlag(); // get the boolean value of the flag
					// ... do something ...
				})
				.execute(args); // parse the arguments received by the main method
	}
}
