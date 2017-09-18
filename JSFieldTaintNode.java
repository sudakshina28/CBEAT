package analysis;

import com.ibm.wala.types.FieldReference;

/**
 * HTML/js field as TaintNode
 */
public class JSFieldTaintNode implements JSTaintNode {
    public FieldReference value;

    public JSFieldTaintNode(FieldReference field) {
        value = field;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JSFieldTaintNode)) return false;
        if (value == null) return ((JSFieldTaintNode) o).value == null;
        return value.equals(((JSFieldTaintNode) o).value);
    }

    public String toString() {
        return String.format("{{field|%s}}", value.getSignature());
    }
}
