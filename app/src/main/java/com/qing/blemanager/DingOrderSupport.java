package com.qing.blemanager;

import android.util.Log;

import com.qing.blemanager.ble.utils.BLETools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * Created by liuqing on 16/5/30.
 */
public class DingOrderSupport {
    private static final String TAG = DingOrderSupport.class.getSimpleName();
    public static UUID DingOrderServiceUUID = UUID.fromString("dba30001-b4ee-3a90-714e-7d0233123998");
    public static UUID DingOrderService_write_UUID = UUID.fromString("dba30002-b4ee-3a90-714e-7d0233123998");
    public static UUID DingOrderService_read_UUID = UUID.fromString("dba30003-b4ee-3a90-714e-7d0233123998");

    /**
     * 命令号
     */
    public static String ORDER_TYPE_BOXINFO = "01";//收到药盒广播
    public static String ORDER_TYPE_BOXTUNK = "02";//药盒敲击确认
    public static String ORDER_TYPE_BINDBOX = "03";//绑定药盒
    public static String ORDER_TYPE_UNBINDBOX = "04";//解绑药盒
    public static String ORDER_TYPE_SYNCMEDICINEPLAN = "05";//同步服药计划
    public static String ORDER_TYPE_BOXREMIND = "06";//药盒提醒用户
    public static String ORDER_TYPE_QUERYRECORD = "07";//向药盒查询服药记录
    public static String ORDER_TYPE_SYNCMEDICINEPLANSUCCESS = "08";//通知药盒服药记录同步成功
    public static String ORDER_TYPE_CHECKBOXBATTERYSTATUS = "09";//查询药盒电池状态
    public static String ORDER_TYPE_BOXSETTING = "0A";//药盒设置
    public static String ORDER_TYPE_AUDIOFILETRANSFER = "0B";//传输语音文件
    public static String ORDER_TYPE_CHECKBOXVERSION = "0C";//查询固件版本
    public static String ORDER_TYPE_FINDBOX = "0E";//查找药盒


    public static Order getBoxInfoOrder() {
        return new Order(Order.TYPE_WRITE, ORDER_TYPE_BOXINFO, Arrays.asList(new byte[][]{BLETools.getHexBytes("11010114")}), "获取药盒信息");
    }

    public static Order getQueryMedPlanOrder() {
        return new Order(Order.TYPE_WRITE, ORDER_TYPE_QUERYRECORD, Arrays.asList(new byte[][]{BLETools.getHexBytes("110711AA")}), "同步记录");
    }

    public static Order getClearMedRecordOrder() {
        return new Order(Order.TYPE_WRITE, "", Arrays.asList(new byte[][]{BLETools.getHexBytes("110801AA")}), "删除已同步的记录");
    }

    public static Order getClearBoxTokenOrder(){
        return new Order(Order.TYPE_WRITE,"", Arrays.asList(new byte[][]{BLETools.getHexBytes("110D01C0")}),"清除药盒token");
    }

    public static Order getBoxTokenOrder(){
        return new Order(Order.TYPE_WRITE,"", Arrays.asList(new byte[][]{BLETools.getHexBytes("110D02C0")}),"查看药盒token");
    }


    /**
     * @param token        用户token 必须8位
     * @param dingBoxToken 药盒token 必须8位
     * @param time         时间戳（秒级别）
     */
    public static Order getBindOrder(String token, String dingBoxToken, int time) {
        String arg1 = str2Data(token);
        String arg2 = str2Data(dingBoxToken);
        String arg3 = toData(time, 4);
        return new Order(Order.TYPE_WRITE, ORDER_TYPE_BINDBOX, arg1 + arg2 + arg3, "绑定药盒");
    }



    /**
     * @param dingBoxToken 药盒token 必须8位
     */
    public static Order getUnBindDeviceOrder(String dingBoxToken) {
        String arg1 = str2Data(dingBoxToken);
        return new Order(Order.TYPE_WRITE, ORDER_TYPE_UNBINDBOX, arg1, "解绑药盒");
    }



