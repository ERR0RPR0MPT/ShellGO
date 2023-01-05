package com.weclont.shellgo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainService extends Service {

    public static final int NOTICE_ID = 6679;
    private static final String TAG = "MainService";
    private static Boolean isServiceDestroyed = false;
    private static final List<Process> ProcessPool = new ArrayList<>();
    private static final List<Thread> ThreadPool = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        MainApplication.setServiceContext(this);
        String configJsonStr = FileUtil.getFile("config.json");
        if (Objects.equals(configJsonStr, "") || configJsonStr == null) {
            isServiceDestroyed = true;
            stopSelf();
            return;
        }
        try {
            FileUtil.deletefile("log.txt");
            JSONObject configJson = new JSONObject(configJsonStr);
            JSONArray configCommands = configJson.getJSONArray("commands");
            for (int i = 0; i < configCommands.length(); i++) {
                JSONObject jsonObject = configCommands.getJSONObject(i);
                String name = jsonObject.getString("name");
                String cmd = jsonObject.getString("cmd");
                String execCmd = jsonObject.getString("exec-cmd");
                runForegroundCommand(cmd, name, execCmd);
                FileUtil.deletefile(name + "-log.txt");
                String log = "Start Running: name=" + name + ", command=" + cmd;
                Log.e(TAG, "onCreate: " + log);
                Toast.makeText(getApplicationContext(), log, Toast.LENGTH_SHORT).show();
                FileUtil.inputLineLog(log);
            }
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: JSONException, " + e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra("isServiceDestroyed", false)) {
            isServiceDestroyed = true;
            stopSelf();
            return START_NOT_STICKY;
        }
        if (intent.getBooleanExtra("isUserCommand", false)) {
            try {
                String[] cmdText = intent.getStringExtra("UserCommand").split(" ");
                String configJsonStr = FileUtil.getFile("config.json");
                if (Objects.equals(configJsonStr, "") || configJsonStr == null) {
                    isServiceDestroyed = true;
                    stopSelf();
                    return START_NOT_STICKY;
                }
                JSONObject configJson = new JSONObject(configJsonStr);
                JSONArray configCommands = configJson.getJSONArray("commands");
                for (int i = 0; i < configCommands.length(); i++) {
                    JSONObject jsonObject = configCommands.getJSONObject(i);
                    String name = jsonObject.getString("name");
                    if (!name.equals(cmdText[1])) {
                        continue;
                    }
                    if (Objects.equals(cmdText[0], "start")) {
                        String cmd = jsonObject.getString("cmd");
                        String execCmd = jsonObject.getString("exec-cmd");
                        runForegroundCommand(cmd, name, execCmd);
                        String log = "Start Running: name=" + name + ", command=" + cmd;
                        Log.e(TAG, log);
                        Toast.makeText(getApplicationContext(), log, Toast.LENGTH_SHORT).show();
                        FileUtil.inputLineLog(log);
                    }
                    if (Objects.equals(cmdText[0], "stop")) {
                        String stop = jsonObject.getString("stop");
                        String execStop = jsonObject.getString("exec-stop");
                        runBackgroundCommand(stop, execStop);
                        String log = "Stop Running: name=" + name + ", stop=" + stop;
                        Log.e(TAG, log);
                        Toast.makeText(getApplicationContext(), log, Toast.LENGTH_SHORT).show();
                        FileUtil.inputLineLog(log);
                    }
                    return START_STICKY;
                }
                String log = cmdText[1]+" is not found, please check your configure";
                Log.e(TAG, log);
                Toast.makeText(getApplicationContext(), log, Toast.LENGTH_SHORT).show();
                FileUtil.inputLineLog(log);
                return START_STICKY;
            } catch (JSONException e) {
                Log.e(TAG, "JSONException, " + e.getMessage());
            }
        }
        // Set Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            String id = "ShellGo-Notification";
            CharSequence name = "ShellGO";
            String description = "Now ShellGO is RUNNING...";
            NotificationChannel notificationChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(description);
            manager.createNotificationChannel(notificationChannel);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, id)
                    .setContentTitle("ShellGO")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("Now ShellGO is RUNNING..."))
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_baseline_code_24)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_code_24))
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX);
            startForeground(NOTICE_ID, mBuilder.build());
        }
        Log.e(TAG, "onStartCommand: Notified OK");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationManager mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mManager.cancel(NOTICE_ID);
        for (int i = 0; i < ProcessPool.size(); i++) {
            try {
                ProcessPool.get(i).destroy();
            } catch (Exception ignored) {
            }
        }
        for (int i = 0; i < ThreadPool.size(); i++) {
            try {
                ThreadPool.get(i).interrupt();
            } catch (Exception ignored) {
            }
        }
        try {
            String configJsonStr = FileUtil.getFile("config.json");
            if (Objects.equals(configJsonStr, "") || configJsonStr == null) {
                return;
            }
            JSONObject configJson = new JSONObject(configJsonStr);
            JSONArray configCommands = configJson.getJSONArray("commands");
            for (int i = 0; i < configCommands.length(); i++) {
                JSONObject jsonObject = configCommands.getJSONObject(i);
                String name = jsonObject.getString("name");
                String execStop = jsonObject.getString("exec-stop");
                String stop = jsonObject.getString("stop");
                if (stop.equals("")){
                    continue;
                }
                runBackgroundCommand(stop, execStop);
                String log = "Stop Running: name=" + name + ", stop=" + stop;
                Log.e(TAG, "onDestroy: " + log);
                Toast.makeText(getApplicationContext(), log, Toast.LENGTH_SHORT).show();
                FileUtil.inputLineLog(log);
            }
        } catch (JSONException e) {
            Log.e(TAG, "onDestroy: JSONException, " + e.getMessage());
        }
        if (!isServiceDestroyed) {
            Log.e(TAG, "onDestroy: MainService was killed, restarting...");
            // Restart
            Intent intent = new Intent(this, MainService.class);
            startService(intent);
        }
        Log.e(TAG, "onDestroy: MainService was killed, closing...");
    }

    public void runForegroundCommand(String command, String name, String execCmd) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(execCmd);
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.flush();
            os.close();
            ProcessPool.add(process);
            Process finalProcess = process;
            Thread t = new Thread(() -> {
                BufferedReader ios = new BufferedReader(new InputStreamReader(finalProcess.getInputStream()));
                try {
                    while (true) {
                        String logLine = ios.readLine();
                        if (logLine == null) {
                            String msg = "------name=" + name + ", Task Start.------";
                            Toast.makeText(MainApplication.getServiceContext(), msg, Toast.LENGTH_SHORT).show();
                            FileUtil.inputLineLog(msg, name + "-log.txt");
                            Log.e(TAG, "runForegroundCommand: " + msg);
                            return;
                        }
                        Log.e(TAG, "runForegroundCommand: " + logLine);
                        FileUtil.inputLineLog(logLine, name + "-log.txt");
                    }
                } catch (Exception ignored) {
                }
            });
            ThreadPool.add(t);
            t.start();
        } catch (Exception e) {
            Log.d(TAG, "runForegroundCommand: " + e.getMessage());
        }
    }

    public static void runBackgroundCommand(String stop, String execStop) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(execStop);
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(stop + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            Log.d(TAG, "runBackgroundCommand: " + e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                assert process != null;
                process.destroy();
            } catch (Exception ignored) {
            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}