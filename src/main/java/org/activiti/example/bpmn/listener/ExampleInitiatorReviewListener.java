package org.activiti.example.bpmn.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

@SuppressWarnings("serial")
public class ExampleInitiatorReviewListener implements TaskListener {	

	@Override
	public void notify(DelegateTask delegateTask) {
		Integer rejectCnt = (Integer)delegateTask.getVariable("rejectCnt");
		if (null != rejectCnt) {
			rejectCnt++;
			delegateTask.setVariable("rejectCnt", rejectCnt);
		}
	}
}
