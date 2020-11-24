# ReadyCLI: A framework for quick building of command-line interfaces in Java
The next two sections go directly to the core, showing you how to do argument parsing and how to build CLIs with ReadyCLI through two examples. The other sections, describe the main ideas behind ReadyCLI and introduce the main concepts.

## Overview example about argument parsing
This overview example shows you how to parse command-line arguments.
Just define your arguments and options, then write your code in a Lambda expression that can access them in a ready-to-use data structure.

~~~
package examples.readycli.overview;

import readycli.Command;
import readycli.Option;

public class OverviewArgs {
	public static void main(String[] args) {
		Command.forMain("command-name", "command-description")
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
~~~

The followings are samples of command-line to use the command parser defined above:

~~~
# set myflag to true and set parameters of myoption
java -cp overview-args.jar examples.readycli.overview.OverviewArgs path/to/a/file --myflag --myoption "p1 value" p2-value

# show the command help
java -cp overview-args.jar examples.readycli.overview.OverviewArgs ?

# executes the sub-command "my-sub-command"
java -cp overview-args.jar examples.readycli.overview.OverviewArgs my-sub-command path/to/a/text-file

~~~

## Overview example about CLI building
This overview example shows you how to build a CLI in a very fast way.
Just define your commands and what they do through Lambda expressions.

~~~
package examples.readycli.overview;

import readycli.CLI;
import readycli.Command;
import readycli.Option;

public class OverviewCLI {
	public static void main(String[] args) {
		CLI cli = CLI.create("CLI Overview", "insert a command> ");

		cli.addCommand(Command.forCLI("my-command", "A command for my CLI")
				.addOption(Option.create("opt", "An option of my-command")
						.addAlias("o")
						.addParameter("my-param", "A parameter for --opt", "my default value")
						.build())
				.build(context -> { // build the "my-command" command
					context.out.println("arguments: " + context.arguments); // print required arguments
					context.out.println("options: " + context.options); // print options
				})) // end of "my-command" command
				.addCommand(Command.forCLI("set-prompt", "Set a prompt for my CLI")
						.addRequiredArgument("new-prompt", "The new prompt to set")
						.build(context -> { // build the "set-prompt" command
							String newPrompt = context.getArgument("new-prompt");
							// access the CLI itself and modifies it from this lambda
							cli.setCommandPrompt(newPrompt + "> ");
						})) // end of "set-prompt" command
				.addCommand(Command.forCLI("exit", "Exits the program")
						.build(context -> System.exit(0))); // end of exit command

		cli.execute(); // executes the CLI on the standard I/O
	}
}
~~~

The following is an execution example of the CLI shown above:

~~~
CLI Overview
Insert '?' to show available commands
insert a command> ?
Commands:
	exit: Exits the program; doc-options: [--help, -h, ?]
	my-command: A command for my CLI; doc-options: [--help, -h, ?]
	set-prompt: Set a prompt for my CLI; doc-options: [--help, -h, ?]
To see detailed documentation about a command, type one of the strings indicated in the 'doc-options' list of that command.

insert a command> my-command
arguments: {}
options: {opt={flag=false, parameters={my-param=my default value}}}

insert a command> my-command --opt 25
arguments: {}
options: {opt={flag=true, parameters={my-param=25}}}

insert a command> my-command ?
Name and description:
	my-command - A command for my CLI

Usage:
	my-command [--opt <my-param>]

Options:
	--help, -h, ?:  Shows the documentation of this command

	--opt, -o:  An option of my-command
		required option parameters:
		(1)	my-param:  A parameter for --opt (deafult value: "my default value")


insert a command> exit

~~~

## The main idea of ReadyCLI
The main idea of ReadyCLI is to exploits new programming models and features to let the developers to create CLIs and command parsers in a smart way, without losing the focus on more important features of the program that they are developing on. To reach this target, ReadyCLI largely uses the Builder design pattern and Lambda expressions, in such a way the developer can define its CLI or command parser in a very compact and essential way, but giving the opportunity to extend them in a second moment. In this way, also a simple test main method is ready to accept arguments from command-line or it can have a simple CLI.

ReadyCLI is very easy and intuitive to use, but it is also very powerful. ReadyCLI also allows you to write very complex CLIs and command parsers.

## Main elements of a command: Arguments, Options, Sub-commands and CommandExecutor
A command should parse two main types of elements:

- Required arguments: are those values that are necessary to the execution of the command (e.g. the source file name and the destination file name for a file copy command);
- Options: allows to specify other parameters or flags (e.g. to say if the source file should be removed after a file copy); these parameters always have a predetermined default value used in the case they are not specified by the user.

ReadyCLI allows to create both required arguments and options, and to store them in a simple data structure to be accessed easily, allowing to setup default values and to create proper documentation for each of them. The data structure which will contain all the parsed arguments and options is called Command Context. It represents the runtime context of a command, that is composed by some elements such as the actual values of arguments and options parameters, the input and output streams used to interact with the user (who can be on the standard output, on a socket, etc.).

ReadyCLI has the concept of nested commands. A command can be a sub-command for another one, i.e. the super-command. The sub-command can be invoked by typing its name just after the super-command name.

The [Command Context](https://sv-giampa.github.io/ReadyCLI/readycli/CommandContext.html) built during the command-line parsing is then passed to the [Command Executor](https://sv-giampa.github.io/ReadyCLI/readycli/CommandExecutor.html), that is a developer-defined object that uses the Command Context and executes the code of the command. The [Command Executor](https://sv-giampa.github.io/ReadyCLI/readycli/CommandExecutor.html) can be specified as a Lambda Expression accepting the Command Context as single argument. It is specified at the moment the command is built (see examples above).

# Full documentation
The full documentation of the library can be found in the  __docs__  folder or in the [GitHub Pages](https://sv-giampa.github.io/ReadyCLI/index.html) of this repository.
