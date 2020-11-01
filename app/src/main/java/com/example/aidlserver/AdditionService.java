package com.example.aidlserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

public class AdditionService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
  }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IAdd.Stub mBinder = new IAdd.Stub() {
        @Override
        public void getOrientationSenderData(IDBCallback callback) throws RemoteException {
            MainActivity.getScreenOrientation(callback);
        }
    };


}