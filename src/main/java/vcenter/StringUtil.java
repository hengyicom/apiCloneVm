package vcenter;

public class StringUtil {
    public static boolean isEmpty(String s) {
        boolean empty = (s == null || s.trim().isEmpty());
        return empty;
    }
}
