package org.example;

import com.vmware.vim25.*;
import entity.VcenterEntity;
import vcenter.CloneVm;

public class Main {
    public static void main(String[] args) throws Exception {
        VcenterEntity vcenterEntity = new VcenterEntity();
        vcenterEntity.setVcenterName("Administrator@VSPHERE.LOCAL");
        vcenterEntity.setVcenterPassWald("Dtt@2021");
        vcenterEntity.setVcenterUrl("https://10.1.100.121/sdk/vimService");
        CloneVm cloneVm = new CloneVm();
        cloneVm.CloneVM();
    }
}