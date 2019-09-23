package com.atguigu.gmall0401.service;

import com.atguigu.gmall0401.bean.UserInfo;

import java.util.List;

public interface UserService {

    List<UserInfo> getUserInfoListAll();

    void addUser(UserInfo userInfo);

    void updateUser(UserInfo userInfo);

    void updateUserByName(String name,UserInfo userInfo);

    void delUser(UserInfo userInfo);

    public UserInfo getUserInfoById(String userid);

    public UserInfo login(UserInfo userInfo);

    public Boolean verify(String userId);


}
