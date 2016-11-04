package jp.shts.android.sampleimagecreator;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;

public class Store {

    private static final String KEY_ENABLE_BACKGROUND_COLOR = "key_background_color_enable";
    private static final String KEY_ENABLE_TEXT_COLOR = "key_text_color_enable";
    private static final String KEY_BACKGROUND_COLOR = "key_background_color";
    private static final String KEY_TEXT_COLOR = "key_text_color";

    private final SharedPreferences pref;

    Store(Context context) {
        this.pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    boolean checkedBackgroundColor() {
        return pref.getBoolean(KEY_ENABLE_BACKGROUND_COLOR, false);
    }

    void checkedBackgroundColor(boolean checked) {
        pref.edit().putBoolean(KEY_ENABLE_BACKGROUND_COLOR, checked).apply();
    }

    boolean checkedTextColor() {
        return pref.getBoolean(KEY_ENABLE_TEXT_COLOR, false);
    }

    void checkedTextColor(boolean checked) {
        pref.edit().putBoolean(KEY_ENABLE_TEXT_COLOR, checked).apply();
    }

    @ColorInt
    int backgroundColor() {
        return pref.getInt(KEY_BACKGROUND_COLOR, -1);
    }

    void backgroundColor(@ColorInt int color) {
        pref.edit().putInt(KEY_BACKGROUND_COLOR, color).apply();
    }

    @ColorInt
    int textColor() {
        return pref.getInt(KEY_TEXT_COLOR, -1);
    }

    void textColor(@ColorInt int color) {
        pref.edit().putInt(KEY_TEXT_COLOR, color).apply();
    }
}
