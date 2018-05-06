package com.reddogsoftware.mbresnan.cyniceightball;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AccelerometerListener {
    ImageButton micButton;
    TextView displayText;
    SpeechRecognizer speechRecognizer;
    Intent speechIntent;
    TypedArray fortunes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        micButton = findViewById(R.id.micButton);
        displayText = findViewById(R.id.displayText);

        micButton.setOnClickListener(onClick);

        fortunes = getResources().obtainTypedArray(R.array.fortunes_array);

        // Request necessary permissions
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    @Override
    public void onAccelerationChanged(float x, float y, float z) {

    }

    @Override
    public void onShake(float force) {
        stopAccelerometerListening();
        int fortuneChoice = (int) (Math.random() * fortunes.length());
        displayText.setText(fortunes.getString(fortuneChoice));
    }

    @Override
    public void onStop() {
        super.onStop();

        //Check device supported Accelerometer sensor or not
        if (AccelerometerManager.isListening()) {

            //Start Accelerometer Listening
            AccelerometerManager.stopListening();

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (AccelerometerManager.isListening()) {
            AccelerometerManager.stopListening();
        }
    }

    private View.OnClickListener onClick = v -> {
        micButton.setImageResource(R.drawable.ic_mic_active);
        initSpeechRecognizer();
    };

    public void startAccelerometerListening() {
        if (AccelerometerManager.isSupported(this)) {
            AccelerometerManager.startListening(this);
        }
    }

    public void stopAccelerometerListening() {
        if (AccelerometerManager.isSupported(this)) {
            AccelerometerManager.stopListening();
        }
    }

    private void initSpeechRecognizer() {

        // Create the speech recognizer and set the listener
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this.getApplicationContext());
        speechRecognizer.setRecognitionListener(recognitionListener);

        // Create the intent with ACTION_RECOGNIZE_SPEECH
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);

        listen();
    }

    public void listen() {

        // startListening should be called on Main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = () -> speechRecognizer.startListening(speechIntent);
        mainHandler.post(myRunnable);
    }

    // This should be it's own class, but I'm lazy
    RecognitionListener recognitionListener = new RecognitionListener() {

        @Override
        public void onReadyForSpeech(Bundle params) {}

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {
            speechRecognizer.stopListening();
            micButton.setImageResource(R.drawable.ic_mic_button);
            displayText.setText(R.string.display_shake);
            startAccelerometerListening();
        }

        @Override
        public void onError(int errorCode) {

            // ERROR_NO_MATCH states that we didn't get any valid input from the user
            // ERROR_SPEECH_TIMEOUT is invoked after getting many ERROR_NO_MATCH
            // In these cases, let's restart listening.
            // It is not recommended by Google to listen continuously user input, obviously it drains the battery as well,
            // but for now let's ignore this warning
            if ((errorCode == SpeechRecognizer.ERROR_NO_MATCH) || (errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {

                listen();
            }
        }

        @Override
        public void onResults(Bundle bundle) {}

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };
}


