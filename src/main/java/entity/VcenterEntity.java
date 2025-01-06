package entity;

import jdk.nashorn.internal.objects.annotations.Getter;
import jdk.nashorn.internal.objects.annotations.Setter;

public class VcenterEntity {
    private static String vcenterUrl;
    private static String VcenterName;

    public static String getVcenterUrl() {
        return vcenterUrl;
    }

    public void setVcenterUrl(String vcenterUrl) {
        VcenterEntity.vcenterUrl = vcenterUrl;
    }

    public static String getVcenterName() {
        return VcenterName;
    }

    public void setVcenterName(String vcenterName) {
        VcenterName = vcenterName;
    }

    public static String getVcenterPassWald() {
        return VcenterPassWald;
    }

    public void setVcenterPassWald(String vcenterPassWald) {
        VcenterPassWald = vcenterPassWald;
    }

    private static String VcenterPassWald;
}
