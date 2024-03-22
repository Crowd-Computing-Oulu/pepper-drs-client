package fi.oulu.danielszabo.pepper.screens.main;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import fi.oulu.danielszabo.pepper.PepperApplication;
import fi.oulu.danielszabo.pepper.R;
import fi.oulu.danielszabo.pepper.log.LOG;
import fi.oulu.danielszabo.pepper.tools.SimpleController;

public class MainFragment extends Fragment {

//    This application only handles a single dialogue
    private static final String CONV_ID = "c1";

//    Conversation Service

//    Self-reference for other threads ot be able to call Activity
    private final MainFragment thisMainFragment = this;

//     Contextual (based on latest response) available options and their respective phrase sets
    private String[] displayedContextualOptions;
    private String[][] contextualOptionPhraseSets;

//    UI Element references
    private LinearLayout messagesContainer;
    private TextView hmmText, instructionText, captionText;
    private ProgressBar hmmSpinner;
    private Button buttonSkip, speakButton;

    private void initConv() {
        runOnUiThread(() -> {
            setCaptionsVisible(false);
            setThinkingVisible(false);
        });
    }

//    Fragment lifecycle methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        hmmText = view.findViewById(R.id.txt_hmm);
        instructionText = view.findViewById(R.id.txt_instruction);
        captionText = view.findViewById(R.id.txt_caption);
        hmmSpinner = view.findViewById(R.id.spinner_hmm);
        messagesContainer = view.findViewById(R.id.messages_container);
        speakButton = view.findViewById(R.id.speak_btn);

        speakButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {
                startInput();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                finishInput();
            }
            return false;
        });
        //    Initialisation
        {
            initConv();
        }

        return view;
    }


    public void startInput() {
        speakButton.setBackgroundColor(Color.parseColor("#77b2e0"));
        PepperApplication.speechRecService.startSpeechRecognition();
    }

    // might want to make this async in the future
    public void finishInput() {
        speakButton.setBackgroundColor(Color.parseColor("#D6E8F6"));
        String utterance = PepperApplication.speechRecService.stopSpeechRecognition();
        String CAresponse = PepperApplication.convAgentService.sendUserMessage(utterance);
        PepperApplication.synthService.speak(CAresponse);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LOG.removeListener("logView");
    }

//    Button press event listeners

    private void onStartButtonPressed(View view) {
        AsyncTask.execute(() -> {
            SimpleController.say(__ -> {}, "Hello!");
        });
    }

    private void onOptionButtonPressed(View view) {

    }

//    Conversation logic methods
    private void setCaptionsVisible(boolean visible) {
        captionText.setVisibility(visible?View.VISIBLE:View.INVISIBLE);
    }

    private void setThinkingVisible(boolean visible) {
        hmmSpinner.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        hmmText.setVisibility(visible?View.VISIBLE:View.INVISIBLE);
    }

//    Other helper methods

    private void runOnUiThread(Runnable runnable){
        thisMainFragment.getActivity().runOnUiThread(runnable);
    }

}
