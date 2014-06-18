package net.robertkarl.gridimagesearch.app.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Connectivity {

    /**
     * Check if DNS is functioning and packets can reach google.com.
     */
    public static boolean pingGoogleSynchronous() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();

            BufferedReader r = new BufferedReader(new InputStreamReader(p1.getInputStream()));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            Log.d("DEBUG", total.toString());

            boolean reachable = (returnVal==0);
            return reachable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
