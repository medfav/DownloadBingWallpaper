package com.medfav.bing.common;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**图片下载
 * Create by wzx on 2020/12/11 15:14
 */
@Slf4j
public class DownloadUtil {
    /**
     * 下载图片
     * @param url 下载链接
     * @param path 完整文件路径（包括文件名）
     * @param referer 请求头中的referer
     */
    public static void downPic(String url, String path,String referer) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                URLConnection connection = new URL(url).openConnection();
                connection.setRequestProperty("Referer", referer);
                if (!file.getParentFile().exists())
                    file.getParentFile().mkdirs();
                ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
                byte[] buff = new byte[1000];
                int rc = 0;
                InputStream inputStream = connection.getInputStream();
                while ((rc = inputStream.read(buff, 0, 1000)) > 0) {
                    swapStream.write(buff, 0, rc);
                }
                Files.write(Paths.get(path), swapStream.toByteArray());
                log.info("文件下载成功：{}",file.getPath());
            }else {
                log.info("文件已存在：{}", file.getPath());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取json接口数据
     * @param url 请求连接
     * @param parameters 参数 Map<String,String>
     * @param header 请求头 Map<String,String>
     * @return
     * @throws Exception
     */
    public static HashMap<String, String> getJson(String url, Map<String,String> parameters, Map<String,String> header) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(createUri(url, parameters));
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).setSocketTimeout(6000).build();
        httpGet.setConfig(requestConfig);
        writeHeader(header, httpGet);
        CloseableHttpResponse httpResponse = null;
        try {
            return getHttpClientResult(httpResponse, httpClient, httpGet);
        }catch (Exception e){
            release(httpResponse, httpClient);
            log.info("请求错误！");
            return null;
        }finally {
            release(httpResponse, httpClient);
        }
    }

    /**
     * 获取json接口数据
     * @param url 请求连接
     * @param parameters 参数 Map<String,String>
     * @param header 请求头 Map<String,String>
     * @return
     * @throws Exception
     */
    public static HashMap<String, String> postJson(String url, List<NameValuePair> paramEntity, Map<String,String> parameters, Map<String,String> header) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(createUri(url, parameters));
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).setSocketTimeout(6000).build();
        httpPost.setConfig(requestConfig);
        httpPost.setEntity(new UrlEncodedFormEntity(paramEntity,Consts.UTF_8));
        writeHeader(header, httpPost);
        CloseableHttpResponse httpResponse = null;
//        try {
            return getHttpClientResult(httpResponse, httpClient, httpPost);
