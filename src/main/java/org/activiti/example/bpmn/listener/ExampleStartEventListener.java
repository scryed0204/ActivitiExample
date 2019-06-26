package org.activiti.example.bpmn.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

@SuppressWarnings("serial")
public class ExampleStartEventListener implements ExecutionListener {
	
	@Override
	public void notify(DelegateExecution execution) throws Exception {
		execution.setVariable("rejectCnt", 0);//設定流程變數rejectCnt，值為0
	}
}
