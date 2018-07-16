package springroll.framework.connector;

public interface FrameProtocol {

    Frame marshal(Object message);

    Object unmarshal(Frame frame, String namespace);

    String serialize(Frame frame);

    Frame unseralize(String serialized);

}
