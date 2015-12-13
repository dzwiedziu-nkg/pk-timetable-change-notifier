package pl.nkg.notifier.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Date;

import de.greenrobot.event.EventBus;
import pl.nkg.notifier.NotifierApplication;
import pl.nkg.notifier.PreferencesProvider;
import pl.nkg.notifier.R;
import pl.nkg.notifier.events.StatusUpdatedEvent;
import pl.nkg.notifier.services.CheckChartService;

import static pl.nkg.notifier.PreferencesProvider.longToDateOrNull;

public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener {

    private MainFragment fragment;
    private NotifierApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (NotifierApplication) getApplication();
        setContentView(R.layout.activity_main);
        fragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    private void update() {
        PreferencesProvider preferencesProvider = application.getPreferencesProvider();
        fragment.setLastCheckedDate(longToDateOrNull(preferencesProvider.getPrefLastCheckedSuccessTime()));
        fragment.setLastCheckedErrorDate(preferencesProvider.getPrefErrorType() == 0 ? null : new Date(preferencesProvider.getPrefLastCheckedTime()));
        fragment.setStageTimetableChanged(1, preferencesProvider.getPrefLastChangedDate(1), preferencesProvider.getPrefLastCheckedUrl(1));
        fragment.setStageTimetableChanged(2, preferencesProvider.getPrefLastChangedDate(2), preferencesProvider.getPrefLastCheckedUrl(2));
        fragment.setPendingStatus(application.isPending());
    }

    public void onEventMainThread(StatusUpdatedEvent event) {
        update();
    }

    @Override
    public void onClickChangedKnown() {
    }

    @Override
    public void onClickErrorKnown() {

    }

    @Override
    public void onClickCheckNow() {
        CheckChartService.startService(this);
    }

    @Override
    public void onClickTimetableURL(int stage) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(application.getPreferencesProvider().getPrefLastCheckedUrl(stage).toString())));
    }

    @Override
    public void onClickErrorDetail() {

    }
}
