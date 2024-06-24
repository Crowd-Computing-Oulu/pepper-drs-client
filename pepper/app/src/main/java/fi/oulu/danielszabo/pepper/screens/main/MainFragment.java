package fi.oulu.danielszabo.pepper.screens.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


import java.util.concurrent.atomic.AtomicBoolean;

import fi.oulu.danielszabo.pepper.PepperApplication;
import fi.oulu.danielszabo.pepper.R;
import fi.oulu.danielszabo.pepper.log.LOG;
import fi.oulu.danielszabo.pepper.tools.SimpleController;

public class MainFragment extends Fragment {

//    Self-reference for other threads ot be able to call Activity
    private final MainFragment thisMainFragment = this;

//     Contextual (based on latest response) available options and their respective phrase sets
    private String[] displayedContextualOptions;
    private String[][] contextualOptionPhraseSets;

//    UI Element references
    private LinearLayout messagesContainer;
    private ScrollView messagesContainerScrollView;
    private Button speakButton;

    private ProgressBar progress;

    private Button resetButton;
    private TextView instructionTextView;
    private ImageView watchImageView;


//    Fragment lifecycle methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        PepperApplication.mainActivity.fragment = this;
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {}
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        messagesContainer = view.findViewById(R.id.messages_container);
        messagesContainerScrollView = view.findViewById(R.id.messages_container_scrollview);
        speakButton = view.findViewById(R.id.speak_btn);
        progress = view.findViewById(R.id.loading_spinner);
        resetButton = view.findViewById(R.id.btn_reset);
        instructionTextView = view.findViewById(R.id.instr_text);
        watchImageView = view.findViewById(R.id.watch_image);

        resetButton.setOnClickListener(view1 -> {
            resetConv();

//            also allow new input, in case we are resetting because it got stuck
            progress.setVisibility(View.INVISIBLE);
            speakButton.setText("SPEAK");
            speakButton.setEnabled(true);
        });

        speakButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == 0) {
                speakButton.setText("HOLD");
                startInput();
            } else if (event.getAction() == 1) {
                speakButton.setEnabled(false);
                speakButton.setText("HMM...");
                finishInput();
            }
            return false;
        });

        return view;
    }

    public void resetConv() {
        PepperApplication.getConversationService().resetConversation();
        messagesContainer.removeAllViews();
    }


    public void startInput() {
        if(PepperApplication.getConversationService().numMessages() == 0) {
            // TODO show instructions on the screen and remove them after the first message
        }
        speakButton.setBackgroundColor(Color.parseColor("#77b2e0"));
        if(!PepperApplication.MOCK_SPEECH_INPUT) {
            PepperApplication.speechRecService.startSpeechRecognition();
        }
    }

    // might want to make this async in the future
    public void finishInput() {
        speakButton.setBackgroundColor(Color.parseColor("#D6E8F6"));
        progress.setVisibility(View.VISIBLE);
        if (!PepperApplication.MOCK_SPEECH_INPUT) {
             PepperApplication.speechRecService.stopSpeechRecognitionAsync(utterance1 -> {
                 View userBubble = LayoutInflater.from(getActivity()).inflate(R.layout.user_text_bubble_view, null);
                 runOnUiThread(() -> {
                     messagesContainer.addView(userBubble);
                 });
                 if(utterance1.isEmpty()){
                     utterance1 = "[silence]";
                 }
                 ((TextView) userBubble.findViewById(R.id.messageContentTextView2)).setText(utterance1);

                 if(utterance1.equals("[silence]")) {
                     String silenceResponse = "I'm sorry, I didn't catch that.";
                     handleCAResponse(silenceResponse);
                 } else {
                     PepperApplication.getConversationService().sendUserMessageAsync(utterance1, s -> handleCAResponse(s));
                 }


             });
        } else {
            String utterance = "Hello, I am the user.";
            View userBubble = LayoutInflater.from(getActivity()).inflate(R.layout.user_text_bubble_view, null);
            messagesContainer.addView(userBubble);
            ((TextView) userBubble.findViewById(R.id.messageContentTextView2)).setText(utterance);

            PepperApplication.getConversationService().sendUserMessageAsync(utterance, s -> handleCAResponse(s));

        }

    }

    private void handleCAResponse(String response){
        View pepperBubble = LayoutInflater.from(getActivity()).inflate(R.layout.pepper_text_bubble_view, null);
        ((TextView) pepperBubble.findViewById(R.id.messageContentTextView)).setText(response);
        AtomicBoolean doneAddingView = new AtomicBoolean(false);
        getActivity().runOnUiThread(() -> {
            messagesContainer.addView(pepperBubble);
            progress.setVisibility(View.INVISIBLE);
            speakButton.setText("SPEAK");
            speakButton.setEnabled(true);
            doneAddingView.set(true);
        });
        getActivity().runOnUiThread(() -> {
//            wait for previous runOnUIThread to finish
            while (!doneAddingView.get()){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            messagesContainerScrollView.post(() -> messagesContainerScrollView.fullScroll(View.FOCUS_DOWN));
        });
        PepperApplication.getSpeechSynthService().speak(response);
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

//    Other helper methods

    private void runOnUiThread(Runnable runnable){
        thisMainFragment.getActivity().runOnUiThread(runnable);
    }

    public void transferToWearable() {
        resetConv();
        instructionTextView.setVisibility(View.INVISIBLE);
        speakButton.setVisibility(View.INVISIBLE);
        resetButton.setVisibility(View.INVISIBLE);
        watchImageView.setVisibility(View.VISIBLE);
    }
}
