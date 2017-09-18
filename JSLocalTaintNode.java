package analysis;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.collections.Pair;

/**
 * local variable in js code as TaintNode
 */
public class JSLocalTaintNode implements JSTaintNode {
    public Pair<MethodReference, Integer> value;

    public JSLocalTaintNode(Pair<MethodReference, Integer> value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JSLocalTaintNode)) return false;
        if (value == null) return ((JSLocalTaintNode) o).value == null;
        return value.equals(((JSLocalTaintNode) o).value);
    }

    public String toString() {
        return String.format("{{local:%d|%s}}", value.snd, value.fst.getSignature());
    }
}
