package pl.nkg.notifier.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import pl.nkg.notifier.services.CheckChartService;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        CheckChartService.startService(context);
    }
}
