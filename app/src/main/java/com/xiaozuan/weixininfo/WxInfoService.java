package com.xiaozuan.weixininfo;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WxInfoService extends AccessibilityService {

    File from;
    File file;

    FileInputStream fr;
    InputStreamReader isr;
    BufferedReader br;
    FileWriter fw;

    String gender;
    String nick;
    String address;
    int index = 0;

    String desc;

    private List<String> phoneList;

    @Override
    public void onCreate() {
        super.onCreate();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        from = new File(path, "phone.txt");
        file = new File(path, "info.txt");
        try {
            if(!file.exists()) {
                System.out.println("File not extst");
                file.createNewFile();
            }
            fw = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        phoneList = new ArrayList<>();
        queryContactPhoneNumber();
    }

    /**
     * 获取手机通讯录手机号
     */
    private void queryContactPhoneNumber() {
        phoneList.clear();
        String[] cols = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                cols, null, null, null);
        if (cursor != null){
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                // 取得联系人名字
                int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                int numberFieldColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String name = cursor.getString(nameFieldColumnIndex);
                String number = cursor.getString(numberFieldColumnIndex);
                number = number.replace(" ", "");
                phoneList.add(number);
            }
            cursor.close();
        }

    }

    /**
     * 从文件获取手机号
     */
    private void getPhoneFromFile(){
        phoneList.clear();
        try {
            if(!from.exists()) {
                System.out.println("File not extst");
                from.createNewFile();
            }
            String phone;
            fr = new FileInputStream(from);
            isr = new InputStreamReader(fr);
            br = new BufferedReader(isr);

//            fw = new FileWriter(file);

//                char buffer[] = new char[(int) from.length()];
            while((phone = br.readLine()) != null) {
                phoneList.add(phone);
            }
            fr.close();
            br.close();
            isr.close();
//            fw.flush();
//            fw.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        //得到事件的包名。如果注册了多个应用的事件，可以在此做一个判断。
        String packageName = accessibilityEvent.getPackageName().toString();
        String className = accessibilityEvent.getClassName().toString();

        //得到对应的事件类型，这里有很多很多种的事件类型，具体可以自行翻阅AccessibilityEvent类中的定义。
        int eventType = accessibilityEvent.getEventType();

        Log.i("微信辅助", packageName + "," + className + "," + eventType + ", " + accessibilityEvent.getText().toString() + ", " + index);

        //得到根的view节点。可以当做当前acitivity的视图看成是树状结构的（实际上也是~。~），而我们现在就得到了它的根节点。
        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null)
            return;
        if (index > phoneList.size() - 1){
            return;
        }

        if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
            List<AccessibilityNodeInfo> nodeMany = root.findAccessibilityNodeInfosByText("频繁");
            if (nodeMany != null && nodeMany.size() > 0){
                for (AccessibilityNodeInfo info : nodeMany) {
                    info.recycle();
                }
                return;
            }
            List<AccessibilityNodeInfo> nodeEmpty = root.findAccessibilityNodeInfosByText("不存在");
            List<AccessibilityNodeInfo> nodeException = root.findAccessibilityNodeInfosByText("异常");
            if (nodeEmpty != null && nodeEmpty.size() > 0){
                desc = nodeEmpty.get(0).getText().toString();
            }else if (nodeException != null && nodeException.size() > 0){
                desc = nodeException.get(0).getText().toString();
            }
            if ((nodeEmpty != null && nodeEmpty.size() > 0) || (nodeException != null && nodeException.size() > 0)){
                try {
                    fw.append(++index + "、手机号：" + phoneList.get(index - 1) + "  " + desc + "\r\n");
                    fw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                inputNumberAndSearch(root, eventType);
            List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ht");
            if (infos != null && infos.size() > 0){
                for (AccessibilityNodeInfo info : infos){
                    if ("android.widget.EditText".equals(info.getClassName().toString())){
                        setText(info, "");
//                        info.performAction(AccessibilityNodeInfo.ACTION_CUT);
                    }
                }
            }
            return;
            }
        }

        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null)
                return;
            List<AccessibilityNodeInfo> nodeInfoDetail = rootNode.findAccessibilityNodeInfosByText("详细资料");

            if (nodeInfoDetail != null && nodeInfoDetail.size() > 0){

//                //微信未开通
//                List<AccessibilityNodeInfo> nodeNotOpen = rootNode.findAccessibilityNodeInfosByText("未开通");
//                if (nodeNotOpen != null && nodeNotOpen.size() > 0){
////                    desc = nodeNotOpen.get(0).getText().toString();
//                    desc = "还未开通微信";
//                    try {
//                        fw.append(++index + "、手机号：" + phoneList.get(index - 1) + "  " + desc + "\r\n");
//                        fw.flush();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
////                    try {
////                        Thread.sleep(2000);
////                    } catch (InterruptedException e) {
////                        e.printStackTrace();
////                    }
////                    performGlobalAction(GLOBAL_ACTION_BACK);
//                }else {
                    //获取信息
                    List<AccessibilityNodeInfo> nodeName = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/pm");
                    if (nodeName != null && nodeName.size() > 0){
                        nick = nodeName.get(0).getText().toString();
                    }
                    List<AccessibilityNodeInfo> nodeGender = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ann");
                    if (nodeGender != null && nodeGender.size() > 0){
                        gender = nodeGender.get(0).getContentDescription().toString();
                    }
                    List<AccessibilityNodeInfo> nodeArea = root.findAccessibilityNodeInfosByViewId("android:id/summary");
                    if (nodeArea != null && nodeArea.size() > 0){
                        address = nodeArea.get(0).getText().toString();
                    }

                    Log.i("微信辅助", nick + "<--详细资料");

                    try {
                        fw.append(++index + "、手机号：" + phoneList.get(index - 1) + "  昵称：" + nick + ", 性别：" + gender + ", 地区：" + address + "\r\n");
                        fw.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                performGlobalAction(GLOBAL_ACTION_BACK);
                return;
            }
        }
        inputNumberAndSearch(root, eventType);
    }

    private void setText(AccessibilityNodeInfo editText, String text){
        Bundle arguments = new Bundle();
        arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
        arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                true);
        editText.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                arguments);
        editText.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        ClipData clip = ClipData.newPlainText("label", text);
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(clip);
        editText.performAction(AccessibilityNodeInfo.ACTION_PASTE);
    }

    private boolean inputNumberAndSearch(AccessibilityNodeInfo root, int eventType){
        boolean flag = false;
        List<AccessibilityNodeInfo> infosByViewId = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ht");
        if (infosByViewId != null && infosByViewId.size() > 0 && root.findAccessibilityNodeInfosByText("朋友圈").size() == 0
                && root.findAccessibilityNodeInfosByText("登录").size() == 0){

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
//            List<AccessibilityNodeInfo> nodeInfos = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ht");
            AccessibilityNodeInfo info = infosByViewId.get(0);
            if (!phoneList.get(index).equals(info.getText())){
                setText(info, phoneList.get(index));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                AccessibilityNodeInfo rootNode2 = getRootInActiveWindow();
                List<AccessibilityNodeInfo> nodeInfos2 = rootNode2.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/in");
//                List<AccessibilityNodeInfo> nodeInfos2 = rootNode2.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/qm");
                if (nodeInfos2 != null && nodeInfos2.size() > 0){
                    Log.i("微信辅助", nodeInfos2.size() + "size");
                    AccessibilityNodeInfo nodeInfo = nodeInfos2.get(0);
//                    for (AccessibilityNodeInfo nodeInfo : nodeInfos2){
                        if (nodeInfo != null && "android.widget.TextView".equals(nodeInfo.getClassName().toString()) && eventType != AccessibilityEvent.TYPE_VIEW_CLICKED){
                            nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Log.i("微信辅助", nodeInfo.getText().toString());
                            flag = true;
                        }
//                    }

                }
            }
        }
        return flag;
    }

    @Override
    public void onInterrupt() {

    }

}
