package custom.activiti.test.config;

import javax.sql.DataSource;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ActivitiTestConfig {
	
	@Bean 
	public DataSource dataSource() {
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		
		try {
			dataSource.setDriverClass(org.h2.Driver.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Connection settings
		dataSource.setUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000");
		dataSource.setUsername("sa");
		dataSource.setPassword("");

		return dataSource;
	}
	
	@Bean
	public PlatformTransactionManager transactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(dataSource());
		return transactionManager;
	}
	
	@Bean
	public ProcessEngineFactoryBean processEngineFactoryBean() {
		ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
		processEngineFactoryBean.setProcessEngineConfiguration(processEngineConfiguration());
		return processEngineFactoryBean;
	}
	
	@Bean
	public ProcessEngineConfigurationImpl processEngineConfiguration() {
		SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
		processEngineConfiguration.setDataSource(dataSource());
		processEngineConfiguration.setDatabaseSchemaUpdate("true");
		processEngineConfiguration.setTransactionManager(transactionManager());
		processEngineConfiguration.setJobExecutorActivate(false);
		processEngineConfiguration.setAsyncExecutorEnabled(true);
		processEngineConfiguration.setAsyncExecutorActivate(true);
		processEngineConfiguration.setHistory("full");

		return processEngineConfiguration;
	}
	
	@Bean
	public ProcessEngine processEngine() {
		try {
			return processEngineFactoryBean().getObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Bean
	  public RepositoryService repositoryService() {
	  	return processEngine().getRepositoryService();
	  }

	@Bean
	public RuntimeService runtimeService() {
		return processEngine().getRuntimeService();
	}

	@Bean
	public TaskService taskService() {
		return processEngine().getTaskService();
	}

	@Bean
	public HistoryService historyService() {
		return processEngine().getHistoryService();
	}

	@Bean
	public FormService formService() {
		return processEngine().getFormService();
	}

	@Bean
	public IdentityService identityService() {
		return processEngine().getIdentityService();
	}

	@Bean
	public ManagementService managementService() {
		return processEngine().getManagementService();
	}
}
