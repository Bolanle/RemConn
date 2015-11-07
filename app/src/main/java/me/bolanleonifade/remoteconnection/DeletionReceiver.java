package me.bolanleonifade.remoteconnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeletionReceiver extends BroadcastReceiver {
    public static String ACTION_DELETE = "me.bolanleonifade.remoteconnection.receiver.delete";
    public DeletionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        Intent deleteService = new Intent(context, DeletionService.class);
        context.startService(deleteService);
    }
}
