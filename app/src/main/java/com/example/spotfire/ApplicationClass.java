package com.example.spotfire;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.onesignal.OSNotification;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

public class ApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Logging set to help debug issues, remove before releasing your app.
        //OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.WARN);

        OneSignal.startInit(this)
                .setNotificationReceivedHandler(new ExampleNotificationReceivedHandler())
                .setNotificationOpenedHandler(new ExampleNotificationOpenedHandler())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
    }

    private class ExampleNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
        @Override
        public void notificationReceived(OSNotification notification) {
            JSONObject data = notification.payload.additionalData;
            String notificationID = notification.payload.notificationID;
            String title = notification.payload.title;
            String body = notification.payload.body;
            String smallIcon = notification.payload.smallIcon;
            String largeIcon = notification.payload.largeIcon;
            String bigPicture = notification.payload.bigPicture;
            String smallIconAccentColor = notification.payload.smallIconAccentColor;
            String sound = notification.payload.sound;
            String ledColor = notification.payload.ledColor;
            int lockScreenVisibility = notification.payload.lockScreenVisibility;
            String groupKey = notification.payload.groupKey;
            String groupMessage = notification.payload.groupMessage;
            String fromProjectNumber = notification.payload.fromProjectNumber;
            String rawPayload = notification.payload.rawPayload;

            String customKey;

            Log.i("OneSignalExample", "NotificationID received: " + notificationID);

            if (data != null) {
                customKey = data.optString("customkey", null);
                if (customKey != null)
                    Log.i("OneSignalExample", "customkey set with value: " + customKey);
            }
        }
    }


    private class ExampleNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            JSONObject data = result.notification.payload.additionalData;

            double Latitude = 0, Longitude = 0;

            if (data != null) {
                Latitude = data.optDouble("Latitude");
                Longitude = data.optDouble("Longitude");
            }
            Intent intent = new Intent(getApplicationContext(), FireMapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("Latitude", Latitude);
            intent.putExtra("Longitude", Longitude);
            startActivity(intent);

        }
    }

}
