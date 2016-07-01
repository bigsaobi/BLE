package com.qing.blemanager.ble.utils;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by liuqing on 16/6/11.
 */
public class BLETools {
    //获取权限
    public static String getProperties(BluetoothGattCharacteristic characteristic) {
        String result = "";
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
            result = result + ",WRITE";
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
            result = result + ",WRITE_NO_RESPONSE";
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) != 0) {
            result = result + ",SIGNED_WRITE";
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            result = result + ",NOTIFY";
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
            result = result + ",READ";
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_BROADCAST) != 0) {
            result = result + ",BROADCAST";
        }

        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
            result = result + ",INDICATE";
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) != 0) {
            result = result + ",EXTENDED_PROPS";
        }
        if (!TextUtils.isEmpty(result) && result.startsWith(",")) {
            result = result.substring(1, result.length());
        }
        return result;
    }


    public static String getExtra(BluetoothGattCharacteristic characteristic) {
        String result = "";
        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
        for (BluetoothGattDescriptor descriptor : descriptors) {
            result = result + "\n UUID:" + descriptor.getUuid() + "\n value:" + getExtraDescriptorValue(descriptor.getValue());
        }
        return result;
    }

    private static String getExtraDescriptorValue(byte[] value) {
        if (value == null) {
            return "";
        }
        if (value == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
            return "ENABLE_NOTIFICATION";
        } else if (value == BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE) {
            return "DISABLE_NOTIFICATION";
        } else if (value == BluetoothGattDescriptor.ENABLE_INDICATION_VALUE) {
            return "ENABLE_INDICATION";
        }
        return "";
    }


    public static String getWriteType(final int type) {
        switch (type) {
            case BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT:
                return "WRITE REQUEST";
            case BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE:
                return "WRITE COMMAND";
            case BluetoothGattCharacteristic.WRITE_TYPE_SIGNED:
                return "WRITE SIGNED";
            default:
                return "UNKNOWN: " + type;
        }
    }


    /**
     * 将16进制的字符串转换为字节数组
     *
     * @param message
     * @return 字节数组
     */
    public static byte[] getHexBytes(String message) throws java.lang.NumberFormatException {
        int len = message.length() / 2;
        char[] chars = message.toCharArray();
        String[] hexStr = new String[len];
        byte[] bytes = new byte[len];
        for (int i = 0, j = 0; j < len; i += 2, j++) {
            hexStr[j] = "" + chars[i] + chars[i + 1];
            bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
        }
        return bytes;
    }


    // byte转十六进制字符串
    public static String bytes2HexString(byte[] bytes) {
        String ret = "";
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase(Locale.CHINA);
        }
        return ret;
    }

    public static String getCheckSumNum(String... args) {
        int sum = 0;
        for (String itemString : args) {
            sum += Integer.parseInt(makeChecksum(itemString), 16);
        }
//        int result = 256 - sum;
        return Integer.toHexString((256 - sum)).toUpperCase();
    }

    public static String makeChecksum(String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }
        int total = 0;
        int len = data.length();
        int num = 0;
        while (num < len) {
            String s = data.substring(num, num + 2);
//            System.out.println(s);
            total += Integer.parseInt(s, 16);
            num = num + 2;
        }
        /**
         * 用256求余最大是255，即16进制的FF
         */
        int mod = total % 256;
        String hex = Integer.toHexString(mod);
        len = hex.length();
        //如果不够校验位的长度，补0,这里用的是两位校验
        if (len < 2) {
            hex = "0" + hex;
        }
        return hex;
    }


    public static byte[] getFileContent(String filePath) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();
        Log.d("ss", "getFileContent: fileSize:" + fileSize);
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("file too big...");
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        // 确保所有数据均被读取
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file "
                    + file.getName());
        }
        fi.close();
        return buffer;
    }

    //小端  将低位存储在低位 支持 1，2，4长度的byte
    public static byte[] intToByte(int i, int len) {
        byte[] abyte = null;
        if (len == 1) {
            abyte = new byte[len];
            abyte[0] = (byte) (0xff & i);
        } else if (len == 2) {
            abyte = new byte[len];
            abyte[0] = (byte) (0xff & i);
            abyte[1] = (byte) ((0xff00 & i) >> 8);
        } else {
            abyte = new byte[len];
            abyte[0] = (byte) (0xff & i);
            abyte[1] = (byte) ((0xff00 & i) >> 8);
            abyte[2] = (byte) ((0xff0000 & i) >> 16);
            abyte[3] = (byte) ((0xff000000 & i) >> 24);
        }
        return abyte;
    }

    public static int bytesToInt(byte[] bytes) {
        int addr = 0;
        if (bytes.length == 1) {
            addr = bytes[0] & 0xFF;
        } else if (bytes.length == 2) {
            addr = bytes[0] & 0xFF;
            addr |= ((bytes[1] << 8) & 0xFF00);
        } else if (bytes.length == 4) {
            addr = bytes[0] & 0xFF;
            addr |= ((bytes[1] << 8) & 0xFF00);
            addr |= ((bytes[2] << 16) & 0xFF0000);
            addr |= ((bytes[3] << 24) & 0xFF000000);
        }
        return addr;
    }

    /**
     * 字符转ASC
     *
     * @param st
     * @return
     */
    public static int getAsc(String st) {
        byte[] gc = st.getBytes();
        int ascNum = (int) gc[0];
        return ascNum;
    }

    /**
     * ASC转字符
     *
     * @param backnum
     * @return
     */
    public static char backchar(int backnum) {
        char strChar = (char) backnum;
        return strChar;
    }


}
