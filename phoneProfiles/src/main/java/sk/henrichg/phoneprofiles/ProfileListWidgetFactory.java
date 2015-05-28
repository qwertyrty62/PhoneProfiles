package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

@SuppressLint("NewApi")
public class ProfileListWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

	private DataWrapper dataWrapper;

	private Context context=null;
	//private int appWidgetId;
	private List<Profile> profileList;

	public ProfileListWidgetFactory(Context ctxt, Intent intent) {
		context = ctxt;
		/*appWidgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                       AppWidgetManager.INVALID_APPWIDGET_ID); */
	}
  
	public void createProfilesDataWrapper()
	{
		GlobalData.loadPreferences(context);
		
		int monochromeValue = 0xFF;
		if (GlobalData.applicationWidgetListIconLightness.equals("0")) monochromeValue = 0x00;
		if (GlobalData.applicationWidgetListIconLightness.equals("25")) monochromeValue = 0x40;
		if (GlobalData.applicationWidgetListIconLightness.equals("50")) monochromeValue = 0x80;
		if (GlobalData.applicationWidgetListIconLightness.equals("75")) monochromeValue = 0xC0;
		if (GlobalData.applicationWidgetListIconLightness.equals("100")) monochromeValue = 0xFF;

		if (dataWrapper == null)
		{
			dataWrapper = new DataWrapper(context, true,  
														GlobalData.applicationWidgetListIconColor.equals("1"), 
														monochromeValue);
		}
		else
		{
			dataWrapper.setParameters(true, GlobalData.applicationWidgetListIconColor.equals("1"), 
														monochromeValue);
		}
	}
	
	public void onCreate() {
	}
  
	public void onDestroy() {
		if (dataWrapper != null)
			dataWrapper.invalidateDataWrapper();
		dataWrapper = null;
	}

	public int getCount() {
        if (profileList != null)
		    return(profileList.size());
        else
            return 0;
	}

	public RemoteViews getViewAt(int position) {
		
		RemoteViews row;
		if (!GlobalData.applicationWidgetListGridLayout)
			row=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_item);
		else
			row=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget_item);
    
		Profile profile = profileList.get(position);

		if (profile.getIsIconResourceID())
		{
			row.setImageViewResource(R.id.widget_profile_list_item_profile_icon, 
					context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable", context.getPackageName()));
		}
		else
		{
    		row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, profile._iconBitmap);
		}
		if ((!GlobalData.applicationWidgetListHeader) && (profile._checked))
		{
			// hm, interesting, how to set bold style for RemoteView text ;-)
			String profileName = profile._name;
			if ((profile._duration > 0) && (profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING))
				profileName = "[" + profile._duration + "] " + profileName;
			Spannable sb = new SpannableString(profileName);
			sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, profileName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			row.setTextViewText(R.id.widget_profile_list_item_profile_name, sb);
		}	
		else {
			String profileName = profile._name;
			if ((profile._duration > 0) && (profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING))
				profileName = "[" + profile._duration + "] " + profileName;
			row.setTextViewText(R.id.widget_profile_list_item_profile_name, profileName);
		}
		int red = 0xFF;
		int green = 0xFF;
		int blue = 0xFF;
		if (GlobalData.applicationWidgetListLightnessT.equals("0")) red = 0x00;
		if (GlobalData.applicationWidgetListLightnessT.equals("25")) red = 0x40;
		if (GlobalData.applicationWidgetListLightnessT.equals("50")) red = 0x80;
		if (GlobalData.applicationWidgetListLightnessT.equals("75")) red = 0xC0;
		if (GlobalData.applicationWidgetListLightnessT.equals("100")) red = 0xFF;
		green = red; blue = red;
		if (!GlobalData.applicationWidgetListHeader)
		{
			if (profile._checked)
			{
		    	if (android.os.Build.VERSION.SDK_INT >= 16)
		    		row.setTextViewTextSize(R.id.widget_profile_list_item_profile_name, TypedValue.COMPLEX_UNIT_SP, 17);
		    	
		        //if (GlobalData.applicationWidgetListIconColor.equals("1"))
					row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
		        //else
				//	row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.parseColor("#33b5e5"));
			}
			else
			{
		    	if (android.os.Build.VERSION.SDK_INT >= 16)
		    		row.setTextViewTextSize(R.id.widget_profile_list_item_profile_name, TypedValue.COMPLEX_UNIT_SP, 15);
				
		        //if (GlobalData.applicationWidgetListIconColor.equals("1"))
		        	row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xCC, red, green, blue));
		        //else
		        //	row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
			}
		}
		else
		{
			row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
		}
		if (!GlobalData.applicationWidgetListGridLayout)
		{
			if (GlobalData.applicationWidgetListPrefIndicator)
				row.setImageViewBitmap(R.id.widget_profile_list_profile_pref_indicator, profile._preferencesIndicator);
			else
				row.setImageViewResource(R.id.widget_profile_list_profile_pref_indicator, R.drawable.ic_empty);
		}

		Intent i=new Intent();
		Bundle extras=new Bundle();
    
		extras.putLong(GlobalData.EXTRA_PROFILE_ID, profile._id);
		extras.putInt(GlobalData.EXTRA_START_APP_SOURCE, GlobalData.STARTUP_SOURCE_SHORTCUT);
		i.putExtras(extras);
		row.setOnClickFillInIntent(R.id.widget_profile_list_item, i);

		return(row);
	}

	public RemoteViews getLoadingView() {
		return(null);
	}
  
	public int getViewTypeCount() {
		return(1);
	}

	public long getItemId(int position) {
		return(position);
	}

	public boolean hasStableIds() {
		return(true);
	}

	public void onDataSetChanged() {
		createProfilesDataWrapper();
		
		dataWrapper.clearProfileList();
		profileList = dataWrapper.getProfileList();
	}
}