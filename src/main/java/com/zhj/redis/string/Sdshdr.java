package com.zhj.redis.string;

import lombok.Data;

/**
 * @program: zhj_redis_data_structure
 * @description: redis sds 实现类
 * @packagename: com.zhj.redis.string
 * @author: zhanghongjie
 * @date: 2022-08-07 17:42
 **/

public class Sdshdr {
    /**
     * 记录buf数组中已经使用的字节长度
     * 等于字符串的长度
     */
    private int len;

    /**
     * 记录buf数组中未使用的字节长度
     */
    private int free;

    char[] buf;


    public Sdshdr(){
        buf = new char[16];
        free = 16;
    }

    public Sdshdr(int len){
        buf = new char[len];
        free = len;
    }

    public Sdshdr(String str, int len)throws Exception {
        buf = new char[len];
        free = len;
        strcat(str);
    }


    public Sdshdr(char[] buf, int len)throws Exception {
        buf = new char[len];
        free = len;
        strcat(buf, len, 0);
    }







    /**
     * 创建一个sds副本，深拷贝
     * 时间复杂度：O(N), N是字符串长度
     * @return
     * @throws Exception
     */
    public Sdshdr sdshdrdup() throws Exception{
        Sdshdr sdshdr2 = new Sdshdr(buf, len);
        return sdshdr2;
    }

    /**
     * 清空sds
     */
    public void strclear(){
        buf = null;
    }

    /**
     * 释放N的buf长度，模拟释放内存
     * @param n
     */
    public void sdsfree(int n){
        if (n  >= len){
            strclear();
        }
        if (n >= free){
            n = free;
        }
        freeBuf(n);
    }

    private void freeBuf(int n) {
        char[] newBuf = new char[buf.length - n];
        for (int i = 0; i < len; i ++){
            newBuf[i] = buf[i];
        }
        newBuf[len] = '\0';
        free = free - n;
    }

    /**
     * 返回sds 使用的字符串长度
     * O(1)
     * @return
     */
    public int sdslen(){
        return this.len;
    }

    /**
     * 返回sds 未使用的字符串长度
     * O(1)
     * @return
     */
    public int sdsvail(){
        return this.free;
    }

    /**
     * 将给定字符串拼接到本sds 末尾
     * O(N)
     * @param str
     */
    public void strcat(String str) throws Exception {
        if (str == null || str.length() == 0){
            throw new MyException("put的字符串为null");
        }
        char[] arr = str.toCharArray();
        strcat(arr, str.length(), len - 1);
    }

    /**
     * 将给定的sds 拼接到本sds末尾
     * @param sdshdr2
     */
    public void sdscatsds(Sdshdr sdshdr2){
        if (sdshdr2 == null || sdshdr2.len == 0){
            return;
        }
        strcat(sdshdr2.buf, sdshdr2.sdslen(), len - 1);
    }

    /**
     * 将str复制到sds里面，覆盖原有字符串
     * O(N)
     * @param str
     * @throws Exception
     */
    public void sdscpy(String str) throws Exception{
        if (str == null || str.length() == 0){
            throw new MyException("put的字符串为null");
        }
        char[] arr = str.toCharArray();
        strcat(arr, str.length(), 0);
    }

    /**
     *
     */
    public void sdsgrowzero(int addLen){
        addLen(addLen);
    }


    /**
     * 保存sds 给定区间内的数据，不在区间内的会被清除
     * [startIndex, endIndex)
     * 不好实现O(N)，N是保留长度
     * @param
     */
    public void sdsrange(int startIndex, int endIndex){
        char[] clearArr = new char[startIndex - 0];
        strcat(clearArr, clearArr.length, 0);
        char[] clearArr2 = new char[len - endIndex];
        strcat(clearArr2, clearArr2.length, endIndex);
    }

    /**
     * 移除全部 的str
     * O(M*N)，M是str的长度
     * @param str
     */
    public void sdstrim(String str){
        if (str == null || str.length() == 0){
            return;
        }
        char[] arr = str.toCharArray();
        for (int i = 0;i < len; i ++){
            if (buf[i] != arr[0]) continue;
            if (len - i < arr.length) continue;
            boolean findFlag = true;
            for (int j = 1; j < len && j < arr.length;j ++){
                if (buf[i + j] == arr[j]) continue;
                else findFlag = false;
            }
            if (findFlag){
                for (int j = i;j < arr.length; j ++){
                    buf[j] = ' ';
                }
            }
        }
    }


    /**
     * 对比和sdshdr2 是否相同
     * @param sdshdr2
     */
    public boolean sdscmp(Sdshdr sdshdr2){
        if (sdshdr2.sdslen() != sdslen() || sdsvail() != sdshdr2.sdsvail()){
            return false;
        }
        for(int i = 0;i < len; i++){
            if (buf[i] != sdshdr2.buf[i]) return false;
        }
        return true;
    }


    private void strcat(char[] arr, int strLen, int startIndex){
        if (strLen < free){
            // 剩余空间不够时，对sds进行扩容
            addLen(strLen);
        }
        for (int i = 0; i < strLen; i++){
            buf[startIndex + i] = arr[i];
        }
        len = strLen;
        free = buf.length - len;
        buf[len] = '\0';
    }

    public void strTrimLast(String str){

    }

    public void strTrimFirst(String str){

    }

    public void strTrimAll(String str){

    }

    /**
     * redis 中空间预分配策略：
     * 1.如果len < 1024 * 1024,  未分配空间也会增加 所需的 长度
     * 2.如果len > 1024 * 1024, 未分配的空间会增加 1024 * 1024
     * @param strLen
     */
    private void addLen(int strLen) {
        int newBufLen = buf.length;
        if (len < (1024 * 1024)) {
            newBufLen =  2 * strLen + 1 ;
            free = strLen;

        }else if (len >= (1024 * 1024)){
            newBufLen = strLen + 1024 * 1024 + 1 ;
            free = (1024 * 1024);
        }
        char[] newBuf = new char[newBufLen];
        for (int i = 0; i < len; i++){
            newBuf[i] = buf[i];
        }
        buf = newBuf;
    }

}
