package springroll.framework.connector.util;

public class Classes {

    public static Class<?> guess(String type) {
        try {
            return Class.forName(type);
        } catch(ClassNotFoundException e) {
            return null;
        }
    }

}
