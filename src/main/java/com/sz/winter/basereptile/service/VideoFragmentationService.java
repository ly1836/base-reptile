package com.sz.winter.basereptile.service;

import com.sz.winter.basereptile.base.BaseDao;
import com.sz.winter.basereptile.model.VideoFragmentation;
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
}
