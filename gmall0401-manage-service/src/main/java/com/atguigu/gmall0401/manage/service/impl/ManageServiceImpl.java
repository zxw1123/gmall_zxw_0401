package com.atguigu.gmall0401.manage.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.*;
import com.atguigu.gmall0401.manage.mapper.*;
import com.atguigu.gmall0401.service.ManageService;
import com.atguigu.gmall0401.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServiceImpl implements ManageService {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    SpuImageMapper spuImageMapper;
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;


    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;


    public static final String  SKUKEY_PREFIX="sku:";
    public static final String  SKUKEY_INFO_SUFFIX=":info";
    public static final String  SKUKEY_LOCK_SUFFIX=":lock";

    //查询一级分类
    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    //查询二级分类
    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2);
        return baseCatalog2List;
    }

    //查询三级分类
    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3);
        return baseCatalog3List;
    }


    //根据三级分类查询属性列表。
    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
       // 第一种方法
        // BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        //baseAttrInfo.setCatalog3Id(catalog3Id);
//        Example example = new Example(BaseAttrInfo.class);
//        example.createCriteria().andEqualTo("catalog3Id",catalog3Id);
//        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectByExample(example);
//        //查询平台属性值
//        for (BaseAttrInfo baseAttrInfo : baseAttrInfoList) {
//            BaseAttrValue baseAttrValue = new BaseAttrValue();
//            baseAttrValue.setAttrId(baseAttrInfo.getId());
//            List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
//            baseAttrInfo.setAttrValueList(baseAttrValueList);
//        }

        //根据自己定义的sql语句来实现业务的查询工作。
        List<BaseAttrInfo> baseAttrInfoListByCatalog3Id = baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);
        return baseAttrInfoListByCatalog3Id;
    }

    //提取attrInfo信息，保存属性信息
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if(baseAttrInfo.getId()!=null&&baseAttrInfo.getId().length()>0){
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        }else {
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        BaseAttrValue baseAttrValue4Del = new BaseAttrValue();
        baseAttrValue4Del.setAttrId(baseAttrInfo.getId());

        baseAttrValueMapper.delete(baseAttrValue4Del);
        if(baseAttrInfo.getAttrValueList()!=null&&baseAttrInfo.getAttrValueList().size()>0){
            for (BaseAttrValue attrValue:baseAttrInfo.getAttrValueList()){
                attrValue.setId(null);
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    //通过attrId得到baseAttrInfo。
    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        //在查出一个BaseAttrInfo之后，由于它的集合不在这个表中有显示
        //所以需要通过这个表中的id来到另一个表中进行查询，才能得到相关的数据。
        BaseAttrInfo attrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrInfo.getId());
        List<BaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);
        attrInfo.setAttrValueList(attrValueList);
        return attrInfo;

    }

    //得到基本销售属性的所有列表？？？
    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    //保存标准产品单元信息。
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //spu基本信息
        spuInfoMapper.insertSelective(spuInfo);
        // 图片信息
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(spuImage);
        }
        // 销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insertSelective(spuSaleAttr);
            // 销售属性值
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }
        }
    }

    //通过三级分类得到spu列表。
    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        return spuInfoMapper.select(spuInfo);
    }

    //通过spuid得到spuList。
    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    //通过spuid得到spu销售属性列表
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrListBySpuId(spuId);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        //基本信息
        if (skuInfo.getId() == null || skuInfo.getId().length() == 0) {
            int i = skuInfoMapper.insertSelective(skuInfo);
            System.out.println(i);
        } else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        //平台属性
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue attrValue : skuAttrValueList) {
            attrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insertSelective(attrValue);
        }
        //销售属性
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
            saleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(saleAttrValue);
        }


        //保存图片
        SkuImage skuImage4Del = new SkuImage();
        skuImage4Del.setId(skuInfo.getId());
        skuImageMapper.delete(skuImage4Del);
        for (SkuImage skuImage : skuInfo.getSkuImageList()) {
            skuImageMapper.insertSelective(skuImage);
        }

    }


