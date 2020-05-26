package com.sz.winter.basereptile.service;

import com.sz.winter.basereptile.base.BaseDao;
import com.sz.winter.basereptile.model.VideoFragmentation;
import com.sz.winter.basereptile.model.resp.VideoFragmentationInfoResp;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoFragmentationService extends BaseDao {

    /**
     * <p>
     *     批量插入视频分片信息
     * </p>
     * @param videoFragmentations 视频分片信息
     */
    public void insertBatchVideoFragmentations(List<VideoFragmentation> videoFragmentations){
        getSession().insert("insertBatchVideoFragmentation",videoFragmentations);
    }

    /**
     * <p>
     *     插入视频分片信息
     * </p>
     * @param videoFragmentations 视频分片信息
     */
    public void insertVideoFragmentations(VideoFragmentation videoFragmentations){
        getSession().insert("insertVideoFragmentation",videoFragmentations);
    }

    /**
     * <p>
     *     获取分片视频信息
     * </p>
     * @return List
     */
    public List<VideoFragmentation> getVideoFragmentationList(){
        return getSession().selectList("selectVideoFragmentationList");
    }

    /**
     * <p>
     *     获取分片视频详细信息v1(除当前下载条件外)
     * </p>
     * @return List
     */
    public List<VideoFragmentationInfoResp> getVideoFragmentationInfoRespv1(VideoFragmentation vf){
        return getSession().selectList("selectVideoFragmentationInfoListv1",vf);
    }
    /**
     * <p>
     *     获取分片视频详细信息v2(只有当前下载条件)
     * </p>
     * @return List
     */
    public List<VideoFragmentationInfoResp> getVideoFragmentationInfoRespv2(VideoFragmentation vf){
        return getSession().selectList("selectVideoFragmentationInfoListv2",vf);
    }


    public void updateVideoFragmentation(VideoFragmentation videoFragmentations){
        getSession().update("updateVideoFragmentation",videoFragmentations);
    }
}
