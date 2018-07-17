package springroll.framework.connector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class TestSpringRollProtocol {
    private static Logger log = LoggerFactory.getLogger(TestSpringRollProtocol.class);

    SpringRollFrameProtocol springRollFrameProtocol = new SpringRollFrameProtocol();

    String sample = "ASK /chats/80\r\n" +
            "Serial-No: 1\r\n" +
            "Content-Length: 18\r\n" +
            "Content-Type: application/json\r\n" +
            "Content-Class: chat.Hello\r\n" +
            "\r\n" +
            "{content:\"Hello!\"}";

    @Test
    public void test1() {
        Frame frame = springRollFrameProtocol.unseralize(sample);
        log.info(frame.toString());
        String s = springRollFrameProtocol.serialize(frame);
        log.info(s);
    }

}
