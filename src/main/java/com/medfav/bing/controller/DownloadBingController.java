package com.medfav.bing.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.medfav.bing.common.RequestBingJson;
import com.medfav.bing.entity.Picture;
import com.medfav.bing.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Create by wzx on 2020/12/14 15:52
 */
@Slf4j
@Controller
public class DownloadBingController {
    @Autowired
    private PictureService pictureService;
    @Autowired
    private RequestBingJson requestBingJson;

    /**
     * 手动开始获取Bing壁纸
     *
     * @return the pic
     * @throws Exception the exception
     */
    @GetMapping("/getPic")
    @ResponseBody
    public String getPic() throws Exception {
        try {
            requestBingJson.downloadBingPicture();
            return "true";
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * Bing壁纸信息列表
     *
     * @param pageIndex the page index
     * @param pageSize  the page size
     * @param map       the map
     * @return the bing pic list
     */
    @GetMapping(value = {"/","/getBingPicList"})
    public String getBingPicList(@RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                 @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
                                 ModelMap map){
        PageHelper.startPage(pageIndex, pageSize);
        List<Picture> pictureList = pictureService.selectAllPic();
        PageInfo<Picture> pageInfo = new PageInfo<>(pictureList);
        map.put("pageInfo", pageInfo);
        log.info("当前页：{}，当前页记录数：{}", pageInfo.getPageNum(), pageInfo.getSize());
        return "PicList";
    }
}
