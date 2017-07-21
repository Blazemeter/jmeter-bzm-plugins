package org.loadosophia.jmeter;

import kg.apc.jmeter.http.HttpUtils;
import kg.apc.jmeter.notifier.StatusNotifierCallback;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.GZIPOutputStream;

public class LoadosophiaAPIClient extends HttpUtils {

    private static final Logger log = LoggingManager.getLoggerForClass();
    public static final String COLOR_NONE = "none";
    public static final String[] colors = {COLOR_NONE, "red", "green", "blue", "gray", "orange", "violet", "cyan", "black"};
    public static final int STATUS_DONE = 4;
    public static final int STATUS_ERROR = 5;
    private final StatusNotifierCallback notifier;
    private final String project;
    private final String token;
    private final String colorFlag;
    private final String title;

    public LoadosophiaAPIClient(StatusNotifierCallback informer, String aAddress, String aToken, String projectName, String aColorFlag, String aTitle) {
        super(informer, aAddress, "");
        project = projectName;
        token = aToken;
        notifier = informer;
        colorFlag = aColorFlag;
        title = aTitle;
    }

    public String startOnline() throws IOException {
        String uri = address + "api/active/receiver/start";
        LinkedList<FormBodyPart> partsList = new LinkedList<>();
        partsList.add(new FormBodyPart("token", new StringBody(token)));
        partsList.add(new FormBodyPart("projectKey", new StringBody(project)));
        partsList.add(new FormBodyPart("title", new StringBody(title)));
        JSONObject obj = queryObject(createPost(uri, partsList), 201);
        return address + "gui/active/" + obj.optString("OnlineID", "N/A") + "/";
    }

    public void sendOnlineData(JSONArray data) throws IOException {
        String uri = address + "api/active/receiver/data";
        LinkedList<FormBodyPart> partsList = new LinkedList<>();
        String dataStr = data.toString();
        log.debug("Sending active test data: " + dataStr);
        partsList.add(new FormBodyPart("data", new StringBody(dataStr)));
        query(createPost(uri, partsList), 202);
    }

    public void endOnline(String redirectLink) throws IOException {
        String uri = address + "api/active/receiver/stop";
        LinkedList<FormBodyPart> partsList = new LinkedList<>();
        partsList.add(new FormBodyPart("redirect", new StringBody(redirectLink)));
        query(createPost(uri, partsList), 205);
    }

    public LoadosophiaUploadResults sendFiles(File targetFile, LinkedList<String> perfMonFiles) throws IOException {
        notifier.notifyAbout("Starting upload to BM.Sense");
        LoadosophiaUploadResults results = new LoadosophiaUploadResults();
        LinkedList<FormBodyPart> partsList = getUploadParts(targetFile, perfMonFiles);

        JSONObject res = queryObject(createPost(address + "api/files", partsList), 201);

        int queueID = Integer.parseInt(res.getString("QueueID"));
        results.setQueueID(queueID);

        int testID = getTestByUpload(queueID);
        results.setTestID(testID);

        setTestTitleAndColor(testID, title.trim(), colorFlag);
        results.setRedirectLink(address + "gui/" + testID + "/");
        return results;
    }

    private LinkedList<FormBodyPart> getUploadParts(File targetFile, LinkedList<String> perfMonFiles) throws IOException {
        if (targetFile.length() == 0) {
            throw new IOException("Cannot send empty file to BM.Sense");
        }

        log.info("Preparing files to send");
        LinkedList<FormBodyPart> partsList = new LinkedList<>();
        partsList.add(new FormBodyPart("projectKey", new StringBody(project)));
        partsList.add(new FormBodyPart("jtl_file", new FileBody(gzipFile(targetFile))));

        Iterator<String> it = perfMonFiles.iterator();
        int index = 0;
        while (it.hasNext()) {
            File perfmonFile = new File(it.next());
            if (!perfmonFile.exists()) {
                log.warn("File not exists, skipped: " + perfmonFile.getAbsolutePath());
                continue;
            }

            if (perfmonFile.length() == 0) {
                log.warn("Empty file skipped: " + perfmonFile.getAbsolutePath());
                continue;
            }

            partsList.add(new FormBodyPart("perfmon_" + index, new FileBody(gzipFile(perfmonFile))));
            index++;
        }
        return partsList;
    }

    private File gzipFile(File src) throws IOException {
        // Never try to make it stream-like on the fly, because content-length still required
        // Create the GZIP output stream
        String outFilename = src.getAbsolutePath() + ".gz";
        notifier.notifyAbout("Gzipping " + src.getAbsolutePath());
        GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(outFilename), 1024 * 8, true);

        // Open the input file
        FileInputStream in = new FileInputStream(src);

        // Transfer bytes from the input file to the GZIP output stream
        byte[] buf = new byte[10240];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();

        // Complete the GZIP file
        out.finish();
        out.close();

        src.delete();

        return new File(outFilename);
    }

    private int getTestByUpload(int queueID) throws IOException {
        while (true) {
            try {
                Thread.sleep(5000); // TODO: parameterize it
            } catch (InterruptedException ex) {
                throw new RuntimeException("Interrupted on getting TestID");
            }

            JSONObject status = queryObject(new HttpGet(address + "api/files/" + queueID), 200);

            int intStatus = Integer.parseInt(status.getString("status"));
            if (intStatus == STATUS_DONE) {
                return Integer.parseInt(status.getString("TestID"));
            } else if (intStatus == STATUS_ERROR) {
                throw new IOException("File processing finished with error: " + status.getString("UserError"));
            }
        }
    }

    private void setTestTitleAndColor(int testID, String title, String color) throws IOException {
        if (title.isEmpty() && (color.isEmpty() || color.equals(COLOR_NONE)) ) {
            return;
        }

        JSONObject data = new JSONObject();
        if (!title.isEmpty()) {
            data.put("title", title);
        }

        if (!title.isEmpty()) {
            data.put("colorFlag", color);
        }

        query(createPatch(address + "api/tests/" + testID, data), 200);
    }
}
