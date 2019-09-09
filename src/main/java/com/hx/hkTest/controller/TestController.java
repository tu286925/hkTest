package com.hx.hkTest.controller;

import com.hx.hkTest.utils.ClientDemo;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//摄像头注册的类
@RestController
@RequestMapping("/a")
public class TestController {
	
	@RequestMapping(value = "/test" , method = RequestMethod.GET)
	@ResponseBody
	public int test(@RequestParam(value = "username", required = true) String username,
                    @RequestParam(value = "password", required = true) String password,
                    @RequestParam(value = "deviceIp", required = true) String deviceIp,
                    @RequestParam(value = "channel", required = true) int channel,
                    @RequestParam(value = "startTime", required = true) String startTime,
                    @RequestParam(value = "endTime", required = true) String endTime) {
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


		return 1; 
	}
}