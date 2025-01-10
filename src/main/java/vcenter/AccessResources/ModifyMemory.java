package vcenter.AccessResources;

import com.vmware.vim25.*;
import vcenter.ObjectSpecBuilder;
import vcenter.PropertyFilterSpecBuilder;
import vcenter.PropertySpecBuilder;
import vcenter.ServiceConnection;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ModifyMemory {
    private final ServiceConnection serviceConnection;

    public ModifyMemory(ServiceConnection serviceConnection) {
        this.serviceConnection = serviceConnection;
    }

    /**
     * 设置虚机的内存，cpu
     * @param vmMor
     * @throws Exception
     */
   public void reconfigDeviceSetting(ManagedObjectReference vmMor) throws Exception{
        VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
        int memorySize = Integer.parseInt("4");
        vmConfigSpec.setMemoryMB((long) (1024 * memorySize));
        vmConfigSpec.setNumCPUs(Integer.parseInt("4"));
        vmConfigSpec.setNumCoresPerSocket(Integer.parseInt("4"));
        reConfig(vmMor,vmConfigSpec);
    }

    /**
     * 在vcenter根据虚机名字得到虚机
     * @param name 克隆的虚机名
     * @return 如果这个名字在vcenter中返回该名字
     */
    public ManagedObjectReference getVmByName(String name) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        GetSource getSources =  GetSource.getInstance(serviceConnection);
        ManagedObjectReference vm = null;
        Map<String, ManagedObjectReference> vmMap = getSources.inContainerByType(serviceConnection.getServiceContent().getRootFolder(), "VirtualMachine");
        for (String vmName : vmMap.keySet()) {
            if (!vmName.contains(name)) continue;
            vm = vmMap.get(vmName);
        }
        return vm;
    }

    /**
     * 开始修改并等待完成
     * @param virtualMachine
     * @param vmConfigSpec
     */
   public void reConfig(ManagedObjectReference virtualMachine, VirtualMachineConfigSpec vmConfigSpec) throws InvalidPropertyFaultMsg, DuplicateNameFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidNameFaultMsg, InvalidCollectorVersionFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference tmor = serviceConnection.getService().reconfigVMTask(virtualMachine, vmConfigSpec);
        WaitCloneVm waitCloneVm = new WaitCloneVm(serviceConnection);
        if (waitCloneVm.getTaskResultAfterDone(tmor)) {
            System.out.print("Virtual Machine reconfigured successfully");
        } else {
            System.out.print("Virtual Machine reconfigur failed");
        }
    }

    /**
     * 创建磁盘
     * @param vmName 虚机名称
     * @param dataStoreName  存储位置
     * @param diskSizeMB 磁盘大小
     * @param diskNumber 磁盘数量
     * @param prefixFileName 磁盘名称
     * @return 是否成功
     */

    public boolean createDisk(String vmName, String dataStoreName, long diskSizeMB, int diskNumber, String prefixFileName) {
        ArrayList<VirtualDeviceConfigSpec> vdiskSpecs = new ArrayList<VirtualDeviceConfigSpec>();
        VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
        GetSource getSource = new GetSource(serviceConnection);
        try {
            ManagedObjectReference vmRef = getVmByName(vmName);
            List<VirtualDisk> disks = ((ArrayOfVirtualDevice) getSource.entityProps(vmRef, new String[] { "config.hardware.device" })
                    .get("config.hardware.device")).getVirtualDevice().stream().filter(d -> d instanceof VirtualDisk)
                    .map(d -> (VirtualDisk) d).collect(Collectors.toList());
            for(int i = 0; i < diskNumber; i++) {
                Thread.sleep(100);
                VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();
                diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.CREATE);
                diskSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
                VirtualDisk newDisk = new VirtualDisk();
                VirtualDiskFlatVer2BackingInfo diskfileBacking = new VirtualDiskFlatVer2BackingInfo();
                String diskFileName = getVolumeName(dataStoreName) + " " + vmName + "/" + (prefixFileName.isEmpty() ? "dataDisk" : prefixFileName) +
                        new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()) + ".vmdk";
                diskfileBacking.setFileName(diskFileName);
                diskfileBacking.setDiskMode("persistent");
                diskfileBacking.setEagerlyScrub(false);
                diskfileBacking.setThinProvisioned(true);  //精简置备
                newDisk.setKey(disks.get(disks.size()-1).getKey() + i + 1);
                newDisk.setControllerKey(disks.get(0).getControllerKey());
                newDisk.setUnitNumber(disks.get(disks.size() - 1).getUnitNumber() + i + 1);
                newDisk.setBacking(diskfileBacking);
                newDisk.setCapacityInKB(diskSizeMB * 1024 * 1024);
                diskSpec.setDevice(newDisk);
                vdiskSpecs.add(diskSpec);
            }
            vmConfigSpec.getDeviceChange().addAll(vdiskSpecs);
            reConfig(vmRef, vmConfigSpec);
            return true;
        } catch(Exception ex) {
            System.out.print(ex);
        }
        return false;
    }
    String getVolumeName(String volName) {
        String volumeName = null;
        volumeName = volName != null && volName.length() > 0 ? "[" + volName + "]" : "[Local]";
        return volumeName;
    }


}
