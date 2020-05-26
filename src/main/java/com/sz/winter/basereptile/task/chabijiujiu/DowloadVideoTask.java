package com.sz.winter.basereptile.task.chabijiujiu;

import com.sz.winter.basereptile.config.Constant;
import com.sz.winter.basereptile.config.RedisService;
import com.sz.winter.basereptile.model.ClassifyList;
import com.sz.winter.basereptile.model.VideoFragmentation;
import com.sz.winter.basereptile.model.resp.VideoFragmentationInfoResp;
import com.sz.winter.basereptile.module.chabijiujiu.ClassifyListModule;
import com.sz.winter.basereptile.service.ClassifyListService;
import com.sz.winter.basereptile.service.VideoFragmentationService;
import com.sz.winter.basereptile.util.BaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 下载视频线程
 * </p>
 */
public class DowloadVideoTask implements Runnable {
    Logger logger = LoggerFactory.getLogger(getClass());

    private VideoFragmentationInfoResp resp;

    private ClassifyListService classifyListService;

    private VideoFragmentationService videoFragmentationService;

    private ClassifyListModule classifyListModule;

    public DowloadVideoTask(VideoFragmentationInfoResp resp, ClassifyListService classifyListService,
                            VideoFragmentationService videoFragmentationService, ClassifyListModule classifyListModule) {
        this.resp = resp;
        this.classifyListService = classifyListService;
        this.videoFragmentationService = videoFragmentationService;
        this.classifyListModule = classifyListModule;
    }

    @Override
    public void run() {
        try {

            //redis key
            String key = Constant.VIDEO_FRAGMENTATION + resp.getWebsiteName() + ":" + resp.getClassifyId() + ":" + resp.getClassifyListId();

            Jedis jedis = RedisService.getResource();
            //获取有序列表中第一个值
            Set<String> zrange = jedis.zrange(key, 0, 0);

            //设置该视频下载中状态
            ClassifyList classifyList = new ClassifyList();
            classifyList.setId(resp.getClassifyListId());
            classifyList.setIsDownload(2);
            classifyList.setLastUpdate(new Date());
            classifyListService.updateClassifys(classifyList);

            //更新视频下载状态
            VideoFragmentation v = new VideoFragmentation();
            v.setClassifyListId(resp.getClassifyListId());
            v.setDowloadType(2);
            v.setLastUpdate(new Date());
            videoFragmentationService.updateVideoFragmentation(v);

            String fileDirKeys = null;

            if(zrange == null ||zrange.isEmpty())
                return;

            while (zrange != null && !zrange.isEmpty()) {
                try {
                    //获取待下载的视频分片相对路径
                    String vf = zrange.toArray()[0].toString();

                    //拼接完整分片视频URL
                    String videoUrl = classifyListModule.getM3u8Url() + vf;

                    //视频文件名称缓存
                    fileDirKeys = Constant.VIDEO_DIR_NAME + resp.getWebsiteName() + ":" + resp.getClassifyId();
                    String fileName = jedis.hget(fileDirKeys, resp.getClassifyListId().toString());

                    //查看redis中是否已经创建该文件
                    if (fileName == null) {
                        //拼接完整磁盘路径文件名
                        fileName = classifyListModule.getVideoDir() + System.currentTimeMillis() + BaseUtil.random(1, 999) + classifyListModule.getM3u8FileSuffix();

                        Map<String, String> map = new HashMap<>();
                        map.put(resp.getClassifyListId().toString(), fileName);
                        RedisService.hmset(fileDirKeys, map);

                        //更新视频磁盘路径
                        v.setVideoDir(fileName);
                        v.setLastUpdate(new Date());
                        videoFragmentationService.updateVideoFragmentation(v);

                    }

                    //判断是否下载成功
                    boolean flag = BaseUtil.dowloadNerworkFile(videoUrl, fileName, true);

                    //礼貌性线程休眠
                    Thread.sleep(50);

                    if (flag) {
                        //删除当前视频分片信息
                        jedis.zremrangeByRank(key, 0, 0);
                        zrange = jedis.zrange(key, 0, 0);
                    }
                } catch (Exception ex) {
                    logger.error("", ex);
                }
            }

            //设置该视频下载状态为完成
            classifyList.setIsDownload(1);
            classifyList.setLastUpdate(new Date());
            classifyListService.updateClassifys(classifyList);

            //获取该视频md5校验和
            try (InputStream is = Files.newInputStream(Paths.get(v.getVideoDir()))) {
                String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
                v.setVideoMd5(md5);
            }

            //设置视频已下载完成状态值
            v.setDowloadType(1);
            v.setLastUpdate(new Date());
            videoFragmentationService.updateVideoFragmentation(v);

            //删除缓存的下载中文件名
            RedisService.hdel(fileDirKeys,resp.getClassifyListId().toString());

        } catch (Exception ex) {
            logger.error("{}", ex);
        }
    }
}
