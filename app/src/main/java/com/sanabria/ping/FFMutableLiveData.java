package com.sanabria.ping;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FFMutableLiveData extends AndroidViewModel {

    private final MutableLiveData<String> info = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPingRunning = new MutableLiveData<>();

    ExecutorService executor;

    public FFMutableLiveData(@NonNull Application application) {
        super(application);
    }

    public boolean executorisShutDown() {
        if (executor != null)
            return executor.isShutdown();
        else {
            return true;
        }
    }

    public void stopPing() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public void iniciarPing(String url, String numeroPaquetes) {

        stopPing();


        String pingCommand = "/system/bin/ping -c " + numeroPaquetes + " " + url;

        executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    isPingRunning.postValue(true);
                    String buffer = "";
                    Runtime runtime = Runtime.getRuntime();
                    Process process = runtime.exec(pingCommand);

                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

                    String lineaEnBuffer = "";
                    while ((lineaEnBuffer = bufferedReader.readLine()) != null) {

                        if (Thread.currentThread().isInterrupted()) {
                            buffer = "";
                            info.postValue(buffer);
                            process.destroy();
                            bufferedReader.close();
                            isPingRunning.postValue(false);
                            executor.shutdownNow();
                            return;
                        }
                        buffer += lineaEnBuffer + "\n";
                        info.postValue(buffer);
                    }

                    StringBuilder error = new StringBuilder();
                    for (int i = 0; i < process.getErrorStream().available(); i++) {
                        error.append("").append((char) process.getErrorStream().read());
                    }
                    if (!TextUtils.isEmpty(error.toString())) {
                        info.postValue(getApplication().getResources().getString(
                                R.string.host_desconocido));
                    }

                    process.destroy();
                    bufferedReader.close();
                    isPingRunning.postValue(false);
                    executor.shutdownNow();

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("EXCEPTION: ", e.toString());
                }
            }
        });


    }

    public LiveData<String> getInfo() {
        return info;
    }

    public LiveData<Boolean> isPingRunning() {
        return isPingRunning;
    }
}
