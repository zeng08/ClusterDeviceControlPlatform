package cc.bitky.clusterdeviceplatform.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import cc.bitky.clusterdeviceplatform.server.config.CommSetting;
import cc.bitky.clusterdeviceplatform.server.config.DbSetting;
import cc.bitky.clusterdeviceplatform.server.config.ExSetting;

@SpringBootApplication
public class ServerApplication {
    private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    /**
     * 数据库是否可到达
     *
     * @return 返回可到达
     */
    private static boolean dataBaseReachable() {
        try {
            InetAddress inetAddress = InetAddress.getByName(DbSetting.HOST);
            if (inetAddress.isReachable(500)) {
                Socket s = new Socket();
                s.connect(new InetSocketAddress(inetAddress, DbSetting.MONGODB_PORT));
                s.close();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            logger.error("数据库异常，请检查数据库是否成功启动");
            return false;
        }
    }

    /**
     * 读取外部配置文件并初始化服务器的配置
     *
     * @return 读取文件并初始化成功
     */

    private static boolean initSetting() {
        ExSetting exSetting = null;
        try {
            String strings = new String(Files.readAllBytes(Paths.get(DbSetting.CONFIG_FILE_PATH)), StandardCharsets.UTF_8);
            exSetting = JSON.parseObject(strings, ExSetting.class);
        } catch (IOException e) {
            logger.warn("「外部配置文件」未能读取到配置文件");
        } catch (JSONException e) {
            logger.warn("「外部配置文件」反序列化失败");
        }
        if (exSetting != null) {
            logger.info("「外部配置文件」外部配置读取成功");
            DbSetting.HOST = exSetting.数据库服务器的主机名或IP;
            CommSetting.FRAME_SEND_INTERVAL = exSetting.帧发送间隔;
            CommSetting.DEPLOY_REMAIN_CHARGE_TIMES = exSetting.部署剩余充电次数阈值;
            DbSetting.DEFAULT_EMPLOYEE_CARD_NUMBER = exSetting.员工默认卡号;
            DbSetting.DEFAULT_EMPLOYEE_NAME = exSetting.员工默认姓名;
            DbSetting.DEFAULT_EMPLOYEE_DEPARTMENT = exSetting.员工默认部门;
            CommSetting.AUTO_REPEAT_REQUEST_TIMES = exSetting.检错重发最大重复次数;
            CommSetting.DEVICE_INIT_CHARGE_TIMES = exSetting.初始充电次数;
            CommSetting.DEPLOY_MSG_NEED_REPLY = exSetting.帧送达监测;
            return true;
        }
        logger.error("外部配置文件读取错误，请使用「服务器预设置」软件进行设置");
        return false;
    }
}