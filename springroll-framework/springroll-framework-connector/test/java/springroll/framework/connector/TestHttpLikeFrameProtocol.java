package springroll.framework.connector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springroll.framework.TestFlux;

@RunWith(JUnit4.class)
public class TestHttpLikeFrameProtocol {
    private static Logger log = LoggerFactory.getLogger(TestHttpLikeFrameProtocol.class);

    HttpLikeFrameProtocol httpLikeFrameProtocol = new HttpLikeFrameProtocol();

    String sample = "ASK /chats/80\r\n" +
            "Serial-No: 1\r\n" +
            "Content-Length: 18\r\n" +
            "Content-Type: application/json\r\n" +
            "Content-Class: chat.Hello\r\n" +
            "\r\n" +
            "{content:\"Hello!\"}";

    @Test
    public void test1() {
        Frame frame = httpLikeFrameProtocol.unseralize(sample);
        log.info(frame.toString());
        String s = httpLikeFrameProtocol.serialize(frame);
        log.info(s);
    }

}
