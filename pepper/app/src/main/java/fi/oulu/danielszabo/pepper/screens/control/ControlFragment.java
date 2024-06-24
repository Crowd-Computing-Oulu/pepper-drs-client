package fi.oulu.danielszabo.pepper.screens.control;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import fi.oulu.danielszabo.pepper.Config;
import fi.oulu.danielszabo.pepper.PepperApplication;
import fi.oulu.danielszabo.pepper.R;
import fi.oulu.danielszabo.pepper.log.LOG;
import fi.oulu.danielszabo.pepper.log.LogFragment;
import fi.oulu.danielszabo.pepper.tools.SimpleController;


public class ControlFragment extends Fragment {

    private final ControlFragment thisControlFragment = this;
    private OnFragmentInteractionListener mListener;

    private Button turnLeftBtn, turnRightBtn, turnAroundBtn, stepForwardBtn, sayBtn, logsBtn;
    private EditText sayField;
    private Switch switch_tts;
    private Switch switch_animations;
    private Switch switch_chatgpt;

    public ControlFragment() {
        // Required empty public constructor
    }

    public static ControlFragment newInstance(String param1, String param2) {
        ControlFragment fragment = new ControlFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_control, container, false);

        turnLeftBtn = view.findViewById(R.id.btn_left);
        turnLeftBtn.setOnClickListener(this::onTurnLeftButtonPressed);
        turnRightBtn = view.findViewById(R.id.btn_right);
        turnRightBtn.setOnClickListener(this::onTurnRightButtonPressed);
        turnAroundBtn = view.findViewById(R.id.btn_turnaround);
        turnAroundBtn.setOnClickListener(this::onTurnAroundButtonPressed);
        stepForwardBtn = view.findViewById(R.id.btn_forward);
        stepForwardBtn.setOnClickListener(this::onForwardButtonPressed);
        sayBtn = view.findViewById(R.id.btn_say);
        sayBtn.setOnClickListener(this::onSayButtonPressed);
        logsBtn = view.findViewById(R.id.btn_logs);
        logsBtn.setOnClickListener(this::onLogsButtonPressed);

        this.sayField = view.findViewById(R.id.txtfield_say);

        // Mimic3 engine On and Off switch
        switch_animations = view.findViewById(R.id.animations_switch);
        switch_tts = view.findViewById(R.id.tts_switch);
        switch_chatgpt = view.findViewById(R.id.ca_switch);

        switch_animations.setChecked(Config.isSpeech_animations_enabled());
        switch_tts.setChecked(Config.isMimic3_enabled());
        switch_chatgpt.setChecked(Config.isChatGPT_enabled());

        switch_tts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                LOG.debug(this, "enabled mimic 3 tts");
                Config.setMimic3_enabled(true);
            } else {
                LOG.debug(this, "disabled mimic 3 tts");
                Config.setMimic3_enabled(false);
            }
        });

        switch_animations.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                LOG.debug(this, "enabled animations");
                Config.setSpeech_animations_enabled(true);
            } else {
                LOG.debug(this, "disabled animations");
                Config.setSpeech_animations_enabled(false);
            }
        });

        switch_chatgpt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                LOG.debug(this, "enabled chatgpt");
                Config.setChatGPT_enabled(true);
            } else {
                LOG.debug(this, "disabled chatgpt");
                Config.setChatGPT_enabled(false);
            }
        });

        return view;
    }


    public void onForwardButtonPressed(View view) {
        SimpleController.moveForward();
    }

    public void onTurnLeftButtonPressed(View view) {
        SimpleController.turnLeft();
    }

    public void onTurnRightButtonPressed(View view) {
        SimpleController.turnRight();
    }

    public void onTurnAroundButtonPressed(View view) {
        SimpleController.turnAround();
    }

    public void onLogsButtonPressed(View view) {
        if(!PepperApplication.qiContextInitialised){
            return;
        }
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setReorderingAllowed(true);
        Fragment newFragment = new LogFragment();
        fragmentTransaction.replace(R.id.fragment, newFragment, this.getClass().getSimpleName());
        fragmentTransaction.commit();
    }

    public void onSayButtonPressed(View view) {
        SimpleController.say(sayField.getText().toString());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
