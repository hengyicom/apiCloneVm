package vcenter;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.SelectionSpec;

import java.util.ArrayList;
import java.util.Arrays;

public class ObjectSpecBuilder
        extends ObjectSpec {
    private void init() {
        if (this.selectSet == null) {
            this.selectSet = new ArrayList<>();
        }
    }

    public ObjectSpecBuilder obj(ManagedObjectReference objectReference) {
        this.setObj(objectReference);
        return this;
    }

    public ObjectSpecBuilder skip(Boolean skip) {
        this.setSkip(skip);
        return this;
    }

    public /* varargs */ ObjectSpecBuilder selectSet(SelectionSpec... selectionSpecs) {
        this.init();
        this.selectSet.addAll(Arrays.asList(selectionSpecs));
        return this;
    }
}