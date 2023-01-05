package com.weclont.shellgo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals(Intent.ACTION_REBOOT)) {
            Intent newIntent = new Intent(context, MainService.class);
            context.startService(newIntent);
        }
    }
}
