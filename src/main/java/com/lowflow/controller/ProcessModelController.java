package com.lowflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.lowflow.common.CommUtil;
import com.lowflow.common.Result;
import com.lowflow.pojo.ProcessModel;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

@RestController
@RequestMapping("/model")
public class ProcessModelController {

    /**
     * 转为bpmn，并下载
     *
     * @param processModel
     * @throws IOException
     */
    @PostMapping("/download")
    public void downloadXml(@RequestBody ProcessModel processModel) throws IOException {
        BpmnModel bpmnModel = processModel.toBpmnModel();
        byte[] xmlBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
        if (Objects.nonNull(xmlBytes)) {
            String fileName = processModel.getName().replaceAll(" ", "_") + ".bpmn20.xml";
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            if (Objects.nonNull(response)) {
                response.setContentType("application/xml");
                response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
                response.setHeader("Content-Disposition", "attachment; filename=file.bpmn20.xml; filename*=" + URLEncoder.encode(fileName, "UTF-8"));
                ServletOutputStream servletOutputStream = response.getOutputStream();
                BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(xmlBytes));
                byte[] buffer = new byte[8096];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    servletOutputStream.write(buffer, 0, count);
                }
                // 刷新并关闭流
                servletOutputStream.flush();
                servletOutputStream.close();
            }
        }
    }

    /**
     * push
     */
    @PostMapping("/push")
    public Result<List<Map>> push(@RequestBody ProcessModel processModel) {
        BpmnModel bpmnModel = processModel.toBpmnModel();
        byte[] xmlBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
//        CommUtil.printToFile(new String(xmlBytes), "D:\\code\\end\\lowflow-design-converter\\src\\main\\resources\\bpmnxml\\test_" + IdWorker.getIdStr() + ".xml");
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        Deployment deployment = processEngine
                .getRepositoryService()
                .createDeployment()
                .addBpmnModel("testName", bpmnModel)
                .deploy();
        System.out.println("流程部署id：" + deployment.getId());
        System.out.println("流程部署名称：" + deployment.getName());
        return Result.OK();
    }

    /**
     * todolist
     */
    @PostMapping("/todolist")
    public Result<Object> todolist(@RequestBody Map map) {
        // 任务负责人
        String assignee = "zhangsan";
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 创建TaskService
        TaskService taskService = processEngine.getTaskService();
        // 根据流程key 和 任务负责人 查询任务
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(Objects.toString(map.get("userId")))//只查询该任务负责人的任务
                .orderByTaskCreateTime().desc()
                .list();
        for (Task task : list) {
            Map<String, Object> processVariables = task.getProcessVariables();
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
        return Result.OK(list);
    }

    /**
     * start
     */
    @PostMapping("/start")
    public Result<List<Map>> start(@RequestBody Map map) {
        // 1、创建ProcessEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 2、获取RunTimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
        TaskService taskService = processEngine.getTaskService();
        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put("FORM_VAR", map.get("formData"));
        processVariables.put("PROCESS_STATUS", "1");
//        processVariables.put("START_USER_INFO", JSONObject.toJSONString(startUserInfo));
//        processVariables.put("INITIATOR_ID", startUserInfo.getId());
//        ArrayList<Map> userInfos = Lists.newArrayList(startUserInfo);
//        processVariables.put("root", JSONObject.toJSONString(userInfos));
//        Map<String, List<UserInfo>> processUsers = startProcessInstanceDTO.getProcessUsers();
//        if (CollUtil.isNotEmpty(processUsers)) {
//            Set<String> strings = processUsers.keySet();
//            for (String string : strings) {
//                List<UserInfo> selectUserInfo = processUsers.get(string);
//                List<String> users = new ArrayList<>();
//                for (UserInfo userInfo : selectUserInfo) {
//                    users.add(userInfo.getId());
//                }
//                processVariables.put(string, users);
//            }
//        }
        // 3、根据流程定义Id启动流程
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .businessKey("myEvection")
                .variables(processVariables)
                .start();
        //手动完成第一个任务
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        if (task != null) {
            taskService.complete(task.getId());
        }
        // 输出内容
        System.out.println("流程定义id：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例id：" + processInstance.getId());
        System.out.println("当前活动Id：" + processInstance.getActivityId());
        return Result.OK();
    }

    /**
     * approve
     */
    @PostMapping("/approve")
    public Result<List<Map>> approve(@RequestBody Map map) {
//        String username = Objects.toString(map.get("username"));
//        String taskId = Objects.toString(map.get("taskId"));
//        Map<String,Object> variables = JSONObject.parseObject(Objects.toString(map.get("variables")),Map.class);
//        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
//        TaskService taskService = processEngine.getTaskService();
//        taskService.setAssignee(taskId, username);
//        // 查出流程实例id
//        String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
//        if (variables == null) {
//            taskService.complete(taskId);
//        } else {
//            // 添加审批意见
//            if (variables.get("comment") != null) {
//                taskService.addComment(taskId, processInstanceId, (String) variables.get("comment"));
//                variables.remove("comment");
//            }
//            taskService.complete(taskId, variables);
//        }
        return Result.OK();
    }

    /**
     * reject
     */
    @PostMapping("/reject")
    public Result<List<Map>> reject(@RequestBody Map map) {
//        String username = Objects.toString(map.get("username"));
//        String taskId = Objects.toString(map.get("taskId"));
//        Map<String,Object> variables = JSONObject.parseObject(Objects.toString(map.get("variables")),Map.class);
//        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
//        RuntimeService runtimeService = processEngine.getRuntimeService();
//        TaskService taskService = processEngine.getTaskService();
//        taskService.setAssignee(taskId, username);
//        // 查出流程实例id
//        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
//        String processInstanceId = task.getProcessInstanceId();
//        if (variables == null) {
//            taskService.complete(taskId);
//        } else {
//            // 添加审批意见
//            if (variables.get("comment") != null) {
//                taskService.addComment(taskId, processInstanceId, (String) variables.get("comment"));
//                variables.remove("comment");
//            }
//            taskService.complete(taskId, variables);
//        }
//        runtimeService.deleteProcessInstance(task.getProcessInstanceId(), "拒绝");
        return Result.OK();
    }

    /**
     * rollback
     */
    @PostMapping("/rollback")
    public Result<List<Map>> rollback(@RequestBody Map map) {
//        String username = Objects.toString(map.get("username"));
//        String taskId = Objects.toString(map.get("taskId"));
//        Map<String,Object> variables = JSONObject.parseObject(Objects.toString(map.get("variables")),Map.class);
//        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
//        RuntimeService runtimeService = processEngine.getRuntimeService();
//        TaskService taskService = processEngine.getTaskService();
//        taskService.setAssignee(taskId, username);
//        // 查出流程实例id
//        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
//        String processInstanceId = task.getProcessInstanceId();
//        if (variables == null) {
//            taskService.complete(taskId);
//        } else {
//            // 添加审批意见
//            if (variables.get("comment") != null) {
//                taskService.addComment(taskId, processInstanceId, (String) variables.get("comment"));
//                variables.remove("comment");
//            }
//            taskService.complete(taskId, variables);
//        }
//        runtimeService.createProcessInstanceBuilder()
//                .processDefinitionId(task.getProcessInstanceId())
//                .moveActivityIdsToSingleActivityId(taskIds, handleDataDTO.getRollbackId())
//                .changeState();
        return Result.OK();
    }

    /**
     * getUserList
     */
    @PostMapping("/getUserList")
    public Result<List<Map>> getUserList() {
        return Result.OK();
    }

    /**
     * getRoleList
     */
    @PostMapping("/getRoleList")
    public Result<List<Map>> getRoleList() {
        return Result.OK();
    }

}
