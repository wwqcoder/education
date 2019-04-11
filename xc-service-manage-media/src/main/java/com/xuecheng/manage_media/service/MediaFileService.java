package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaFileService {

    @Autowired
    MediaFileRepository mediaFileRepository;

    //媒资文件分页查询
    public QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {

        if (queryMediaFileRequest == null){
            queryMediaFileRequest = new QueryMediaFileRequest();
        }
        //条件值对象
        MediaFile mediaFile = new MediaFile();
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getTag())){
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())){
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())){
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }
        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                                        .withMatcher("tag", ExampleMatcher.GenericPropertyMatchers.contains())
                                        .withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains());



        Example<MediaFile> example = Example.of(mediaFile,exampleMatcher);
        //分页查询对象
        if (page <= 0){
            page = 1;
        }
        page = page - 1;
        if (size <= 0){
            size = 10;
        }
        Pageable pageable = new PageRequest(page, size);
        //分页查询
        Page<MediaFile> all = mediaFileRepository.findAll(example, pageable);
        //总记录数
        long total = all.getTotalElements();
        //数据列表
        List<MediaFile> content = all.getContent();
        //返回的数据集
        QueryResult<MediaFile> queryResult = new QueryResult<>();
        queryResult.setList(content);
        queryResult.setTotal(total);
        //返回结果
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);



    }
}
