package fi.oulu.danielszabo.pepper;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;

import fi.oulu.danielszabo.pepper.screens.Help;
import fi.oulu.danielszabo.pepper.screens.control.ControlFragment;
import fi.oulu.danielszabo.pepper.screens.main.MainFragment;
import fi.oulu.danielszabo.pepper.log.LOG;
import fi.oulu.danielszabo.pepper.log.LogFragment;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks, LogFragment.OnFragmentInteractionListener, ControlFragment.OnFragmentInteractionListener, Help.OnFragmentInteractionListener {
    private Fragment fragment;
    private ImageButton btn_help, btnvolume;
    private TextView statusText;
    private boolean isMuted = false;

    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOG.info(this, "onCreate");


        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);

        // Set activity layout
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        statusText = (TextView) findViewById(R.id.txt_status);

        this.fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);

        // Add mute button and display skip button
        btnvolume = findViewById(R.id.btn_volume);
        btnvolume.setOnClickListener(v -> {
            isMuted = !isMuted;
            if (isMuted) {
                btnvolume.setImageResource(R.drawable.ic_volume_off);
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            } else {
                btnvolume.setImageResource(R.drawable.ic_volume_on);
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            }
        });

    }

    public void onAppSelectorPressed(View view) {
        if(!PepperApplication.qiContextInitialised){
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setReorderingAllowed(true);
        Fragment newFragment = null;
        int id = view.getId();
        if (id == R.id.btn_home) {
            newFragment = new MainFragment();
        }  else if (id == R.id.btn_control) {
            newFragment = new ControlFragment();
        }  else if (id == R.id.btn_help) {
            newFragment = new Help();
        }  else return;

        fragmentTransaction.replace(R.id.fragment, newFragment, this.getClass().getSimpleName());
        fragmentTransaction.commit();
    }

    public void initialised(boolean success) {
        LOG.debug(this, "Initialisation result: " + success);
        runOnUiThread(() -> {
            if (success) {
                this.statusText.setText(R.string.status_ok);
            } else {
                this.statusText.setText(R.string.status_error);
            }
        });
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        LOG.debug(this, "onRobotFocusGained");

        // Store context object in global application state, initialize the whole app
        PepperApplication.initialize(qiContext, this);

        LOG.debug(this, "Qi Context Created");

        // Set default fragment
        onAppSelectorPressed(findViewById(R.id.btn_home));
    }

    @Override
    public void onRobotFocusLost() {
        LOG.debug(this, "onRobotFocusLost");
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        LOG.debug(this, "onRobotFocusRefused: " + reason);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        LOG.debug(this, "onFragmentInteraction: " + uri);
    }
}
