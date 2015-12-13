package pl.nkg.notifier;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class PreferencesProvider {
    public final static String PREF_ENABLED = "enabled";
    public final static String PREF_VIBRATION = "vibration";
    public final static String PREF_SOUND = "sound";
    public final static String PREF_HAS_LAST_CHECKED = "has";
    public final static String PREF_LAST_CHECKED_TIME = "checked";
    public final static String PREF_LAST_CHECKED_SUCCESS_TIME = "success";
    public final static String PREF_LAST_CHECKED_HASH = "hash";
    public final static String PREF_LAST_CHECKED_URL = "url";
    public final static String PREF_LAST_CHANGED_DATE = "date";
    public final static String PREF_ERROR_TYPE = "error";
    public final static String PREF_ERROR_DETAILS = "details";


    private SharedPreferences sharedPreferences;

    public PreferencesProvider(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Automatyczne sprawdzanie w tle jest aktywne dla studiów stopnia &quot;stage&quot;.
     *
     * @param stage 1 - pierwszy stopień, 2 - drugi stopień
     * @return true - aktywne, false - nieaktywne
     */
    public boolean isPrefEnabled(int stage) {
        return sharedPreferences.getBoolean(PREF_ENABLED + "_" + stage, true);
    }

    public void setPrefEnabled(int stage, boolean enabled) {
        apply(sharedPreferences.edit().putBoolean(PREF_ENABLED + "_" + stage, enabled));
    }

    public boolean isPrefVibration() {
        return sharedPreferences.getBoolean(PREF_VIBRATION, true);
    }

    public boolean isPrefSound() {
        return sharedPreferences.getBoolean(PREF_SOUND, true);
    }

    /**
     * Automatyczne sprawdzanie w tle jest aktywne w ogóle (dla co najmniej jednego stopnia studiów).
     *
     * @return true - aktywne, false - nieaktywne
     */
    public boolean isPrefEnabled() {
        return isPrefEnabled(1) || isPrefEnabled(2);
    }

    /**
     * Data ostatniej próby sprawdzenia czy się zmienił grafik (nie zależnie czy się powiodła czy nie).
     *
     * @return timestamp
     */
    public long getPrefLastCheckedTime() {
        return sharedPreferences.getLong(PREF_LAST_CHECKED_TIME, 0);
    }

    public void setPrefLastCheckedTime(long time) {
        apply(sharedPreferences.edit().putLong(PREF_LAST_CHECKED_TIME, time));
    }

    /**
     * Czy kiedykolwiek dokonano sprawdzania aktualności grafika.
     *
     * @return true - tak, false - nie
     */
    public boolean isPrefHasLastChecked() {
        return getPrefLastCheckedSuccessTime() != 0;
    }

    /**
     * Data ostatniej pomyślnej próby sprawdzenia czy zmienił się grafik.
     *
     * @return timestamp
     */
    public long getPrefLastCheckedSuccessTime() {
        return sharedPreferences.getLong(PREF_LAST_CHECKED_SUCCESS_TIME, 0);
    }

    public void setPrefLastCheckedSuccessTime(long time) {
        apply(sharedPreferences.edit().putLong(PREF_LAST_CHECKED_SUCCESS_TIME, time));
    }

    @Deprecated
    public String getPrefLastCheckedHash(int stage) {
        return sharedPreferences.getString(PREF_LAST_CHECKED_HASH + "_" + stage, "");
    }

    @Deprecated
    public void setPrefLastCheckedHash(int stage, String hash) {
        apply(sharedPreferences.edit().putString(PREF_LAST_CHECKED_HASH + "_" + stage, hash));
    }

    /**
     * Adres URL do grafika wyciągnięty ze strony PK.
     *
     * @param stage 1 - pierwszy stopień, 2 - drugi stopień
     * @return URL do grafiku
     */
    public URL getPrefLastCheckedUrl(int stage) {
        try {
            String url = sharedPreferences.getString(PREF_LAST_CHECKED_URL + "_" + stage, null);
            return url == null ? null : new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPrefLastCheckedUrl(int stage, URL url) {
        apply(sharedPreferences.edit().putString(PREF_LAST_CHECKED_URL + "_" + stage, url.toString()));
    }

    /**
     * Data akutalizacji grafika odczytana ze strony PK.
     *
     * @param stage 1 - pierwszy stopień, 2 - drugi stopień
     * @return data zmiany lub null gdy nigdy nie sprawdzano
     */
    public Date getPrefLastChangedDate(int stage) {
        return longToDateOrNull(sharedPreferences.getLong(PREF_LAST_CHANGED_DATE + "_" + stage, 0));
    }

    public void setPrefLastChangedDate(int stage, Date date) {
        apply(sharedPreferences.edit().putLong(PREF_LAST_CHANGED_DATE + "_" + stage, date.getTime()));
    }

    /**
     * Typ błędu jaki był podczas ostatniego sprawdzania grafiku.
     *
     * @return 0 - brak błędu, >= 1 - był błąd
     */
    public int getPrefErrorType() {
        return sharedPreferences.getInt(PREF_ERROR_TYPE, 0);
    }

    public void setPrefErrorType(int type) {
        apply(sharedPreferences.edit().putInt(PREF_ERROR_TYPE, type));
    }

    /**
     * Dodatkowe informacje na temat komunikatu o błędzie np. komunikat jaki zwrócił serwer albo system operacyjny smartfona.
     *
     * @return komunikat o błędzie
     */
    public String getPrefErrorDetails() {
        return sharedPreferences.getString(PREF_ERROR_DETAILS, "");
    }

    public void setPrefErrorDetails(String details) {
        apply(sharedPreferences.edit().putString(PREF_ERROR_DETAILS, details));
    }

    private static void apply(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT >= 9) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public static Date longToDateOrNull(long date) {
        return date == 0 ? null : new Date(date);
    }
}
