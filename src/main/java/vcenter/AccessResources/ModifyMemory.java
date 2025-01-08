package vcenter.AccessResources;

import com.vmware.vim25.*;
import vcenter.ServiceConnection;

import java.util.Map;

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
        vmConfigSpec.setMemoryMB(new Long(1024 * memorySize));
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

//    public void addDisk(String vmName){
//        VirtualDisk disk = new VirtualDisk();
//        disk.setCapacityInKB(10485760); // 设置磁盘大小为 10 GB（单位：KB）
//        disk.setUnitNumber(1); // 设置磁盘单元号
//        disk.setControllerKey(1000); // 设置控制器键值（通常为 1000）
//
//        // 设置磁盘后端
//        VirtualDiskFlatVer2BackingInfo backingInfo = new VirtualDiskFlatVer2BackingInfo();
//        backingInfo.setFileName("[datastore1] " + vmName + "/newDisk.vmdk"); // 设置磁盘文件路径
//        backingInfo.setDiskMode("persistent"); // 设置磁盘模式
//        disk.setBacking(backingInfo);
//
//        // 创建设备配置规范
//        VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();
//        diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.CREATE);
//        diskSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD); // 添加磁盘
//        diskSpec.setDevice(disk);
//
//        // 创建虚拟机配置规范
//        VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
//        configSpec.getDeviceChange().add(diskSpec);
//        serviceConnection.getService().reconfigVMTask(vmName,configSpec);
//    }
}
