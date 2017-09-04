package com.example.azure.wdmedia;

/**
 * Created by baiqiaoyu on 2017/6/5.
 */

        import java.io.Serializable;
        import java.security.Provider;

/**
 * Created by joy_white on 2017/2/22.
 */

public class ServiceInfo implements Serializable{
    private String funcname;
    private String D_ID;
    private String D_device;
    private String R_ID;
    private String other;
    private byte [] data;

    public ServiceInfo(){}

    public ServiceInfo(String funcname, String D_ID, String D_device, String R_ID, String other, byte  [] data){
        this.funcname = funcname;
        this.D_ID = D_ID;
        this.D_device = D_device;
        this.R_ID = R_ID;
        this.other = other;
        this.data = data;
    }

    public ServiceInfo setfuncname(String funcname){
        this.funcname = funcname;
        return this;
    }

    public ServiceInfo setDID(String D_ID){
        this.D_ID = D_ID;
        return this;
    }

    public ServiceInfo setDdevice(String D_device){
        this.D_device = D_device;
        return this;
    }

    public ServiceInfo setRID(String R_ID){
        this.R_ID = R_ID;
        return this;
    }

    public ServiceInfo setOther(String other){
        this.other = other;
        return this;
    }

    public ServiceInfo setData(byte [] data){
        this.data = data;
        return this;
    }

    public String getFuncname(){
        return this.funcname;
    }

    public String getD_ID(){
        return this.D_ID;
    }

    public String getD_device(){
        return this.D_device;
    }

    public String getR_ID(){
        return this.R_ID;
    }

    public String getOther(){
        return this.other;
    }

    public byte[] getData(){ return this.data; }


}

