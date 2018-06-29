package springroll.example.shell;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class TestCommands {

    @ShellMethod("Prints what has been entered.")
    public String echo(String what) {
        return "You said " + what;
    }

    @ShellMethod("Add two integers together.")
    public int add(int a, int b) {
        return a + b;
    }

}
