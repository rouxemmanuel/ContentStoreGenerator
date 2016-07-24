package org.alfresco.tool.contentStoreGenerator;

import java.io.*;
import java.util.*;
import org.apache.commons.io.FileUtils;

public class ContentStoreFiller {

    public ContentStoreFiller() {
    }

    public static void doWork(ArrayList<String> contentUrls, Properties mimeMap, String alfData, String contentStoreName, String dummyDir, String defaultFilename, String DEBUG) {
        HashMap<String, String> unknownMimeTypes = new HashMap<String, String>();
        System.out.println("Copying files...");
        Iterator<String> it;
        if(DEBUG.equals("FULL")) {
            for(it = contentUrls.iterator(); it.hasNext(); System.out.println((String)it.next()));
        }
        it = contentUrls.iterator();
        int count = 0;
        while(it.hasNext()) {
            String fullUrl = (String)it.next();
            String splitUrl[] = fullUrl.split("\\|");
            if(DEBUG.equals("FULL")) {
                for(int i = 0; i < splitUrl.length; i++) {
                    System.out.println((new StringBuilder(String.valueOf(i))).append(splitUrl[i]).toString());
                }

            }
            if(splitUrl.length < 1) {
                System.out.println((new StringBuilder("Couldn't parse url: ")).append(fullUrl).toString());
                System.out.println((new StringBuilder("length: ")).append(splitUrl.length).toString());
            } else {
                String targetFilename = (new StringBuilder(String.valueOf(alfData))).append("/").append(contentStoreName).append(splitUrl[0]).toString();
                String sourceFilename = (new StringBuilder(String.valueOf(dummyDir))).append("/").append(mimeMap.getProperty(splitUrl[1].substring(9))).toString();
                if(sourceFilename.equals((new StringBuilder(String.valueOf(dummyDir))).append("/null").toString())) {
                    sourceFilename = (new StringBuilder(String.valueOf(dummyDir))).append("/").append(defaultFilename).toString();
                    unknownMimeTypes.put(splitUrl[1], "1");
                }
                try {
                    FileUtils.copyFile(new File(sourceFilename), new File(targetFilename));
                }
                catch(IOException e) {
                    System.out.println((new StringBuilder("Could not copy file ")).append(sourceFilename).append(" to ").append(targetFilename).toString());
                    e.printStackTrace();
                }
                if(++count % (contentUrls.size() / 10) == 0) {
                    System.out.println((new StringBuilder("Copied ")).append(count).append(" of ").append(contentUrls.size()).append(" -- ").append((count * 100) / contentUrls.size()).append("%").toString());
                }
            }
        }
        if(unknownMimeTypes.size() > 0) {
            System.out.println("Unknown MimeTypes:");
            for(Iterator<String> mimes = unknownMimeTypes.keySet().iterator(); mimes.hasNext(); System.out.println((String)mimes.next()));
        } else {
            System.out.println("All mimetypes recognised");
        }
    }
}

