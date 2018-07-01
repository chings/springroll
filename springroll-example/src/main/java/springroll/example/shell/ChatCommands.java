package springroll.example.shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

@ShellComponent
public class ChatCommands {
    private static Logger log = LoggerFactory.getLogger(ChatCommands.class);

    @ShellMethod("Join a/the ChatActor0")
    public void join(String memberId, @ShellOption(defaultValue = "") String channelId) {
        if(StringUtils.hasText(channelId)) {
            log.info("Joining the chat '{}'", channelId);
        } else {
            log.info("Joining a chat");
        }
    }

}
