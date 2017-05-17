package com.earthgee.library.core;

/**
 * Created by zhaoruixuan on 2017/4/26.
 */
public class Env {

    public static final String EXTRA_TARGET_AUTHORITY="TargetAuthority";
    //在startActivity进行替换时保存原有的intent
    public static final String EXTRA_TARGET_INTENT="com.earthgee.plugin2.OldIntent";
    public static final String EXTRA_TARGET_INTENT_URI="com.earthgee.plugin2.OldIntent.Uri";
    public static final String EXTRA_TARGET_INFO="com.earthgee.plugin2.OldInfo";
    public static final String EXTRA_STUB_INFO="com.earthgee.plugin2.NewInfo";

}