    //返回数据转换  asc码转回
    public static String data2Str(String str){
        int size = str.length()/2;
        String result = "";
        String itemString = "";
        for (int i = 0; i < size; i++) {
            itemString = str.substring((i*2),(i*2+2));
            result += String.valueOf(BLETools.backchar(fromData(itemString)));
        }
        return result;
    }
    //混杂数字文字数据转换为命令数据
    public static String str2Data(String str) {
        String result = "";
        char[] chars = str.toCharArray();
        for (char c : chars) {
            String itemString = String.valueOf(c);
            result += toData(BLETools.getAsc(itemString), 1);
        }
        return result;
    }

    //数据转换命令并做大小端转换
    public static String toData(int data, int length) {
        return BLETools.bytes2HexString(BLETools.intToByte(data, length));
    }
    //返回数据转换为正常数据
    public static int fromData(String data) {
        return BLETools.bytesToInt(BLETools.getHexBytes(data));
    }



    public static class Order {
        private static final String TAG = Order.class.getSimpleName();
        public static int ORDER_LONG_SIZE = 15 * 2;//长命令每个数据包数据长度
        public static int ORDER_SHORT_SIZE = 17 * 2;//短命令只有一个数据包，数据长度
        public static String ORDER_LONG_HEADTYPE1 = "22";//长命令命令头 第一个数据包
        public static String ORDER_LONG_HEADTYPE2 = "12";//长命令命令头 第二个以及以后数据包
        public static String ORDER_SHORT_HEAD = "11";//短命令命令头


        public static final int TYPE_READ = 1;
        public static final int TYPE_WRITE = 2;

        int type;
        String orderType;
        String data;
        List<byte[]> values;
        String orderName;

        public Order(int type, String orderType, String data, String orderName) {
            this.type = type;
            this.orderName = orderName;
            this.orderType = orderType;
            if (data.length() > ORDER_SHORT_SIZE) {
                values = longOrderTransform(data);
            } else {
                values = shortOrderTransform(data);
            }
        }

        public Order(int type, String orderType, List<byte[]> data, String orderName) {
            this.type = type;
            this.orderName = orderName;
            this.orderType = orderType;
            this.values = data;
        }

        public int getType() {
            return type;
        }

        public List<byte[]> getValues() {
            return values;
        }

        public String getOrderName() {
            return orderName;
        }


        private List<byte[]> longOrderTransform(String data) {
            List<byte[]> orderPkgs = new ArrayList<byte[]>();
            int count = data.length();
            int size = count / ORDER_LONG_SIZE;
            List<String> orderStrings = new ArrayList<String>();
            int start;
            int end = 0;
            for (int i = 0; i < size; i++) {
                start = i * ORDER_LONG_SIZE;
                end = start + ORDER_LONG_SIZE;
                orderStrings.add(data.substring(start, end));
            }
            orderStrings.add(data.substring(end, count));
            int count1 = orderStrings.size();
            for (int i = 0; i < count1; i++) {
//                String result = "";
                String arg1 = "";
                String arg2 = "";
                String arg3 = "";
                String arg4 = "";
                String arg5 = "";
                if (i == 0) {
                    arg1 = ORDER_LONG_HEADTYPE1;
                } else {
                    arg1 = ORDER_LONG_HEADTYPE2;
                }
                arg2 = orderType;
                int index = count1 - 1 - i;
                arg3 = toData(index, 2);
                arg4 = orderStrings.get(i);
                arg5 = BLETools.getCheckSumNum(arg1, arg2, arg3, arg4);
                String result = arg1 + arg2 + arg3 + arg4 + arg5;
                Log.d(TAG, "longOrderTransform: " + result);
                orderPkgs.add(BLETools.getHexBytes(result));
            }
            return orderPkgs;
        }

        public List<byte[]> shortOrderTransform(String data) {
            List<byte[]> orderPkgs = new ArrayList<byte[]>();
            String arg1 = ORDER_SHORT_HEAD;
            String arg2 = orderType;
            String arg3 = data;
            String result = arg1 + arg2 + data;
            result += BLETools.getCheckSumNum(arg1,arg2,arg3);
            orderPkgs.add(BLETools.getHexBytes(result));
            return orderPkgs;
        }
    }
}
