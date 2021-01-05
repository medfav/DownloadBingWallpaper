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
        log.info("添加记录：{}",pictureInfo.getCopyright());
        return pictureMapper.insertPicInfo(pictureInfo);
    }

    @Override
    public Picture selectPicInfo(String url) {
        return pictureMapper.selectPicInfo(url);
    }

    @Override
    public List<Picture> selectAllPic() {
//        PageHelper.startPage(1, 5);
        List<Picture> pictureList = pictureMapper.selectAllPic();
//        PageInfo<Picture> pageInfo = new PageInfo<>(pictureList);
        return pictureList;
    }
}
