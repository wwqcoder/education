package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.*;
import com.xuecheng.manage_course.feign.CmsPageClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    CourseMapper courseMapper;
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanRespository teachplanRespository;
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CoursePicRepository coursePicRepository;
    @Autowired
    CoursePubRepository coursePubRepository;
    @Autowired
    CmsPageClient cmsPageClient;
    @Autowired
    TeachplanMediaRespository teachplanMediaRespository;
    @Autowired
    TeachplanMediaPubRespository teachplanMediaPubRespository;
    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;
    //查询课程计划
    public TeachplanNode findTeachplanList(String courseId){
        return teachplanMapper.selectList(courseId);
    }
    //增、删、改，都需要添加事务
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if (teachplan == null ||
                StringUtils.isEmpty(teachplan.getPname()) ||
                StringUtils.isEmpty(teachplan.getCourseid()))
         {
            //非法参数
             ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //获取课程ID
        String courseid = teachplan.getCourseid();
        //获取父节点的ID
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)){
            //如果父节点的ID为空，那设置为课程的根节点
            parentid = getTeachplanRoot(courseid);
        }
        //查询根节点的信息
        Optional<Teachplan> optional = teachplanRespository.findById(parentid);
        Teachplan teachplan1 = optional.get();
        //父节点的级别
        String parent_grade = teachplan1.getGrade();
        //创建一个新节点准备添加
        Teachplan teachplanNew = new Teachplan();
        //将teachplan的属性copy到teachplanNew中
        BeanUtils.copyProperties(teachplan, teachplanNew);
        //设置必要的属性
        teachplanNew.setParentid(parentid);
        if (parent_grade.equals("1")){
            teachplanNew.setGrade("2");
        }else {
            teachplanNew.setGrade("3");
        }
        //未发布
        teachplanNew.setStatus("0");
        teachplanRespository.save(teachplanNew);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //获取课程的根节点
    private String getTeachplanRoot(String courseid) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseid);
        //说明对象为空
        if (!optional.isPresent()){
            return null;
        }
        CourseBase courseBase = optional.get();
        //调用DAO查询teachplan表得到该课程的根节点(一级节点)
        List<Teachplan> teachplanList = teachplanRespository.findByCourseidAndParentid(courseid, "0");
        if (teachplanList == null || teachplanList.size() <= 0){
            //新添加一个课程的节点
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseid);
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setStatus("0");
            teachplan.setPname(courseBase.getName());
            teachplanRespository.save(teachplan);
            return teachplan.getId();
        }
        return teachplanList.get(0).getId();
    }

    //添加课程提交
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase){
        //课程状态默认为未发布
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS, courseBase.getId());
    }

    //通过课程ID获得课程基础信息
    public CourseBase getCoursebaseById(String courseid){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseid);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }
    @Transactional
    public ResponseResult updateCourseBase(String id,CourseBase courseBase){
        CourseBase one = this.getCoursebaseById(id);
        if (one == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //修改课程信息
        one.setName(courseBase.getName());
        one.setMt(courseBase.getMt());
        one.setSt(courseBase.getSt());
        one.setGrade(courseBase.getGrade());
        one.setStudymodel(courseBase.getStudymodel());
        one.setUsers(courseBase.getUsers());
        one.setDescription(courseBase.getDescription());
        CourseBase save = courseBaseRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CourseMarket getCourseMarketById(String courseId){
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Transactional
    public CourseMarket updateCourseMarket(String id,CourseMarket courseMarket){
        CourseMarket one = this.getCourseMarketById(id);
        if (one == null){
            one = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, one);
            one.setId(id);
            courseMarketRepository.save(one);
        }else{
            one.setCharge(courseMarket.getCharge());
            one.setStartTime(courseMarket.getStartTime());
            one.setEndTime(courseMarket.getEndTime());
            one.setPrice(courseMarket.getPrice());
            one.setQq(courseMarket.getQq());
            one.setValid(courseMarket.getValid());
            courseMarketRepository.save(one);
        }
        return one;
    }
    //添加课程图片
    @Transactional
    public ResponseResult addCoursePic(String courseId,String pic){
        //查询课程图片
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if (optional.isPresent()){
            coursePic = optional.get();
        }
        //没有课程图片则新建对象
        if (coursePic == null){
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        //保存课程图片
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //查询课程图片
    public CoursePic findCoursePic(String courseId){
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()){
            CoursePic coursePic = optional.get();
            return coursePic;
        }
        return null;
    }
    //删除课程图片
    @Transactional
    public ResponseResult deleteCoursePic(String courseId){
        //执行删除，返回1表示删除成功，返回0表示删除失败
        long result = coursePicRepository.deleteByCourseid(courseId);
        if (result > 0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }
    //课程视图查询
    public CourseView getCourseView(String id) {
        CourseView courseView = new CourseView();
        //查询课程基本信息
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if (optional.isPresent()){
            CourseBase courseBase = optional.get();
            courseView.setCourseBase(courseBase);
        }
        //查询课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if (marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }
        //查询课程图片信息
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if (picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            courseView.setCoursePic(coursePic);
        }
        //查询课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_DENIED_DELETE);
        return null;
    }

    //课程预览
    public CoursePublishResult preview(String id) {
        //查询课程
        CourseBase courseBaseById = this.findCourseBaseById(id);
        //请求cms添加页面
        //准备cmsPage信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点ID
        cmsPage.setDataUrl(publish_dataUrlPre+id); //数据模型URL
        cmsPage.setPageName(id+".html"); //页面名称
        cmsPage.setPageAliase(courseBaseById.getName()); //页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webpath
        cmsPage.setTemplateId(publish_templateId);//页面模板id

        //远程调用cms
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if (!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();
        //拼装页面预览的url
        String url = previewUrl + pageId;
        //返回CoursePublishResult对象，包括页面预览的URL
        return new CoursePublishResult(CommonCode.SUCCESS,url);
    }
    //课程发布
    @Transactional
    public CoursePublishResult publish(String id) {
        //查询课程
        CourseBase courseBaseById = this.findCourseBaseById(id);
        //请求cms添加页面
        //准备cmsPage信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点ID
        cmsPage.setDataUrl(publish_dataUrlPre+id); //数据模型URL
        cmsPage.setPageName(id+".html"); //页面名称
        cmsPage.setPageAliase(courseBaseById.getName()); //页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webpath
        cmsPage.setTemplateId(publish_templateId);//页面模板id
        //调用cms一键发布接口将课程详情页面发布到服务器
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!cmsPostPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //保存课程的发布状态为‘已发布’
        CourseBase courseBase = this.saveCoursePubState(id);
        if (courseBase == null){
            return new CoursePublishResult(CommonCode.FAIL, null);
        }

        //保存课程索引信息
        //先创建一个coursePub对象
        CoursePub coursePub = createCoursePub(id);
        //将coursePub对象保存到数据库
        saveCoursePub(id,coursePub);
        //缓存课程的信息
        //得到页面的URL
        String pageUrl = cmsPostPageResult.getPageUrl();
        //保存课程计划媒资信息到索引表
        saveTeachplanMediaPub(id);
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    //保存课程计划媒资信息到索引表
    private void saveTeachplanMediaPub(String courseId) {
        //先删除TeachplanMediaPub中的数据
        teachplanMediaPubRespository.deleteByCourseId(courseId);
        //从teachplanMedia中查询
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRespository.findByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
        //将teachplanMediaList数据放到teachplanMediaPubs中
        for (TeachplanMedia teachplanMedia : teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia, teachplanMediaPub);
            //添加时间戳
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubs.add(teachplanMediaPub);
        }
        //将teachplanMediaList插入到teachplanMediaPub
        teachplanMediaPubRespository.saveAll(teachplanMediaPubs);
    }

    //将coursePub对象保存到数据库
    private CoursePub saveCoursePub(String id, CoursePub coursePub) {
        CoursePub coursePubNew = null;
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        if (coursePubOptional.isPresent()){
            coursePubNew = coursePubOptional.get();
        }else {
            coursePubNew = new CoursePub();
        }

        //将CoursePub对象中的信息保存到coursePubNew中
        BeanUtils.copyProperties(coursePub, coursePubNew);
        coursePubNew.setId(id);
        //时间戳，给logstach使用
        coursePubNew.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(date);
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }
    //创建一个coursePub对象
    private CoursePub createCoursePub(String id) {
        CoursePub coursePub = new CoursePub();
        //根据课程ID查询course_base
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(id);
        if (baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            //将coursebase属性copy到CoursePub中
            BeanUtils.copyProperties(courseBase, coursePub);
        }
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }

        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }
        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        String jsonString = JSON.toJSONString(teachplanNode);
        //将课程计划信息json保存到course_pub中
        coursePub.setTeachplan(jsonString);
        return coursePub;
    }

    //保存课程的发布状态为‘已发布’
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBaseById = this.findCourseBaseById(courseId);
        courseBaseById.setStatus("202002");
        courseBaseRepository.save(courseBaseById);
        return courseBaseById;
    }

    //保存课程计划㔿媒资文件的关联信息
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if (teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //校验课程计划是否是3级
        //课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        Optional<Teachplan> optional = teachplanRespository.findById(teachplanId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //查询到教学计划
        Teachplan teachplan = optional.get();
        //取出等级
        String grade = teachplan.getGrade();
        if (StringUtils.isEmpty(grade) || !grade.equals("3")){
            //只允许选择第三级的课程计划关联视频
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }

        //查询teachplanMedia
        Optional<TeachplanMedia> mediaOptional = teachplanMediaRespository.findById(teachplanId);
        TeachplanMedia one = null;
        if (mediaOptional.isPresent()){
            one = mediaOptional.get();
        }else{
            one = new TeachplanMedia();
        }
        //将one保存到数据库
        one.setCourseId(teachplan.getCourseid());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        one.setTeachplanId(teachplanId);
        teachplanMediaRespository.save(one);

        return new ResponseResult(CommonCode.SUCCESS);
    }
    //查询课程
    public QueryResponseResult<CourseInfo> findCourseList(String company_id, int page, int size, CourseListRequest courseListRequest) {
        if (courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }

        //将公司ID参数传入dao
        courseListRequest.setCompanyId(company_id);
        //分页
        PageHelper.startPage(page, size);
        //调用dao
        Page<CourseInfo> courseList = courseMapper.findCourseList(courseListRequest);
        List<CourseInfo> list = courseList.getResult();
        long total = courseList.getTotal();
        QueryResult<CourseInfo> courseInfoQueryResult = new QueryResult<>();
        courseInfoQueryResult.setTotal(total);
        courseInfoQueryResult.setList(list);
        return new QueryResponseResult<>(CommonCode.SUCCESS, courseInfoQueryResult);
    }
}