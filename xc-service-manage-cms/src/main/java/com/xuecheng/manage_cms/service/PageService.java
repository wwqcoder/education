package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    CmsConfigRepository cmsConfigRepository;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    CmsSiteRepository cmsSiteRepository;

    public QueryResponseResult<CmsPage> findList(int page, int size, QueryPageRequest queryPageRequest) {
        if (queryPageRequest == null){
            queryPageRequest = new QueryPageRequest();
        }
        //自定义条件查询
        //定义一个条件选择器  页面别名
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("pageName", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("pageType", ExampleMatcher.GenericPropertyMatchers.exact());

        //条件值对象
        CmsPage cmsPage = new CmsPage();
        //设置条件值
        //判断站点ID不为空
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //判断模板ID不为空 作为查询条件
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //判断页面别名不为空 作为查询条件
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //判断页面别名不为空 作为查询条件
        if (StringUtils.isNotEmpty(queryPageRequest.getPageName())){
            cmsPage.setPageName(queryPageRequest.getPageName());
        }
        //判断页面类型不为空 作为查询条件
        if (StringUtils.isNotEmpty(queryPageRequest.getPageType())){
            cmsPage.setPageType(queryPageRequest.getPageType());
        }
        //定义Example
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);
        //PageRequest.of()中需要分页参数
        if (page < 0){
            page = 1;
        }
        page = page - 1;
        if (size <= 0){
            size = 10;
        }
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);//自定义条件查询并且分页
        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());//数据列表
        queryResult.setTotal(all.getTotalElements());//数据总记录数
        QueryResponseResult<CmsPage> queryResponseResult = new QueryResponseResult<CmsPage>(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }

    //新增页面
    /*public CmsPageResult add(CmsPage cmsPage){
        //校验页面名称，站点id，页面webpath的唯一性。
        //根据三个字段查询，如果查询到，说明此页面已经存在，否则，此页面不存在。继续添加
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1 == null){
            //调用dao新增页面
            cmsPage.setPageId(null);
            cmsPageRepository.save(cmsPage);
            return new CmsPageResult(CommonCode.SUCCESS,cmsPage);
        }
        return new CmsPageResult(CommonCode.FAIL,null);
    }*/
    public CmsPageResult add(CmsPage cmsPage){
        //校验页面名称，站点id，页面webpath的唯一性。
        //根据三个字段查询，如果查询到，说明此页面已经存在，否则，此页面不存在。继续添加
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1 != null){
            //页面已经存在
            //抛出异常，异常内容就是页面已经存在
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
            //调用dao新增页面
            cmsPage.setPageId(null);
            cmsPageRepository.save(cmsPage);
            return new CmsPageResult(CommonCode.SUCCESS,cmsPage);
    }
    //根据页面ID来查询页面信息
    public CmsPage getById(String id){
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()){
            CmsPage cmsPage = optional.get();
            return cmsPage;
        }
            return null;
    }
    //修改页面
    public CmsPageResult update(String id,CmsPage cmsPage){
        //根据id从数据库中查询页面信息
        CmsPage cmsPage1 = this.getById(id);
        if (cmsPage1 != null){
            //准备更新数据
            //设置要修改的值
            cmsPage1.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            cmsPage1.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            cmsPage1.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            cmsPage1.setPageName(cmsPage.getPageName());
            //更新访问路径
            cmsPage1.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            cmsPage1.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新数据路径
            cmsPage1.setDataUrl(cmsPage.getDataUrl());
            //执行更新
            cmsPageRepository.save(cmsPage1);
            //返回成功
            return new CmsPageResult(CommonCode.SUCCESS,cmsPage1);
            }
            return new CmsPageResult(CommonCode.FAIL,null);
        }

        //删除页面
        public ResponseResult delete(String id){
            CmsPage cmsPage = this.getById(id);
            if (cmsPage != null){
                cmsPageRepository.deleteById(id);
                return new ResponseResult(CommonCode.SUCCESS);
            }
            return new ResponseResult(CommonCode.FAIL);
        }

    //根据ID查询cmsConfig
    public CmsConfig getConfigById(String id){
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if (optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }
    //页面静态化方法
    public String getPageHtml(String pageId){
        /**
         * 静态化程序获取页面的DataUrl
         * 静态化程序远程请求DataUrl获取数据模型。
         * 静态化程序获取页面的模板信息
         * 执行页面静态化
         */
        //获取数据模型
        Map model = getModelByPageId(pageId);
        if (model == null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //获取页面的模板信息
        String template = getTemplateByPageId(pageId);
        if (template == null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //执行静态化
        String html = generateHtml(template, model);
        return html;
    }
        //执行静态化
    private String generateHtml(String templateContent,Map model){

        //创建配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        //创建模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateContent);
        //配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        //获取模板内容
        try {
            Template template = configuration.getTemplate("template");
            //调用API进行静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //获取数据模型。
    private Map getModelByPageId(String pageId){
        //取出页面的信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)){
            //页面dataUrl为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //通过restTemplate远程调用获取数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }
    //获取页面的模板信息
    private String getTemplateByPageId(String pageId) {
        //取出页面的信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //获取页面的模板ID
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //获取模板的信息
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            //获取模板文件ID
            String templateFileId = cmsTemplate.getTemplateFileId();
            //从GridFS中取模板文件的内容
            //根据文件的ID查询文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开一个下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建gridFsResource，用于获取流对象
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            //从流中取数据
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    /**
     * cms将静态化后的页面存储到GridFs中，然后通过mq通知客户端去GridFs下载到服务器。
     * @param pageId
     * @return
     */

    //页面发布
    public ResponseResult post(String pageId){
        //执行页面静态化
        String pageHtml = this.getPageHtml(pageId);
        //将页面静态化文件存储到GridFs中
        CmsPage cmsPage = saveHtml(pageId, pageHtml);
        //向MQ发送消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //向MQ发送消息
    private void sendPostPage(String pageId) {
        //得到页面信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //创建消息对象
        HashMap<String, String> msg = new HashMap<>();
        msg.put("pageId", pageId);
        //转成json串
        String jsonString = JSON.toJSONString(msg);
        //发送给mq
        //站点id
        String siteId = cmsPage.getSiteId();
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, siteId,jsonString);
    }
    //将页面静态化文件存储到GridFs中
    private CmsPage saveHtml(String pageId,String htmlContent){
        //先得到页面信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        ObjectId objectId = null;
        try {
            //将htmlContent内容转成输入流
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            //将html文件内容保存到GridFs
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将html文件id更新到CmsPage中
        cmsPage.setHtmlFileId(objectId.toHexString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }
    //添加页面，如果已存在则更新页面
    public CmsPageResult save(CmsPage cmsPage) {
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(
                cmsPage.getPageName(), cmsPage.getSiteId(),
                cmsPage.getPageWebPath());
        if (cmsPage1 != null){
            //更新
            return this.update(cmsPage1.getPageId(), cmsPage);
        }else{
            //添加
            return this.add(cmsPage);
        }

    }
    //一键发布页面
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //将页面信息存储到cms_page集合中
        CmsPageResult save = this.save(cmsPage);
        if (!save.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面的ID
        CmsPage cmsPageSave = save.getCmsPage();
        String pageId = cmsPageSave.getPageId();
        //执行页面发布(先静态化，保存GridFS,向MQ发送消息)
        ResponseResult post = this.post(pageId);
        if (!post.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //拼接页面Url= cmsSite.siteDomain+cmsSite.siteWebPath+ cmsPage.pageWebPath + cmsPage.pageName
        //取出站点id
        String siteId = cmsPageSave.getSiteId();
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        //页面URL
        String pageUrl = cmsSite.getSiteDomain()+cmsSite.getSiteWebPath()+cmsPageSave.getPageWebPath()+cmsPageSave.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS, pageUrl);
    }
    //根据站点id查询站点信息
    public CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }
}
