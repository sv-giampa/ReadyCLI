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

package examples.readycli.usecase;

import readycli.Command;
import readycli.Option;

public class CommandUseCase {
	public static void main(String[] args) {
		Command.forMain("example-command", "an example command")
				.addRequiredArgument("file-name", "in file name")
				.addFlag("myflag", "an example flag")
				.addOption(Option.create("optarg", "an example of optional argument")
						.addAlias("opt")
						.addParameter("value", "value of optarg", "optarg-default")
						.build())
				.addOption(Option.create("myoption", "an example option")
						.addParameter("p1", "parameter 1 of myoption", "35")
						.addParameter("p2", "parameter 2 of myoption", "myoption-p2-default")
						.build())
				.addSubCommand(Command.forCLI("my-sub-command", "an example sub-command")
						.addRequiredArgument("text-file", "a required argument of my-sub-command")
						.build(context -> {
							context.out.println("arguments: " + context.arguments);
							context.out.println("options: " + context.options);
						}))
				.build(context -> {
					String fileName = context.getArgument("file-name");
					boolean flag = context.getOption("myflag")
							.getFlag();
					context.out.println("arguments: " + context.arguments);
					context.out.println("options: " + context.options);
				})
				.execute("-? path/to/file -opt \"the alias works\"");
	}
}
