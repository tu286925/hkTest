package com.hx.hkTest.controller;

import com.hx.hkTest.utils.ClientDemo;
import com.hx.hkTest.utils.LinuxClient;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//摄像头注册的类
@RestController
@RequestMapping("/a")
public class TestController {
	
	@RequestMapping("/test")
	@ResponseBody
	public int test(String username,String password,String deviceIp, int channel,
            String startTime, String endTime) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime sdt = LocalDateTime.parse(startTime,df);
        LocalDateTime edt = LocalDateTime.parse(endTime,df);

		if (Platform.isWindows()) {
			ClientDemo cd = new ClientDemo();
			NativeLong lUserID = cd.register(username, password, deviceIp);
//			LocalDateTime start = LocalDateTime.of(2019, 8, 28, 11, 0, 0, 0);
//			LocalDateTime end = LocalDateTime.of(2019, 8, 28, 11, 10, 0, 0);
			cd.getFile(lUserID, channel, sdt, edt);
			cd.download(lUserID, deviceIp, channel, sdt, edt);
		}
        if (Platform.isLinux()) {
            LinuxClient linuxClient = new LinuxClient();
            linuxClient.CameraInit();
            NativeLong lUserID = linuxClient.register(username, password, deviceIp);
//            LocalDateTime start = LocalDateTime.of(2019, 8, 28, 11, 0, 0, 0);
//            LocalDateTime end = LocalDateTime.of(2019, 8, 28, 11, 10, 0, 0);
            linuxClient.getFile(lUserID, channel, sdt, edt);
            linuxClient.download(lUserID, deviceIp, channel, sdt, edt);
        }

		return 1; 
	}
}