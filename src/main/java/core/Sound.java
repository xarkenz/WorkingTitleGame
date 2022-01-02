package core;

import util.Logger;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class Sound {

    private int bufferID;
    private int sourceID;
    private String filePath;

    private boolean playing = false;

    public Sound(String filePath) {
        this.filePath = filePath;

        // Allocate space to store return information from stb
        stackPush();
        IntBuffer channelsBuffer = stackMallocInt(1);
        stackPush();
        IntBuffer sampleRateBuffer = stackMallocInt(1);

        ShortBuffer rawAudioBuffer = stb_vorbis_decode_filename(filePath, channelsBuffer, sampleRateBuffer);
        if (rawAudioBuffer == null) {
            Logger.critical("Unable to load sound file '" + filePath + "'.");
            stackPop();
            stackPop();
            return;
        }

        // Retrieve extra info
        int channels = channelsBuffer.get();
        int sampleRate = sampleRateBuffer.get();
        stackPop();
        stackPop();

        // Find correct OpenAL format
        int format = -1;
        if (channels == 1) {
            format = AL_FORMAT_MONO16;
        } else if (channels == 2) {
            format = AL_FORMAT_STEREO16;
        }

        bufferID = alGenBuffers();
        alBufferData(bufferID, format, rawAudioBuffer, sampleRate);

        // Generate source
        sourceID = alGenSources();

        alSourcei(sourceID, AL_BUFFER, bufferID);
        alSourcei(sourceID, AL_LOOPING, 0);
        alSourcei(sourceID, AL_POSITION, 0);
        alSourcef(sourceID, AL_GAIN, 1);

        // Free raw audio buffer
        free(rawAudioBuffer);
    }

    public void delete() {
        alDeleteSources(sourceID);
        alDeleteBuffers(bufferID);
    }

    public void play() {
        alSourcei(sourceID, AL_POSITION, 0);
        alSourcePlay(sourceID);
        playing = true;
    }

    public void stop() {
        if (playing) {
            alSourceStop(sourceID);
            playing = false;
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isPlaying() {
        return playing = alGetSourcei(sourceID, AL_SOURCE_STATE) != AL_STOPPED;
    }

}
