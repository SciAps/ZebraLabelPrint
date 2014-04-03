package com.sciaps.android.zebralabelprint.zebraprint;

/**
* Created by MonkeyFish on 3/7/14.
*/
public class DeviceListItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_PAIRED = 1;
    public static final int TYPE_FOUND = 2;

    private final String text;
    private final int type;
    private final int resId;
    private final String address;

    public String getAddress() {
        return address;
    }



    public DeviceListItem(String text, int res){
        this.text =text;
        this.resId = res;
        this.type = TYPE_HEADER;
        this.address = "";
    }

    public DeviceListItem(String text, String address, int res){
        this.text =text;
        this.resId = res;
        this.type = TYPE_FOUND;
        this.address = address;
    }

    public String getText() {
        return text;
    }

    public int getType() {
        return type;
    }

    public int getResId() {
        return resId;
    }
}
