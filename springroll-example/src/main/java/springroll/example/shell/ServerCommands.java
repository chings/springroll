package springroll.example.shell;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.commands.Quit;

@ShellComponent
public class ServerCommands implements Quit.Command {

    @ShellMethod("Shut the server down.")
    public void shutdown() {
        System.exit(0);
    }

}