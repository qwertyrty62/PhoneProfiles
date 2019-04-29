package sk.henrichg.phoneprofiles;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import me.drakeet.support.toast.ToastCompat;

public class GrantPermissionActivity extends AppCompatActivity {

    private int grantType;
    private ArrayList<Permissions.PermissionType> permissions;
    static private ArrayList<Permissions.PermissionType> permissionsForRecheck;
    private boolean forceGrant;
    //private boolean mergedNotification;
    //private boolean forGUI;
    //private boolean monochrome;
    //private int monochromeValue;
    private int startupSource;
    private boolean interactive;
    private String applicationDataPath;
    private boolean activateProfile;
    private boolean fromNotification;

    private Profile profile;
    private DataWrapper dataWrapper;

    private boolean started = false;

    private boolean showRequestWriteSettings = false;
    private boolean showRequestAccessNotificationPolicy = false;
    private boolean showRequestDrawOverlays = false;
    private boolean showRequestReadExternalStorage = false;
    private boolean showRequestReadPhoneState = false;
    private boolean showRequestWriteExternalStorage = false;
    private boolean showRequestAccessCoarseLocation = false;
    private boolean showRequestAccessFineLocation = false;
    private boolean[][] whyPermissionType = null;
    private boolean rationaleAlreadyShown = false;

    private boolean restoredInstanceState;

    private static final int PERMISSIONS_REQUEST_CODE = 9090;

    private static final int WRITE_SETTINGS_REQUEST_CODE = 9091;
    private static final int ACCESS_NOTIFICATION_POLICY_REQUEST_CODE = 9092;
    private static final int DRAW_OVERLAYS_REQUEST_CODE = 9093;
    //private static final int WRITE_SETTINGS_REQUEST_CODE_FORCE_GRANT = 9094;
    //private static final int ACCESS_NOTIFICATION_POLICY_REQUEST_CODE_FORCE_GRANT = 9095;
    //private static final int DRAW_OVERLAYS_REQUEST_CODE_FORCE_GRANT = 9096;

    private static final String EXTRA_WITH_RATIONALE = "sk.henrichg.phoneprofiles.EXTRA_WITH_RATIONALE";

