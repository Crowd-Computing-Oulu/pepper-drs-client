package fi.oulu.danielszabo.pepper;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Config {

    private static SharedPreferences sharedPreferences;

    public static void init(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
    public static boolean isChatGPT_enabled() {
        return sharedPreferences.getBoolean("chatgpt_enabled", true);
    }

    public static void setChatGPT_enabled(boolean chatGPT_enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("chatgpt_enabled", chatGPT_enabled);
        editor.apply();
    }

    public static boolean isMimic3_enabled() {
        return sharedPreferences.getBoolean("mimic3_enabled", true);
    }

    public static void setMimic3_enabled(boolean mimic3_enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("mimic3_enabled", mimic3_enabled);
        editor.apply();
    }

    public static boolean isSpeech_animations_enabled() {
        return sharedPreferences.getBoolean("animations_enabled", true);
    }

    public static void setSpeech_animations_enabled(boolean speech_animations_enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("animations_enabled", speech_animations_enabled);
        editor.apply();
    }
}
