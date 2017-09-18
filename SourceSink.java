package analysis;



import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this class represents a source/sink definition in HTML taint analysis
 */
public class SourceSink implements JSTaintNode {
    public final String signature;
    public final boolean isSource;
    public final String typeTag;
    private final String[] tags;

    private static final String patternStr = "^HTML <\\((.*)\\) (.*)> -> _(SOURCE|SINK)_$";
    private static final Pattern pattern = Pattern.compile(patternStr);

    public SourceSink(String signature) {
        this.signature = signature.trim();

        Matcher m = pattern.matcher(this.signature);
        if (m.find()) {
            this.typeTag = m.group(1);
            this.tags = m.group(2).split(",");
            this.isSource = m.group(3).equals("SOURCE");
        }
        else {
            System.out.println("invalid source/sink definition: " + this.signature);
            this.typeTag = null;
            this.tags = new String[]{};
            this.isSource = true;
        }
    }

    public boolean matches(HashSet<String> matchTags) {
        if (this.tags.length == 0 || matchTags == null)
            return false;
        for (String tag : tags) {
        	//System.out.println(tag);
            boolean matched = false;
            for (String matchTag : matchTags) {
            	if (matchTag.contains(tag)) {
            		//System.out.println(matchTag);
                    
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
            //else return true;
        }
        //System.out.println("hiiiiiiiiiiii");
        return true;
    }

    public boolean isArgs() {
        return "ARGS".equals(this.typeTag);
    }

    public boolean isRet() {
        return "RET".equals(this.typeTag);
    }

    public static void main(String[] args) {
        String line = "HTML <(ARGS) window,console> -> _SINK_";
        Matcher m = pattern.matcher(line);
        if (m.find()) {
            System.out.println(m.group(0));
            System.out.println(m.group(1));
            System.out.println(m.group(2));
            System.out.println(m.group(3));
        }
    }

    public String toString() {
        return String.format("{{%s}}", this.signature);
    }
}
