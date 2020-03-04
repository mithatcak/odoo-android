package com.app.example.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Administrator on 8/2/13.
 */
public class LibDebugManage {

    public static void  DeleteLog() {
        File file = new File("mnt/sdcard/sz_oem_log.txt");
        file.delete();
    }
    
    public static void WriteLog(String strLog)
    {
        String logPath = "log\\liblog.txt";

        File templog = new File(logPath);

        try {

            if(!templog.exists())
                templog.createNewFile();

            RandomAccessFile raf = new RandomAccessFile(logPath, "rw");
            raf.seek(raf.length());

            raf.writeBytes(strLog);
            raf.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void  WriteLog2(String str) {
        String str_Path_Full = "mnt/sdcard/sz_oem_log.txt";
        File file = new File(str_Path_Full);
        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
            }
        } else {
            try {
                BufferedWriter bfw = new BufferedWriter(new FileWriter(str_Path_Full,true));
                bfw.write(str);
                bfw.write("\r\n");
                bfw.flush();
                bfw.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }
        }
    }
    
    public static void  WriteBuffer(byte[] p_pBuf, int p_nLen) {
        String str_Path_Full = "mnt/sdcard/sz_oem_log.txt";
        File file = new File(str_Path_Full);
        int i;
        
        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
            }
        } else {
            try {
                BufferedWriter bfw = new BufferedWriter(new FileWriter(str_Path_Full,true));
                for (i = 0; i < p_nLen; i++)
                {
                	bfw.write(String.format("%02X ", p_pBuf[i]));
                }
                bfw.write("\n");
                bfw.flush();
                bfw.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }
        }
    }
    
    public static void WriteBmp(byte[] image, int nlen)
    {
        String logPath = "mnt/sdcard/log/libfp.bmp";

        File templog = new File(logPath);

        try {

            if(!templog.exists())
                templog.createNewFile();

            RandomAccessFile raf = new RandomAccessFile(logPath, "rw");
            raf.seek(raf.length());

            raf.write(image, 0, nlen);
            raf.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
}
