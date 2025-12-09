package Texture;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class AudioManager {
    private HashMap<String, Clip> soundClips;
    private HashMap<String, String> soundPaths;
    private float volume = 0.7f;
    private boolean muted = false;
    private Clip backgroundMusic;




    public AudioManager() {
        soundClips = new HashMap<>();
        soundPaths = new HashMap<>();
    }

    // دالة محسنة لتحميل الصوت
    public boolean loadSound(String name, String filePath) {
        System.out.println("Attempting to load: " + name + " from " + filePath);

        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                System.err.println("File does not exist: " + filePath);
                return false;
            }

            // حاول معرفة نوع الملف
            String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();

            if (extension.equals("wav")) {
                return loadWAV(name, audioFile);
            } else if (extension.equals("mp3")) {
                return loadMP3(name, audioFile);
            } else {
                System.err.println("Unsupported format: " + extension);
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error loading sound " + name + ": " + e.getMessage());
            return false;
        }
    }

    // تحميل WAV
    private boolean loadWAV(String name, File audioFile) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();

            DataLine.Info info = new DataLine.Info(Clip.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Line not supported for WAV");
                return false;
            }

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            soundClips.put(name, clip);
            soundPaths.put(name, audioFile.getAbsolutePath());

            System.out.println("WAV loaded successfully: " + name);
            return true;

        } catch (Exception e) {
            System.err.println("Failed to load WAV: " + e.getMessage());
            return false;
        }
    }

    // تحميل MP3 - محاولات متعددة
    private boolean loadMP3(String name, File audioFile) {
        System.out.println("Trying to load MP3: " + name);

        // المحاولة الأولى: الطريقة العادية
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat baseFormat = audioStream.getFormat();

            // تحويل MP3 إلى PCM
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );

            AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, audioStream);
            Clip clip = AudioSystem.getClip();
            clip.open(decodedStream);

            soundClips.put(name, clip);
            soundPaths.put(name, audioFile.getAbsolutePath());

            System.out.println("MP3 loaded successfully (Method 1)");
            return true;

        } catch (Exception e1) {
            System.out.println("MP3 Method 1 failed: " + e1.getMessage());

            // المحاولة الثانية: استخدام InputStream مباشرة
            try {
                return loadMP3Alternative(name, audioFile);
            } catch (Exception e2) {
                System.err.println("All MP3 methods failed for " + name);
                return false;
            }
        }
    }

    // طريقة بديلة لـ MP3
    private boolean loadMP3Alternative(String name, File audioFile) {
        try {
            // جرب مع معالجة الخطأ بشكل مختلف
            AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);

            // الحصول على التنسيق
            AudioFormat format = stream.getFormat();
            System.out.println("MP3 Format: " + format);

            // إذا كان تنسيق MP3، نحتاج لتحويله
            if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                AudioFormat targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        format.getSampleRate(),
                        16,
                        format.getChannels(),
                        format.getChannels() * 2,
                        format.getSampleRate(),
                        false
                );

                stream = AudioSystem.getAudioInputStream(targetFormat, stream);
            }

            Clip clip = AudioSystem.getClip();
            clip.open(stream);

            soundClips.put(name, clip);
            soundPaths.put(name, audioFile.getAbsolutePath());

            System.out.println("MP3 loaded (Alternative method)");
            return true;

        } catch (Exception e) {
            System.err.println("Alternative MP3 load failed: " + e.getMessage());

            // أخيراً: إذا MP3 مش شغال، استخدم ملفات WAV بديلة
            System.out.println("Suggestion: Convert MP3 files to WAV format");
            return false;
        }
    }

    // بهندل مشكلة لو الاصوات mp3 لكن الاحسن احولها wav يدوي
    public boolean loadSoundSimple(String name, String filePath) {
        try {
            System.out.println("Simple load: " + filePath);

            File file = new File(filePath);
            if (!file.exists()) {
                // جرب امتداد .wav بدلاً من .mp3
                String wavPath = filePath.replace(".mp3", ".wav");
                file = new File(wavPath);
                if (!file.exists()) {
                    System.err.println("File not found: " + filePath);
                    return false;
                }
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);

            soundClips.put(name, clip);
            System.out.println("Loaded: " + name);
            return true;

        } catch (Exception e) {
            System.err.println("Simple load failed: " + e.getMessage());
            return false;
        }
    }

    //دوال التشغيل
    public void playSound(String name) {
        if (muted) return;

        try {
            Clip clip = soundClips.get(name);
            if (clip != null) {
                if (clip.isRunning()) clip.stop();
                clip.setFramePosition(0);
                setClipVolume(clip, volume);
                clip.start();
            }
        } catch (Exception e) {
            System.err.println("Play error: " + e.getMessage());
        }
    }

    public void playBackgroundMusic(String name) {
        if (muted) return;

        try {
            Clip clip = soundClips.get(name);
            if (clip != null) {
                if (backgroundMusic != null && backgroundMusic.isRunning()) {
                    backgroundMusic.stop();
                }
                backgroundMusic = clip;
                backgroundMusic.setFramePosition(0);
                setClipVolume(backgroundMusic, volume * 0.5f); // أخفض للموسيقى
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.err.println("Background music error: " + e.getMessage());
        }
    }

    private void setClipVolume(Clip clip, float vol) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(vol) / Math.log(10.0) * 20.0);
                gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
            }
        } catch (Exception e) {
            // اي حاجه
        }
    }

    public void setVolume(float vol) {
        this.volume = Math.max(0.0f, Math.min(1.0f, vol));
    }

    public void toggleMute() {
        muted = !muted;
        if (muted) {
            stopAllSounds();
        } else if (backgroundMusic != null) {
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopAllSounds() {
        for (Clip clip : soundClips.values()) {
            if (clip.isRunning()) clip.stop();
        }
    }

    public void cleanup() {
        stopAllSounds();
        for (Clip clip : soundClips.values()) {
            clip.close();
        }
        soundClips.clear();
    }
    // Get current volume
    public float getVolume() {
        return volume;
    }

    // Check if muted
    public boolean isMuted() {
        return muted;
    }

    // Stop background music specifically
    public void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }

    // Play sound with custom volume
    public void playSound(String name, float customVolume) {
        if (muted) return;

        try {
            Clip clip = soundClips.get(name);
            if (clip != null) {
                if (clip.isRunning()) clip.stop();
                clip.setFramePosition(0);
                setClipVolume(clip, customVolume);
                clip.start();
            }
        } catch (Exception e) {
            System.err.println("Play error: " + e.getMessage());
        }
    }
}