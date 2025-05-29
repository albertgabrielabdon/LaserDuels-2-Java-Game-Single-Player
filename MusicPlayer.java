import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class MusicPlayer {
    private Clip themeClip;
    private Clip bossClip;
    private Thread transitionThread;

    public void start() {
        try {
            stop(); 

            themeClip = AudioSystem.getClip();
            AudioInputStream themeStream = AudioSystem.getAudioInputStream(new File("theme.wav"));
            themeClip.open(themeStream);

            bossClip = AudioSystem.getClip();
            AudioInputStream bossStream = AudioSystem.getAudioInputStream(new File("boss.wav"));
            bossClip.open(bossStream);

            FloatControl volumeControl = (FloatControl) themeClip.getControl(FloatControl.Type.MASTER_GAIN);
            themeClip.loop(Clip.LOOP_CONTINUOUSLY);
            themeClip.start();

            transitionThread = new Thread(() -> {
                try {
                    Thread.sleep(49000); //48 is also good
                    fadeOut(volumeControl, themeClip, 5000); //5 suspense
                    bossClip.loop(Clip.LOOP_CONTINUOUSLY);
                    bossClip.start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            transitionThread.start();

        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    public void restart() {
        stop(); 
        start(); 
    }

    private void stop() {
        try {
            if (transitionThread != null && transitionThread.isAlive()) {
                transitionThread.interrupt();
            }

            if (themeClip != null && themeClip.isRunning()) {
                themeClip.stop();
            }
            if (themeClip != null) themeClip.close();

            if (bossClip != null && bossClip.isRunning()) {
                bossClip.stop();
            }
            if (bossClip != null) bossClip.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fadeOut(FloatControl volumeControl, Clip clip, int fadeDurationMillis) throws InterruptedException {
        float maxVolume = volumeControl.getValue();
        float minVolume = volumeControl.getMinimum();
        int steps = 30;
        int sleepTime = fadeDurationMillis / steps;

        for (int i = 0; i < steps; i++) {
            float volume = maxVolume - i * (maxVolume - minVolume) / steps;
            volumeControl.setValue(volume);
            Thread.sleep(sleepTime);
        }
        clip.stop();
        clip.close();
    }
}
