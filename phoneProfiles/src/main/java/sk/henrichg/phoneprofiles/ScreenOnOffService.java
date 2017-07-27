package sk.henrichg.phoneprofiles;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class ScreenOnOffService extends IntentService {

    public ScreenOnOffService() {
        super("ScreenOnOffService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            final Context appContext = getApplicationContext();
            
            //PPApplication.loadPreferences(context);

            if (intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
                    PPApplication.logE("@@@ ScreenOnOffService.onReceive", "screen on");
                else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    PPApplication.logE("@@@ ScreenOnOffService.onReceive", "screen off");

                    //boolean lockDeviceEnabled = false;
                    if (PPApplication.lockDeviceActivity != null) {
                        //lockDeviceEnabled = true;
                        PPApplication.lockDeviceActivity.finish();
                        PPApplication.lockDeviceActivity.overridePendingTransition(0, 0);
                    }

                    //ActivateProfileHelper.setScreenUnlocked(appContext, false);

                    if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                            ApplicationPreferences.notificationHideInLockscreen(appContext)) {
                        DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                        //dataWrapper.getActivateProfileHelper().removeNotification();
                        //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                        Profile activatedProfile = dataWrapper.getActivatedProfile();
                        dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                        dataWrapper.invalidateDataWrapper();
                    }
                }
                if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    PPApplication.logE("@@@ ScreenOnOffService.onReceive", "screen unlock");
                    //ActivateProfileHelper.setScreenUnlocked(appContext, true);

                    final DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);

                    if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                            ApplicationPreferences.notificationHideInLockscreen(appContext)) {
                        //dataWrapper.getActivateProfileHelper().removeNotification();
                        //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                        Profile activatedProfile = dataWrapper.getActivatedProfile();
                        dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                    }

                    // change screen timeout
                    /*if (lockDeviceEnabled && Permissions.checkLockDevice(appContext))
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutBeforeDeviceLock);*/
                    final int screenTimeout = ActivateProfileHelper.getActivatedProfileScreenTimeout(appContext);
                    if ((screenTimeout > 0) && (Permissions.checkScreenTimeout(appContext))) {
                        if (PPApplication.screenTimeoutHandler != null) {
                            PPApplication.screenTimeoutHandler.post(new Runnable() {
                                public void run() {
                                    dataWrapper.getActivateProfileHelper().setScreenTimeout(screenTimeout, appContext);
                                    dataWrapper.invalidateDataWrapper();
                                }
                            });
                        }/* else {
                        dataWrapper.getActivateProfileHelper().setScreenTimeout(screenTimeout);
                        dataWrapper.invalidateDataWrapper();
                    }*/
                    }

                    // enable/disable keyguard
                    Intent keyguardService = new Intent(appContext, KeyguardService.class);
                    appContext.startService(keyguardService);
                }

                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    PPApplication.logE("@@@ ScreenOnOffService.onReceive", "screen on");
                    if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                            ApplicationPreferences.notificationHideInLockscreen(appContext)) {
                        DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                        //dataWrapper.getActivateProfileHelper().removeNotification();
                        //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                        Profile activatedProfile = dataWrapper.getActivatedProfile();
                        dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                        dataWrapper.invalidateDataWrapper();
                    }
                }
            }
        }
    }

}
