package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachplanRespository extends JpaRepository<Teachplan,String> {

    //根据课程ID和父ID查询教学计划列表，可以使用此方法实现查询根节点
    public List<Teachplan> findByCourseidAndParentid(String courseId,String parentId);
}
