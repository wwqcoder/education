package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDao {
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanRespository teachplanRespository;
    @Autowired
    CategoryMapper categoryMapper;

    @Test
    public void testCourseBaseRepository(){
        Optional<CourseBase> optional = courseBaseRepository.findById("402885816240d276016240f7e5000002");
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            System.out.println(courseBase);
        }

    }

    @Test
    public void testCourseMapper(){
        CourseBase courseBase = courseMapper.findCourseBaseById("402885816240d276016240f7e5000002");
        System.out.println(courseBase);

    }
    @Test
    public void testTeachplanMapper(){
        TeachplanNode teachplanNode = teachplanMapper.selectList("4028e581617f945f01617f9dabc40000");
        System.out.println(teachplanNode);

    }

    @Test
    public void testTeachplanRespository(){
        /*List<Teachplan> teachplans = teachplanRespository.findByCourseidAndParentid("4028e581617f945f01617f9dabc40000", "0");
        System.out.println(teachplans);*/
        Optional<Teachplan> optional = teachplanRespository.findById("1");
        if (optional.isPresent()){
            Teachplan teachplan = optional.get();
            System.out.println(teachplan);
        }
    }
    @Test
    public void testPageHelper(){
        PageHelper.startPage(1, 10);
        Page<CourseBase> courseBasePage = courseMapper.findCourseListPage();
        List<CourseBase> result = courseBasePage.getResult();
        System.out.println(result);
    }
    @Test
    public void testCategoryMapper(){
        CategoryNode categoryNode = categoryMapper.selectList();
        System.out.println(categoryNode);

    }
    @Test
    public void getCoursebaseById(){
        Optional<CourseBase> optional = courseBaseRepository.findById("4028e581617f945f01617f9dabc40000");
        if (optional.isPresent()){
            CourseBase courseBase = optional.get();
            System.out.println(courseBase);
        }
    }
}
