package org.azim.controller;

import org.azim.model.RespBean;
import org.azim.model.TableClass;
import org.azim.service.GenerateCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 生成代码控制器
 */
@RestController
public class GenerateCodeController {

    @Autowired
    GenerateCodeService generateCodeService;

    /**
     * 代码生成
     * @param tableClassList 所有表信息
     * @param req HttpServletRequest
     * @return
     */
    @PostMapping("/generate")
    public RespBean generate(@RequestBody List<TableClass> tableClassList, HttpServletRequest req){
        return generateCodeService.generateCode(tableClassList,req.getServletContext().getRealPath("/"));
    }

}
