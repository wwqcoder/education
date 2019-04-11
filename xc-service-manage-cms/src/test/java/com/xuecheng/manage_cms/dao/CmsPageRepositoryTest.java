package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.system.SysDictionary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    SysDictionaryRepository sysDictionaryRepository;
    @Autowired
    CmsSiteRepository cmsSiteRepository;

    @Test
    public void findAll(){
        List<CmsPage> all = cmsPageRepository.findAll();
        //System.out.println(all);
    }

    @Test
    public void testFindPage(){
        //PageRequest.of()中需要分页参数
        int page = 0; // 页码应该从0开始
        int size = 10;
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
       //System.out.println(all);
    }

    @Test
    public void testFindAllByExample(){
        int page = 0; // 页码应该从0开始
        int size = 10;
        Pageable pageable = PageRequest.of(page,size);
        //条件值对象
        CmsPage cmsPage = new CmsPage();
      //  cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
        //设置模板ID
     //   cmsPage.setTemplateId("5a962c16b00ffc514038fafd");
        //设置页面别名
        cmsPage.setPageAliase("轮播");
        //条件匹配器
//        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
//        exampleMatcher = exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        //定义Example
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        List<CmsPage> content = all.getContent();
        System.out.println(content);
    }
    @Test
    public void testUpdate(){
        //查询对象
        //Optional是一个容器对象
        Optional<CmsPage> optional = cmsPageRepository.findById("5a7be667d019f14d90a1fb1c");
        //把非空判断标准化
        if (optional.isPresent()){
            CmsPage cmsPage = optional.get();
            //设置要修改的值  分类导航
            cmsPage.setPageAliase("test01");
            //修改
            CmsPage save = cmsPageRepository.save(cmsPage);
            System.out.println(save);
        }
    }
    @Test
    public void testfindByPageName(){
        CmsPage cmsPage = cmsPageRepository.findByPageName("preview_4028e581617f945f01617f9dabc40000.html");
        System.out.println(cmsPage);
    }

    @Test
    public void testSysDictionaryRepository(){
        SysDictionary sysDictionary = sysDictionaryRepository.findBydType("100");
        System.out.println(sysDictionary);
    }
    @Test
    public void cmsSiteRepository(){
        Optional<CmsSite> optional = cmsSiteRepository.findById("5a751fab6abb5044e0d19ea1");
        if (optional.isPresent()){
            System.out.println(optional.get());
        }else{
            System.out.println("查询失败");
        }
    }
}
