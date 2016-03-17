package sk.henrichg.phoneprofiles;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class ExecuteRadioProfilePrefsService extends IntentService //WakefulIntentService 
{

    public static final String	PPHELPER_ACTION_SETPROFILEPREFERENCES = "sk.henrichg.phoneprofileshelper.ACTION_SETPROFILEPREFERENCES";

    private static final String PPHELPER_PROCEDURE = "procedure";
    private static final String PPHELPER_PROCEDURE_RADIO_CHANGE = "radioChange";
    private static final String PPHELPER_GPS_CHANGE = "GPSChange";
    private static final String PPHELPER_AIRPLANE_MODE_CHANGE = "airplaneModeChange";
    private static final String PPHELPER_NFC_CHANGE = "NFCChange";
    private static final String PPHELPER_WIFI_CHANGE = "WiFiChange";
    private static final String PPHELPER_BLUETOOTH_CHANGE = "bluetoothChange";
    private static final String PPHELPER_MOBILE_DATA_CHANGE = "mobileDataChange";
    private static final String PPHELPER_WIFI_AP_CHANGE = "WiFiAPChange";
    private static final String PPHELPER_NETWORK_TYPE_CHANGE = "networkTypeChange";

    public ExecuteRadioProfilePrefsService() {
        super("ExecuteRadioProfilePrefsService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //@Override
    //protected void doWakefulWork(Intent intent) {
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();

        GlobalData.loadPreferences(context);

        DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);

        long profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
        Profile profile = dataWrapper.getProfileById(profile_id);

        profile = GlobalData.getMappedProfile(profile, context);
        if (profile != null) {
            if (PhoneProfilesHelper.isPPHelperInstalled(context, 0)) {
                // broadcast PPHelper
                Intent ppHelperIntent = new Intent();
                ppHelperIntent.setAction(PPHELPER_ACTION_SETPROFILEPREFERENCES);
                ppHelperIntent.putExtra(PPHELPER_PROCEDURE, PPHELPER_PROCEDURE_RADIO_CHANGE);
                ppHelperIntent.putExtra(PPHELPER_GPS_CHANGE, profile._deviceGPS);
                ppHelperIntent.putExtra(PPHELPER_AIRPLANE_MODE_CHANGE, profile._deviceAirplaneMode);
                ppHelperIntent.putExtra(PPHELPER_NFC_CHANGE, profile._deviceNFC);
                ppHelperIntent.putExtra(PPHELPER_WIFI_CHANGE, profile._deviceWiFi);
                ppHelperIntent.putExtra(PPHELPER_BLUETOOTH_CHANGE, profile._deviceBluetooth);
                ppHelperIntent.putExtra(PPHELPER_MOBILE_DATA_CHANGE, profile._deviceMobileData);
                ppHelperIntent.putExtra(PPHELPER_WIFI_AP_CHANGE, profile._deviceWiFiAP);
                ppHelperIntent.putExtra(PPHELPER_NETWORK_TYPE_CHANGE, profile._deviceNetworkType);
                context.sendBroadcast(ppHelperIntent);
            } else {
                if (Permissions.checkProfileRadioPreferences(context, profile)) {
                    // run execute radios from ActivateProfileHelper
                    ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
                    aph.initialize(null, context);
                    aph.executeForRadios(profile);
                    aph = null;
                }
            }
        }

        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;

    }

}
