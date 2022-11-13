package cz.koup.vs;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import cz.koup.ir.AbstractHTMLDownloader;
import cz.koup.ir.HTMLDownloaderSelenium;
import cz.koup.ir.Utils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * CrawlerVSCOM class acts as a controller. You should only adapt this file to serve your needs.
 * Created by Tigi on 31.10.2014.
 */
public class CrawlerVSCOM {
    /**
     * Xpath expressions to extract and their descriptions.
     */
    private final static Map<String, String> xpathMap = new HashMap<String, String>();

    static {
        xpathMap.put("title", "//div[@class='property-title']/h1/span/span[contains(@class, 'name')]/allText()");
        xpathMap.put("location", "//div[@class='property-title']/h1/span/span[contains(@class, 'location')]/allText()");
        xpathMap.put("parameterName", "//li[contains(@class, 'param')]/label/tidyText()");
        xpathMap.put("parameterValue", "//li[contains(@class, 'param')]/strong/allText()");
    }

    private static final String STORAGE = "./storage/VSCOMTest";
    private static String SITE = "https://www.sreality.cz/";
    private static String SITE_SUFFIX = "hledani/%3F/prodej";


    /**
     * Be polite and don't send requests too often.
     * Waiting period between requests. (in milisec)
     */
    private static final int POLITENESS_INTERVAL = 1200;
    private static final Logger log = Logger.getLogger(CrawlerVSCOM.class);
    private static List<Record> records = new ArrayList<Record>();

    /**
     * Main method
     */
    public static void main(String[] args) {

        File file = new File(STORAGE + Utils.SDF.format(System.currentTimeMillis()) + "_records.json");

        //Initialization
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        File outputDir = new File(STORAGE);
        if (!outputDir.exists()) {
            boolean mkdirs = outputDir.mkdirs();
            if (mkdirs) {
                log.info("Output directory created: " + outputDir);
            } else {
                log.error("Output directory can't be created! Please either create it or change the STORAGE parameter.\nOutput directory: " + outputDir);
            }
        }
//        HTMLDownloader downloader = new HTMLDownloader();
        AbstractHTMLDownloader downloader = new HTMLDownloaderSelenium();
        Map<String, Map<String, List<String>>> results = new HashMap<String, Map<String, List<String>>>();

//        Collection<String> urlsSet = new ArrayList<String>();
        Collection<String> urlsSet = new HashSet<>();
        Map<String, PrintStream> printStreamMap = new HashMap<String, PrintStream>();

        //Try to load links
        File links = new File(STORAGE + "_urls.txt");
        if (links.exists()) {
            try {
                List<String> lines = Utils.readTXTFile(new FileInputStream(links));
                for (String line : lines) {
                    urlsSet.add(line);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {

            int max = 2500;
            for (int i = 0; i < max; i = i + 1) {
                String link = SITE + SITE_SUFFIX + "?strana=" + i;
                urlsSet.addAll(downloader.getLinks(link, "//div[@type='property']/div/div/a/@href"));
            }
            Utils.saveFile(new File(STORAGE + Utils.SDF.format(System.currentTimeMillis()) + "_links_size_" + urlsSet.size() + ".txt"),
                    urlsSet);
        }

        int count = 0;
        for (String url : urlsSet) {
            String link = url;
            if (!link.contains(SITE)) {
                link = SITE + url;
            }
            //Download and extract data according to xpathMap
            try {
                records.add(downloader.processUrl(link, xpathMap));
            } catch (Exception e) {
                log.error("Couldnt fetch data from the page :" + url, e);
            }

            count++;
            if (count % 100 == 0) {
                log.info(count + " / " + urlsSet.size() + " = " + count / (0.0 + urlsSet.size()) + "% done.");
            }
            try {
                Thread.sleep(POLITENESS_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        Type recordsType = new TypeToken<List<Record>>() {}.getType();
        Gson customGson = gsonBuilder.create();
        String customJSON = customGson.toJson(records, recordsType);
        Utils.appendTo(file, customJSON);

        // Save links that failed in some way.
        // Be sure to go through these and explain why the process failed on these links.
        // Try to eliminate all failed links - they consume your time while crawling data.
        reportProblems(downloader.getFailedLinks());
        downloader.emptyFailedLinks();
        log.info("-----------------------------");
        System.exit(0);
    }


    /**
     * Save file with failed links for later examination.
     *
     * @param failedLinks links that couldn't be downloaded, extracted etc.
     */
    private static void reportProblems(Set<String> failedLinks) {
        if (!failedLinks.isEmpty()) {

            Utils.saveFile(new File(STORAGE + Utils.SDF.format(System.currentTimeMillis()) + "_undownloaded_links_size_" + failedLinks.size() + ".txt"),
                    failedLinks);
            log.info("Failed links: " + failedLinks.size());
        }
    }


}