    private static final String NOTIFICATION_DELETED_ACTION = "sk.henrichg.phoneprofiles.PERMISSIONS_NOTIFICATION_DELETED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        Intent intent = getIntent();
        grantType = intent.getIntExtra(Permissions.EXTRA_GRANT_TYPE, 0);
        boolean onlyNotification = intent.getBooleanExtra(Permissions.EXTRA_ONLY_NOTIFICATION, false);
        forceGrant = intent.getBooleanExtra(Permissions.EXTRA_FORCE_GRANT, false);
        permissions = intent.getParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES);
        permissionsForRecheck = intent.getParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES);
        /*mergedNotification = false;
        if (permissions == null) {
            permissions = Permissions.getMergedPermissions(getApplicationContext());
            mergedNotification = true;
        }*/

        long profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        //forGUI = intent.getBooleanExtra(Permissions.EXTRA_FOR_GUI, false);
        //monochrome = intent.getBooleanExtra(Permissions.EXTRA_MONOCHROME, false);
        //monochromeValue = intent.getIntExtra(Permissions.EXTRA_MONOCHROME_VALUE, 0xFF);
        startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_ACTIVATOR);
        interactive = intent.getBooleanExtra(Permissions.EXTRA_INTERACTIVE, true);
        applicationDataPath = intent.getStringExtra(Permissions.EXTRA_APPLICATION_DATA_PATH);
        activateProfile = intent.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, true) /*&& (profile_id != Profile.SHARED_PROFILE_ID)*/;

        fromNotification = intent.getBooleanExtra(Permissions.EXTRA_FROM_NOTIFICATION, false);

        dataWrapper = new DataWrapper(getApplicationContext(), /*forGUI,*/ false, 0, false);
        //if (profile_id != Profile.SHARED_PROFILE_ID)
            profile = dataWrapper.getProfileById(profile_id, false, false);
        //else
        //    profile = Profile.getSharedProfile(getApplicationContext());

        restoredInstanceState = savedInstanceState != null;

        if (onlyNotification) {
            showNotification();
            started = true;
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (started) return;
        started = true;

        if ((grantType == Permissions.GRANT_TYPE_PROFILE) && (profile == null)) {
            finish();
            return;
        }

        final Context context = getApplicationContext();

        if (fromNotification) {
            // called from notification - recheck permissions
            if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
                boolean granted = Permissions.checkInstallTone(context, permissions);
                if (granted) {
                    Toast msg = ToastCompat.makeText(context.getApplicationContext(),
                            context.getResources().getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }
            else
            if (grantType == Permissions.GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION) {
                boolean granted = Permissions.checkPlayRingtoneNotification(context, permissions);
                if (granted) {
                    Toast msg = ToastCompat.makeText(context.getApplicationContext(),
                            context.getResources().getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }
            else
            if (grantType == Permissions.GRANT_TYPE_LOG_TO_FILE) {
                boolean granted = Permissions.checkLogToFile(context, permissions);
                if (granted) {
                    Toast msg = ToastCompat.makeText(context.getApplicationContext(),
                            context.getResources().getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }
            else {
                // get permissions from shared preferences and recheck it
                /*permissions = Permissions.recheckPermissions(context, Permissions.getMergedPermissions(context));
                mergedNotification = true;*/
                permissions = Permissions.recheckPermissions(context, permissions);
                if (permissions.size() == 0) {
                    Toast msg = ToastCompat.makeText(context.getApplicationContext(),
                            context.getResources().getString(R.string.toast_permissions_granted),
                            Toast.LENGTH_SHORT);
                    msg.show();
                    finish();
                    return;
                }
            }
        }

        if (!restoredInstanceState) {
            boolean withRationale = canShowRationale(context, false);

            int iteration = 4;
            if (showRequestWriteSettings)
                iteration = 1;
            else if (showRequestAccessNotificationPolicy)
                iteration = 2;
            else if (showRequestDrawOverlays)
                iteration = 3;
            requestPermissions(iteration, withRationale);
        }
    }

    private boolean canShowRationale(final Context context, boolean forceGrant) {
        showRequestWriteSettings = false;
        showRequestAccessNotificationPolicy = false;
        showRequestDrawOverlays = false;
        showRequestReadExternalStorage = false;
        showRequestReadPhoneState = false;
        showRequestWriteExternalStorage = false;
        showRequestAccessCoarseLocation = false;
        showRequestAccessFineLocation = false;

        whyPermissionType = new boolean[9][100];

        for (Permissions.PermissionType permissionType : permissions) {
            if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                showRequestWriteSettings = Permissions.getShowRequestWriteSettingsPermission(context) || forceGrant;
                whyPermissionType[0][permissionType.type] = true;
            }
            if (permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
                showRequestAccessNotificationPolicy = Permissions.getShowRequestAccessNotificationPolicyPermission(context) || forceGrant;
                whyPermissionType[1][permissionType.type] = true;
            }
            if (permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                showRequestDrawOverlays = Permissions.getShowRequestDrawOverlaysPermission(context) || forceGrant;
                whyPermissionType[2][permissionType.type] = true;
            }
            if (permissionType.permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showRequestReadExternalStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType.permission) || forceGrant;
                whyPermissionType[3][permissionType.type] = true;
            }
            if (permissionType.permission.equals(Manifest.permission.READ_PHONE_STATE)) {
                showRequestReadPhoneState = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType.permission) || forceGrant;
                whyPermissionType[4][permissionType.type] = true;
            }
            if (permissionType.permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showRequestWriteExternalStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType.permission) || forceGrant;
                whyPermissionType[6][permissionType.type] = true;
            }
            if (permissionType.permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showRequestAccessCoarseLocation = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType.permission) || forceGrant;
                whyPermissionType[7][permissionType.type] = true;
            }
            if (permissionType.permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showRequestAccessFineLocation = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionType.permission) || forceGrant;
                whyPermissionType[8][permissionType.type] = true;
            }
        }

        return (showRequestWriteSettings ||
                showRequestReadExternalStorage ||
                showRequestReadPhoneState ||
                showRequestWriteExternalStorage ||
                showRequestAccessNotificationPolicy ||
                showRequestAccessCoarseLocation ||
                showRequestAccessFineLocation ||
                showRequestDrawOverlays);

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showRationale(final Context context) {
        if (rationaleAlreadyShown)
            finishGrant();
        rationaleAlreadyShown = true;

        if (canShowRationale(context, forceGrant)) {

            /*if (onlyNotification) {
                showNotification(context);
            }
            else {*/
            String showRequestString;

            if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE)
                showRequestString = context.getString(R.string.permissions_for_install_tone_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_WALLPAPER)
                showRequestString = context.getString(R.string.permissions_for_wallpaper_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON)
                showRequestString = context.getString(R.string.permissions_for_custom_profile_icon_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_EXPORT)
                showRequestString = context.getString(R.string.permissions_for_export_app_data_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_IMPORT)
                showRequestString = context.getString(R.string.permissions_for_import_app_data_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG)
                showRequestString = context.getString(R.string.permissions_for_brightness_dialog_text1) + "<br><br>";
            else if (grantType == Permissions.GRANT_TYPE_LOG_TO_FILE)
                showRequestString = context.getString(R.string.permissions_for_log_to_file_text1) + "<br><br>";
            else {
                    /*if (mergedNotification) {
                        showRequestString = context.getString(R.string.permissions_for_profile_text1m) + " ";
                        showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text2) + "<br><br>";
                    }
                    else {*/
                showRequestString = context.getString(R.string.permissions_for_profile_text1) + " ";
                if (profile != null)
                    showRequestString = showRequestString + "\"" + profile._name + "\" ";
                showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text2) + "<br><br>";
                //}
            }

            if (showRequestWriteSettings) {
                showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_write_settings) + "</b>";
                String whyPermissionString = getWhyPermissionString(whyPermissionType[0]);
                if (whyPermissionString != null)
                    showRequestString = showRequestString + whyPermissionString;
                showRequestString = showRequestString + "<br>";
            }
            if (showRequestReadExternalStorage || showRequestWriteExternalStorage) {
                showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_storage) + "</b>";
                boolean[] permissionTypes = new boolean[100];
                for (int i = 0; i < 100; i++) {
                    permissionTypes[i] = whyPermissionType[3][i] || whyPermissionType[6][i];
                }
                String whyPermissionString = getWhyPermissionString(permissionTypes);
                if (whyPermissionString != null)
                    showRequestString = showRequestString + whyPermissionString;
                showRequestString = showRequestString + "<br>";
            }
            if (showRequestReadPhoneState) {
                showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_phone) + "</b>";
                boolean[] permissionTypes = new boolean[100];
                for (int i = 0; i < 100; i++) {
                    permissionTypes[i] = whyPermissionType[4][i] || whyPermissionType[5][i];
                }
                String whyPermissionString = getWhyPermissionString(permissionTypes);
                if (whyPermissionString != null)
                    showRequestString = showRequestString + whyPermissionString;
                showRequestString = showRequestString + "<br>";
            }
            if (showRequestAccessCoarseLocation || showRequestAccessFineLocation) {
                showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_location) + "</b>";
                boolean[] permissionTypes = new boolean[100];
                for (int i = 0; i < 100; i++) {
                    permissionTypes[i] = whyPermissionType[7][i] || whyPermissionType[8][i];
                }
                String whyPermissionString = getWhyPermissionString(permissionTypes);
                if (whyPermissionString != null)
                    showRequestString = showRequestString + whyPermissionString;
                showRequestString = showRequestString + "<br>";
            }
            if (showRequestAccessNotificationPolicy) {
                showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_access_notification_policy) + "</b>";
                String whyPermissionString = getWhyPermissionString(whyPermissionType[1]);
                if (whyPermissionString != null)
                    showRequestString = showRequestString + whyPermissionString;
                showRequestString = showRequestString + "<br>";
            }
            if (showRequestDrawOverlays) {
                if (!PPApplication.romIsMIUI)
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_draw_overlays) + "</b>";
                else
                    showRequestString = showRequestString + "<b>" + "\u2022 " + context.getString(R.string.permission_group_name_draw_overlays_miui) + "</b>";
                String whyPermissionString = getWhyPermissionString(whyPermissionType[2]);
                if (whyPermissionString != null)
                    showRequestString = showRequestString + whyPermissionString;
                showRequestString = showRequestString + "<br>";
            }

            showRequestString = showRequestString + "<br>";

            if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_install_tone_text2);
            else if (grantType == Permissions.GRANT_TYPE_WALLPAPER)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_wallpaper_text2);
            else if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_custom_profile_icon_text2);
            else if (grantType == Permissions.GRANT_TYPE_EXPORT)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_export_app_data_text2);
            else if (grantType == Permissions.GRANT_TYPE_IMPORT)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_import_app_data_text2);
            else if (grantType == Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_brightness_dialog_text2);
            else if (grantType == Permissions.GRANT_TYPE_LOG_TO_FILE)
                showRequestString = showRequestString + context.getString(R.string.permissions_for_log_to_file_text2);
            else
                showRequestString = showRequestString + context.getString(R.string.permissions_for_profile_text3);

            // set theme and language for dialog alert ;-)
            // not working on Android 2.3.x
            GlobalGUIRoutines.setTheme(this, true, true);
            GlobalGUIRoutines.setLanguage(this);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.permissions_alert_title);
            dialogBuilder.setMessage(GlobalGUIRoutines.fromHtml(showRequestString));
            dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int iteration = 4;
                    if (showRequestWriteSettings)
                        iteration = 1;
                    else if (showRequestAccessNotificationPolicy)
                        iteration = 2;
                    else if (showRequestDrawOverlays)
                        iteration = 3;
                    requestPermissions(iteration, canShowRationale(context, false));
                }
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            AlertDialog dialog = dialogBuilder.create();
                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        if (positive != null) positive.setAllCaps(false);
                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        if (negative != null) negative.setAllCaps(false);
                    }
                });*/
            if (!isFinishing())
                dialog.show();
            //}
        }
        else {
            showRequestWriteSettings = false;
            showRequestAccessNotificationPolicy = false;
            showRequestDrawOverlays = false;

            for (Permissions.PermissionType permissionType : permissions) {
                if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                    showRequestWriteSettings = true;
                }
                if (permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
                    showRequestAccessNotificationPolicy = true;
                }
                if (permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                    showRequestDrawOverlays = true;
                }
            }

            PPApplication.logE("GrantPermissionActivity.showRationale", "showRequestWriteSettings="+showRequestWriteSettings);
            PPApplication.logE("GrantPermissionActivity.showRationale", "showRequestAccessNotificationPolicy="+showRequestAccessNotificationPolicy);
            PPApplication.logE("GrantPermissionActivity.showRationale", "showRequestDrawOverlays="+showRequestDrawOverlays);

            int iteration = 4;
            if (showRequestWriteSettings)
                iteration = 1;
            else if (showRequestAccessNotificationPolicy)
                iteration = 2;
            else if (showRequestDrawOverlays)
                iteration = 3;

            requestPermissions(iteration, canShowRationale(context, false));
        }
    }

    private String getWhyPermissionString(boolean[] permissionTypes) {
        String s = "";
        for (int permissionType = 0; permissionType < 100; permissionType++) {
            if (permissionTypes[permissionType]) {
                switch (permissionType) {
                    //case Permissions.PERMISSION_PROFILE_VOLUME_PREFERENCES:
                    //    break;
                    case Permissions.PERMISSION_PROFILE_VIBRATION_ON_TOUCH:
                        s = getString(R.string.permission_why_profile_vibration_on_touch);
                        break;
                    case Permissions.PERMISSION_PROFILE_RINGTONES:
                        s = getString(R.string.permission_why_profile_ringtones);
                        break;
                    case Permissions.PERMISSION_PROFILE_SCREEN_TIMEOUT:
                        s = getString(R.string.permission_why_profile_screen_timeout);
                        break;
                    case Permissions.PERMISSION_PROFILE_SCREEN_BRIGHTNESS:
                        s = getString(R.string.permission_why_profile_screen_brightness);
                        break;
                    case Permissions.PERMISSION_PROFILE_AUTOROTATION:
                        s = getString(R.string.permission_why_profile_autorotation);
                        break;
                    case Permissions.PERMISSION_PROFILE_WALLPAPER:
                        s = getString(R.string.permission_why_profile_wallpaper);
                        break;
                    case Permissions.PERMISSION_PROFILE_RADIO_PREFERENCES:
                        s = getString(R.string.permission_why_profile_radio_preferences);
                        break;
                    case Permissions.PERMISSION_PROFILE_PHONE_STATE_BROADCAST:
                        s = getString(R.string.permission_why_profile_phone_state_broadcast);
                        break;
                    case Permissions.PERMISSION_PROFILE_CUSTOM_PROFILE_ICON:
                        s = getString(R.string.permission_why_profile_custom_profile_icon);
                        break;
                    case Permissions.PERMISSION_INSTALL_TONE:
                        s = getString(R.string.permission_why_install_tone);
                        break;
                    case Permissions.PERMISSION_EXPORT:
                        s = getString(R.string.permission_why_export);
                        break;
                    case Permissions.PERMISSION_IMPORT:
                        s = getString(R.string.permission_why_import);
                        break;
                    case Permissions.PERMISSION_PROFILE_NOTIFICATION_LED:
                        s = getString(R.string.permission_why_profile_notification_led);
                        break;
                    case Permissions.PERMISSION_PROFILE_VIBRATE_WHEN_RINGING:
                        s = getString(R.string.permission_why_profile_vibrate_when_ringing);
                        break;
                    case Permissions.PERMISSION_PLAY_RINGTONE_NOTIFICATION:
                        s = getString(R.string.permission_why_play_ringtone_notification);
                        break;
                    case Permissions.PERMISSION_PROFILE_ACCESS_NOTIFICATION_POLICY:
                        s = getString(R.string.permission_why_profile_access_notification_policy);
                        break;
                    case Permissions.PERMISSION_PROFILE_LOCK_DEVICE:
                        s = getString(R.string.permission_why_profile_lock_device);
                        break;
                    case Permissions.PERMISSION_RINGTONE_PREFERENCE:
                        s = getString(R.string.permission_why_ringtone_preference);
                        break;
                    case Permissions.PERMISSION_PROFILE_DTMF_TONE_WHEN_DIALING:
                        s = getString(R.string.permission_why_profile_dtmf_tone_when_dialing);
                        break;
                    case Permissions.PERMISSION_PROFILE_SOUND_ON_TOUCH:
                        s = getString(R.string.permission_why_profile_sound_on_touch);
                        break;
                    case Permissions.PERMISSION_BRIGHTNESS_PREFERENCE:
                        s = getString(R.string.permission_why_brightness_preference);
                        break;
                    case Permissions.PERMISSION_WALLPAPER_PREFERENCE:
                        s = getString(R.string.permission_why_wallpaper_preference);
                        break;
                    case Permissions.PERMISSION_CUSTOM_PROFILE_ICON_PREFERENCE:
                        s = getString(R.string.permission_why_custom_profile_icon_preference);
                        break;
                    case Permissions.PERMISSION_LOG_TO_FILE:
                        s = getString(R.string.permission_why_log_to_file);
                        break;
                }
            }
        }
        if (s.isEmpty())
            return s;
        else
            return "<br>" + "&nbsp;&nbsp;&nbsp;- " + s;
    }

    private void showNotification() {
        final Context context = getApplicationContext();
        if (canShowRationale(context, false)) {
            int notificationID;
            NotificationCompat.Builder mBuilder;

            PPApplication.createGrantPermissionNotificationChannel(context);

            Intent intent = new Intent(context, GrantPermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);  // this close all activities with same taskAffinity
            if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
                String nTitle = context.getString(R.string.permissions_for_install_tone_text_notification);
                String nText = context.getString(R.string.permissions_for_install_tone_big_text_notification);
                if (android.os.Build.VERSION.SDK_INT < 24) {
                    nTitle = context.getString(R.string.app_name);
                    nText = context.getString(R.string.permissions_for_install_tone_text_notification) + ": " +
                            context.getString(R.string.permissions_for_install_tone_big_text_notification);
                }
                mBuilder = new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                        .setColor(ContextCompat.getColor(context, R.color.primary))
                        .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                        .setContentTitle(nTitle) // title for notification
                        .setContentText(nText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                        .setAutoCancel(true); // clear notification after click
                notificationID = PPApplication.GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID;
            } else if (grantType == Permissions.GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION) {
                String nTitle = context.getString(R.string.permissions_for_install_tone_text_notification);
                String nText = context.getString(R.string.permissions_for_play_ringtone_notification_big_text_notification);
                if (android.os.Build.VERSION.SDK_INT < 24) {
                    nTitle = context.getString(R.string.app_name);
                    nText = context.getString(R.string.permissions_for_install_tone_text_notification) + ": " +
                            context.getString(R.string.permissions_for_play_ringtone_notification_big_text_notification);
                }
                mBuilder = new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                        .setColor(ContextCompat.getColor(context, R.color.primary))
                        .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                        .setContentTitle(nTitle) // title for notification
                        .setContentText(nText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                        .setAutoCancel(true); // clear notification after click
                notificationID = PPApplication.GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_ID;
            } else if (grantType == Permissions.GRANT_TYPE_LOG_TO_FILE) {
                String nTitle = context.getString(R.string.permissions_for_install_tone_text_notification);
                String nText = context.getString(R.string.permissions_for_log_to_file_big_text_notification);
                if (android.os.Build.VERSION.SDK_INT < 24) {
                    nTitle = context.getString(R.string.app_name);
                    nText = context.getString(R.string.permissions_for_install_tone_text_notification) + ": " +
                            context.getString(R.string.permissions_for_log_to_file_big_text_notification);
                }
                mBuilder = new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                        .setColor(ContextCompat.getColor(context, R.color.primary))
                        .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                        .setContentTitle(nTitle) // title for notification
                        .setContentText(nText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                        .setAutoCancel(true); // clear notification after click
                notificationID = PPApplication.GRANT_LOG_TO_FILE_PERMISSIONS_NOTIFICATION_ID;
            } else {
                String nTitle = context.getString(R.string.permissions_for_install_tone_text_notification);
                String nText = "";
                if (android.os.Build.VERSION.SDK_INT < 24) {
                    nTitle = context.getString(R.string.app_name);
                    nText = context.getString(R.string.permissions_for_profile_text_notification) + ": ";
                }
            /*if (mergedNotification) {
                nText = nText + context.getString(R.string.permissions_for_profile_text1m) + " " +
                        context.getString(R.string.permissions_for_profile_big_text_notification);
            }
            else {*/
                nText = nText + context.getString(R.string.permissions_for_profile_text1) + " ";
                if (profile != null)
                    nText = nText + "\"" + profile._name + "\" ";
                nText = nText + context.getString(R.string.permissions_for_profile_big_text_notification);
                //}
                mBuilder = new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                        .setColor(ContextCompat.getColor(context, R.color.primary))
                        .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                        .setContentTitle(nTitle) // title for notification
                        .setContentText(nText) // message for notification
                        .setAutoCancel(true); // clear notification after click
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));

                Intent deleteIntent = new Intent(NOTIFICATION_DELETED_ACTION);
                PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, grantType, deleteIntent, 0);
                mBuilder.setDeleteIntent(deletePendingIntent);

                //intent.putExtra(Permissions.EXTRA_FOR_GUI, forGUI);
                //intent.putExtra(Permissions.EXTRA_MONOCHROME, monochrome);
                //intent.putExtra(Permissions.EXTRA_MONOCHROME_VALUE, monochromeValue);

                if (profile != null) {
                    intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                    notificationID = 9999 + (int) profile._id;
                } else
                    notificationID = PPApplication.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID;
            }
            //permissions.clear();
            intent.putExtra(Permissions.EXTRA_GRANT_TYPE, grantType);
            intent.putParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES, permissions);
            //intent.putExtra(Permissions.EXTRA_ONLY_NOTIFICATION, false);
            intent.putExtra(Permissions.EXTRA_FROM_NOTIFICATION, true);
            intent.putExtra(Permissions.EXTRA_FORCE_GRANT, forceGrant);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
            intent.putExtra(Permissions.EXTRA_INTERACTIVE, interactive);
            intent.putExtra(Permissions.EXTRA_ACTIVATE_PROFILE, activateProfile);

            PendingIntent pi = PendingIntent.getActivity(context, grantType, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pi);
            mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
            mBuilder.setOnlyAlertOnce(true);
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
                mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
                mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            //}
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null)
                mNotificationManager.notify(notificationID, mBuilder.build());
        }
        finish();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.

                boolean allGranted = true;
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        allGranted = false;
                        //forceGrant = false;
                        break;
                    }
                }

                Context context = getApplicationContext();
                for (Permissions.PermissionType permissionType : this.permissions) {
                    if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                        if (!Settings.System.canWrite(context)) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                                allGranted = false;
                                break;
                            }
                        }
                    }
                    if (permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                        if (!Settings.canDrawOverlays(context)) {
                            allGranted = false;
                            break;
                        }
                    }
                }

                if (allGranted)
                    finishGrant();
                else
                    showRationale(context);

                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Context context = getApplicationContext();
        final boolean withRationale = (data == null) || data.getBooleanExtra(EXTRA_WITH_RATIONALE, true);
        if ((requestCode == WRITE_SETTINGS_REQUEST_CODE)/* || (requestCode == WRITE_SETTINGS_REQUEST_CODE_FORCE_GRANT)*/) {
            if (!Settings.System.canWrite(context)) {
                //forceGrant = false;
                //if (!forceGrant) {
                    // set theme and language for dialog alert ;-)
                    // not working on Android 2.3.x
                    GlobalGUIRoutines.setTheme(this, true, true);
                    GlobalGUIRoutines.setLanguage(this);

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setTitle(R.string.permissions_alert_title);
                    dialogBuilder.setMessage(R.string.permissions_write_settings_not_allowed_confirm);
                    dialogBuilder.setPositiveButton(R.string.permission_not_ask_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Permissions.setShowRequestWriteSettingsPermission(context, false);
                            if (rationaleAlreadyShown)
                                removePermission(Manifest.permission.WRITE_SETTINGS);
                            requestPermissions(2, withRationale);
                        }
                    });
                    dialogBuilder.setNegativeButton(R.string.permission_ask_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Permissions.setShowRequestWriteSettingsPermission(context, true);
                            if (rationaleAlreadyShown)
                                removePermission(Manifest.permission.WRITE_SETTINGS);
                            requestPermissions(2, withRationale);
                        }
                    });
                    dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (rationaleAlreadyShown)
                                removePermission(Manifest.permission.WRITE_SETTINGS);
                            requestPermissions(2, withRationale);
                        }
                    });
                    AlertDialog dialog = dialogBuilder.create();
                    /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                            if (positive != null) positive.setAllCaps(false);
                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                            if (negative != null) negative.setAllCaps(false);
                        }
                    });*/
                    if (!isFinishing())
                        dialog.show();
                /*}
                else {
                    //if (requestCode == WRITE_SETTINGS_REQUEST_CODE)
                    requestPermissions(2);
                    //else
                    //    finishGrant();
                }*/
            }
            else {
                Permissions.setShowRequestWriteSettingsPermission(context, true);
                //if (requestCode == WRITE_SETTINGS_REQUEST_CODE)
                    requestPermissions(2, withRationale);
                //else
                //    finishGrant();
            }
        }
        if ((requestCode == ACCESS_NOTIFICATION_POLICY_REQUEST_CODE)/* || (requestCode == ACCESS_NOTIFICATION_POLICY_REQUEST_CODE_FORCE_GRANT)*/) {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                    //forceGrant = false;
                    //if (!forceGrant) {
                        // set theme and language for dialog alert ;-)
                        // not working on Android 2.3.x
                        GlobalGUIRoutines.setTheme(this, true, true);
                        GlobalGUIRoutines.setLanguage(this);

                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                        dialogBuilder.setTitle(R.string.permissions_alert_title);
                        dialogBuilder.setMessage(R.string.permissions_access_notification_policy_not_allowed_confirm);
                        dialogBuilder.setPositiveButton(R.string.permission_not_ask_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Permissions.setShowRequestAccessNotificationPolicyPermission(context, false);
                                if (rationaleAlreadyShown)
                                    removePermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
                                requestPermissions(3, withRationale);
                            }
                        });
                        dialogBuilder.setNegativeButton(R.string.permission_ask_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                                if (rationaleAlreadyShown)
                                    removePermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
                                requestPermissions(3, withRationale);
                            }
                        });
                        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                if (rationaleAlreadyShown)
                                    removePermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
                                requestPermissions(3, withRationale);
                            }
                        });
                        AlertDialog dialog = dialogBuilder.create();
                        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                if (positive != null) positive.setAllCaps(false);
                                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                if (negative != null) negative.setAllCaps(false);
                            }
                        });*/
                        if (!isFinishing())
                            dialog.show();
                    /*}
                    else {
                        //if (requestCode == ACCESS_NOTIFICATION_POLICY_REQUEST_CODE)
                            requestPermissions(3);
                        //else
                        //    finishGrant();
                    }*/
                } else {
                    Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                    //if (requestCode == ACCESS_NOTIFICATION_POLICY_REQUEST_CODE)
                        requestPermissions(3, withRationale);
                    //else
                    //    finishGrant();
                }
            }
            else {
                //if (requestCode == ACCESS_NOTIFICATION_POLICY_REQUEST_CODE)
                    removePermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
                    requestPermissions(3, withRationale);
                //else
                //    finishGrant();
            }
        }
        if ((requestCode == DRAW_OVERLAYS_REQUEST_CODE)/* || (requestCode == DRAW_OVERLAYS_REQUEST_CODE_FORCE_GRANT)*/){
            if (!Settings.canDrawOverlays(context)) {
                //forceGrant = false;
                //if (!forceGrant) {
                    // set theme and language for dialog alert ;-)
                    // not working on Android 2.3.x
                    GlobalGUIRoutines.setTheme(this, true, true);
                    GlobalGUIRoutines.setLanguage(this);

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setTitle(R.string.permissions_alert_title);
                    if (!PPApplication.romIsMIUI)
                        dialogBuilder.setMessage(R.string.permissions_draw_overlays_not_allowed_confirm);
                    else
                        dialogBuilder.setMessage(R.string.permissions_draw_overlays_not_allowed_confirm_miui);
                    dialogBuilder.setPositiveButton(R.string.permission_not_ask_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Permissions.setShowRequestDrawOverlaysPermission(context, false);
                            if (rationaleAlreadyShown)
                                removePermission(Manifest.permission.SYSTEM_ALERT_WINDOW);
                            requestPermissions(4, withRationale);
                        }
                    });
                    dialogBuilder.setNegativeButton(R.string.permission_ask_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Permissions.setShowRequestDrawOverlaysPermission(context, true);
                            if (rationaleAlreadyShown)
                                removePermission(Manifest.permission.SYSTEM_ALERT_WINDOW);
                            requestPermissions(4, withRationale);
                        }
                    });
                    dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (rationaleAlreadyShown)
                                removePermission(Manifest.permission.SYSTEM_ALERT_WINDOW);
                            requestPermissions(4, withRationale);
                        }
                    });
                    AlertDialog dialog = dialogBuilder.create();
                    /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                            if (positive != null) positive.setAllCaps(false);
                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                            if (negative != null) negative.setAllCaps(false);
                        }
                    });*/
                    if (!isFinishing())
                        dialog.show();
                /*}
                else {
                    //if (requestCode == DRAW_OVERLAYS_REQUEST_CODE)
                        requestPermissions(4);
                    //else
                    //    finishGrant();
                }*/
            }
            else {
                Permissions.setShowRequestDrawOverlaysPermission(context, true);
                //if (requestCode == DRAW_OVERLAYS_REQUEST_CODE)
                    requestPermissions(4, withRationale);
                //else
                //    finishGrant();
            }
        }
        if ((requestCode == Permissions.REQUEST_CODE + grantType)/* || (requestCode == Permissions.REQUEST_CODE_FORCE_GRANT + grantType)*/) {

            boolean finishActivity;// = false;
            boolean permissionsChanged;// = Permissions.getPermissionsChanged(context);

            boolean calendarPermission = Permissions.checkCalendar(context);
            permissionsChanged = Permissions.getCalendarPermission(context) != calendarPermission;
            PPApplication.logE("GrantPermissionActivity.onActivityResult", "calendarPermission="+permissionsChanged);
            // finish Editor when permission is disabled
            finishActivity = permissionsChanged && (!calendarPermission);
            if (!permissionsChanged) {
                boolean contactsPermission = Permissions.checkContacts(context);
                permissionsChanged = Permissions.getContactsPermission(context) != contactsPermission;
                PPApplication.logE("GrantPermissionActivity.onActivityResult", "contactsPermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!contactsPermission);
            }
            if (!permissionsChanged) {
                boolean locationPermission = Permissions.checkLocation(context);
                permissionsChanged = Permissions.getLocationPermission(context) != locationPermission;
                PPApplication.logE("GrantPermissionActivity.onActivityResult", "locationPermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!locationPermission);
            }
            if (!permissionsChanged) {
                boolean smsPermission = Permissions.checkSMS(context);
                permissionsChanged = Permissions.getSMSPermission(context) != smsPermission;
                PPApplication.logE("GrantPermissionActivity.onActivityResult", "smsPermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!smsPermission);
            }
            if (!permissionsChanged) {
                boolean phonePermission = Permissions.checkPhone(context);
                PPApplication.logE("GrantPermissionActivity.onActivityResult", "phonePermission="+phonePermission);
                permissionsChanged = Permissions.getPhonePermission(context) != phonePermission;
                PPApplication.logE("GrantPermissionActivity.onActivityResult", "permissionsChanged="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!phonePermission);
            }
            if (!permissionsChanged) {
                boolean storagePermission = Permissions.checkStorage(context);
                permissionsChanged = Permissions.getStoragePermission(context) != storagePermission;
                PPApplication.logE("GrantPermissionActivity.onActivityResult", "storagePermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!storagePermission);
            }
            if (!permissionsChanged) {
                boolean cameraPermission = Permissions.checkCamera(context);
                permissionsChanged = Permissions.getCameraPermission(context) != cameraPermission;
                PPApplication.logE("GrantPermissionActivity.onActivityResult", "cameraPermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!cameraPermission);
            }
            if (!permissionsChanged) {
                boolean microphonePermission = Permissions.checkMicrophone(context);
                permissionsChanged = Permissions.getMicrophonePermission(context) != microphonePermission;
                PPApplication.logE("GrantPermissionActivity.onActivityResult", "microphonePermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!microphonePermission);
            }
            if (!permissionsChanged) {
                boolean sensorsPermission = Permissions.checkSensors(context);
                permissionsChanged = Permissions.getSensorsPermission(context) != sensorsPermission;
                PPApplication.logE("GrantPermissionActivity.onActivityResult", "sensorsPermission="+permissionsChanged);
                // finish Editor when permission is disabled
                finishActivity = permissionsChanged && (!sensorsPermission);
            }

            Permissions.saveAllPermissions(context, permissionsChanged);
            PPApplication.logE("GrantPermissionActivity.onActivityResult", "permissionsChanged="+permissionsChanged);

            if (permissionsChanged) {
                PPApplication.showProfileNotification(/*context*/);
                ActivateProfileHelper.updateGUI(context, !finishActivity);

                if (finishActivity) {
                    setResult(Activity.RESULT_CANCELED);
                    finishAffinity();
                }
            }

            if (!finishActivity) {
                boolean granted = false;
                for (Permissions.PermissionType permissionType : permissions) {
                    if (permissionType.permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        granted = (ContextCompat.checkSelfPermission(context, permissionType.permission) == PackageManager.PERMISSION_GRANTED);
                    }
                    if (permissionType.permission.equals(Manifest.permission.READ_PHONE_STATE)) {
                        granted = (ContextCompat.checkSelfPermission(context, permissionType.permission) == PackageManager.PERMISSION_GRANTED);
                    }
                    if (permissionType.permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        granted = (ContextCompat.checkSelfPermission(context, permissionType.permission) == PackageManager.PERMISSION_GRANTED);
                    }
                    if (permissionType.permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        granted = (ContextCompat.checkSelfPermission(context, permissionType.permission) == PackageManager.PERMISSION_GRANTED);
                    }
                    if (permissionType.permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        granted = (ContextCompat.checkSelfPermission(context, permissionType.permission) == PackageManager.PERMISSION_GRANTED);
                    }
                }
                if (granted)
                    finishGrant();
                else
                    showRationale(context);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions(int iteration, boolean withRationale) {

        if (isFinishing())
            return;

        if (iteration == 1) {
            boolean writeSettingsFound = false;
            for (Permissions.PermissionType permissionType : permissions) {
                if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                    //if (!PPApplication.romIsMIUI) {
                        if (GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS, getApplicationContext())) {
                            writeSettingsFound = true;
                            final Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.putExtra(EXTRA_WITH_RATIONALE, withRationale);
                            startActivityForResult(intent, WRITE_SETTINGS_REQUEST_CODE);
                            break;
                        }
                    /*}
                    else {
                        try {
                            // MIUI 8
                            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                            localIntent.putExtra("extra_pkgname", getPackageName());
                            intent.putExtra(EXTRA_WITH_RATIONALE, withRationale);
                            startActivityForResult(localIntent, WRITE_SETTINGS_REQUEST_CODE);
                            writeSettingsFound = true;
                        } catch (Exception e) {
                            try {
                                // MIUI 5/6/7
                                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                localIntent.putExtra("extra_pkgname", getPackageName());
                                intent.putExtra(EXTRA_WITH_RATIONALE, withRationale);
                                startActivityForResult(localIntent, WRITE_SETTINGS_REQUEST_CODE);
                                writeSettingsFound = true;
                            } catch (Exception e1) {
                                writeSettingsFound = false;
                            }
                        }
                    }*/
                }
            }
            if (!writeSettingsFound)
                requestPermissions(2, withRationale);
        }
        else
        if (iteration == 2) {
            boolean accessNotificationPolicyFound = false;
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            for (Permissions.PermissionType permissionType : permissions) {
                if (no60 && permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getApplicationContext())) {
                    accessNotificationPolicyFound = true;
                    final Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    intent.putExtra(EXTRA_WITH_RATIONALE, withRationale);
                    startActivityForResult(intent, ACCESS_NOTIFICATION_POLICY_REQUEST_CODE);
                    break;
                }
            }
            if (!accessNotificationPolicyFound)
                requestPermissions(3, withRationale);
        }
        else
        if (iteration == 3) {
            boolean drawOverlaysFound = false;
            //boolean api25 = android.os.Build.VERSION.SDK_INT >= 25;
            for (Permissions.PermissionType permissionType : permissions) {
                if (/*api25 &&*/ permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                    //if (!PPApplication.romIsMIUI) {
                        if (GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, getApplicationContext())) {
                            drawOverlaysFound = true;
                            final Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.putExtra(EXTRA_WITH_RATIONALE, withRationale);
                            startActivityForResult(intent, DRAW_OVERLAYS_REQUEST_CODE);
                            break;
                        }
                    /*}
                    else {
                        try {
                            // MIUI 8
                            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                            localIntent.putExtra("extra_pkgname", getPackageName());
                            intent.putExtra(EXTRA_WITH_RATIONALE, withRationale);
                            startActivityForResult(localIntent, DRAW_OVERLAYS_REQUEST_CODE);
                            drawOverlaysFound = true;
                            break;
                        } catch (Exception e) {
                            try {
                                // MIUI 5/6/7
                                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                localIntent.putExtra("extra_pkgname", getPackageName());
                                intent.putExtra(EXTRA_WITH_RATIONALE, withRationale);
                                startActivityForResult(localIntent, DRAW_OVERLAYS_REQUEST_CODE);
                                drawOverlaysFound = true;
                                break;
                            } catch (Exception e1) {
                                drawOverlaysFound = false;
                            }
                        }
                    }*/
                }
            }
            if (!drawOverlaysFound)
                requestPermissions(4, withRationale);
        }
        else {
            List<String> permList = new ArrayList<>();
            for (Permissions.PermissionType permissionType : permissions) {
                if ((!permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) &&
                    (!permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) &&
                    (!permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) &&
                    (!permList.contains(permissionType.permission))) {
                    permList.add(permissionType.permission);
                }
            }

            if (permList.size() > 0) {
                if (!withRationale && rationaleAlreadyShown) {
                    Permissions.saveAllPermissions(getApplicationContext(), false);
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:sk.henrichg.phoneprofiles"));
                    if (GlobalGUIRoutines.activityIntentExists(intent, getApplicationContext())) {
                        intent.putExtra(EXTRA_WITH_RATIONALE, false);
                        startActivityForResult(intent, Permissions.REQUEST_CODE/*_FORCE_GRANT*/ + grantType);
                    }
                    else
                        finishGrant();
                }
                else {
                    String[] permArray = new String[permList.size()];
                    for (int i = 0; i < permList.size(); i++) permArray[i] = permList.get(i);

                    ActivityCompat.requestPermissions(this, permArray, PERMISSIONS_REQUEST_CODE);
                }
            }
            else {
                Context context = getApplicationContext();
                boolean allGranted = true;
                for (Permissions.PermissionType permissionType : permissions) {
                    if (permissionType.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                        if (!Settings.System.canWrite(context)) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (permissionType.permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                                allGranted = false;
                                break;
                            }
                        }
                    }
                    if (permissionType.permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                        if (!Settings.canDrawOverlays(context)) {
                            allGranted = false;
                            break;
                        }
                    }
                }
                if (allGranted)
                    finishGrant();
                else
                    showRationale(context);
            }
        }
    }

    private void removePermission(final String permission) {
        for (Permissions.PermissionType permissionType : permissions) {
            if (permissionType.permission.equals(permission)) {
                permissions.remove(permissionType);
                break;
            }
        }
    }

    private void finishGrant() {
        Context context = getApplicationContext();

        /*
        if (forGUI && (profile != null))
        {
            // regenerate profile icon
            dataWrapper.refreshProfileIcon(profile, monochrome, monochromeValue);
        }
        */

        if (grantType == Permissions.GRANT_TYPE_INSTALL_TONE) {
            //finishAffinity();
            finish();
            Permissions.removeInstallToneNotification(context);
            TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, context);
        }
        else
        if (grantType == Permissions.GRANT_TYPE_WALLPAPER) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.wallpaperViewPreference != null)
                Permissions.wallpaperViewPreference.startGallery();*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.profileIconPreference != null)
                Permissions.profileIconPreference.startGallery();*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_EXPORT) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.editorActivity != null)
                Permissions.editorActivity.doExportData();*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_IMPORT) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(Permissions.EXTRA_APPLICATION_DATA_PATH, applicationDataPath);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
            /*if (Permissions.editorActivity != null)
                Permissions.editorActivity.doImportData(applicationDataPath);*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.brightnessDialogPreference != null)
                Permissions.brightnessDialogPreference.enableViews();*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_RINGTONE_PREFERENCE) {
            setResult(Activity.RESULT_OK);
            finish();
            /*if (Permissions.ringtonePreference != null)
                Permissions.ringtonePreference.refreshListView();*/
        }
        else
        if (grantType == Permissions.GRANT_TYPE_LOG_TO_FILE) {
            //finishAffinity();
            finish();
            Permissions.removeLogToFileNotification(context);
        }
        else {
            /*Intent returnIntent = new Intent();
            returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            returnIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
            returnIntent.putExtra(Permissions.EXTRA_ACTIVATE_PROFILE, activateProfile);
            setResult(Activity.RESULT_OK,returnIntent);*/

            //finishAffinity();
            finish();
            Permissions.removeProfileNotification(context);
            if (activateProfile)
                dataWrapper._activateProfile(profile, startupSource, interactive,null);
        }

        if (permissionsForRecheck != null) {
            permissions = Permissions.recheckPermissions(context, permissionsForRecheck);
            if (permissions.size() != 0) {
                Toast msg = ToastCompat.makeText(context.getApplicationContext(),
                        context.getResources().getString(R.string.toast_permissions_not_granted),
                        Toast.LENGTH_LONG);
                msg.show();
            }
        }

        //Permissions.releaseReferences();
        /*if (mergedNotification)
            Permissions.clearMergedPermissions(context);*/

        //if (grantType != Permissions.GRANT_TYPE_PROFILE) {
            PPApplication.showProfileNotification(/*dataWrapper.context*/);
            ActivateProfileHelper.updateGUI(dataWrapper.context, true);
        //}
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
