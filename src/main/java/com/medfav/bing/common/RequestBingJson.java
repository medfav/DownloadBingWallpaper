package com.medfav.bing.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.medfav.bing.entity.Picture;
import com.medfav.bing.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
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

//    @PostConstruct
    @Scheduled(cron = "${customer.config.schedules}")
    public void downloadBingPicture() throws Exception {
//        boolean flag = true;
//        for (int i = 1; flag; i++) {
//            flag = startDownloadBingPicture(i);
//        }
        List[] piclist = getPiclist();
        startDownloadBingPicture(piclist[0], piclist[1]);
    }

    private List[] getPiclist() throws Exception {
        List<Picture> picList = new ArrayList<>();
        List<Picture> picListEnglish = new ArrayList<>();

        for (int i = 1; i<=2; i++) {
            HashMap<String, String> parameterMap = new HashMap<>();
            parameterMap.put("format", "js");
            parameterMap.put("idx", String.valueOf((i - 1) * 8 - 1));
            parameterMap.put("n", "8");
            parameterMap.put("mkt", "zh-CN");
            HashMap<String, String> responseMap = DownloadUtil.getJson(rootUrl + urlPath, parameterMap, null);
            parameterMap.put("ensearch", "1");
            HashMap<String, String> responseMapEnglish = DownloadUtil.getJson(rootUrl + urlPath, parameterMap, null);
            log.info("图片地址：{}", responseMap.get("content"));
            JSONObject responseDate = JSONObject.parseObject(responseMap.get("content"));
            JSONObject responseDateEnglish = JSONObject.parseObject(responseMapEnglish.get("content"));
            picList.addAll(JSONArray.parseArray(JSON.toJSONString(responseDate.get("images")), Picture.class));
            picListEnglish.addAll(JSONArray.parseArray(JSON.toJSONString(responseDateEnglish.get("images")), Picture.class));
        }
        //List去重
        removeListDuplicate(picList);
        removeListDuplicate(picListEnglish);
        if (picList.get(0).getStartdate().equals(picListEnglish.get(0).getStartdate())) {
            picListEnglish.add(0, new Picture());
            picList.add(new Picture());
        }
        List[] pl = {picList, picListEnglish};
        return pl;
    }

    private void startDownloadBingPicture (List<Picture> picList, List<Picture> picListEnglish) throws Exception {
        for (int i = 0;i<picList.size();i++) {
            Picture item = picList.get(i);
            Picture itemEnglish = picListEnglish.get(i);
            String url = "";
            String uhdUrl = "";
            String filePath = "";
            String filePathUhd = "";
            //查询记录是否存在
            Picture hasPic = pictureService.selectPicInfo((item.getEnddate() == null) ? itemEnglish.getStartdate() : item.getEnddate());
            if (hasPic == null && item.getStartdate() != null) {
                url = item.getUrl();
                item.setFilename(url.split("[?&]\\w+=", 0)[1]);
                filePath = savePath + "bingPic/1920x1080/" + item.getFilename();
                uhdUrl = item.getUrl().replace("1920x1080", "UHD");
                item.setUhdfilename(uhdUrl.split("[?&]\\w+=", 0)[1]);
                filePathUhd = savePath + "bingPic/UHD/" + item.getUhdfilename();
                item.setUhdurl(uhdUrl);
                item.setRooturl(rootUrl);
            }
            if ((hasPic==null||(hasPic != null && hasPic.getEncyImgTitle() == null)) && itemEnglish.getStartdate() != null) {
                if (item.getStartdate() == null) {
                    item.setEnddate(itemEnglish.getStartdate());
                }
                item.setEnCopyright(itemEnglish.getCopyright());
                item.setCopyrightlink(itemEnglish.getCopyrightlink());
                item.setQuiz(itemEnglish.getQuiz());
            }
            //下载壁纸
            if (hasPic == null || hasPic.getEncyImgTitle() == null) {
                if (itemEnglish.getCopyrightlink() != null && (hasPic == null || hasPic.getEncyImgTitle() == null)) {
                    //获取图片介绍
                    Elements elements = DownloadUtil.getHtml(itemEnglish.getCopyrightlink() + "&ensearch=1", "#encycloCanvas");
//                    String encyImgTitle = elements.select(".ency_imgTitle").text();//解析网页，获得英文标题
                    List<Object> translator = DownloadUtil.bingTranslator(itemEnglish.getTitle(), "en", "zh-Hans");
                    String cnEncyImgTitle = ((JSONObject) ((JSONArray) ((JSONObject) translator.get(0)).get("translations")).get(0)).get("text").toString();
                    item.setEncyImgTitle(itemEnglish.getTitle());
                    item.setCnEncyImgTitle(cnEncyImgTitle);
                    String encyDesc = elements.select(".ency_desc").text();
                    translator = DownloadUtil.bingTranslator(encyDesc, "en", "zh-Hans");
                    String cnEncyDesc = ((JSONObject) ((JSONArray) ((JSONObject) translator.get(0)).get("translations")).get(0)).get("text").toString();
                    item.setEncyDesc(encyDesc);
                    item.setCnEncyDesc(cnEncyDesc);
                }

                if (hasPic == null){
                    //壁纸信息写入数据库
                    pictureService.insertPicInfo(item);
                    //下载1920x1080壁纸
                    DownloadUtil.downPic(rootUrl + url, filePath, rootUrl);
                    //下载UHD壁纸
                    DownloadUtil.downPic(rootUrl + uhdUrl, filePathUhd, rootUrl);
                    //发送增加壁纸消息
                    WebSocketServer.BroadCastInfo("+1");
                }else if (item.getEnCopyright() != null){
                    //更新壁纸信息
                    pictureService.updatePicInfo(item);
                    //发送更新壁纸消息
                    WebSocketServer.BroadCastInfo("1");
                }else {
                    log.info("无更新数据：{}", hasPic.getEnddate() + " ==> " + hasPic.getCopyright());
                }
            } else {
                log.info("已存在壁纸：{}", hasPic.getEnddate() + " ==> " + hasPic.getCopyright());
            }
        }
    }

    /**
     * List去重
     * @param list
     * @return
     */
    public static void removeListDuplicate(List list){
        LinkedHashSet linkedHashSet = new LinkedHashSet<>(list.size());
        linkedHashSet.addAll(list);
        list.clear();
        list.addAll(linkedHashSet);
    }
}
