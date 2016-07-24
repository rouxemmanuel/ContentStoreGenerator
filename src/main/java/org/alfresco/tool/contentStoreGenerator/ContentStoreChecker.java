package org.alfresco.tool.contentStoreGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

public class ContentStoreChecker {

    public ContentStoreChecker() {
    }

    public static void doWork(ArrayList<String> contentUrls, Properties mimeMap, String alfData, String contentStoreName, String dummyDir, String defaultFilename, String DEBUG) {
        Iterator<String> it;
        if(DEBUG.equals("FULL")) {
            for(it = contentUrls.iterator(); it.hasNext(); System.out.println((String)it.next()));
        }
        it = contentUrls.iterator();
        int count = 0;
        int filesMissing = 0;
        int filesMissized = 0;
        while(it.hasNext()) {
            String fullUrl = (String)it.next();
            String splitUrl[] = fullUrl.split("\\|");
            if(DEBUG.equals("FULL")) {
                for(int i = 0; i < splitUrl.length; i++) {
                    System.out.println((new StringBuilder(String.valueOf(i))).append(") ").append(splitUrl[i]).toString());
                }

            }
            if(splitUrl.length < 1) {
                System.out.println((new StringBuilder("Couldn't parse url: ")).append(fullUrl).toString());
                System.out.println((new StringBuilder("length: ")).append(splitUrl.length).toString());
            } else {
                String targetFilename = (new StringBuilder(String.valueOf(alfData))).append("/").append(contentStoreName).append(splitUrl[0]).toString();
                Long fileSize = Long.decode(splitUrl[2].substring(5));
                File file = new File(targetFilename);
                if(!file.exists()) {
                    System.out.println((new StringBuilder("file does not exist: ")).append(targetFilename).toString());
                    filesMissing++;
                } else if(file.length() != fileSize.longValue()) {
                    System.out.println((new StringBuilder("file is wrong size: ")).append(targetFilename).append(" Should be ").append(fileSize).append(", is ").append(file.length()).toString());
                    filesMissized++;
                }
                if(++count % (contentUrls.size() / 10) == 0) {
                    System.out.println((new StringBuilder("Checked ")).append(count).append(" of ").append(contentUrls.size()).append(" -- ").append((count * 100) / contentUrls.size()).append("% - ").append(filesMissing + filesMissized).append(" incorrect files found.").toString());
                }
            }
        }
        System.out.println((new StringBuilder("total number of URLs that do not exist in filesystem: ")).append(filesMissing).toString());
        System.out.println((new StringBuilder("total number of files that are the wrong size: ")).append(filesMissized).toString());
    }
}
