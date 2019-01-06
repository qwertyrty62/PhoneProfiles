package com.thelittlefireman.appkillermanager.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.thelittlefireman.appkillermanager.utils.ActionsUtils;
import com.thelittlefireman.appkillermanager.utils.Manufacturer;

public class HTC extends DeviceAbstract {

    private static final String HTC_PITROAD_PACKAGE_NAME = "com.htc.pitroad";
    private static final String HTC_PITROAD_POWERSAVING = " com.htc.pitroad.landingpage.activity.LandingPageActivity";

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.HTC;
    }

    @Override
    public boolean isActionPowerSavingAvailable(Context context) {
        return ActionsUtils.isIntentAvailable(context, getActionPowerSaving(context));
    }

    @Override
    public boolean isActionAutoStartAvailable(Context context) {
        return ActionsUtils.isIntentAvailable(context, getActionAutoStart(context));
    }

    @Override
    public boolean isActionNotificationAvailable(Context context) {
        return ActionsUtils.isIntentAvailable(context, getActionNotification(context));
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(HTC_PITROAD_PACKAGE_NAME,HTC_PITROAD_POWERSAVING));
        return intent;
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        return null;
    }

    @Override
    public Intent getActionNotification(Context context) {
        return null;
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        // TODO
        return null;
    }

    @Override
    public int getHelpImagePowerSaving() {
        return 0;
    }

    @Override
    public boolean needToUseAlongwithActionDoseMode(){
        return true;
    }
}
