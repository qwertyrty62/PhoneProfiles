package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class ExecuteVolumeProfilePrefsJob extends Job {

    static final String JOB_TAG  = "ExecuteVolumeProfilePrefsJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PPApplication.logE("ExecuteVolumeProfilePrefsJob.onRunJob", "xxx");

        final Context appContext = getContext().getApplicationContext();

        ActivateProfileHelper.setMergedRingNotificationVolumes(appContext, false);

        DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
        ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
        aph.initialize(dataWrapper, appContext);

        Bundle bundle = params.getTransientExtras();
        
        int linkUnlink;
        if (ActivateProfileHelper.getMergedRingNotificationVolumes(appContext) && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(appContext))
            linkUnlink = bundle.getInt(ActivateProfileHelper.EXTRA_LINKUNLINK_VOLUMES, PhoneCallJob.LINKMODE_NONE);
        else
            linkUnlink = PhoneCallJob.LINKMODE_NONE;
        long profile_id = bundle.getLong(PPApplication.EXTRA_PROFILE_ID, 0);
        Profile profile = dataWrapper.getProfileById(profile_id);
        profile = Profile.getMappedProfile(profile, appContext);

        boolean forProfileActivation = bundle.getBoolean(ActivateProfileHelper.EXTRA_FOR_PROFILE_ACTIVATION, false);

        if (profile != null)
        {
            aph.setTones(profile);

            if (/*Permissions.checkProfileVolumePreferences(context, profile) &&*/
                    Permissions.checkProfileAccessNotificationPolicy(appContext, profile)) {

                aph.changeRingerModeForVolumeEqual0(profile);
                aph.changeNotificationVolumeForVolumeEqual0(profile);

                RingerModeChangeReceiver.internalChange = true;

                final AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);

                aph.setRingerMode(profile, audioManager, true, forProfileActivation);
                aph.setVolumes(profile, audioManager, linkUnlink, forProfileActivation);
                aph.setRingerMode(profile, audioManager, false, forProfileActivation);

                //try { Thread.sleep(500); } catch (InterruptedException e) { }
                //SystemClock.sleep(500);
                PPApplication.sleep(500);

                final Handler handler = new Handler(appContext.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PPApplication.logE("ExecuteVolumeProfilePrefsJob.onHandleIntent", "disable ringer mode change internal change");
                        RingerModeChangeReceiver.internalChange = false;
                    }
                }, 3000);

            }

            aph.setTones(profile);
        }
        dataWrapper.invalidateDataWrapper();

        return Result.SUCCESS;
    }

    static void start(long profile_id, int linkUnlinkVolumes, boolean forProfileActivation) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putLong(PPApplication.EXTRA_PROFILE_ID, profile_id);
        bundle.putInt(ActivateProfileHelper.EXTRA_LINKUNLINK_VOLUMES, linkUnlinkVolumes);
        bundle.putBoolean(ActivateProfileHelper.EXTRA_FOR_PROFILE_ACTIVATION, forProfileActivation);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .setTransientExtras(bundle)
                .startNow()
                .build()
                .schedule();
    }
    
}