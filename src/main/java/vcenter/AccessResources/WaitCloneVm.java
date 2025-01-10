package vcenter.AccessResources;

import com.vmware.vim25.*;
import org.w3c.dom.Element;
import vcenter.ServiceConnection;

import java.util.Arrays;
import java.util.List;

public class WaitCloneVm {
    private final ServiceConnection serviceConnection;

    public WaitCloneVm(ServiceConnection serviceConnection) {
        this.serviceConnection = serviceConnection;
    }

    public boolean getTaskResultAfterDone(ManagedObjectReference task) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg {
        boolean retVal = false;
        Object[] result = wait(task, new String[]{"info.state", "info.error"}, new String[]{"state"}, new Object[][]{{TaskInfoState.SUCCESS, TaskInfoState.ERROR}});
        if (result[0].equals(TaskInfoState.SUCCESS)) {
            retVal = true;
        }
        if (result[1] instanceof LocalizedMethodFault) {
            throw new RuntimeException(((LocalizedMethodFault)result[1]).getLocalizedMessage());
        }
        return retVal;
    }

    /**
     * 监控对象的属性变化，直到满足指定的条件
     * @param objmor 要监控的对象
     * @param filterProps 需要监控的属性列表
     * @param endWaitProps 用于判断是否结束等待的属性列表
     * @param expectedVals 期望的属性值
     * @return 包含监控结果的数组
     */
    public Object[] wait(ManagedObjectReference objmor, String[] filterProps, String[] endWaitProps, Object[][] expectedVals) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg {
        ManagedObjectReference filterSpecRef = null;
        String version = "";
        Object[] endVals = new Object[endWaitProps.length];
        Object[] filterVals = new Object[filterProps.length];
        String stateVal = null;
        PropertyFilterSpec spec = this.propertyFilterSpec(objmor, filterProps);
        filterSpecRef = serviceConnection.getService().createFilter(serviceConnection.getServiceContent().getPropertyCollector(), spec, true);
        boolean reached = false;
        UpdateSet updateset = null;
        List<PropertyFilterUpdate> filtupary = null;
        List<ObjectUpdate> objupary = null;
        List<PropertyChange> propchgary = null;
        while (!reached) {
            updateset = serviceConnection.getService().waitForUpdatesEx(serviceConnection.getServiceContent().getPropertyCollector(), version, new WaitOptions());
            if (updateset == null || updateset.getFilterSet() == null) continue;
            version = updateset.getVersion();
            filtupary = updateset.getFilterSet();
            for (PropertyFilterUpdate filtup : filtupary) {
                objupary = filtup.getObjectSet();
                for (ObjectUpdate objup : objupary) {
                    if (objup.getKind() != ObjectUpdateKind.MODIFY && objup.getKind() != ObjectUpdateKind.ENTER && objup.getKind() != ObjectUpdateKind.LEAVE) continue;
                    propchgary = objup.getChangeSet();
                    for (PropertyChange propchg : propchgary) {
                        this.updateValues(endWaitProps, endVals, propchg);
                        this.updateValues(filterProps, filterVals, propchg);
                    }
                }
            }
            Object expctdval = null;
            for (int chgi = 0; chgi < endVals.length && !reached; ++chgi) {
                for (int vali = 0; vali < expectedVals[chgi].length && !reached; ++vali) {
                    expctdval = expectedVals[chgi][vali];
                    if (endVals[chgi] == null) continue;
                    if (endVals[chgi].toString().contains("val: null")) {
                        Element stateElement = (Element)endVals[chgi];
                        if (stateElement == null || stateElement.getFirstChild() == null) continue;
                        stateVal = stateElement.getFirstChild().getTextContent();
                        reached = expctdval.toString().equalsIgnoreCase(stateVal) || reached;
                        continue;
                    }
                    expctdval = expectedVals[chgi][vali];
                    reached = expctdval.equals(endVals[chgi]) || reached;
                    stateVal = "filtervals";
                }
            }
        }
        Object[] retVal = null;
        try {
            serviceConnection.getService().destroyPropertyFilter(filterSpecRef);
        }
        catch (RuntimeFaultFaultMsg e) {
            e.printStackTrace();
        }
        if (stateVal != null) {
            if (stateVal.equalsIgnoreCase("ready")) {
                retVal = new Object[]{HttpNfcLeaseState.READY};
            }
            if (stateVal.equalsIgnoreCase("error")) {
                retVal = new Object[]{HttpNfcLeaseState.ERROR};
            }
            if (stateVal.equals("filtervals")) {
                retVal = filterVals;
            }
        } else {
            retVal = new Object[]{HttpNfcLeaseState.ERROR};
        }
        return retVal;
    }

    /**
     * 更新属性值数组
     * @param props 属性名称列表。
     * @param vals 属性值数组
     * @param propchg 属性变化对象
     */
    void updateValues(String[] props, Object[] vals, PropertyChange propchg) {
        for (int findi = 0; findi < props.length; ++findi) {
            if (propchg.getName().lastIndexOf(props[findi]) < 0) continue;
            vals[findi] = propchg.getOp() == PropertyChangeOp.REMOVE ? "" : propchg.getVal();
        }
    }

    /**
     * 创建属性过滤器规范。
     * @param objmor 要监控的对象
     * @param filterProps 需要监控的属性列表
     */
    public PropertyFilterSpec propertyFilterSpec(ManagedObjectReference objmor, String[] filterProps) {
        PropertyFilterSpec spec = new PropertyFilterSpec();
        ObjectSpec oSpec = new ObjectSpec();
        oSpec.setObj(objmor);
        oSpec.setSkip(Boolean.FALSE);
        spec.getObjectSet().add(oSpec);
        PropertySpec pSpec = new PropertySpec();
        pSpec.getPathSet().addAll(Arrays.asList(filterProps));
        pSpec.setType(objmor.getType());
        spec.getPropSet().add(pSpec);
        return spec;
    }

}
