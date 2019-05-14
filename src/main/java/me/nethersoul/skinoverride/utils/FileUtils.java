package me.nethersoul.skinoverride.utils;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * File Utils
 *
 * @author NetherSoul
 */
public class FileUtils {

    public static List<String> readUrlResponseFull(URL url) {
        try {
            List<String> lines = new ArrayList<>();
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (!inputLine.isEmpty()) {
                    lines.add(inputLine);
                }
            }
            in.close();
            return lines;
        } catch (Exception e) {
        }
        return null;
    }
}