package org.ar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.ar.guest.LiveListActivity;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this,LiveListActivity.class));
        finish();
    }
}