//        }catch (Exception e){
//            release(httpResponse, httpClient);
//            log.info("请求错误！");
//            return null;
//        }finally {
//            release(httpResponse, httpClient);
//        }
    }

    private static URI createUri(String baseUrl, Map<String,String> parameters) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(baseUrl);
        if (parameters != null) {
            Set<Map.Entry<String, String>> entrySet = parameters.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }

        return uriBuilder.build();
    }

    private static void writeHeader(Map<String, String> params, HttpRequestBase httpMethod) {
        if (params != null) {
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                // 设置到请求头到HttpRequestBase对象中
                httpMethod.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private static HashMap<String, String> getHttpClientResult(CloseableHttpResponse httpResponse, CloseableHttpClient httpClient, HttpRequestBase httpMethod) throws Exception {
        // 执行请求
        httpResponse = httpClient.execute(httpMethod);
        HashMap<String, String> responseMap = new HashMap<>();

        // 获取返回结果
        if (httpResponse != null && httpResponse.getStatusLine() != null) {
            String content = "";
            if (httpResponse.getEntity() != null) {
                content = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
            }
            responseMap.put("code", String.valueOf(httpResponse.getStatusLine().getStatusCode()));
            responseMap.put("content", content);
            return responseMap;
        }
        responseMap.put("code", String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        return responseMap;
    }

    private static void release(CloseableHttpResponse httpResponse, CloseableHttpClient httpClient) throws IOException {
        // 释放资源
        if (httpResponse != null) {
            httpResponse.close();
        }
        if (httpClient != null) {
            httpClient.close();
        }
    }

    /**
     * 获取网页
     * @param url 网页地址
     * @return
     */
    public static Document getHtml(String url) throws IOException {
        Document document = Jsoup.connect(url)
                .header("user-agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                .header("cookie","DUP=Q=Sz94v_oJl6QF1xfbdq2EWQ2&T=408952711&A=2&IG=C60182A7025D445CA69DBD3D938CF06F; SRCHD=AF=NOFORM; SRCHUID=V=2&GUID=93B9B4FD8D874A478122218B4307ECF3&dmnchg=1; _EDGE_V=1; MUID=34C9184EE5736A58049617ECE45D6BB5; MUIDB=34C9184EE5736A58049617ECE45D6BB5; _UR=MC=1; _EDGE_S=mkt=zh-cn&F=1&SID=02A68F752C5863E8051380D72D7662F0; ULC=P=1DB14|6:1&H=1DB14|6:1&T=1DB14|6:1:2; _FP=hta=on; MUIDV=NU=1; ENSEARCH=BENVER=1; SerpPWA=reg=1; btstkn=tAHuR5%252Fak0BMOQPJNKFvLQ%252By4fYHhgabWNrXbum9mALvoi5AozMB9QtereGSVo0J; _TTSS_IN=hist=WyJ6aC1IYW5zIiwiZW4iLCJhdXRvLWRldGVjdCJd; _TTSS_OUT=hist=WyJlbiIsInpoLUhhbnMiXQ==; _tarLang=default=zh-Hans; SRCHUSR=DOB=20201216&T=1608095846000&TPC=1608080824000; ipv6=hit=1608099454230&t=4; _RwBf=mtu=0&g=0&cid=&o=2&p=&c=&t=0&s=0001-01-01T00:00:00.0000000+00:00&ts=2020-12-16T05:17:44.9653024+00:00; _SS=SID=02A68F752C5863E8051380D72D7662F0&bIm=439555&R=0&RB=0&GB=0&RG=0&RP=0; SRCHHPGUSR=CW=1920&CH=325&DPR=1&UTC=480&DM=0&HV=1608096145&WTS=63743692646&BRW=W&BRH=S&BZA=0; SNRHOP=I=&TS=")
                .get();
//        Elements elements = document.getElementsByClass(selecter);
        return document;
    }

    /**
     * 获取网页
     * @param url 网页地址
     * @param selecter Css选择器(不用带点)
     * @return
     */
    public static Elements getHtml(String url, String selecter) throws IOException {
        Document document = Jsoup.connect(url)
                .header("user-agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                .header("cookie","DUP=Q=Sz94v_oJl6QF1xfbdq2EWQ2&T=408952115&A=2&IG=48A449F070824934A9EE4A2FF213E8DB; SRCHD=AF=NOFORM; SRCHUID=V=2&GUID=6A96F3C7827741BBB34CAC9BB04E125B&dmnchg=1; SRCHUSR=DOB=20201211; _EDGE_V=1; MUID=192F2B009C9C6A611E26249D9DDF6B16; MUIDB=192F2B009C9C6A611E26249D9DDF6B16; _SS=SID=207D1E48DDC26A37183B11EADC816B80; _EDGE_S=SID=207D1E48DDC26A37183B11EADC816B80")
                .get();
        Elements elements = document.select(selecter);
        return elements;
    }

    /**
     * 获取元素内容
     * @param element 网页元素
     * @param selecters Css选择器集合
     * @return
     * @throws IOException
     */
    public static Map<String,String> getText(Element element, Map<String,String> selecters) throws IOException {
        selecters.forEach((key, value) -> {
            selecters.put(key,element.select(value).text());
        });
        return selecters;
    }

    /**
     * bing翻译
     * @param originalText 原文
     * @param fromLanguage 原文语言
     * @param toLanguage 译文语言
     * @return
     */
    public static List<Object> bingTranslator(String originalText, String fromLanguage, String toLanguage) throws Exception {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("isVertical", "1");
        parameters.put("IG", "2071F7613D944854980F1FF5EB2E500D");
        parameters.put("IID", "translator.5025.2");
        HashMap<String, String> header = new HashMap<>();
        header.put("content-type", "application/x-www-form-urlencoded");
//        header.put("Content-Length", String.valueOf(originalText.length()));
        header.put("Host", "cn.bing.com");
        List<NameValuePair> paramEntity = new ArrayList<>();
        paramEntity.add(new BasicNameValuePair("fromLang", fromLanguage));
        paramEntity.add(new BasicNameValuePair("to", toLanguage));
        paramEntity.add(new BasicNameValuePair("text", originalText));
        HashMap<String, String> json = postJson("https://cn.bing.com/ttranslatev3", paramEntity, parameters, header);
        return JSON.parseArray(json.get("content"));
    }

}
