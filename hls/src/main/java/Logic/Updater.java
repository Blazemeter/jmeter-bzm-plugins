package Logic;

/**
 * Created by Aero Kang on 9/17/2015.
 */
//public class Updater extends Parser implements Runnable {
//    private List<String> lastMedia = new ArrayList<>();
//    private static String dir;
//    public Updater(String liveSrcUrl, String dir) {
////        super(liveSrcUrl);
////        this.dir = dir;
//    }
//
//    @Override
//    public void run() {
//        ExecutorService service = Executors.newCachedThreadPool();
//        try {
//            List<String> newMedia = super..getMediaList();
//            newMedia.removeAll(lastMedia);
//            lastMedia = super.getMediaList();
//            if (!newMedia.isEmpty()) {
//                service.execute(new Downloader(newMedia, super.getBaseUrl(), this.dir));
//            } else {
//                System.out.println("Media is latest, no need to update media.");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        service.shutdown();
//    }
//}
