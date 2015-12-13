package pl.nkg.notifier;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import de.greenrobot.event.EventBus;
import pl.nkg.notifier.events.StatusUpdatedEvent;
import pl.nkg.notifier.services.AlarmReceiver;

public class NotifierApplication extends Application {

    private static final long REPEAT_TIME = 1000 * 15;

    private PreferencesProvider preferencesProvider;

    private boolean pending = false;

    @Override
    public void onCreate() {
        super.onCreate();
        preferencesProvider = new PreferencesProvider(this);
        EventBus.getDefault().register(this);
        updateBackgroundChecker();
    }

    public PreferencesProvider getPreferencesProvider() {
        return preferencesProvider;
    }

    public boolean isPending() {
        return pending;
    }

    public void onEventMainThread(StatusUpdatedEvent event) {
        pending = event.isPending();
    }

    public void updateBackgroundChecker() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pending = PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);

        if (preferencesProvider.isPrefEnabled()) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), REPEAT_TIME, pending);
        } else {
            alarmManager.cancel(pending);
        }
    }
}
