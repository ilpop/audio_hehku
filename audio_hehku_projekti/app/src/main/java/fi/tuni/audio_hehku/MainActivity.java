package fi.tuni.audio_hehku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_AUDIO_SETTINGS = 0;
    private Scale scale;
    private static String TAG = "Audio Hehkuu: ";
    private AudioPlayer audioPlayer;
    private Context ctx;
    private float currentPitch = 1.0f;
    private List<float[]> scales;
    private List<String> scaleNames;
    private List<String> sampleNames;
    private int sampleResourceId = 0;
    private Button btnStop;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FrameLayout frameLayout = findViewById(R.id.touch_view);
        ImageView imageViewText = findViewById(R.id.imageViewText);
        ctx = this;
        audioPlayer = new AudioPlayer(ctx, imageViewText);
        // Initialize the Stop button
        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the stopAllMediaPlayers method to stop all playing audio
                audioPlayer.stopAllMediaPlayers();
            }
        });

        scale = new Scale();

        scales = new ArrayList<>();
        scales.add(scale.getScale("Scale 1"));
        scales.add(scale.getScale("Scale 2"));
        scales.add(scale.getScale("Scale 3"));
        scales.add(scale.getScale("Scale 4"));
        scales.add(scale.getScale("Scale 5"));

        scaleNames = new ArrayList<>();
        scaleNames.add("Scale 1");
        scaleNames.add("Scale 2");
        scaleNames.add("Scale 3");
        scaleNames.add("Scale 4");
        scaleNames.add("Scale 5");

        sampleNames = new ArrayList<>();
        sampleNames.add("Piano");
        sampleNames.add("Guitar");
        sampleNames.add("Synth");

        // Set up the sample spinner using the sample names
        Spinner sampleSpinner = findViewById(R.id.sample_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sampleNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sampleSpinner.setAdapter(adapter);
        sampleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"position int: " + position);
                sampleResourceId = position;
                audioPlayer.changeSample(MainActivity.this, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case where nothing is selected
                sampleResourceId = audioPlayer.getSampleResourceIds()[0];
                Log.d(TAG, "Position: No selection");
                Log.d(TAG, "Sample Resource ID: " + sampleResourceId);
                audioPlayer.changeSample(MainActivity.this, sampleResourceId);
            }
        });

        Spinner spinner = findViewById(R.id.scale_spinner);
        ArrayAdapter<String> scaleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, scaleNames);
        scaleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(scaleAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedScale = parent.getItemAtPosition(position).toString();
                audioPlayer.updateScale(selectedScale);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case when nothing is selected
            }
        });

        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();

                    // Recalculate the pitch for each touch event
                    float touchX = x;
                    float touchY = y;
                    float screenWidth = v.getWidth();
                    float screenHeight = v.getHeight();

                    Log.d(TAG, "y: " +touchY + " x: " + screenHeight);
                    float pitch = audioPlayer.calculatePitch(touchX, touchY, screenHeight, screenWidth, spinner.getSelectedItemPosition());

                    // Check if the pitch has changed
                    if (pitch != currentPitch) {
                        // Create and start a new MediaPlayer with the updated pitch
                        audioPlayer.startMediaPlayerWithPitch(pitch, ctx, sampleResourceId);

                        currentPitch = pitch; // Update the current pitch value
                    }
                    return true;
                }
                return false;
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_AUDIO_SETTINGS) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Permission request was denied.
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioPlayer.releaseMediaPlayer();
        audioPlayer.releaseAllMediaPlayers();
    }
}
