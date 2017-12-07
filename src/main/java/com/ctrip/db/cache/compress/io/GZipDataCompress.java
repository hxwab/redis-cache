package com.ctrip.db.cache.compress.io;


import com.ctrip.db.cache.compress.DataPersistCompress;
import com.ctrip.db.cache.util.JSONUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * 采用GZip进行数据的压缩和解压缩
 * Created by zhao.yong on 2017/11/8.
 */
public class GZipDataCompress implements DataPersistCompress {
    private static final int BUFFER_SIZE = 4*1024;

    @Override
    public byte[] compress(Object object) {
        Deflater deflater = new Deflater();
        deflater.setLevel(Deflater.BEST_COMPRESSION);
        byte[] data = JSONUtil.toJSONBytes(object);
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try {
            byte[] buffer = new byte[4*1024];
            while (!deflater.finished()){
                int count = deflater.deflate(buffer);
                bos.write(buffer,0,count);
            }
            return bos.toByteArray();
        }catch (Exception e){

        }finally {
            if(bos != null){
                try {
                    bos.close();
                } catch (IOException e) {

                }
            }
        }
        return null;
    }

    @Override
    public <T> T uncompress(byte[] data,Class<T> targetClass) {
        Inflater deflater = new Inflater();
        deflater.setInput(data);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (!deflater.finished()){
                int count = deflater.inflate(buffer);
                bos.write(buffer,0,count);
            }
            byte[] serialData = bos.toByteArray();
            if(targetClass.isAssignableFrom(String.class)){
                return (T) new String(serialData);
            }
        }catch (Exception e){

        }finally {
            if(bos != null){
                try {
                    bos.close();
                } catch (IOException e) {

                }
            }
        }
        return null;
    }
}
