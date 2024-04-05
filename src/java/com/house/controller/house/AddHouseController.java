package com.house.controller.house;

import com.house.dto.HouseEquipmentList;
import com.house.entity.House;
import com.house.entity.HouseEquipment;
import com.house.service.IHouseEquipmentService;
import com.house.service.IHouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

/**
 * 添加房源信息控制类
 *
 * @author z
 */
@Controller
@RequestMapping("/house")
public class AddHouseController {

    /**
     * 映射本地路径
     * 注意：别漏了最后一个 /
     */
    private String dirPath = "./upload/hrs/";
    /**
     * 简介图片地址
     * 注意：虚拟路径映射的关键位置
     */
    private String simplePath = "/hrs/";
    /**
     * 详细图片地址
     */
    private StringBuilder detailsPath = new StringBuilder();

    @Autowired
    private IHouseService service;

    @Autowired
    private IHouseEquipmentService houseEquipmentService;
    /**
     * 添加房源界面
     *
     * @return view
     */
    @GetMapping("/addHouse.html")
    public String addHouse() {
        return "addHouse.jsp";
    }

    /**
     * 简介图片上传
     *
     * @param briefFile file
     * @return res
     */
    @RequestMapping("/briefImage")
    @ResponseBody
    public Map<String, Object> briefImage(@RequestParam("brief") MultipartFile briefFile) {

        simplePath = "/hrs/";
        Map<String, Object> map = new HashMap<>(16);
        try {
            String suffixName = Objects.requireNonNull(briefFile.getOriginalFilename())
                .substring(briefFile.getOriginalFilename().lastIndexOf("."));
            String filename = UUID.randomUUID().toString().replace("-", "") + suffixName;
            File filePath = new File(dirPath);
            if (!filePath.exists()) {
                boolean mkdirs = filePath.mkdirs();
            }
            //创建虚拟路径存储
            simplePath += filename;
            map.put("image", simplePath);
            briefFile.transferTo(new File(dirPath + filename));
            map.put("code", 0);
            map.put("msg", "上传成功");
        } catch (Exception e) {
            map.put("code", 1);
            map.put("msg", "上传失败");
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 详情图片上传
     *
     * @param file file
     * @param req  req
     * @return res
     */
    @RequestMapping("/detailsImage")
    @ResponseBody
    public Map<String, Object> detailsImage(@RequestParam("detailsImage") List<MultipartFile> file, HttpServletRequest req) {
        String localPath="";
        Map<String, Object> map = new HashMap<>(16);
        if (!file.isEmpty()) {
            for (MultipartFile f : file) {
                try {
                    // 文件名
                    String filename = UUID.randomUUID()
                        + Objects.requireNonNull(f.getOriginalFilename())
                        .substring(f.getOriginalFilename().lastIndexOf("."));

                    // 存储虚拟路径
                     localPath = "/hrs/"+ filename;
                    detailsPath.append(localPath + ":-:");

                    File filePath = new File(dirPath);
                    if (!filePath.exists()) {
                        boolean mkdirs = filePath.mkdirs();
                    }
                    //上传
                    f.transferTo(new File(dirPath + filename));

                } catch (Exception e) {
                    map.put("code", 1);
                    map.put("msg", "上传失败");
                    e.printStackTrace();
                }
            }
            map.put("image2",localPath+":-:");
            map.put("code", 0);
            map.put("msg", "上传成功");
        }
        return map;
    }

    /**
     * 添加新房源信息
     *
     * @param house 房源数据
     * @return res
     */
    @PostMapping("/addHouseRecord")
    @ResponseBody
    public String addHouse(House house, HouseEquipmentList houseEquipmentList) {
        if (house.getPublisher() == null || "".equals(house.getPublisher())) {
            house.setPublisher("管理员");
        }
//        house.setHouseImage(simplePath);
//        house.setHouseDetailsImg(detailsPath.toString());
        int n = service.addNewHouse(house);

        Map map=service.findHouseByName(house.getHouseDesc());
        Integer houseId=null;
        if(map!=null){
            houseId=Integer.valueOf(map.get("h_id").toString());
        }

        Integer[] str= HouseEquipmentList.getString(houseEquipmentList);
        for(int i=0;i<str.length;i++){
            if(str[i]!=null&&str[i]!=0){
                HouseEquipment houseEquipment =new HouseEquipment();
                houseEquipment.setHouseId(houseId);
                houseEquipment.setType(str[i]);
                houseEquipmentService.insert(houseEquipment);
            }
        }




       return "OK";


    }
}
