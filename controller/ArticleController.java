package com.testblog.controller;

import com.testblog.component.StringAndArray;
import com.testblog.constant.CodeType;
import com.testblog.entity.Article;
import com.testblog.service.ArticleService;
import com.testblog.service.CategoryService;
import com.testblog.service.UserService;
import com.testblog.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;


@RestController
@Slf4j
public class ArticleController {
    @Autowired
    ArticleService articleService;
    @Autowired
    UserService userService;

    @Autowired
    CategoryService categoryService;

    /**
     * 发表博客
     * @param principal 当前登录用户
     * @param article 文章
     * @param request httpServletRequest
     * @return
     */
    @PostMapping(value = "/publishArticle", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String publishArticle(@AuthenticationPrincipal Principal principal, Article article,
                                 @RequestParam("articleGrade") String articleGrade,
                                 HttpServletRequest request){
        try {

            String username = principal.getName();
            String phone = userService.findPhoneByUsername(username);
            if(!userService.isSuperAdmin(phone)){
                return JsonResult.fail(CodeType.PUBLISH_ARTICLE_NO_PERMISSION).toJSON();
            }
            String id = request.getParameter("id");
            long articleId = Integer.parseInt(id);
            article.setArticleId(articleId);
            article.setAuthor(username);
            DataMap data = articleService.insertArticle(article);
            return JsonResult.build(data).toJSON();
        } catch (Exception e){
            log.error("Publish article [{}] exception", article.getArticleTitle(), e);
        }
        return JsonResult.fail(CodeType.SERVER_EXCEPTION).toJSON();
    }

    /**
     * 验证是否有权限写博客
     * @param principal
     * @return
     */
    @GetMapping(value = "/canYouWrite", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)

    public String canYouWrite(@AuthenticationPrincipal Principal principal){

        try {
            String username = principal.getName();
            String phone = userService.findPhoneByUsername(username);
            if(userService.isSuperAdmin(phone)){
                return JsonResult.success().toJSON();
            }
            return JsonResult.fail().toJSON();
        } catch (Exception e){
            log.error("Can you write exception", e);
        }
        return JsonResult.fail(CodeType.SERVER_EXCEPTION).toJSON();
    }

    /**
     * 获得所有的分类
     * @return
     */
    @GetMapping(value = "/findCategoriesName", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String findCategoriesName(){
        try {
            DataMap data = categoryService.findCategoriesName();
            return JsonResult.build(data).toJSON();
        } catch (Exception e){
            log.error("Find category name exception", e);
        }
        return JsonResult.fail(CodeType.SERVER_EXCEPTION).toJSON();
    }

    @PostMapping(value = "/updateCategory", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String updateCategory(@RequestParam("categoryName") String  categoryName,
                                 @RequestParam("type") int type){
        try {
            DataMap data = categoryService.updateCategory(categoryName, type);
            return JsonResult.build(data).toJSON();
        } catch (Exception e){
            log.error("Update type [{}] article categories [{}] exception", type, categoryName, e);
        }
        return JsonResult.fail(CodeType.SERVER_EXCEPTION).toJSON();
    }



    /**
     * 文章编辑本地上传图片
     */
    @RequestMapping("/uploadImage")
    public Map<String,Object> uploadImage(HttpServletRequest request, HttpServletResponse response,
                                          @RequestParam(value = "editormd-image-file", required = false) MultipartFile file){
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
            request.setCharacterEncoding( "utf-8" );
            //设置返回头后页面才能获取返回url
            response.setHeader("X-Frame-Options", "SAMEORIGIN");

            FileUtil fileUtil = new FileUtil();
            String filePath = this.getClass().getResource("/").getPath().substring(1) + "blogImg/";
            String fileContentType = file.getContentType();
            String fileExtension = fileContentType.substring(fileContentType.indexOf("/") + 1);
            TimeUtil timeUtil = new TimeUtil();
            String fileName = timeUtil.getLongTime() + "." + fileExtension;

            String subCatalog = "blogArticles/" + new TimeUtil().getFormatDateForThree();
            String fileUrl = fileUtil.uploadFile(fileUtil.multipartFileToFile(file, filePath, fileName), subCatalog);

            resultMap.put("success", 1);
            resultMap.put("message", "上传成功");
            resultMap.put("url", fileUrl);
        } catch (Exception e) {
            try {
                response.getWriter().write( "{\"success\":0}" );
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return resultMap;
    }

    @GetMapping(value = "/deleteArticle", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String deleteArticle(@RequestParam("id") String id){
        try {
            if(StringUtil.BLANK.equals(id) || id == null){
                return JsonResult.build(DataMap.fail(CodeType.DELETE_ARTICLE_FAIL)).toJSON();
            }
            DataMap data = articleService.deleteArticle(Long.parseLong(id));
            return JsonResult.build(data).toJSON();
        } catch (Exception e){
            log.error("Delete article [{}] exception", id, e);
        }
        return JsonResult.fail(CodeType.SERVER_EXCEPTION).toJSON();
    }

    @GetMapping(value = "/getArticleCategories", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getArticleCategories(){
        try {
            DataMap data = categoryService.findAllCategories();
            return JsonResult.build(data).toJSON();
        } catch (Exception e){
            log.error("Get article categories exception", e);
        }
        return JsonResult.fail(CodeType.SERVER_EXCEPTION).toJSON();
    }


}
