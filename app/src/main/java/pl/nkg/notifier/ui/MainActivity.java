package pl.nkg.notifier.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import pl.nkg.notifier.BuildConfig;
import pl.nkg.notifier.NotifierApplication;
import pl.nkg.notifier.PreferencesProvider;
import pl.nkg.notifier.R;
import pl.nkg.notifier.events.StatusUpdatedEvent;
import pl.nkg.notifier.services.CheckChartService;

import static pl.nkg.notifier.PreferencesProvider.longToDateOrNull;

public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener {

    private final static SimpleDateFormat BUILD_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private MainFragment fragment;
    private NotifierApplication application;
    private TextView versionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (NotifierApplication) getApplication();
        setContentView(R.layout.activity_main);
        fragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        versionTextView = (TextView) findViewById(R.id.versionTextView);
        updateVersionInfo();
    }

    private void updateVersionInfo() {
        CharSequence versionString = "Version";
        versionTextView.setText(versionString + ": " + BuildConfig.VERSION_NAME + " (" + BUILD_DATE.format(new Date(BuildConfig.TIMESTAMP)) + ")");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_check_now:
                onClickCheckNow();
                return true;
        }

        return super.onOptionsItemSelected(item);
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
    public void onClickCheckNow() {
        CheckChartService.startService(this);
    }

    @Override
    public void onClickTimetableURL(int stage) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(application.getPreferencesProvider().getPrefLastCheckedUrl(stage).toString())));
    }
}
