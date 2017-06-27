package Logic;

import com.github.axet.wget.WGet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Aero Kang on 9/22/2015.
 */
public class Downloader implements Runnable {
    private static List<String> newMedia;
    private static String baseUrl;
    private static String dir;

    public Downloader(List<String> newMedia, String baseUrl, String dir) {
        this.newMedia = newMedia;
        this.baseUrl = baseUrl;
        this.dir = dir;
    }

    @Override
    public void run() {
        try {
            URL url;
            File file = new File(dir);

            if(!file.exists()) {
                file.mkdirs();
            }

            for (String filePath : newMedia) {
                if (filePath.startsWith("http")) {
                    url = new URL(filePath);
                } else {
                    url = new URL(baseUrl + filePath);
                }
                WGet w = new WGet(url, file);
                w.download();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RuntimeException allDownloadExceptions) {
            allDownloadExceptions.printStackTrace();
        }
    }
}
