package com.hxch.activiti;

import com.google.common.collect.Maps;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class DemoMain {

    public static final Logger logger = LoggerFactory.getLogger(DemoMain.class);

    public static void main(String[] args) throws Exception{
        logger.info("启动程序");

        // 创建流程引擎
        ProcessEngine processEngine = getProcessEngine();
        // 部署流程定义文件

        ProcessDefinition processDefinition = getProcessDefinition(processEngine);

        // 启动运行流程
        ProcessInstance processInstance = getProcessInstance(processEngine, processDefinition.getId());

        // 处理流程任务
        processTaskHandler(processEngine, processDefinition, processInstance);

        logger.info("关闭程序");


    }

    private static void processTaskHandler(ProcessEngine processEngine, ProcessDefinition processDefinition, ProcessInstance processInstance) throws ParseException {
        Scanner scanner = new Scanner(System.in);
        while (processInstance != null && !processInstance.isEnded()){
            TaskService taskService = processEngine.getTaskService();
            List<Task> tasks = taskService.createTaskQuery().list();
            logger.info("要处理的流程任务数量：[{}]",tasks.size());
            for (Task task : tasks){
                HashMap<String, Object> valueMap = getValueMap(processEngine, scanner, task);
                taskService.complete(task.getId(),valueMap);
                processInstance = processEngine.getRuntimeService()
                        .createProcessInstanceQuery()
                        .processDefinitionId(processDefinition.getId())
                        .singleResult();
            }
        }
        scanner.close();
    }

    private static HashMap<String, Object> getValueMap(ProcessEngine processEngine, Scanner scanner, Task task) throws ParseException {
        logger.info("流程任务：[{}]",task.getName());
        FormService formService = processEngine.getFormService();
        TaskFormData taskFormData = formService.getTaskFormData(task.getId());
        List<FormProperty> formProperties = taskFormData.getFormProperties();
        HashMap<String, Object> valueMap = Maps.newHashMap();
        for (FormProperty property : formProperties){
            String line = null;
            if (StringFormType.class.isInstance(property.getType())){
                logger.info("请输入：{} ？",property.getName());
                line = scanner.nextLine();
                valueMap.put(property.getId(), line);

            }else if (DateFormType.class.isInstance(property.getType())){
                logger.info("请输入：{} ？，格式为 (yyyy-MM-dd)",property.getName());
                line = scanner.nextLine();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date date = format.parse(line);
                valueMap.put(property.getId(), date);
            }else {
                logger.info("你输入的类型暂不支持 [{}]",property.getType());
            }
            logger.info("你输入的内容是：[{}]",line);
        }
        return valueMap;
    }

    private static ProcessInstance getProcessInstance(ProcessEngine processEngine, String id) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(id);
        logger.info("流程启动：[{}]", processInstance.getProcessDefinitionKey());
        return processInstance;
    }

    private static ProcessDefinition getProcessDefinition(ProcessEngine processEngine) {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        deploymentBuilder.addClasspathResource("second_approve.bpmn20.xml");
        Deployment deployment = deploymentBuilder.deploy();
        String deploymentId = deployment.getId();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();

        logger.info("流程定义对象文件：[{}]，ID：[{}]",processDefinition.getName(),processDefinition.getId());
        return processDefinition;
    }

    private static ProcessEngine getProcessEngine() {
        ProcessEngineConfiguration configuration = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
        ProcessEngine processEngine = configuration.buildProcessEngine();
        String name = processEngine.getName();
        String version = ProcessEngine.VERSION;
        logger.info("流程引擎名称：[{}], 版本号：[{}]", name, version);
        return processEngine;
    }
}
