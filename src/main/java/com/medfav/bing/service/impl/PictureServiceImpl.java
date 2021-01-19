package com.medfav.bing.service.impl;


import com.medfav.bing.entity.Picture;
import com.medfav.bing.repository.PictureMapper;
import com.medfav.bing.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by wzx on 2020/12/11 20:53
 */
@Service
@Slf4j
public class PictureServiceImpl implements PictureService {
    @Autowired
    private PictureMapper pictureMapper;

    public Integer insertPicInfo(Picture pictureInfo) {
        log.info("添加记录：{}", pictureInfo.getEnddate() + " ==> " + pictureInfo.getUrl() != null ? pictureInfo.getUrl() : pictureInfo.getCopyright());
        return pictureMapper.insertPicInfo(pictureInfo);
    }

    @Override
    public Integer updatePicInfo(Picture pictureInfo) {
        Integer num = pictureMapper.updatePicInfo(pictureInfo);
        if (num > 0) {
            log.info("更新记录：{}", pictureInfo.getEnddate() + " ==> " + pictureInfo.getCnEncyImgTitle());
        }
        return num;
    }

    @Override
    public Picture selectPicInfo(String enddate) {
        return pictureMapper.selectPicInfo(enddate);
    }

    @Override
    public List<Picture> selectAllPic() {
        List<Picture> pictureList = pictureMapper.selectAllPic();
        return pictureList;
    }
}
