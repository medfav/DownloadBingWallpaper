package com.medfav.bing.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.medfav.bing.entity.Picture;
import com.medfav.bing.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Create by wzx on 2020/12/11 15:31
 */
@Component
@Slf4j
@Service
@EnableScheduling
public class RequestBingJson {
    @Autowired
    private PictureService pictureService;

    @Value("${customer.config.savePath}")
    private String savePath;
    @Value("${customer.config.rootUrl}")
    private String rootUrl;
    @Value("${customer.config.urlPath}")
    private String urlPath;

    private static String root = "E:/xiuren/";
    private static String base = "https://www.aitaotu.com";

//    @PostConstruct
    public static void downloadPic() throws IOException {
        String baseUrl = "https://www.aitaotu.com/tag/mitaoshe.html";
        List<String> pagesList = new ArrayList<>();
        pagesList.add(baseUrl);
        for (int j = 2; j < 8; j++) {
            pagesList.add(baseUrl.replace(".html", "/" + j + ".html"));
        }
        for (String string : pagesList) {

            Document document = Jsoup.connect(string).get();
            Elements select = document.select("#mainbody .Pli-litpic");
            for (Element element : select) {
                String path = element.attr("title").replace(" ", "");
                String last = path.substring(path.length() - 7) + path.substring(0, path.length() - 7);
                log.info("开始下载：{}",last);
                List<String> pages = new ArrayList<>();
                pages.add(base + element.attr("href"));
                int pageCount = new Integer(Jsoup.connect(base + element.attr("href")).get().select(".totalpage").get(0).text());
                for (int i = 2; i < pageCount + 1; i++)
                    pages.add(base + element.attr("href").replace(".html", "_" + i + ".html"));
                for (String url : pages) {
                    String pic = Jsoup.connect(url).get().select("#big-pic img").attr("src");
                    DownloadUtil.downPic(pic, root + last + "/" + pic.split("/")[7], base);
                    log.info("下载成功：{}",pic);
                }
            }
        }
    }


//    @PostConstruct
    @Scheduled(cron = "${customer.config.schedules}")
    public void downloadBingPicture() throws Exception {
        boolean flag = true;
        for (int i = 1; flag; i++) {
            flag = startDownloadBingPicture(i);
        }
    }

    private boolean startDownloadBingPicture (Integer pageNum) throws Exception {
        HashMap<String, String> parameterMap = new HashMap<>();
        parameterMap.put("format", "js");
        parameterMap.put("idx", String.valueOf((pageNum - 1) * 8 - 1));
        parameterMap.put("n", "8");
        parameterMap.put("mkt", "zh-CN");
        HashMap<String, String> responseMap = DownloadUtil.getJson(rootUrl + urlPath, parameterMap, null);
        parameterMap.put("ensearch","1");
        HashMap<String, String> responseMapEnglish = DownloadUtil.getJson(rootUrl + urlPath, parameterMap, null);
        log.info("图片地址：{}",responseMap.get("content"));
        JSONObject responseDate = JSONObject.parseObject(responseMap.get("content"));
        JSONObject responseDateEnglish = JSONObject.parseObject(responseMapEnglish.get("content"));
        List<Picture> picList = JSONArray.parseArray(JSON.toJSONString(responseDate.get("images")),Picture.class);
        List<Picture> picListEnglish = JSONArray.parseArray(JSON.toJSONString(responseDateEnglish.get("images")),Picture.class);
        for (int i = 0;i<picList.size();i++) {
            Picture item = picList.get(i);
            Picture itemEnglish = picListEnglish.get(i);
            String url = item.getUrl();
            item.setFilename(url.split("[?&]\\w+=", 0)[1]);
            String filePath = savePath + "bingPic/1920x1080/" + item.getFilename();
            String uhdUrl = item.getUrl().replace("1920x1080","UHD");
            item.setUhdfilename(uhdUrl.split("[?&]\\w+=", 0)[1]);
            String filePathUhd = savePath + "bingPic/UHD/" +item.getUhdfilename();
            item.setUhdurl(uhdUrl);
            item.setRooturl(rootUrl);
            item.setEnCopyright(itemEnglish.getCopyright());
            item.setCopyrightlink(itemEnglish.getCopyrightlink());
            item.setQuiz(itemEnglish.getQuiz());
            //下载壁纸
            Picture hasPic = pictureService.selectPicInfo(url);
            if (hasPic==null) {
                //获取图片介绍
                Elements elements = DownloadUtil.getHtml(itemEnglish.getCopyrightlink()+"&ensearch=1", "#encycloCanvas");
                String encyImgTitle = elements.select(".ency_imgTitle").text();
                List<Object> translator = DownloadUtil.bingTranslator(encyImgTitle, "en", "zh-Hans");
                String cnEncyImgTitle = ((JSONObject) ((JSONArray) ((JSONObject) translator.get(0)).get("translations")).get(0)).get("text").toString();
                item.setEncyImgTitle(encyImgTitle);
                item.setCnEncyImgTitle(cnEncyImgTitle);
                String encyDesc = elements.select(".ency_desc").text();
                translator = DownloadUtil.bingTranslator(encyDesc, "en", "zh-Hans");
                String cnEncyDesc = ((JSONObject) ((JSONArray) ((JSONObject) translator.get(0)).get("translations")).get(0)).get("text").toString();
                item.setEncyDesc(encyDesc);
                item.setCnEncyDesc(cnEncyDesc);

                //壁纸信息写入数据库
                pictureService.insertPicInfo(item);
                //下载1920x1080壁纸
                DownloadUtil.downPic(rootUrl + url, filePath, rootUrl);
                //下载UHD壁纸
                DownloadUtil.downPic(rootUrl + uhdUrl, filePathUhd, rootUrl);
                WebSocketServer.BroadCastInfo("1");
            }else {
                log.info("已存在壁纸：{}", item.getUrl());
            }
        }
        if (pageNum == 2) {
            return false;
        } else {
            return true;
        }
    }
}
