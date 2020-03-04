import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigDBTest {


    public static final Logger logger = LoggerFactory.getLogger(ConfigDBTest.class);
    @Test
    public void test1(){

        ProcessEngineConfiguration configuration = ProcessEngineConfiguration.createProcessEngineConfigurationFromResourceDefault();
        logger.info("configuration = {}",configuration);
        ProcessEngine processEngine = configuration.buildProcessEngine();
        logger.info("获取流程引擎 {}",processEngine.getName());
        processEngine.close();
    }

    @Test
    public void test2(){

        ProcessEngineConfiguration configuration = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti_druid.cfg.xml");
        logger.info("configuration = {}",configuration);
        ProcessEngine processEngine = configuration.buildProcessEngine();
        logger.info("获取流程引擎 {}",processEngine.getName());
        processEngine.close();
    }


}
