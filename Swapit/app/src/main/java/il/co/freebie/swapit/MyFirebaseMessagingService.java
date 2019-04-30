package il.co.freebie.swapit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by one 1 on 23-Feb-19.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {

            String sender = remoteMessage.getData().get("sender");
            if(FirebaseAuth.getInstance().getCurrentUser() != null && sender!= null && !FirebaseAuth.getInstance().getUid().equals(sender)){
                NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                Notification.Builder builder = new Notification.Builder(this);

                if(Build.VERSION.SDK_INT>=26) {
                    NotificationChannel channel = new NotificationChannel("swapitmsg", "msg", NotificationManager.IMPORTANCE_HIGH);
                    manager.createNotificationChannel(channel);
                    builder.setChannelId("swapitmsg");
                }
                builder.setContentTitle(getResources().getString(R.string.new_message)).setContentText(remoteMessage.getData()
                        .get("message")).setSmallIcon(R.drawable.paperclipwhite).setAutoCancel(true);

                Intent intent = new Intent(this,MainActivity.class);
                intent.putExtra("cameFromNotification",true);
                PendingIntent pendingIntent = PendingIntent.getActivity(this,3,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);
                manager.notify(4,builder.build());
            }
        }
    }
}
