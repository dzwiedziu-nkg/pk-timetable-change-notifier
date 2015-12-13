package pl.nkg.notifier.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import de.greenrobot.event.EventBus;
import pl.nkg.notifier.NotifierApplication;
import pl.nkg.notifier.PreferencesProvider;
import pl.nkg.notifier.R;
import pl.nkg.notifier.events.StatusUpdatedEvent;
import pl.nkg.notifier.parser.ParsedData;
import pl.nkg.notifier.parser.ParsedEntity;
import pl.nkg.notifier.parser.WebParser;
import pl.nkg.notifier.ui.MainActivity;

public class CheckChartService extends IntentService {

    private final static String PK_URL_STRING = "http://www.fmi.pk.edu.pl/?page=rozklady_zajec.php&nc";

    private final static String TAG = CheckChartService.class.getSimpleName();
    private final static URL PK_URL;

    private PreferencesProvider preferencesProvider;

    static {
        try {
            PK_URL = new URL(PK_URL_STRING);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public CheckChartService() {
        super("CheckChartService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferencesProvider = ((NotifierApplication) getApplication()).getPreferencesProvider();
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, CheckChartService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (!isOnline()) {
                preferencesProvider.setPrefErrorType(1);
                preferencesProvider.setPrefErrorDetails("");
                notifyScheduleCheckError(1, "");
                emitStatusUpdated(false);
                return;
            }

            try {
                preferencesProvider.setPrefErrorType(0);
                emitStatusUpdated(true);
                preferencesProvider.setPrefLastCheckedTime(System.currentTimeMillis());
                ParsedData newParsedData = fetchPage(PK_URL);
                ParsedData oldParsedData = fetchFromPreferences(preferencesProvider);
                if (!newParsedData.equals(oldParsedData)) {
                    storeChanged(preferencesProvider, newParsedData);
                    notifyScheduleChanged(oldParsedData, newParsedData);
                } else {
                    preferencesProvider.setPrefLastCheckedSuccessTime(System.currentTimeMillis());
                }
                cancelNotify(1);
            } catch (IOException e) {
                Log.e(TAG, "Unable to download file: " + PK_URL.toString(), e);
                preferencesProvider.setPrefErrorType(2);
                preferencesProvider.setPrefErrorDetails(e.getLocalizedMessage());
                notifyScheduleCheckError(2, e.getLocalizedMessage());
            } catch (ParseException e) {
                Log.e(TAG, "Unable to parse downloaded file: " + PK_URL.toString(), e);
                preferencesProvider.setPrefErrorType(3);
                preferencesProvider.setPrefErrorDetails(e.getLocalizedMessage());
                notifyScheduleCheckError(3, e.getLocalizedMessage());
            } finally {
                emitStatusUpdated(false);
            }
        }
    }

    private void emitStatusUpdated(boolean pending) {
        notifyChecking(pending);
        EventBus.getDefault().post(new StatusUpdatedEvent(pending));
    }

    private void notifyScheduleChanged(ParsedData oldParsedData, ParsedData newParsedData) {
        boolean has = preferencesProvider.isPrefHasLastChecked();
        boolean firstStageNotify = preferencesProvider.isPrefEnabled(1) && (!has || !newParsedData.getFirstStage().equals(oldParsedData.getFirstStage()));
        boolean secondStageNotify = preferencesProvider.isPrefEnabled(2) && (!has || !newParsedData.getSecondStage().equals(oldParsedData.getSecondStage()));

        if (firstStageNotify || secondStageNotify) {
            CharSequence title = getString(R.string.notify_title_schedule_changed);
            CharSequence content = null;
            if (firstStageNotify && secondStageNotify) {
                content = getString(R.string.notify_content_both_changed);
            } else if (firstStageNotify) {
                content = getString(R.string.notify_content_degree_I_changed);
            } else {
                content = getString(R.string.notify_content_degree_II_changed);
            }
            showNotify(title, content, R.drawable.ic_stat_changed, 0);
        }
    }

    private void notifyScheduleCheckError(int type, String error) {
        if (type == 0) {
            cancelNotify(1);
            return;
        }

        CharSequence title = getString(R.string.notify_error_title);
        String content = getResources().getStringArray(R.array.error_type_array)[type - 1];
        CharSequence details = error.length() == 0 ? "" : "\n\n" + error;
        showNotify(title, content + details, R.drawable.ic_stat_notification_sync_problem, 1);
    }

    private void notifyChecking(boolean visible) {
        if (visible) {
            showNotify(getString(R.string.notify_title_checking), getString(R.string.notify_content_checking), R.drawable.ic_stat_notification_sync, 2);
        } else {
            cancelNotify(2);
        }
    }

    private void showNotify(CharSequence title, CharSequence content, int icon, int id) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(icon)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(id, n);
    }

    private void cancelNotify(int id) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    private static ParsedData fetchPage(URL url) throws IOException, ParseException {
        WebParser webParser = new WebParser();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        try {
            InputStream inputStream = connection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            return webParser.parse(rd);
        } finally {
            connection.disconnect();
        }

    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private static ParsedData fetchFromPreferences(PreferencesProvider preferencesProvider) {
        ParsedData parsedData = new ParsedData();
        if (preferencesProvider.isPrefHasLastChecked()) {
            parsedData.setFirstStage(new ParsedEntity(preferencesProvider.getPrefLastCheckedUrl(1), preferencesProvider.getPrefLastChangedDate(1)));
            parsedData.setSecondStage(new ParsedEntity(preferencesProvider.getPrefLastCheckedUrl(2), preferencesProvider.getPrefLastChangedDate(2)));
        }
        return parsedData;
    }

    private static void storeChanged(PreferencesProvider preferencesProvider, ParsedData newParsedData) {
        preferencesProvider.setPrefLastChangedDate(1, newParsedData.getFirstStage().getDate());
        preferencesProvider.setPrefLastCheckedUrl(1, newParsedData.getFirstStage().getUrl());
        preferencesProvider.setPrefLastChangedDate(2, newParsedData.getSecondStage().getDate());
        preferencesProvider.setPrefLastCheckedUrl(2, newParsedData.getSecondStage().getUrl());
        preferencesProvider.setPrefLastCheckedSuccessTime(System.currentTimeMillis());
        preferencesProvider.setPrefLastCheckedTime(System.currentTimeMillis());
    }
}
