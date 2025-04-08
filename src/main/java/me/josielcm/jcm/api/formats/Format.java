package me.josielcm.jcm.api.formats;

import java.util.regex.Pattern;

public class Format {

    private static final Pattern SPACING_CHARS_REGEX = Pattern.compile("[_ \\-]+");
    
    public static String formatTime(int time) {
        int minutes = time / 60;
        int seconds = time % 60;
        
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static String removeSpacingChars(String string) {
		return SPACING_CHARS_REGEX.matcher(string).replaceAll("");
	}

}
