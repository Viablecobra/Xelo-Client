package com.origin.launcher;


import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import com.origin.launcher.Launcher.MinecraftActivity;

public class IntentHandler extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
				patiencepls(getIntent());
        //setContentView(R.layout.main);
		
    }
	void patiencepls(Intent intention) {
		if (isMcLoaded()) {
			intention.setClassName(getApplicationContext(), "com.origin.launcher.Launcher.MinecraftActivity");
		} else {
			intention.setClassName(getApplicationContext(), "com.origin.launcher.MainActivity");
		}
		startActivity(intention);
	}
	Boolean isMcLoaded() {
		try {
			Class.forName("com.mojang.minecraftpe.Launcher", false, getClassLoader());
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
