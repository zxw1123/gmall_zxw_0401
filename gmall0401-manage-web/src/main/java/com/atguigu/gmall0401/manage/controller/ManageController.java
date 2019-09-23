package com.atguigu.gmall0401.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.atguigu.gmall0401.bean.*;

import com.atguigu.gmall0401.service.ListService;
import com.atguigu.gmall0401.service.ManageService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@CrossOrigin
public class ManageController {

    @Reference
    ManageService manageService;

    @Reference
    ListService listService;

    //得到一级分类
    @PostMapping("getCatalog1")
    public List<BaseCatalog1> getBaseCatalog1(){
        List<BaseCatalog1> catalog1List = manageService.getCatalog1();
        return catalog1List;
    }

    //得到二级分类
    @PostMapping("getCatalog2")
    public List<BaseCatalog2> getBaseCatalog2(String catalog1Id){
        List<BaseCatalog2> catalog2List = manageService.getCatalog2(catalog1Id);
        return catalog2List;
    }

    //得到三级分类
    @PostMapping("getCatalog3")
    public List<BaseCatalog3> getBaseCatalog3(String catalog2Id){
        List<BaseCatalog3> catalog3List = manageService.getCatalog3(catalog2Id);
        return catalog3List;
    }

    // 根据三级分类ID查询attrInfoList
    @GetMapping("attrInfoList")
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        return manageService.getAttrList(catalog3Id);
    }

    // 保存BaseAttrInfo信息
    @PostMapping("saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
    }

    //根据attrId查询属性值列表
    @PostMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){
        BaseAttrInfo attrInfo = manageService.getAttrInfo(attrId);
        return attrInfo.getAttrValueList();
    }

    @PostMapping("baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        return manageService.getBaseSaleAttrList();
    }

    //保存spuInfo信息。
    @PostMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return "success";
    }

    //根据三级分类Id获取spu列表
    @GetMapping("spuList")
    public List<SpuInfo> getSpuList(String catalog3Id){
        return manageService.getSpuList(catalog3Id);
    }

    //根据spuId获取图片列表。
    @GetMapping("spuImageList")
    public List<SpuImage> getSpuImageList(String spuId){
        return manageService.getSpuImageList(spuId);

    }

    //根据spuId获取销售信息列表。
    @GetMapping("spuSaleAttrList")
    public  List<SpuSaleAttr> getSpuSaleAttrList(String spuId){
        return  manageService.getSpuSaleAttrList(spuId);
    }

    public  String onSaleBySpu(String spuId){
        // 根据spu 把其下所有sku上架
        return null;
    }

    @PostMapping("onSale")
    public  String onSale(@RequestParam("skuId") String skuId){

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        //
        listService.saveSkuLsInfo(skuLsInfo);

        return "success";

    }
}
