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
                //FIXME: zmienic na notify i to jak wykryje zmiane
                showNotify();
                //Toast.makeText(getApplicationContext(), "Sprawdzanie grafika PK...", Toast.LENGTH_LONG).show();

                preferencesProvider.setPrefErrorType(0);
                emitStatusUpdated(true);
                preferencesProvider.setPrefLastCheckedTime(System.currentTimeMillis());
                ParsedData newParsedData = fetchPage(PK_URL);
                ParsedData oldParsedData = fetchFromPreferences(preferencesProvider);
                if (!newParsedData.equals(oldParsedData)) {
                    storeChanged(preferencesProvider, newParsedData);
                    notifyChartChanged(oldParsedData, newParsedData);
                } else {
                    preferencesProvider.setPrefLastCheckedSuccessTime(System.currentTimeMillis());
                }
            } catch (IOException e) {
                Log.e(TAG, "Unable to download file: " + PK_URL.toString(), e);
                preferencesProvider.setPrefErrorType(1);
                preferencesProvider.setPrefErrorDetails(e.getLocalizedMessage());
            } catch (ParseException e) {
                Log.e(TAG, "Unable to parse downloaded file: " + PK_URL.toString(), e);
                preferencesProvider.setPrefErrorType(2);
                preferencesProvider.setPrefErrorDetails(e.getLocalizedMessage());
            } finally {
                emitStatusUpdated(false);

            }
        }
    }

    private void emitStatusUpdated(boolean pending) {
        EventBus.getDefault().post(new StatusUpdatedEvent(pending));
    }

    private void notifyChartChanged(ParsedData oldParsedData, ParsedData newParsedData) {
        boolean has = preferencesProvider.isPrefHasLastChecked();
        boolean firstStageNotify = preferencesProvider.isPrefEnabled(1) && (!has || !oldParsedData.getFirstStage().equals(newParsedData.getFirstStage()));
        boolean secondStageNotify = preferencesProvider.isPrefEnabled(2) && (!has || !oldParsedData.getSecondStage().equals(newParsedData.getSecondStage()));

        if (firstStageNotify || secondStageNotify) {

        }
    }

    private void showNotify() {
        Intent intent = new Intent(this, MainActivity.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

// build notification
// the addAction re-use the same intent to keep the example short
        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle("PK Schedule changed")
                .setContentText("I detect that your schedule on PK was changed.")
                .setSmallIcon(R.drawable.ic_menu_refresh)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                /*.addAction(R.drawable.ic_menu_refresh, "Call", pIntent)
                .addAction(R.drawable.ic_menu_refresh, "More", pIntent)
                .addAction(R.drawable.ic_menu_refresh, "And more", pIntent)*/.build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);
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
