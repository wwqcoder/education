package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.system.SysDictionary;
import io.swagger.annotations.ApiOperation;

public interface SysDicthinaryControllerApi {
    //数据字典
    @ApiOperation("数据字典查询")
    public SysDictionary getByType(String type);
}
