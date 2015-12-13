package pl.nkg.notifier;

import android.app.Application;

import de.greenrobot.event.EventBus;
import pl.nkg.notifier.events.StatusUpdatedEvent;

public class NotifierApplication extends Application {

    private PreferencesProvider preferencesProvider;

    private boolean pending = false;

    @Override
    public void onCreate() {
        super.onCreate();
        preferencesProvider = new PreferencesProvider(this);
        EventBus.getDefault().register(this);
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
}
