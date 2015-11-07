package me.bolanleonifade.remoteconnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class SynchronisationReceiver extends BroadcastReceiver {
    public static String ACTION_ALARM = "me.bolanleonifade.remoteconnection.receiver.sync";

    public SynchronisationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        Bundle b = intent.getBundleExtra(SessionContract.CURRENT_SESSION);
        Intent syncService = new Intent(context, SynchronisationService.class);
        syncService.putExtra(SessionContract.CURRENT_SESSION, b);
        context.startService(syncService);
    }
}
