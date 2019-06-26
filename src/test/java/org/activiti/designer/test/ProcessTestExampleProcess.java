package org.activiti.designer.test;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ProcessTestExampleProcess {

	public RepositoryService repositoryService;
	public RuntimeService runtimeService;
	public TaskService taskService;
	public FormService formService;
	public HistoryService historyService;

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();

	@Before
	public void init() {
		repositoryService = activitiRule.getRepositoryService(); // 佈署流程圖
		runtimeService = activitiRule.getRuntimeService(); // 起案
		taskService = activitiRule.getTaskService(); // 待辦關卡查詢
		formService = activitiRule.getFormService(); // 待辦關卡送出表單簽核
		historyService = activitiRule.getHistoryService(); // 流程完成後之歷史資料查詢
	}

	@Test
	public void startProcess() throws Exception {
		// 佈署流程圖(.bpmn)
		String filename = "src\\main\\resources\\diagrams\\ExampleProcess.bpmn";
		StringBuilder flowHistory = new StringBuilder();
		repositoryService.createDeployment().addInputStream("ExampleProcess.bpmn20.xml", new FileInputStream(filename))
				.deploy();

		// 起案時需要提供的表單屬性(流程變數)
		Map<String, Object> variableMap = new HashMap<String, Object>();
		variableMap.put("initiator", "Tang");
		variableMap.put("supervisor", "Bryan");
		variableMap.put("manager", "King");
		variableMap.put("amount", "150000");

		// 起案產生流程實體
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("ExampleProcess", variableMap);
		assertNotNull(processInstance.getId());
		String processInstId = processInstance.getId();
		System.out.println(String.format("起案產生流程實體(id=%s)", processInstId));

		// 查詢目前所在關卡(直屬主管審核)
		Task task = taskService.createTaskQuery().processInstanceId(processInstId).singleResult();
		System.out.println(task.getName() + "/" + task.getAssignee()); // 印出：直屬主管審核/Bryan
		flowHistory.append(task.getName());
		Map<String, String> properties = new HashMap<String, String>(); // 關卡要送出的表單屬性資訊
		properties.put("decision", "approve"); // 審核結果：核准
		task = completeTask(task, properties); // 送簽(直屬主管審核)，取得下一關(經理審核)
		System.out.println(task.getName() + "/" + task.getAssignee()); // 印出：經理審核/King
		flowHistory.append(" -> " + task.getName());

		// 目前關卡：經理審核
		properties = new HashMap<String, String>(); // 關卡要送出的表單屬性資訊
		properties.put("decision", "reject"); // 審核結果：退回
		task = completeTask(task, properties); // 送簽(經理審核)，取得下一關(申請人調整)
		System.out.println(task.getName() + "/" + task.getAssignee()); // 印出：申請人調整/Tang
		flowHistory.append(" -> " + task.getName());

		// 目前關卡：申請人調整
		properties = new HashMap<String, String>(); // 關卡要送出的表單屬性資訊
		properties.put("decision", "approve"); // 審核結果：核准
		properties.put("amount", "50000"); // 調整金額<100000
		task = completeTask(task, properties); // 送簽(申請人調整)，取得下一關(直屬主管審核)
		System.out.println(task.getName() + "/" + task.getAssignee()); // 直屬主管審核/Bryan
		flowHistory.append(" -> " + task.getName());

		// 直屬主管審核 (第2次)
		properties = new HashMap<String, String>(); // 關卡要送出的表單屬性資訊
		properties.put("decision", "approve"); // 審核結果：核准
		task = completeTask(task, properties); // 送簽(直屬主管審核)，流程結束沒有下一關
		assertNull(task); // 沒有下一關

		historyService.createProcessInstanceHistoryLogQuery("5").singleResult().getEndTime();

		// 查詢已完成流程實體id
		List<HistoricProcessInstance> finishedProcessInstList = historyService.createHistoricProcessInstanceQuery()
				.finished().list();
		assertNotNull(finishedProcessInstList);
		assertEquals(processInstId, finishedProcessInstList.get(0).getId());

		// 找出流程變數rejectCnt的值
		Object finalRejectCnt = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstId)
				.variableName("rejectCnt").singleResult().getValue();
		System.out.println(String.format("流程實體已結案(id=%s)，案件被退回次數為%d次", finishedProcessInstList.get(0).getId(),
				(Integer) finalRejectCnt));

		// 印出流程過程
		System.out.println(flowHistory.toString());
	}

	/**
	 * 關卡簽核
	 * 
	 * @param task
	 *            待簽核關卡
	 * @param properties
	 *            表單屬性資訊
	 * @return 新的待辦關卡
	 */
	private Task completeTask(Task task, Map<String, String> properties) {
		formService.submitTaskFormData(task.getId(), properties);
		return taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
	}
}