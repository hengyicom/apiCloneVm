package vcenter;

import com.vmware.vim25.PropertySpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class PropertySpecBuilder
        extends PropertySpec {
    private void init() {
        if (this.pathSet == null) {
            this.pathSet = new ArrayList();
        }
    }

    public PropertySpecBuilder all(Boolean all) {
        this.setAll(all);
        return this;
    }

    public PropertySpecBuilder type(String type) {
        this.setType(type);
        return this;
    }

    public /* varargs */ PropertySpecBuilder pathSet(String ... paths) {
        this.init();
        this.pathSet.addAll(Arrays.asList(paths));
        return this;
    }

    public PropertySpecBuilder addToPathSet(Collection<String> paths) {
        this.init();
        this.pathSet.addAll(paths);
        return this;
    }
}