package vcenter.AccessResources;

import com.vmware.vim25.*;
import vcenter.ServiceConnection;

import java.util.Arrays;

public class ModifyNetworkConfig {

    public CustomizationSpec createCustomizationSpec(String hostName, String vmIp) {
        CustomizationSpec spec = new CustomizationSpec();

        // 配置操作系统特定的设置
        configureLinuxSettings(spec, hostName);

        // 配置全局网络设置
        configureGlobalNetworkSettings(spec);

        // 配置网络适配器
        configureNetworkAdapter(spec, vmIp);

        return spec;
    }
    /**
     * 配置 Linux 系统设置
     */
    private void configureLinuxSettings(CustomizationSpec spec, String hostName) {
        CustomizationLinuxPrep sysprep = new CustomizationLinuxPrep();
        CustomizationFixedName fixedName = new CustomizationFixedName();
        fixedName.setName(hostName);
        sysprep.setHostName(fixedName);
        sysprep.setDomain("huxn.com");
        spec.setIdentity(sysprep);
    }
    /**
     * 配置全局网络设置
     */
    private void configureGlobalNetworkSettings(CustomizationSpec spec) {
        CustomizationGlobalIPSettings globalIPSettings = new CustomizationGlobalIPSettings();
        globalIPSettings.getDnsServerList().add("172.20.64.9");
        spec.setGlobalIPSettings(globalIPSettings);
    }

    /**
     * 配置网络适配器
     */
    private void configureNetworkAdapter(CustomizationSpec spec, String vmIp) {
        CustomizationIPSettings ipSetting = new CustomizationIPSettings();
        ipSetting.getGateway().add("10.1.0.1");
        ipSetting.setSubnetMask("255.255.240.0");

        CustomizationFixedIp ip = new CustomizationFixedIp();
        ip.setIpAddress(vmIp);
        ipSetting.setIp(ip);

        CustomizationAdapterMapping adapter = new CustomizationAdapterMapping();
        adapter.setAdapter(ipSetting);
        spec.getNicSettingMap().add(adapter);
    }

}
