package pl.nkg.notifier;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import pl.nkg.notifier.events.StatusUpdatedEvent;
import pl.nkg.notifier.receivers.AlarmReceiver;

public class NotifierApplication extends Application {

    private static final long REPEAT_TIME = 1000 * 60 * 60 * 3;

    private PreferencesProvider preferencesProvider;

    private boolean pending = false;

    private PendingIntent pendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        preferencesProvider = new PreferencesProvider(this);
        EventBus.getDefault().register(this);
        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
        updateBackgroundChecker();
    }

    public PreferencesProvider getPreferencesProvider() {
        return preferencesProvider;
    }

    public boolean isPending() {
        return pending;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(StatusUpdatedEvent event) {
        pending = event.isPending();
    }

    public void updateBackgroundChecker() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (preferencesProvider.isPrefEnabled()) {
            alarmManager.cancel(pendingIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), REPEAT_TIME, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
        }
    }
}
