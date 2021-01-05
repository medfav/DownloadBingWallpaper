package com.medfav.bing.service;

import com.medfav.bing.entity.Picture;

import java.util.List;

/**
 * Create by wzx on 2020/12/11 20:49
 */
public interface PictureService {
    /**
     * 写入bing壁纸信息.
     *
     * @param pictureInfo the picture info
     * @return the integer
     */
    Integer insertPicInfo(Picture pictureInfo);

    /**
     * 查询bing壁纸是否存在.
     *
     * @param url the url
     * @return the picture
     */
    Picture selectPicInfo(String url);

    /**
     * 分页查询壁纸
     *
     * @return the list
     */
    List<Picture> selectAllPic();
}
