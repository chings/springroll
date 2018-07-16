package springroll.framework.connector.util;

import org.springframework.util.StringUtils;

public class Classes {

    public static Class<?> guess(String contentClass, String namespace) {
        if(StringUtils.hasText(namespace)) try {
            return Class.forName(namespace + "." + contentClass);
        } catch(ClassNotFoundException x) {
            //silently
        }
        try {
            return Class.forName(contentClass);
        } catch(ClassNotFoundException x1) {
            //silently
        }
        return null;
    }

}
