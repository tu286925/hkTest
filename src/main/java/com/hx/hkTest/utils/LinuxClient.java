package com.hx.hkTest.utils;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Slf4j
public class LinuxClient {
    private static HCNetSDK hCNetSDK = null;
    private static boolean initSuc = false;

    String PATH_WIN = "HCNetSDK";
    String PATH_LINUX = File.separator + "opt" + File.separator + "hcnet" + File.separator + "libhcnetsdk.so";

    public LinuxClient() {

        if (Platform.isWindows()) {
            hCNetSDK = (HCNetSDK) Native.loadLibrary("HCNetSDK", HCNetSDK.class);
        }
        if (Platform.isLinux()) {
            hCNetSDK =(HCNetSDK) Native.loadLibrary("hcnetsdk", HCNetSDK.class);
//            hCNetSDK = (HCNetSDK) Native.loadLibrary(PATH_LINUX, HCNetSDK.class);
        }
    }

    //
    public void CameraInit() {
        if (!initSuc) {
            initSuc = hCNetSDK.NET_DVR_Init();
            hCNetSDK.NET_DVR_SetLogToFile(true, null, false);
            if (!initSuc) {
                System.out.println("初始化失败");
            } else {
                System.out.println("初始化成功");
            }
        }

    }

    //注册
    public NativeLong register(String username, String password, String deviceIP) {
        //注册之前先注销已注册的用户,预览情况下不可注销
//        if (lUserID.longValue() > -1) {
//            //先注销
//            hCNetSDK.NET_DVR_Logout(lUserID);
//            lUserID = new NativeLong(-1);
//        }
        HCNetSDK.NET_DVR_USER_LOGIN_INFO struLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
        HCNetSDK.NET_DVR_DEVICEINFO_V40 struDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();
        Pointer PointerstruDeviceInfoV40 = struDeviceInfo.getPointer();
        Pointer PointerstruLoginInfo = struLoginInfo.getPointer();
        for(int i=0;i<deviceIP.length();i++) {
            struLoginInfo.sDeviceAddress[i]= (byte)deviceIP.charAt(i);
        }
        for(int i=0;i<password.length();i++) {
            struLoginInfo.sPassword[i] = (byte)password.charAt(i);
        }
        for(int i=0;i<username.length();i++) {
            struLoginInfo.sUserName[i] = (byte)username.charAt(i);
        }
        struLoginInfo.wPort = 8000;
        struLoginInfo.write();
        System.out.println("NET_DVR_Login_V40 before");
        NativeLong nlUserId = hCNetSDK.NET_DVR_Login_V40(PointerstruLoginInfo, PointerstruDeviceInfoV40);
        System.out.println("NET_DVR_Login_V40 after");

        long userID = nlUserId.longValue();
        if (userID == -1) {
            System.out.println("注册失败,code:"+ hCNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("注册成功,lUserID:" + userID + ",code:" + hCNetSDK.NET_DVR_GetLastError());
            struDeviceInfo.read();
            getDeviceInfo(nlUserId, struDeviceInfo);
        }
        return nlUserId;
    }

    public void getDeviceInfo(NativeLong lUserID, HCNetSDK.NET_DVR_DEVICEINFO_V40 strInfo) {
        System.out.println("CreateDeviceTree before");
        HCNetSDK.NET_DVR_DEVICEINFO_V30 strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        strDeviceInfo = strInfo.struDeviceV30;
        log.info("222byChanNum" + strDeviceInfo.byChanNum + ",byIPChanNum:" + strDeviceInfo.byIPChanNum);

        HCNetSDK.NET_DVR_IPPARACFG strIpparaCfg = new HCNetSDK.NET_DVR_IPPARACFG();
        log.info("NET_DVR_GET_IPPARACFG before1,size:" + strIpparaCfg.size());
        IntByReference ibrBytesReturned = new IntByReference(0);
//        strIpparaCfg.write();
        Pointer lpIpParaConfig = strIpparaCfg.getPointer();
        log.info("NET_DVR_GET_IPPARACFG before2,size:" + strIpparaCfg.size());
        boolean bRet = hCNetSDK.NET_DVR_GetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_IPPARACFG,
                new NativeLong(0), lpIpParaConfig, strIpparaCfg.size(), ibrBytesReturned);
        strIpparaCfg.read();
        if (bRet) {
            log.info("byStartChan" + strDeviceInfo.byStartChan + ",byIPChanNum:" + strDeviceInfo.byIPChanNum);
        } else {
            log.info("获取失败,错误码:" + hCNetSDK.NET_DVR_GetLastError());
        }

    }

