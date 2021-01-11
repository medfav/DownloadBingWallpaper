package com.medfav.bing.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.medfav.bing.common.RequestBingJson;
import com.medfav.bing.entity.Picture;
import com.medfav.bing.service.PictureService;
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
@Controller
public class DownloadBingController {
    @Autowired
    private PictureService pictureService;
    @Autowired
    private RequestBingJson requestBingJson;

    @GetMapping("/getPic")
    @ResponseBody
    public Object getPic() throws Exception {
        try {
            requestBingJson.downloadBingPicture();
            return "true";
        }catch (Exception e){
            return e.getMessage();
        }
    }

<<<<<<< HEAD
    @GetMapping(value = {"/","/getBingPicList"})
=======
    @GetMapping("/getBingPicList")
>>>>>>> b29acedf0affd74f598887006d2f6e062d9783a5
    public String getBingPicList(@RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                 @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
                                 ModelMap map){
        PageHelper.startPage(pageIndex, pageSize);
        List<Picture> pictureList = pictureService.selectAllPic();
        PageInfo<Picture> pageInfo = new PageInfo<>(pictureList);
        map.put("pageInfo", pageInfo);
        System.out.println(pageInfo.toString());
        return "PicList";
    }
}
