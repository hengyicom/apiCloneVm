package vcenter;

import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.TraversalSpec;

import java.util.ArrayList;
import java.util.Arrays;

public class TraversalSpecBuilder
        extends TraversalSpec {
    private void init() {
        if (this.selectSet == null) {
            this.selectSet = new ArrayList();
        }
    }

    public TraversalSpecBuilder name(String name) {
        this.setName(name);
        return this;
    }

    public TraversalSpecBuilder path(String path) {
        this.setPath(path);
        return this;
    }

    public TraversalSpecBuilder skip(Boolean skip) {
        this.setSkip(skip);
        return this;
    }

    public TraversalSpecBuilder type(String type) {
        this.setType(type);
        return this;
    }

    public /* varargs */ TraversalSpecBuilder selectSet(SelectionSpec... selectionSpecs) {
        this.init();
        this.selectSet.addAll(Arrays.asList(selectionSpecs));
        return this;
    }
}
