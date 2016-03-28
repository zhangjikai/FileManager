package com.zhangjikai.controller;


import com.zhangjikai.pojo.FileDesc;
import com.zhangjikai.pojo.FileDescList;
import com.zhangjikai.pojo.FileMeta;
import com.zhangjikai.pojo.FileMsg;
import com.zhangjikai.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangjk on 2016/1/14.
 */
@RestController
public class FileUploadController {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    /**
     * Upload single file using Spring Controller
     */
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST, produces = "application/json;charset=utf8")
    @ResponseBody
    public FileDesc uploadFileHandler(@RequestParam("file") MultipartFile file, @RequestParam("path") String path, HttpServletResponse response) throws IOException {

        logger.info(path);
        FileDesc desc = null;
        if (!file.isEmpty()) {

            path = path.replace("\\", File.separator);
            Path folderPath = Paths.get(Constants.UPLOAD_FOLDER + File.separator + path);

            if(Files.notExists(folderPath))
                Files.createDirectories(folderPath);
            Path filePath = Paths.get(Constants.UPLOAD_FOLDER + File.separator + path + File.separator + file.getOriginalFilename());
            if(!Files.notExists(filePath)) {
                desc = new FileDesc(false, "文件已存在");
                return desc;
            }
            try (InputStream in = file.getInputStream(); OutputStream out = Files.newOutputStream(filePath)) {
                byte[] b = new byte[1024];
                int len = 0;
                while ((len = in.read(b)) > 0) {
                    out.write(b, 0, len);
                }
                logger.info("Server File Location=" + filePath.toAbsolutePath().toString());
                desc = new FileDesc(true, "");
                return desc;
            } catch (IOException e) {
                e.printStackTrace();
                desc = new FileDesc(false, "系统内部错误，请稍后重试");
                return desc;
            }
        } else {
            desc = new FileDesc(false, "文件为空");
            return desc;
        }
    }

    @RequestMapping(value = "/uploadMultiFile", method = RequestMethod.POST, produces = "application/json;charset=utf8")
    @ResponseBody
    public List<FileDesc> uploadMultiFileHandler(@RequestParam("file") MultipartFile[] files, @RequestParam("path") String path, HttpServletResponse response) throws IOException {

        logger.info(path);
        FileDesc desc = null;
        List<FileDesc> fileDescList = new ArrayList<>();
        for(MultipartFile file : files) {
            if (!file.isEmpty()) {

                path = path.replace("\\", File.separator);
                Path folderPath = Paths.get(Constants.UPLOAD_FOLDER + File.separator + path);

                if (Files.notExists(folderPath))
                    Files.createDirectories(folderPath);
                Path filePath = Paths.get(Constants.UPLOAD_FOLDER + File.separator + path + File.separator + file.getOriginalFilename());
                if (!Files.notExists(filePath)) {
                    desc = new FileDesc(false, "文件已存在");
                    fileDescList.add(desc);
                    //return desc;
                }
                try (InputStream in = file.getInputStream(); OutputStream out = Files.newOutputStream(filePath)) {
                    byte[] b = new byte[1024];
                    int len = 0;
                    while ((len = in.read(b)) > 0) {
                        out.write(b, 0, len);
                    }
                    logger.info("Server File Location=" + filePath.toAbsolutePath().toString());
                    desc = new FileDesc(true, "");
                    fileDescList.add(desc);
                    //return desc;
                } catch (IOException e) {
                    e.printStackTrace();
                    desc = new FileDesc(false, "系统内部错误，请稍后重试");
                    fileDescList.add(desc);
                    //return desc;
                }
            } else {
                desc = new FileDesc(false, "文件为空");
                fileDescList.add(desc);
                //return desc;
            }
        }

        return fileDescList;
    }
}
