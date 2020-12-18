

package com.secrethq.utils;

public class PTJniHelper {
	public static String password() {
		return "T3bxIMiutloXff4nzai2XR8srSLN/rAKSHzxd8j9vFkYK/AnyK60Ch97qSWc/rVaHnf4IMr84Fwfe6wlm/22Xg==";
	}
	public static native boolean isAdNetworkActive( String name ); 
    public static native String jsSettingsString();
    
    public static native void setSettingsValue(String path, String value);
    public static native String getSettingsValue(String path);
}
