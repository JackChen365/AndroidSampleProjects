package com.cz.android.simplehttp.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlockManager {

    /**
     * Text file storing blocked host names
     */
    private static final String FILENAME = "block.txt";

    /**
     * Array list storing all blocked host names
     */
    private static List<String> blockedHostNames;

    /**
     * Constructor
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public BlockManager() {
        // Synchronized list accessed by multiple threads
        blockedHostNames = new CopyOnWriteArrayList<>();

        // Read host names from text file into array list
        try {
            File blockedFile = new File(FILENAME);

            if (!blockedFile.exists())
                blockedFile.createNewFile();

            BufferedReader blockHostNamesReader = new BufferedReader(new FileReader(blockedFile));

            String hostName;

            while ((hostName = blockHostNamesReader.readLine()) != null)
                blockedHostNames.add(hostName);

            blockHostNamesReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if the request site is in block list
     *
     * @param hostName site hostname
     * @return true if blocked
     */
    public boolean isBlocked(String hostName) {
        return blockedHostNames.contains(hostName);
    }
}
