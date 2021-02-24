package com.enliple.pudding.commons.network;

public class NetworkBusResponse {
    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;

    public NetworkBusResponse(String arg1, String arg2) {
        this(arg1, arg2, "");
    }

    public NetworkBusResponse(String arg1, String arg2, String arg3) {
        this(arg1, arg2, arg3, "");
    }

    public NetworkBusResponse(String arg1, String arg2, String arg3, String arg4) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
        this.arg4 = arg4;
    }
}