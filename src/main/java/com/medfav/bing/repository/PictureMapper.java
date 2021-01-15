package com.medfav.bing.repository;

import com.medfav.bing.entity.Picture;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Create by wzx on 2020/12/11 20:50
 */
@Repository
@Mapper
public interface PictureMapper {
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
    Picture selectPicInfo(@Param("enddate") String enddate);

    /**
     * 分页查询壁纸
     * @return
     */
    List<Picture> selectAllPic();
}
