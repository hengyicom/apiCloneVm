package vcenter;

import com.vmware.vim25.*;
import entity.VcenterEntity;
import vcenter.AccessResources.GetSource;

import java.util.Map;


public class CloneVm {
    LocalVcenterOperator localVcenterOperator = new LocalVcenterOperator(VcenterEntity.getVcenterName(), VcenterEntity.getVcenterPassWald(), VcenterEntity.getVcenterUrl());
    ServiceConnection serviceConnection;
    {
        try {
            serviceConnection = localVcenterOperator.content();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void CloneVM() throws Exception {
        GetSource getSources =  GetSource.getInstance(serviceConnection);
        String dataCenterName = "Datacenter1";
        String dataCenterHost = "10.1.100.116";
        VimPortType vimPort = serviceConnection.getService();
        String crPathName = String.valueOf(dataCenterName) + "/host/" + dataCenterHost;
        String hostPathName = crPathName + "/" + dataCenterHost;
        String poolPathName = String.valueOf(crPathName) + "/Resources";
        ManagedObjectReference poolRef = serviceConnection.getService().findByInventoryPath(serviceConnection.getServiceContent().getSearchIndex(), poolPathName);
        Map<String, ManagedObjectReference> vmMap = getSources.inContainerByType(serviceConnection.getServiceContent().getRootFolder(), "VirtualMachine");
        String sourceName = "dtt_nve_suse_11sp4_x86_64";
        ManagedObjectReference vmRef = null;
        for (String vmName : vmMap.keySet() ){
            if (vmName.contains(sourceName)){
                vmRef = vmMap.get(sourceName);
            }
        }
        VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
        VirtualMachineRelocateSpec relocateSpec = new VirtualMachineRelocateSpec();
        ManagedObjectReference drRef = null;
        ManagedObjectReference crRef = serviceConnection.getService().findByInventoryPath(serviceConnection.getServiceContent().getSearchIndex(), crPathName);
        ManagedObjectReference hostRef = serviceConnection.getService().findByInventoryPath(serviceConnection.getServiceContent().getSearchIndex(), hostPathName);
        for (VirtualMachineDatastoreInfo e : getSources.getConfigTargetForHost(crRef, hostRef).getDatastore()) {
            drRef = e.getDatastore().getDatastore();
        }
        relocateSpec.setDatastore(drRef);
        relocateSpec.setPool(poolRef);
        cloneSpec.setLocation(relocateSpec);
        cloneSpec.setPowerOn(false); // 克隆时不启动虚拟机
        cloneSpec.setTemplate(false); // 克隆虚拟机为普通虚拟机，而不是模板
        ManagedObjectReference targetFolder = serviceConnection.getService().findByInventoryPath(serviceConnection.getServiceContent().getSearchIndex(), String.valueOf(dataCenterName) + "/vm");
        ManagedObjectReference cloneTask = serviceConnection.getService().cloneVMTask(vmRef, targetFolder, "cloneVmName", cloneSpec);

        cloneTask.wait(60);
    }
}