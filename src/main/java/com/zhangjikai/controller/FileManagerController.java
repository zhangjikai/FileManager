package com.zhangjikai.controller;

import com.zhangjikai.pojo.*;
import com.zhangjikai.utils.Constants;
import com.zhangjikai.utils.FileType;
import com.zhangjikai.utils.FtUtils;
import com.zhangjikai.utils.MultipartFileSender;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhangjk on 2016/1/17.
 */
@RestController
public class FileManagerController {

    private static final Logger logger = LoggerFactory.getLogger(FileManagerController.class);

    @RequestMapping(value = "/listFile", produces = "application/json;charset=utf8")
    @ResponseBody
    public FileDescList listFile(@RequestBody ReqParamList paramList) {
        ReqParam param = paramList.getParams();
        String pathStr = param.getPath();
        pathStr = pathStr.replace("\\", File.separator);
        Path currentPath = Paths.get(Constants.UPLOAD_FOLDER + pathStr);

        FileDesc desc = null;
        List<FileDesc> descList = new ArrayList<>();
        FileDescList result = new FileDescList(descList);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath)) {
            for (Path entry : stream) {

                if (Files.isDirectory(entry)) {
                    // 文件夹取不到文件大小
                    desc = new FileDesc(entry.getFileName().toString(), Files.size(entry), new Date(Files.getLastModifiedTime(entry).toMillis()), FileType.DIR.toString());
                } else {
                    desc = new FileDesc(entry.getFileName().toString(), Files.size(entry), new Date(Files.getLastModifiedTime(entry).toMillis()), FileType.File.toString());
                }
                descList.add(desc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    @RequestMapping(value = "/renameFile", produces = "application/json;charset=utf8")
    @ResponseBody
    public FileDescSingle renameFile(@RequestBody ReqParamList paramList) {
        ReqParam param = paramList.getParams();
        String pathStr = param.getPath();
        String newPathStr = param.getNewPath();
        //logger.info("path :" + pathStr);
        //logger.info("newPath: " + newPathStr);
        pathStr = pathStr.replace("\\", File.separator);
        newPathStr = newPathStr.replace("\\", File.separator);

        Path sourcePath = Paths.get(Constants.UPLOAD_FOLDER + pathStr);
        Path desPath = Paths.get(Constants.UPLOAD_FOLDER + newPathStr);

        FileDesc desc = null;
        // List<FileDesc> descList = new ArrayList<>();
        //FileDescList result = new FileDescList(descList);
        FileDescSingle fileDescSingle = new FileDescSingle();

        String errorMsg = "";
        if (Files.notExists(sourcePath)) {
            if (Files.isDirectory(sourcePath)) {
                errorMsg = "源文件夹";
            } else {
                errorMsg = "源文件";
            }
            desc = new FileDesc(false, errorMsg + "不存在");
            //descList.add(desc);
            fileDescSingle.setResult(desc);
            return fileDescSingle;
        }

        if (!Files.exists(sourcePath) && !Files.notExists(sourcePath)) {
            if (Files.isDirectory(sourcePath)) {
                errorMsg = "源文件夹";
            } else {
                errorMsg = "源文件";
            }
            desc = new FileDesc(false, errorMsg + "不可访问");
            //descList.add(desc);
            fileDescSingle.setResult(desc);
            return fileDescSingle;
        }

        // 貌似前台有检测了
        if (!Files.notExists(desPath)) {
            if (Files.isDirectory(sourcePath)) {
                errorMsg = "目标文件夹";
            } else {
                errorMsg = "目标文件";
            }
            desc = new FileDesc(false, errorMsg + "已存在");
            //descList.add(desc);
            fileDescSingle.setResult(desc);
            return fileDescSingle;
        }

        try {
            Files.move(sourcePath, desPath);
            desc = new FileDesc(true, "");
        } catch (IOException e) {
            e.printStackTrace();
            desc = new FileDesc(false, "重命名时出错");
        }
        fileDescSingle.setResult(desc);
        return fileDescSingle;
    }


    @RequestMapping(value = "/removeFile", produces = "application/json;charset=utf8")
    @ResponseBody
    public FileDescSingle removeFile(@RequestBody ReqParamList paramList) {
        ReqParam param = paramList.getParams();
        String pathStr = param.getPath();
        pathStr = pathStr.replace("\\", File.separator);
        Path sourcePath = Paths.get(Constants.UPLOAD_FOLDER + pathStr);
        FileDesc desc = null;
        FileDescSingle fileDescSingle = new FileDescSingle();
        String errorMsg = "";
        try {
            if (Files.isDirectory(sourcePath)) {
                FileUtils.deleteDirectory(sourcePath.toFile());
                errorMsg = "删除文件夹出错";
            } else {
                Files.delete(sourcePath);
                errorMsg = "删除文件出错";
            }
        } catch (IOException e) {
            e.printStackTrace();
            desc = new FileDesc(false, errorMsg);
            fileDescSingle.setResult(desc);
            return fileDescSingle;
        }

        desc = new FileDesc(true, "");
        fileDescSingle.setResult(desc);
        return fileDescSingle;
    }


    @RequestMapping(value = "/getContent", produces = "application/json;charset=utf8")
    @ResponseBody
    public FileDesc getFileContent(@RequestBody ReqParamList paramList) {
        ReqParam param = paramList.getParams();
        String pathStr = param.getPath();
        pathStr = pathStr.replace("\\", File.separator);
        Path sourcePath = Paths.get(Constants.UPLOAD_FOLDER + pathStr);
        FileDesc desc = null;
        String errorMsg = "";
        if (Files.isDirectory(sourcePath)) {
            desc = new FileDesc("打开的是文件夹");
            return desc;
        }

        try {
            if (Files.size(sourcePath) > 1024 * 1024 * 10) {
                desc = new FileDesc("文件大于10M");
                return desc;
            }
        } catch (IOException e) {
            e.printStackTrace();
            desc = new FileDesc("打开文件出错");
            return desc;
        }


        logger.info(pathStr);
        logger.info(FtUtils.getFileEncode(sourcePath.toFile()).toString());
        StringBuilder builder = new StringBuilder("");
        try (BufferedReader reader = Files.newBufferedReader(sourcePath, FtUtils.getFileEncode(sourcePath.toFile()))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
                logger.info(line);
            }
            desc = new FileDesc(builder.toString());
        } catch (IOException e) {
            desc = new FileDesc("打开文件出错");
            e.printStackTrace();
        }
        return desc;
    }


    @RequestMapping(value = "/createFolder", produces = "application/json;charset=utf8")
    @ResponseBody
    public FileDescSingle createFolder(@RequestBody ReqParamList paramList) {
        ReqParam param = paramList.getParams();
        String pathStr = param.getPath();
        pathStr = pathStr.replace("\\", File.separator);
        Path sourcePath = Paths.get(Constants.UPLOAD_FOLDER + File.separator + pathStr + File.separator + param.getName());
        logger.info(pathStr);
        FileDesc desc = null;
        FileDescSingle fileDescSingle = new FileDescSingle();
        try {
            Files.createDirectories(sourcePath);
        } catch (IOException e) {
            e.printStackTrace();

            desc = new FileDesc(false, "创建文件夹出错");
            fileDescSingle.setResult(desc);
            return fileDescSingle;
        }

        desc = new FileDesc(true, null);
        fileDescSingle.setResult(desc);
        return fileDescSingle;
    }

    @RequestMapping(value = "/saveFile", produces = "application/json;charset=utf8")
    @ResponseBody
    public FileDescSingle saveFile(@RequestBody ReqParamList paramList) {
        ReqParam param = paramList.getParams();
        String pathStr = param.getPath();
        pathStr = pathStr.replace("\\", File.separator);
        Path sourcePath = Paths.get(Constants.UPLOAD_FOLDER + pathStr);
        String content = param.getContent();
        FileDesc desc = null;
        FileDescSingle single = new FileDescSingle();
        String errorMsg = "";
        if (Files.isDirectory(sourcePath)) {
            desc = new FileDesc(false, "写入的是文件夹");
            single.setResult(desc);
            return single;
        }


        Charset charset = FtUtils.getFileEncode(sourcePath.toFile());
        try (BufferedWriter writer = Files.newBufferedWriter(sourcePath, charset)) {
            writer.write(content, 0, content.length());
            desc = new FileDesc(true, "");
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
            desc = new FileDesc(false, "写入文件出错");
        }
        /*byte[] data = content.getBytes();

        try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(sourcePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            out.write(data , 0, data.length);
            desc = new FileDesc(true, "");
        } catch (IOException x) {
            System.err.println(x);
            desc = new FileDesc(false, "写入文件出错");
        }*/
        single.setResult(desc);
        return single;
    }

    @RequestMapping(value = "/previewFile")
    @ResponseBody
    public void previewFile(ReqParam param, HttpServletRequest request, HttpServletResponse response) {

        String pathStr = param.getPath();
        /*try {
            pathStr = URLDecoder.decode(pathStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
        pathStr = pathStr.replace("\\", File.separator);
        Path sourcePath = Paths.get(Constants.UPLOAD_FOLDER + pathStr);

        logger.info(param.getPreview() + "");
        if (param.getPreview().equals("true")) {
            response.setContentType("image/gif");
            try (OutputStream out = response.getOutputStream();
                 FileInputStream fis = new FileInputStream(sourcePath.toFile());) {
                byte[] b = new byte[fis.available()];
                fis.read(b);
                out.write(b);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            /*String fileName = sourcePath.getName(sourcePath.getNameCount() - 1).toString();


            try (InputStream in = Files.newInputStream(sourcePath);
                 OutputStream out = response.getOutputStream();) {
                response.setHeader("Content-Type", "application/octet-stream");
                response.setHeader("content-disposition", "attachment;filename="
                        + URLEncoder.encode(fileName, "UTF-8"));
                response.setContentLength((int) Files.size(sourcePath));
                byte buffer[] = new byte[1024];
                int len = 0;

                // 循环将输入流中的内容读取到缓冲区当中
                while ((len = in.read(buffer)) > 0) {
                    // 输出缓冲区的内容到浏览器，实现文件下载
                    out.write(buffer, 0, len);
                    //out.flush();
                }
                response.flushBuffer();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            try {
                MultipartFileSender.fromPath(sourcePath)
                        .with(request)
                        .with(response)
                        .serveResource();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @RequestMapping(value = "/getServerUrl")
    @ResponseBody
    public List<String> getServerUrl(HttpServletRequest request) {
        List<String> urls = new ArrayList<>();
        String appUrl = request.getContextPath();
        logger.info(request.getLocalAddr());
        logger.info(request.getLocalPort() + "");
        //String port = request.g

        List<String> ips = FtUtils.getIps();
        for(String ip : ips) {
            urls.add("http://" + ip + ":" + request.getLocalPort() + appUrl );
        }
        /*for(String url : urls) {
            logger.info(url);
        }*/
        return urls;

        //logger.info(request.getRequestURI());
    }
}


