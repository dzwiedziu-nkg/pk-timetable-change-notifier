package pl.nkg.notifier.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

    private final static String TAG = CheckChartService.class.getSimpleName();
    private final static URL PK_URL;

    private PreferencesProvider preferencesProvider;

    static {
        try {
            PK_URL = new URL("http://www.fmi.pk.edu.pl/?page=rozklady_zajec.php&nc");
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
                preferencesProvider.setPrefErrorType(1);
                preferencesProvider.setPrefErrorDetails(e.getLocalizedMessage());
                notifyScheduleCheckError(1, e.getLocalizedMessage());
            } catch (ParseException e) {
                Log.e(TAG, "Unable to parse downloaded file: " + PK_URL.toString(), e);
                preferencesProvider.setPrefErrorType(2);
                preferencesProvider.setPrefErrorDetails(e.getLocalizedMessage());
                notifyScheduleCheckError(2, e.getLocalizedMessage());
            } finally {
                emitStatusUpdated(false);
            }
        }
    }

    private void emitStatusUpdated(boolean pending) {
        EventBus.getDefault().post(new StatusUpdatedEvent(pending));
    }

    private void notifyScheduleChanged(ParsedData oldParsedData, ParsedData newParsedData) {
        boolean has = preferencesProvider.isPrefHasLastChecked();
        boolean firstStageNotify = preferencesProvider.isPrefEnabled(1) && (!has || !oldParsedData.getFirstStage().equals(newParsedData.getFirstStage()));
        boolean secondStageNotify = preferencesProvider.isPrefEnabled(2) && (!has || !oldParsedData.getSecondStage().equals(newParsedData.getSecondStage()));

        if (firstStageNotify || secondStageNotify) {
            CharSequence title = "PK schedule was changed!";
            CharSequence content = null;
            if (firstStageNotify && secondStageNotify) {
                content = "Schedule for informatics I and II degree of part-time studies was changed.";
            } else if (firstStageNotify) {
                content = "Schedule for informatics I degree of part-time studies was changed.";
            } else {
                content = "Schedule for informatics II degree of part-time studies was changed.";
            }
            showNotify(title, content, R.drawable.ic_menu_refresh, 0);
        }
    }

    private void notifyScheduleCheckError(int type, String error) {
        if (type == 0) {
            cancelNotify(1);
            return;
        }

        CharSequence title = "Unable to check that PK schedule was changed";
        CharSequence content = "Error details: " + error;
        showNotify(title, content, R.drawable.ic_menu_refresh, 1);
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
