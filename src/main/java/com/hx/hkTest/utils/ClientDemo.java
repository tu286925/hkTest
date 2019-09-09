package com.hx.hkTest.utils;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ClientDemo {
    private static HCNetSDK hCNetSDK = null;
    private static boolean initSuc = false;

    public static String DLL_PATH;
    static {
        String path = (ClientDemo.class.getResource("/").getPath()).replaceAll("%20", " ").substring(1).replace("/",
                "\\");
        try {
            DLL_PATH = java.net.URLDecoder.decode(path, "utf-8");
            System.out.println(DLL_PATH);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        if (Platform.isWindows()) {
            System.out.println("准备加载HKNetSDK：");
            hCNetSDK = (HCNetSDK) Native.loadLibrary(DLL_PATH  + File.separator + "HCNetSDK.dll", HCNetSDK.class);
            System.out.println(DLL_PATH  + File.separator + "HCNetSDK.dll");
        }
        if (Platform.isLinux()) {
            hCNetSDK =(HCNetSDK) Native.loadLibrary("hcnetsdk", HCNetSDK.class);
        }
        if (!initSuc) {
            initSuc = hCNetSDK.NET_DVR_Init();
//            hCNetSDK.NET_DVR_SetLogToFile(true, null, false);
            if (!initSuc) {
                log.error("初始化失败");
            } else {
                log.info("初始化成功");
            }
        }
    }
    public ClientDemo() {
        if (Platform.isWindows()) {
            System.out.println("准备加载HKNetSDK：");
            hCNetSDK = (HCNetSDK) Native.loadLibrary(DLL_PATH  + File.separator + "HCNetSDK.dll", HCNetSDK.class);
            System.out.println("加载成功！");
        }
        if (Platform.isLinux()) {
            hCNetSDK =(HCNetSDK) Native.loadLibrary("hcnetsdk", HCNetSDK.class);
        }
        if (!initSuc) {
            initSuc = hCNetSDK.NET_DVR_Init();
//            hCNetSDK.NET_DVR_SetLogToFile(true, null, false);
            if (!initSuc) {
                log.error("初始化失败");
            } else {
                log.info("初始化成功");
            }
        }
    }

    //注册
    public NativeLong register(String username, String password, String deviceIp) {
        HCNetSDK.NET_DVR_DEVICEINFO_V30 deviceinfoV30 = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        int iPort = 8000;
        System.out.println("注册，设备IP：" + deviceIp);
        NativeLong lUserId = hCNetSDK.NET_DVR_Login_V30(deviceIp, (short) iPort, username, password, deviceinfoV30);

        long userId = lUserId.longValue();
        if (userId == -1) {
            System.out.println("注册失败"+ hCNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("注册成功,lUserID:" + userId + "code:" + hCNetSDK.NET_DVR_GetLastError());
            getDeviceInfo(lUserId, deviceinfoV30);
        }
        return lUserId;
    }

    public void getDeviceInfo(NativeLong lUserId, HCNetSDK.NET_DVR_DEVICEINFO_V30 deviceinfoV30) {
        IntByReference ibrBytesReturned = new IntByReference(0);
        HCNetSDK.NET_DVR_IPPARACFG ipParaCfg = new HCNetSDK.NET_DVR_IPPARACFG();
        ipParaCfg.write();
        Pointer lpIpParaConfig = ipParaCfg.getPointer();
        boolean bRet = hCNetSDK
                .NET_DVR_GetDVRConfig(lUserId, HCNetSDK.NET_DVR_GET_IPPARACFG, new NativeLong(0), lpIpParaConfig,
                        ipParaCfg.size(), ibrBytesReturned);
        ipParaCfg.read();
        if (bRet) {
            log.info("共" + deviceinfoV30.byChanNum + "个设备,start:" + deviceinfoV30.byStartChan);
            for (int iChannum = 0; iChannum < HCNetSDK.MAX_IP_CHANNEL; iChannum++) {
                if (ipParaCfg.struIPChanInfo[iChannum].byEnable == 1) {
                    log.info("IPCamera" + (iChannum + deviceinfoV30.byStartChan) + "有设备");
                }
            }
        }

    }

    public void getFile(NativeLong lUserID, int channel, LocalDateTime startTime, LocalDateTime stopTime) {
        HCNetSDK.NET_DVR_FILECOND m_strFilecond = new HCNetSDK.NET_DVR_FILECOND();

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

        m_strFilecond.struStartTime = struStartTime;
        m_strFilecond.struStopTime = struStopTime;
        m_strFilecond.lChannel = new NativeLong(channel);
        m_strFilecond.dwFileType = 0;
        m_strFilecond.dwIsLocked = 0xff;
        m_strFilecond.dwUseCardNo = 0;
        NativeLong lFindFile = hCNetSDK.NET_DVR_FindFile_V30(lUserID, m_strFilecond);
        HCNetSDK.NET_DVR_FINDDATA_V30 strFile = new HCNetSDK.NET_DVR_FINDDATA_V30();
        System.out.println("dissp12::错误码:" + hCNetSDK.NET_DVR_GetLastError());
        long findFile = lFindFile.longValue();
        if (findFile > -1) {
            System.out.println("succ:file" + findFile);
        } else {
            System.out.println("error:file" + findFile);
        }

    }

    public void download(NativeLong lUserID, String deviceIp, int channel, LocalDateTime startTime, LocalDateTime stopTime) {
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

        String fileName = "D:\\DemoForMe\\hkTest\\mp4\\"+deviceIp + channel + struStartTime.toStringTitle()
                + struStopTime.toStringTitle() + ".mp4";
        NativeLong loadHandle = hCNetSDK
                .NET_DVR_GetFileByTime(lUserID, new NativeLong(channel), struStartTime, struStopTime, fileName);

        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().daemon(true).build());
        playbackControlV6(loadHandle, scheduler, hCNetSDK, log);
    }

    static void playbackControlV6(NativeLong loadHandle, ScheduledExecutorService scheduler, HCNetSDK hCNetSDK,
            Logger log) {
        if (loadHandle.intValue() >= 0) {
            long downloadStartTime=System.currentTimeMillis();
            hCNetSDK.NET_DVR_PlayBackControl(loadHandle, HCNetSDK.NET_DVR_PLAYSTART, 0, null);
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    IntByReference nPos = new IntByReference(0);
                    hCNetSDK.NET_DVR_PlayBackControl(loadHandle, HCNetSDK.NET_DVR_PLAYGETPOS, 0, nPos);
                    if (nPos.getValue() > 100) {
                        hCNetSDK.NET_DVR_StopGetFile(loadHandle);
                        loadHandle.setValue(-1);
                        log.info("1由于网络原因或DVR忙,下载异常终止!");
                        scheduler.shutdown();
                    }
                    if (nPos.getValue() == 100) {
                        hCNetSDK.NET_DVR_StopGetFile(loadHandle);
                        loadHandle.setValue(-1);

                        scheduler.shutdown();
                        long downloadEndTime=System.currentTimeMillis();
                        log.info("1按时间下载结束, 共" + (downloadEndTime-downloadStartTime)/1000.0 + "秒");
                    } else {
                        log.info("1回放进度" + nPos.getValue());
                    }
                }
            }, 0, 5, TimeUnit.SECONDS);
        } else {
            log.info("1下载失败");
            log.info("1laste error " + hCNetSDK.NET_DVR_GetLastError());
        }
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
