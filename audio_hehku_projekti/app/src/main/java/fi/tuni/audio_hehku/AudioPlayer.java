package fi.tuni.audio_hehku;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.PresetReverb;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AudioPlayer {
    private Scale scale;
    private MediaPlayer mediaPlayer;
    private List<MediaPlayer> mediaPlayers;
    public static String TAG = "Audio_Hehku: ";
    private float currentPitch = 1.0f;
    private int[] sampleResourceIds = {R.raw.piano_sample_g3, R.raw.mallet_guitar_g, R.raw.synth_reverse_g};
    private int sampleResourceId = 0;
    private int loopDuration = 100;
    private int currentImageIndex = 0;
    PresetReverb pReverb  = new PresetReverb(1,0);
    EnvironmentalReverb  eReverb
            = new EnvironmentalReverb(0,0);
    private ImageView imageViewText;
    private int[] imageResources = {
            R.drawable.logo_color_audio_hehku,
            R.drawable.logo_white
    };
    public AudioPlayer(Context context, ImageView imageViewText) {

        scale = new Scale();
        this.imageViewText = imageViewText;
        mediaPlayers = new ArrayList<>();
        mediaPlayer = MediaPlayer.create(context, sampleResourceIds[sampleResourceId]);
        // Set the looping to false initially
        mediaPlayer.setLooping(false);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // When the playback completes, seek to the desired loop duration
                mp.seekTo(loopDuration);
                // Start the playback again to create the loop effect
                mp.start();
            }
        });
    }
    public int[] getSampleResourceIds() {
        return sampleResourceIds;
    }
    public void changeSample(Context context, int position) {
        // Release the previous MediaPlayer instances if they exist
        sampleResourceId = position;
        if (mediaPlayers != null) {
            for (MediaPlayer mediaPlayer : mediaPlayers) {
                mediaPlayer.release();
            }
        }
            MediaPlayer mediaPlayer = createMediaPlayerWithPitch(context, position, currentPitch);
            mediaPlayers.add(mediaPlayer);
    }
    public void updateScale(String selectedScale) {
        float[] scaleValues = scale.getScale(selectedScale);

        // Update the pitch for all MediaPlayers in the list
        for (MediaPlayer mediaPlayer : mediaPlayers) {
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        changeLogoOnLoop();
                        float pitch = scaleValues[mediaPlayers.indexOf(mediaPlayer) % scaleValues.length];
                        PlaybackParams playbackParams = mediaPlayer.getPlaybackParams();
                        if (playbackParams != null) {
                            mediaPlayer.setPlaybackParams(playbackParams.setPitch(pitch));

                        }
                    }
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Error updating scale: " + e.getMessage());
                }
            }
        }
    }
    public float calculatePitch(float touchX, float touchY, float screenHeight, float screenWidth, int selectedScale) {
        // Calculate the percentage of the touch position relative to the screen height
        float touchXPercentage = touchX / screenWidth;
        float touchYPercentage = touchY / screenHeight;

        int numOctaves = 4;
        // Calculate the octave based on the touch X position
        int octave = (int) (touchXPercentage * numOctaves) + 1; // Add 1 to avoid 0-based indexing

        float[] scaleValues = scale.getScale(String.valueOf(selectedScale));
        // Calculate the note index based on the touch position
        int noteIndex = (int) (touchY / (screenHeight / scaleValues.length));
        // Retrieve the pitch value from the scale array based on the note index
        float pitch = scaleValues[noteIndex] * (float) Math.pow(2, octave -3);
        return pitch;
    }
    public MediaPlayer createMediaPlayerWithPitch(Context context, int sampleResourceId, float pitch) {
        try {
            // Create a new MediaPlayer instance
            MediaPlayer mediaPlayer = new MediaPlayer();

            // Set the data source for the MediaPlayer
            Log.d(TAG, "CREATE MEDIA PALYER WITH PITCH: " + sampleResourceId);
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(sampleResourceIds[sampleResourceId]);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            // Set the audio attributes and prepare the MediaPlayer
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            mediaPlayer.prepare();
            // Update the playback speed based on the pitch
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setPitch(pitch));
            // Set the looping property
            mediaPlayer.setLooping(true);
            changeLogoOnLoop();

            // Add an OnCompletionListener to detect when the sample has finished playing
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Change the logo here
                    changeLogoOnLoop();
                }
            });

            return mediaPlayer;
        } catch (IOException e) {
            Log.e(TAG, "Failed to create MediaPlayer: " + e.getMessage());
        }

        return null;
    }
    public void startMediaPlayerWithPitch(float pitch, Context context, int sampleResourceId) {
        // Create a new MediaPlayer with the specified pitch
        MediaPlayer mediaPlayer = createMediaPlayerWithPitch(context, sampleResourceId, pitch);
        // Add the MediaPlayer instance to the lis and add reverb to the sound
        pReverb.setPreset(PresetReverb.PRESET_LARGEROOM);
        pReverb.setEnabled(true);
        mediaPlayer.attachAuxEffect(pReverb.getId());
        mediaPlayer.setAuxEffectSendLevel(1.0f);
        mediaPlayers.add(mediaPlayer);
        mediaPlayer.start();
    }
    public void changeLogoOnLoop() {
        // Set the drawable resource for the ImageView based on the currentImageIndex
        imageViewText.setImageResource(imageResources[currentImageIndex]);
        // Increment the currentImageIndex and ensure it stays within the bounds of the imageResources array
        currentImageIndex = (currentImageIndex + 1) % imageResources.length;
    }
    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    public void releaseAllMediaPlayers() {
        for (MediaPlayer mediaPlayer : mediaPlayers) {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
        }
        mediaPlayers.clear();
    }
    public void stopAllMediaPlayers() {
        for (MediaPlayer mediaPlayer : mediaPlayers) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
                mediaPlayer.reset(); // Reset the MediaPlayer to its uninitialized state after stopping
                mediaPlayer.release(); // Release the old MediaPlayer instance
            }

        mediaPlayers.clear(); // Clear the list of MediaPlayers
    }
    public void pauseAllMediaPlayers() {
        for (MediaPlayer mediaPlayer : mediaPlayers) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }

    }
}

