package vcenter;

import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;

import java.util.ArrayList;
import java.util.Arrays;

public class PropertyFilterSpecBuilder
        extends PropertyFilterSpec {
    private void init() {
        if (this.propSet == null) {
            this.propSet = new ArrayList();
        }
        if (this.objectSet == null) {
            this.objectSet = new ArrayList();
        }
    }

    public PropertyFilterSpecBuilder reportMissingObjectsInResults(Boolean value) {
        this.setReportMissingObjectsInResults(value);
        return this;
    }

    public /* varargs */ PropertyFilterSpecBuilder propSet(PropertySpec... propertySpecs) {
        this.init();
        this.propSet.addAll(Arrays.asList(propertySpecs));
        return this;
    }

    public /* varargs */ PropertyFilterSpecBuilder objectSet(ObjectSpec... objectSpecs) {
        this.init();
        this.objectSet.addAll(Arrays.asList(objectSpecs));
        return this;
    }
}
