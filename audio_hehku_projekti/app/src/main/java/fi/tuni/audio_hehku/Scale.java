package fi.tuni.audio_hehku;

public class Scale {
    private float[] scaleCmajor = {1.0f, 1.122f, 1.259f, 1.335f, 1.498f, 1.682f, 1.888f, 2.0f};
    private float[] scaleDminor = {1.0f, 1.189f, 1.335f, 1.498f, 1.681f, 1.887f, 2.117f, 2.378f};
    private float[] scaleGMajor = {1.0f, 1.122f, 1.259f, 1.334f, 1.498f, 1.682f, 1.888f, 2.0f};
    private float[] scaleGMinor = {1.0f, 1.189f, 1.335f, 1.498f, 1.682f, 1.888f, 2.117f, 2.378f};
    private float[] scaleGBlues = {1.0f, 1.122f, 1.189f, 1.259f, 1.335f, 1.498f, 1.682f, 2.0f};

    public float[] getScale(String selectedScale) {
        float[] scale;

        switch (selectedScale) {
            case "Scale 1":
                scale = scaleGMajor;
                break;
            case "Scale 2":
                scale = scaleGMinor;
                break;
            case "Scale 3":
                scale = scaleGBlues;
                break;
            case "Scale 4":
                scale = scaleDminor;
                break;
            case "Scale 5":
                scale = scaleCmajor;
                break;
            default:
                scale = scaleGMinor;
                break;
        }

        return scale;
    }
}

