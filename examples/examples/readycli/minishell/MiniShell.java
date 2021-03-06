package examples.readycli.minishell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import readycli.CLI;
import readycli.Command;
import readycli.Option;

public class MiniShell {

	private static File currentDir = new File(".");

	public static void main(String[] args) throws IllegalStateException, IOException {
		CLI cli = CLI.create("MiniShell v1.0.0");

		cli.setCommandPrompt(currentDir.getCanonicalPath() + "> ")
				.addCommand("cd", Command.defaultDocs("cd", "Changes the current directory")
						.addRequiredArgument("path", "The new current directory path")
						.build(ctx -> {
							Path path = Paths.get(ctx.getArgument("path"));
							File newDirFile = currentDir.toPath()
									.resolve(path)
									.toFile();
							if (newDirFile.isDirectory()) {
								try {
									String newCurrentDir = newDirFile.getCanonicalPath();
									currentDir = new File(newCurrentDir);
									cli.setCommandPrompt(newCurrentDir + "> ");
								} catch (IOException e) {
									ctx.out.printf("Error: some error occurred.\n", path);
									e.printStackTrace();
								}
							} else {
								ctx.out.printf("Error: the path '%s' is not an existing directory.\n", path);
							}
						}))
				.addCommand("touch", Command.defaultDocs("touch", "Creates a new empty file")
						.addRequiredArgument("path", "The file path")
						.build(ctx -> {
							String path = ctx.getArgument("path");
							File file = currentDir.toPath()
									.resolve(path)
									.toFile();
							boolean result = false;
							String filePath = path;
							if (!file.exists()) {
								try {
									result = file.createNewFile();
									filePath = file.getCanonicalPath();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							if (result) {
								ctx.out.printf("New file created: %s\n", filePath);
							} else {
								ctx.out.println("Error: the file cannot be created for some reason.");
							}
						}))
				.addCommand("ls", Command.defaultDocs("ls", "List files in the current directory")
						.build(ctx -> {
							for (File file : currentDir.listFiles())
								ctx.out.printf("\t%s\t%s\n",
										file.isFile() ? "FILE" : file.isDirectory() ? "DIR" : "DEV", file.getName());
						}))
				.addCommand("exit", Command.defaultDocs("exit", "Exits from MiniShell")
						.addOption(Option.create("status", "Specifies the exit status")
								.addAlias("s")
								.addParameter("value", "Exit value", "0")
								.build())
						.build(context -> System.exit(0))) // end of exit command
				.addCommand("exec", Command.defaultDocs("exec", "Executes a command for the native runtime")
						.addRequiredArgument("command", "The command to execute")
						.build(ctx -> {
							String command = ctx.getArgument("command");

							try {
								Process process = Runtime.getRuntime()
										.exec(command, null, currentDir);
								// stdin
								StreamCarrier outThread = new StreamCarrier(ctx.in, process.getOutputStream());
								// stdout
								StreamCarrier inThread = new StreamCarrier(process.getInputStream(), ctx.out);
								// stderr
								StreamCarrier errThread = new StreamCarrier(process.getErrorStream(), ctx.out);

								process.waitFor();

								outThread.stop();
								inThread.stop();
								errThread.stop();

							} catch (IOException e) {
								ctx.out.println("Error executing the command");
								e.printStackTrace();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}));

		cli.execute();
	}
}
