package com.cz.android.simplehttp.proxy;

import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CacheManager {

    /**
     * Text file for storing cached urls
     */
    private static final String FILENAME = "cache.txt";

    /**
     * Folder for storing downloaded cache files
     */
    private static final String CACHE_DIR = "cache/";

    /**
     * Array list storing cached urls
     */
    private static List<String> cacheURLs;

    /**
     * Constructor
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public CacheManager() {
        cacheURLs = new CopyOnWriteArrayList<>();

        try {
            File cacheFile = new File(FILENAME);

            if (!cacheFile.exists())
                cacheFile.createNewFile();

            BufferedReader cacheUrlsReader = new BufferedReader(new FileReader(cacheFile));
            String cacheUrl;

            while ((cacheUrl = cacheUrlsReader.readLine()) != null)
                cacheURLs.add(cacheUrl);

            cacheUrlsReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if a request file has local cache
     *
     * @param request request
     * @return true if cached
     */
    public boolean isCached(Request request) {
        return cacheURLs.contains(request.getURL());
    }

    /**
     * Get input stream of the local cache file for reading
     *
     * @param request request
     * @return input stream
     * @throws IOException io error
     */
    public FileInputStream getLocalInputStream(Request request) throws IOException {
        String fileDir = CACHE_DIR + request.getHostName().hashCode();
        File file = new File(fileDir);
        return new FileInputStream(file);
    }

    /**
     * Get output stream of the local cache file for writing
     *
     * @param request request
     * @return output stream
     * @throws IOException io error
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public FileOutputStream getLocalOutputStream(Request request) throws IOException {
        String fileDir = CACHE_DIR + request.getHostName().hashCode();
        File file = new File(fileDir);

        file.getParentFile().mkdirs();
        file.createNewFile();

        return new FileOutputStream(file, false);
    }

    /**
     * Add cache url to array list
     *
     * @param request request
     */
    public void addCache(Request request) {
        cacheURLs.add(request.getURL());
    }

    /**
     * Write urls into persistent text file
     *
     * @throws IOException io error
     */
    public void saveCache() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME, false));

        for (String url : cacheURLs)
            writer.write(url + '\n');

        writer.close();
    }
}
