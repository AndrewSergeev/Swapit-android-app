package il.co.freebie.swapit;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

/**
 * Created by one 1 on 23-Feb-19.
 */

public class FrequentNotifierService extends Service{
    public static final int NOTIF_ID = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        String channelId = "swapit_id";
        String channelName = "Swapit channel";
        if(Build.VERSION.SDK_INT>=26) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.paperclip).setContentTitle(getResources().getString(R.string.swapping_market));
        Intent newIntent = new Intent(this,MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_VIBRATE;
        startForeground(NOTIF_ID, notification);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntentForAlarm = PendingIntent.getService(this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + 30000,pendingIntentForAlarm);

        return super.onStartCommand(intent, flags, startId);
    }
}