    public void getFile(NativeLong lUserID, int channel, LocalDateTime startTime, LocalDateTime stopTime) {
        HCNetSDK.NET_DVR_FILECOND_V40 strFilecond=new HCNetSDK.NET_DVR_FILECOND_V40();

        HCNetSDK.NET_DVR_TIME struStartTime = new HCNetSDK.NET_DVR_TIME();
        HCNetSDK.NET_DVR_TIME struStopTime = new HCNetSDK.NET_DVR_TIME();
        // 开始时间
        struStartTime.dwYear = startTime.getYear();
        struStartTime.dwMonth = startTime.getMonth().getValue();
        struStartTime.dwDay = startTime.getDayOfMonth();
        struStartTime.dwHour = startTime.getHour();
        struStartTime.dwMinute = startTime.getMinute();
        struStartTime.dwSecond = startTime.getSecond();
        // 结束时间
        struStopTime.dwYear = stopTime.getYear();
        struStopTime.dwMonth = stopTime.getMonth().getValue();
        struStopTime.dwDay = stopTime.getDayOfMonth();
        struStopTime.dwHour = stopTime.getHour();
        struStopTime.dwMinute = stopTime.getMinute();
        struStopTime.dwSecond = stopTime.getSecond();

        strFilecond.struStartTime = struStartTime;
        strFilecond.struStopTime = struStopTime;
        strFilecond.lChannel = new NativeLong(channel);
        strFilecond.dwFileType = 0xff;
        strFilecond.dwIsLocked = 0xff;
        strFilecond.dwUseCardNo = 0;

        NativeLong lFindFile40=hCNetSDK.NET_DVR_FindFile_V40(lUserID, strFilecond);
        HCNetSDK.NET_DVR_FINDDATA_V40 strFile=new HCNetSDK.NET_DVR_FINDDATA_V40();
        NativeLong lNext;

        System.out.println("dissp12::错误码:" + hCNetSDK.NET_DVR_GetLastError());
        long findFile = lFindFile40.longValue();
        if (findFile > -1) {
            System.out.println("succ:file" + findFile);
        } else {
            System.out.println("error:file" + findFile);
        }

    }

    public void download(NativeLong lUserID, String deviceIp, int channel, LocalDateTime startTime, LocalDateTime stopTime) {

        HCNetSDK.NET_DVR_TIME struStartTime;
        HCNetSDK.NET_DVR_TIME struStopTime;
        struStartTime = new HCNetSDK.NET_DVR_TIME();
        struStopTime = new HCNetSDK.NET_DVR_TIME();
        struStartTime.dwYear = startTime.getYear();
        struStartTime.dwMonth = startTime.getMonth().getValue();
        struStartTime.dwDay = startTime.getDayOfMonth();
        struStartTime.dwHour = startTime.getHour();
        struStartTime.dwMinute = startTime.getMinute();
        struStartTime.dwSecond = startTime.getSecond();
        struStopTime.dwYear = stopTime.getYear();
        struStopTime.dwMonth = stopTime.getMonth().getValue();
        struStopTime.dwDay = stopTime.getDayOfMonth();
        struStopTime.dwHour = stopTime.getHour();
        struStopTime.dwMinute = stopTime.getMinute();
        struStopTime.dwSecond = stopTime.getSecond();
        String fileName = deviceIp + channel + struStartTime.toStringTitle()
                + struStopTime.toStringTitle() + ".mp4";

        HCNetSDK.NET_DVR_PLAYCOND struDownloadCond = new HCNetSDK.NET_DVR_PLAYCOND();
        struDownloadCond.dwChannel = channel;
        struDownloadCond.struStartTime = struStartTime;
        struDownloadCond.struStopTime = struStopTime;
        NativeLong loadHandle = hCNetSDK.NET_DVR_GetFileByTime_V40(lUserID,fileName,struDownloadCond);



        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().daemon(true).build());
        ClientDemo.playbackControlV6(loadHandle, scheduler, hCNetSDK, log);

    }

    //注销
    public void Logout(NativeLong lUserID) {
        //注销
        if (lUserID.longValue() > -1) {
            if (hCNetSDK.NET_DVR_Logout(lUserID)) {
                System.out.println("注销成功");
                lUserID = new NativeLong(-1);
            }
        }
    }

}
