//package Logic;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
///**
// * Created by Aero Kang on 9/22/2015.
// */
//public class Player {
//    private static String playlistPath;
//    private static String mediaDir;
//    private static long updateCycle = 3;
//
//    public Player(String playlistPath, String mediaDir) {
//        this.playlistPath = playlistPath;
//        this.mediaDir = mediaDir;
//    }
//
//    public void setUpdateCycle(long updateCycle) {
//        this.updateCycle = updateCycle;
//    }
//
//    public void play() throws IOException {
//        List<String> playlist = this.getPlaylist();
//        String liveSrcUrl = playlist.get(new Random().nextInt(playlist.size()));
//        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
//        service.scheduleAtFixedRate(new Updater(liveSrcUrl, this.getFilePath() + "/" + mediaDir), 0, updateCycle, TimeUnit.SECONDS);
//    }
//
//    public List<String> getPlaylist() throws IOException {
//        List<String> list = new ArrayList<>();
//        File file = new File(playlistPath);
//        InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
//        BufferedReader buf = new BufferedReader(reader);
//        String line;
//        while ((line = buf.readLine()) != null) {
//            list.add(line);
//        }
//        return list;
//    }
//
//    public String getFilePath() {
//        File file = new File(playlistPath);
//        return file.getParent();
//    }
//}
