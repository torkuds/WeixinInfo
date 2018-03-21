package com.xiaozuan.weixininfo;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AccessbilityService extends AccessibilityService {

    File from = new File("/sdcard","phone.txt");
    File file = new File("/sdcard","info.txt");

    FileInputStream fr;
    InputStreamReader isr;
    BufferedReader br;
    FileWriter fw;

    String gender;
    String nick;
    String address;
    int index = 0;

    private List<String> phoneList;

    @Override
    public void onCreate() {
        super.onCreate();
        phoneList = new ArrayList<>();
        try {
            if(!from.exists()) {
                System.out.println("File not extst");
                from.createNewFile();
            }
            String phone;
            fr = new FileInputStream(from);
            isr = new InputStreamReader(fr);
            br = new BufferedReader(isr);

            fw = new FileWriter(file);

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

//        AccessibilityNodeInfo root = getRootInActiveWindow();
        List<AccessibilityNodeInfo> nodeInfoDetail = root.findAccessibilityNodeInfosByText("详细资料");
        if (nodeInfoDetail != null && nodeInfoDetail.size() > 0){
            List<AccessibilityNodeInfo> nodeName = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/pm");
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

            try {
                fw.append(++index + "、" + "昵称：" + nick + ", 性别：" + gender + ", 地区：" + address + "\r\n");
                fw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            performGlobalAction(GLOBAL_ACTION_BACK);
            return;
        }

        List<AccessibilityNodeInfo> infosByViewId = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ht");
        if (infosByViewId != null && infosByViewId.size() > 0 && root.findAccessibilityNodeInfosByText("朋友圈").size() == 0
                && root.findAccessibilityNodeInfosByText("登录").size() == 0){

//            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
//            List<AccessibilityNodeInfo> nodeInfos = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ht");
            AccessibilityNodeInfo info = infosByViewId.get(0);
            if (!phoneList.get(index).equals(info.getText())){
                Bundle arguments = new Bundle();
                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                        AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
                arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                        true);
                info.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                        arguments);
                info.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                ClipData clip = ClipData.newPlainText("label", phoneList.get(index));
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clip);
                info.performAction(AccessibilityNodeInfo.ACTION_PASTE);

                AccessibilityNodeInfo rootNode2 = getRootInActiveWindow();
                List<AccessibilityNodeInfo> nodeInfos2 = rootNode2.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/in");
                if (nodeInfos2 != null && nodeInfos2.size() > 0){
                    AccessibilityNodeInfo nodeInfo = nodeInfos2.get(0);
                    if (nodeInfo != null && "android.widget.TextView".equals(nodeInfo.getClassName().toString()) && eventType != AccessibilityEvent.TYPE_VIEW_CLICKED){
                        nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.i("微信辅助", nodeInfo.getText().toString());


                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }
}
