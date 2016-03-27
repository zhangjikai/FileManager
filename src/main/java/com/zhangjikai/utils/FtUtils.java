package com.zhangjikai.utils;

import info.monitorenter.cpdetector.io.*;

import java.io.File;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangjk on 2016/1/19.
 */
public class FtUtils {


    /**
     * 使用cpdetector检测文件编码
     *
     * @param file
     * @return
     */
    public static Charset getFileEncode(File file) {
        try {
            CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
            detector.add(new ParsingDetector(false));
            detector.add(JChardetFacade.getInstance());
            detector.add(ASCIIDetector.getInstance());
            detector.add(UnicodeDetector.getInstance());
            java.nio.charset.Charset charset = null;
            charset = detector.detectCodepage(file.toURI().toURL());
            return charset;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<String> getIps() {
        List<String> ipList = new ArrayList<>();
        try {
            InetAddress ip = InetAddress.getLocalHost();
            InetAddress[] addrs = InetAddress.getAllByName(ip.getHostName());

            for (InetAddress address : addrs) {
                if(address instanceof Inet4Address) {
                    ipList.add(address.getHostAddress());
                }
            }
        } catch (UnknownHostException e) {
            ipList.add("127.0.0.1");
            e.printStackTrace();
        }

        for(String s : ipList) {
            System.out.println(s);
        }
        return ipList;
    }

    public static void main(String[] args) {
        getIps();
    }


}

