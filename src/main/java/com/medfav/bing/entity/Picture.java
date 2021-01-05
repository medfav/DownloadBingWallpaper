package com.medfav.bing.entity;

import lombok.Data;

/**
 * Create by wzx on 2020/12/11 20:45
 */

@Data
public class Picture {
        private Integer id;
        private String startdate;
        private String fullstartdate;
        private String enddate;
        private String rooturl;
        private String url;
        private String uhdurl;
        private String urlbase;
        private String filename;
        private String uhdfilename;
        private String copyright;
        private String enCopyright;
        private String copyrightlink;
        private String title;
        private String quiz;
        private String wp;
        private String hsh;
        private String drk;
        private String top;
        private String bot;
        private String hs;
        private String encyImgTitle;
        private String encyDesc;
        private String cnEncyImgTitle="";
        private String cnEncyDesc="";
}
