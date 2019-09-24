package com.atguigu.gmall0401.gmall0401user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.UserInfo;
import com.atguigu.gmall0401.gmall0401user.mapper.UserMapper;
import com.atguigu.gmall0401.service.UserService;

import com.atguigu.gmall0401.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UserInfo> getUserInfoListAll() {
        List<UserInfo> userInfoList = userMapper.selectAll();
        return userInfoList;
    }

    public UserInfo getUserInfo(String id) {
        UserInfo userInfo = userMapper.selectByPrimaryKey(id);
        return userInfo;
    }

    @Override
    public void addUser(UserInfo userInfo) {
        userMapper.insert(userInfo);
    }

    @Override
    public void updateUser(UserInfo userInfo) {
        userMapper.updateByPrimaryKeySelective(userInfo);
    }

    @Override
    public void updateUserByName(String name, UserInfo userInfo) {

        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name", name);
        userMapper.updateByExample(userInfo, example);

    }

    @Override
    public void delUser(UserInfo userInfo) {
        userMapper.delete(userInfo);
    }

    @Override
    public UserInfo getUserInfoById(String userid) {
        return userMapper.selectByPrimaryKey(userid);
    }

    public String userKey_prefix = "user:";
    public String userinfoKey_suffix = ":info";
    public int userKey_timeOut = 60 * 60 * 24;

    @Override
    public UserInfo login(UserInfo userInfo) {
        //1 比对数据库信息，用户名密码
        String passwd = userInfo.getPasswd();
        String passwdMD5 = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(passwdMD5);
        UserInfo userInfoExists = userMapper.selectOne(userInfo);
        if (userInfoExists != null) {
            Jedis jedis = redisUtil.getJedis();
            String userKey = userKey_prefix + userInfoExists.getId() + userinfoKey_suffix;
            String userInfoJson = JSON.toJSONString(userInfoExists);
            jedis.setex(userKey, userKey_timeOut, userInfoJson);
            jedis.close();

            return userInfoExists;
        }
        return null;
    }

    @Override
    public Boolean verify(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String userKey = userKey_prefix + userId + userinfoKey_suffix;
        Boolean isLogin = jedis.exists(userKey);
        if (isLogin) {  //如果经过验证，延长用户使用时间
            jedis.expire(userKey, userKey_timeOut);
        }
        jedis.close();
        return isLogin;
    }
}
