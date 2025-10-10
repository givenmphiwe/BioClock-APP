package polysphere.dms.com.dtts;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/1/3.
 */

public class FileOperate {

    public static boolean addData(byte[] data) {
        File dir = new File(Environment.getExternalStorageDirectory(), "FingerModelE");
        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(dir, System.currentTimeMillis() + "");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static byte[] getData(File file) {
        if (file != null && file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fileInputStream.read(data, 0, (int) file.length());
                fileInputStream.close();
                return data;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<byte[]> getAllData() {
        File dir = new File(Environment.getExternalStorageDirectory(), "FingerModelE");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File[] list = dir.listFiles();
        List<byte[]> dataList = new ArrayList<>();
        if (list != null && list.length > 0) {
            for (File f : list) {
                dataList.add(getData(f));
            }
            return dataList;
        }
        return null;
    }

}