//   public SkuInfo getSkuInfo(String skuId) {    //教案上的方法
//        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
//
//        SkuImage skuImage = new SkuImage();
//        skuImage.setSkuId(skuId);
//        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
//        skuInfo.setSkuImageList(skuImageList);
//
//        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
//        skuSaleAttrValue.setSkuId(skuId);
//        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);
//        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);
//
//        return skuInfo;
//    }


    //通过skuId获取数据库中的数据
    public SkuInfo getSkuInfoDB(String skuId) {
        System.err.println(Thread.currentThread()+"读取数据库！！");
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        if(skuInfo==null){
            return null;
        }
        //图片
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        //销售属性
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);

        //平台属性
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);


        return skuInfo;
    }



    //通过skuid先获取缓存中的数据，如没有再查询数据库，获取数据。
    @Override
    public SkuInfo getSkuInfo(String skuId) {
        SkuInfo skuInfoResult=null;
        //1  先查redis  没有再查数据库
        Jedis jedis = redisUtil.getJedis();
        int SKU_EXPIRE_SEC=100;
        // redis结构 ： 1 type  string  2 key   sku:101:info  3 value  skuInfoJson
        String skuKey=SKUKEY_PREFIX+skuId+SKUKEY_INFO_SUFFIX;
        String skuInfoJson = jedis.get(skuKey);
        if(skuInfoJson!=null){
            if(!"EMPTY".equals(skuInfoJson)){
                System.out.println(Thread.currentThread()+"命中缓存！！");
                skuInfoResult = JSON.parseObject(skuInfoJson, SkuInfo.class);
            }
        }else{
            Config config = new Config();
            config.useSingleServer().setAddress("redis://redis.gmall.com:6379");
            RedissonClient redissonClient = Redisson.create(config);
            String lockKey=SKUKEY_PREFIX+skuId+SKUKEY_LOCK_SUFFIX;
            RLock lock = redissonClient.getLock(lockKey);
            // lock.lock(10,TimeUnit.SECONDS);
            boolean locked=false ;
            try {
                locked = lock.tryLock(10, 5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(locked) {
                System.out.println(Thread.currentThread() + "得到锁！！");
                // 如果得到锁后能够在缓存中查询 ，那么直接使用缓存数据 不用在查询数据库
                System.out.println(Thread.currentThread()+"再次查询缓存！！");
                String skuInfoJsonResult = jedis.get(skuKey);
                if (skuInfoJsonResult != null) {
                    if (!"EMPTY".equals(skuInfoJsonResult)) {
                        System.out.println(Thread.currentThread() + "命中缓存！！");
                        skuInfoResult = JSON.parseObject(skuInfoJsonResult, SkuInfo.class);
                    }

                } else {
                    skuInfoResult = getSkuInfoDB(skuId);

                    System.out.println(Thread.currentThread() + "写入缓存！！");

                    if (skuInfoResult != null) {
                        skuInfoJsonResult = JSON.toJSONString(skuInfoResult);
                    } else {
                        skuInfoJsonResult = "EMPTY";
                    }
                    jedis.setex(skuKey, SKU_EXPIRE_SEC, skuInfoJsonResult);
                }
                lock.unlock();
            }

        }
        return skuInfoResult;
    }

    public SkuInfo getSkuInfo_redis(String skuId) {

        SkuInfo skuInfoResult=null;
        //1  先查redis  没有再查数据库
        Jedis jedis = redisUtil.getJedis();
        int SKU_EXPIRE_SEC=100;
        // redis结构 ： 1 type  string  2 key   sku:101:info  3 value  skuInfoJson
        String skuKey=SKUKEY_PREFIX+skuId+SKUKEY_INFO_SUFFIX;
        String skuInfoJson = jedis.get(skuKey);
        if(skuInfoJson!=null){
            if(!"EMPTY".equals(skuInfoJson)){
                System.out.println(Thread.currentThread()+"命中缓存！！");
                skuInfoResult = JSON.parseObject(skuInfoJson, SkuInfo.class);
            }

        }else{
            System.out.println(Thread.currentThread()+"未命中！！");
            //setnx     1  查锁   exists 2 抢锁  set
            //定义一下 锁的结构   type  string     key  sku:101:lock      value  locked
            String lockKey=SKUKEY_PREFIX+skuId+SKUKEY_LOCK_SUFFIX;
//            Long locked = jedis.setnx(lockKey, "locked");
//            jedis.expire(lockKey,10);
            String token= UUID.randomUUID().toString();
            String locked = jedis.set(lockKey, token, "NX", "EX", 100);

            if("OK".equals(locked)){
                System.out.println(Thread.currentThread()+"得到锁！！");

                skuInfoResult = getSkuInfoDB(skuId);

                System.out.println(Thread.currentThread()+"写入缓存！！");
                String skuInfoJsonResult=null;
                if(skuInfoResult!=null){
                    skuInfoJsonResult = JSON.toJSONString(skuInfoResult);
                }else{
                    skuInfoJsonResult="EMPTY";
                }
                jedis.setex(skuKey,SKU_EXPIRE_SEC,skuInfoJsonResult);
                System.out.println(Thread.currentThread()+"释放锁！！"+lockKey);
                if(jedis.exists(lockKey)&&token.equals(jedis.get(lockKey))){   // 不完美 ，可以用lua解决
                    jedis.del(lockKey);
                }

            }else{
                System.out.println(Thread.currentThread()+"为得到锁，开始自旋等待！！");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getSkuInfo(  skuId);
            }

        }

        jedis.close();
        return   skuInfoResult;

    }


    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckSkuId(String skuId, String spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.getSpuSaleAttrListBySpuIdCheckSku(skuId, spuId);
        return spuSaleAttrList;
    }

    @Override
    public Map getSkuValueIdsMap(String spuId) {
        List<Map> mapList = skuSaleAttrValueMapper.getSaleAttrValuesBySpu(spuId);
        Map skuValueIdsMap =new HashMap();

        for (Map  map : mapList) {
            String skuId =(Long ) map.get("sku_id") +"";
            String valueIds =(String ) map.get("value_ids");
            skuValueIdsMap.put(valueIds,skuId);
        }
        return skuValueIdsMap;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List attrValueIdList) {
        //attrValueIdList -->  13,15,54 转化为字符串
        String valueIds = StringUtils.join(attrValueIdList.toArray(), ",");

        return  baseAttrInfoMapper.getBaseAttrInfoListByValueIds(valueIds);


    }
}
