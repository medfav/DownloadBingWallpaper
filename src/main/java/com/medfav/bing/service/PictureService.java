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
     * 更新bing壁纸信息小故事.
     *
     * @param pictureInfo the picture info
     * @return the integer
     */
    Integer updatePicInfo(Picture pictureInfo);

    /**
     * 查询bing壁纸是否存在.
     *
     * @param enddate the url
     * @return the picture
     */
    Picture selectPicInfo(String enddate);

    /**
     * 分页查询壁纸
     *
     * @return the list
     */
    List<Picture> selectAllPic();
}
