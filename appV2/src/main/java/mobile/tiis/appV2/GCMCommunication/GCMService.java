package mobile.tiis.appv2.GCMCommunication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import mobile.tiis.appv2.HomeActivityRevised;
import mobile.tiis.appv2.R;
import mobile.tiis.appv2.StockActivityRevised;
import mobile.tiis.appv2.base.BackboneApplication;

public class GCMService extends GCMBaseIntentService {
    private String tone;
    static final public String SynchronisationService_RESULT = "mobile,giis.appv2.CheckForChangesSynchronisationService.REQUEST_PROCESSED";
    static final public String SynchronisationService_MESSAGE = "mobile,giis.appv2.CheckForChangesSynchronisationService..MSG";


    private static final String TAG = "GCMService";
	public GCMService() {
        super(CommonUtilities.SENDER_ID);
        this.tone="";
	}
	
	@Override
	protected void onRegistered(Context context, String regId) {
		Log.d(TAG, "Sending Registration Id to TIIS server = "+regId);
	}
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

	@Override
	protected void onUnregistered(Context context, String regId) {
		Log.d(TAG, "Device unregistered");
        ServerUtilities.unregister(context, regId);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
        Log.d(TAG, "message received");

        BackboneApplication application = (BackboneApplication) getApplication();
        String gcmMessage = intent.getStringExtra("message");
        Log.d(TAG, "Message Received "+gcmMessage);

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        int month       = (calendar.get(Calendar.MONTH)+1);
        int prevMonth   = month - 1;
        if (month == 1){
            prevMonth = 12;
        }
        int year = calendar.get(Calendar.YEAR);

        //HEALTH FACILITY COLD CHAIN
        if (gcmMessage.equals("UpdateHealthFacilityColdChain")){
            application.parseColdChainMonthly(month+"", year+"");
            application.parseColdChainMonthly(prevMonth+"", year+"");

        }
        //DESEASES SURVEILLANCE
        else if (gcmMessage.equals("UpdateHealthFacilityDeseaseSurvailance")){
            application.parseDeseaseSurveilanceMonthly(month+"", year+"");
            application.parseDeseaseSurveilanceMonthly(prevMonth+"", year+"");
        }
        //BCG_OPV_TT
        else if(gcmMessage.equals("UpdateHealthFacilityBcgOpv0AndTTVaccinations")){
            application.parseBcgOpvTtMonthly(month+"", year+"");
            application.parseBcgOpvTtMonthly(prevMonth+"", year+"");
        }
        //SYRINGES AND SAFETY BOXES
        else if(gcmMessage.equals("UpdateHealthFacilitySyringesAndSafetyBoxesStockBalance")){
            application.parseSyringesAndSafetyBoxesMonthly(month+"", year+"");
            application.parseSyringesAndSafetyBoxesMonthly(prevMonth+"", year+"");
        }
        //VITAMIN A STOCK
        else if(gcmMessage.equals("UpdateHealthFacilityVitaminAStockBalance")){
            application.parseVitamAMonthly(month+"", year+"");
            application.parseVitamAMonthly(prevMonth+"", year+"");
        }
        //IMMUNIZATION SESSIONS
        else if(gcmMessage.equals("UpdateHealthFacilityImmunizationSessions")){
            Log.d("IMMUNIZATION_SESSION", "month is : "+month);
            Log.d("IMMUNIZATION_SESSION", "previous month is : "+prevMonth);
            application.parseImmunizationSessionMonthly(month+"", year+"");
            application.parseImmunizationSessionMonthly(prevMonth+"", year+"");
        }
        //STOCK DISTRIBUTIONS
        else if(gcmMessage.equals("newHealthFacilityStockDistributions")){
            Log.d(TAG,"receiving new Heath facility stock distributions");
            application.parseItem();
            application.parseScheduledVaccination();
            application.parseDose();
            application.parseItemLots();
            application.parseStock();
            application.parseStockDistributions();
            createNotification(context,"New Stock Received");
        }

        else if(gcmMessage.equals("proofOfDeliverySentSuccessfully")){
            Log.d(TAG,"updating Heath facility stock distributions");
            application.parseItem();
            application.parseScheduledVaccination();
            application.parseDose();
            application.parseItemLots();
            application.parseStock();
            application.parseStockDistributions();
            createNotification(context,"Proof Of Delivery Sent Successfully");
        }
        else{
            application.getDatabaseInstance().addChildToChildUpdatesQueue(gcmMessage,3);
            synchronized (application) {
                application.parseGCMChildrenInQueueById();
            }
            sendResult(gcmMessage,context);

        }
//        createNotification(context,"updates received");

	}

    public void sendResult(String message,Context context) {
        try {
            Intent intent = new Intent(CommonUtilities.DISPLAY_MESSAGE_ACTION);
            if (message != null)
                intent.putExtra(SynchronisationService_MESSAGE, message);
            context.sendBroadcast(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

	@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.d(TAG, "Received deleted messages notification, count: " + total);
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.d(TAG, "Received error: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		Log.d(TAG, "Received recoverable error: " + errorId);
		return super.onRecoverableError(context, errorId);
	}

    /**
     * Issues a notification to inform the user that server has sent a message.
     */



    public static void createNotification(Context context, String message) {
        Log.d(TAG, "creating notification");

        Intent intent;
        PendingIntent pIntent;

        int notificationId = new Random().nextInt(1000);
        Log.d(TAG, "Creating notification with ID == " + notificationId);

        Log.d(TAG,"the activity is not running");
        intent = new Intent(context, StockActivityRevised.class);
        intent.putExtra("stock_distribution",3);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);



        long[] pattern = {500,500};
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if(alarmSound == null){
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        Notification noti;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            noti= new Notification.Builder(context)
                    .setContentTitle(context.getResources().getString(R.string.app_name))
                    .setContentText(message)
                    .setSmallIcon(R.drawable.launcher_icon)
                    .setColor(context.getResources().getColor(R.color.green_500))
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setStyle(new Notification.BigTextStyle().bigText(message))
                    .setVibrate(pattern)
                    .setSound(alarmSound)
                    .build();
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                noti= new Notification.Builder(context)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setContentText(message)
                        .setSmallIcon(R.drawable.launcher_icon)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .setStyle(new Notification.BigTextStyle().bigText(message))
                        .setVibrate(pattern)
                        .setSound(alarmSound)
                        .build();
            }else{
                noti= new Notification(R.drawable.launcher_icon,message,Notification.FLAG_AUTO_CANCEL);
            }
        }


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        noti.flags |= Notification.FLAG_AUTO_CANCEL;


        Log.d(TAG, "setting notification id == "+notificationId);
        notificationManager.notify(notificationId, noti);

    }



}
