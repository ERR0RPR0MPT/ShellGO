package com.weclont.shellgo;

import android.app.Application;
import android.content.Context;

public class MainApplication extends Application {
    private static Context ServiceContext;

    public static Context getServiceContext() {
        return ServiceContext;
    }

    public static void setServiceContext(Context s) {
        ServiceContext = s;
    }
}
