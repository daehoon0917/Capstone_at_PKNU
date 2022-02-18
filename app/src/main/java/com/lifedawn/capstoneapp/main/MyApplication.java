package com.lifedawn.capstoneapp.main;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {
	public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(3);
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
}
