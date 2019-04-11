package com.xuecheng.ucenter.test;

import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DaoTest {

    @Autowired
    XcMenuMapper menuMapper;

    @Test
    public void selectPermissionByUserId(){
        List<XcMenu> xcMenus = menuMapper.selectPermissionByUserId("49");
        for (XcMenu xcMenu : xcMenus) {
            System.out.println(xcMenu);
        }
    }
}
