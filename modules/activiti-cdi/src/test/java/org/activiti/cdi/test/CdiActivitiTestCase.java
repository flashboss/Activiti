/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.cdi.test;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import javax.enterprise.inject.spi.BeanManager;

import org.activiti.cdi.BusinessProcess;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.cdi.test.util.ProcessEngineLookupForTestsuite;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.test.ActivitiRule;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for executing activiti-cdi tests in a Java SE
 * environment, using Weld-SE.
 * 
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public abstract class CdiActivitiTestCase {

	protected Logger logger = LoggerFactory.getLogger(getClass().getName());

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class).addPackages(true, "org/springframework")
				.addPackages(true, "org/activiti").addPackages(true, "org/h2").addPackages(true, "org/apache")
				.addPackages(true, "org/joda")
				// .addPackages(true, "org/jboss")
				// .addPackages(true, "org/wildfly")
				// .addPackages(true, "org/codehaus")
				// .addPackages(true, "org/slf4j")
				// .addPackages(true, "org/junit")
				// .addPackages(true, "org/infinispan")
				// .addPackages(true, "org/scannotation")
				// .addPackages(true, "org/fusesource")
				// .addPackages(true, "org/jgroups")
				// .addPackages(true, "org/glassfish")
				// .addPackages(true, "org/picketbox")
				// .addPackages(true, "org/hamcrest")
				// .addPackages(true, "org/jipijapa")
				// .addPackages(true, "org/hibernate")
				// .addPackages(true, "org/w3c")
				// .addPackages(true, "org/omg")
				// .addPackages(true, "org/xnio")
				.addAsResource("org/activiti/impl/bpmn/parser/activiti-bpmn-extensions-5.0.xsd")
				.addAsResource("org/activiti/impl/bpmn/parser/activiti-bpmn-extensions-5.10.xsd")
				.addAsResource("org/activiti/impl/bpmn/parser/activiti-bpmn-extensions-5.11.xsd")
				.addAsResource("org/activiti/impl/bpmn/parser/activiti-bpmn-extensions-5.15.xsd")
				.addAsResource("org/activiti/impl/bpmn/parser/activiti-bpmn-extensions-5.18.xsd")
				.addAsResource("org/activiti/impl/bpmn/parser/activiti-bpmn-extensions-5.2.xsd")
				.addAsResource("org/activiti/impl/bpmn/parser/activiti-bpmn-extensions-5.3.xsd")
				.addAsResource("org/activiti/impl/bpmn/parser/activiti-bpmn-extensions-5.4.xsd")
				.addAsResource("org/activiti/impl/bpmn/parser/BPMN20.xsd")
				.addAsResource("org/activiti/impl/bpmn/parser/BPMNDI.xsd")
				.addAsResource("org/activiti/impl/bpmn/parser/DC.xsd")
				.addAsResource("org/activiti/impl/bpmn/parser/DI.xsd")
				.addAsResource("org/activiti/impl/bpmn/parser/Semantic.xsd")
				.addAsResource("org/activiti/cdi/test/api/annotation/BusinessKeyTest.testBusinessKeyInjectable.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/api/annotation/CompleteTaskTest.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/api/annotation/ProcessIdTest.testProcessIdInjectable.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/api/annotation/StartProcessTest.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/api/annotation/TaskIdTest.testTaskIdInjectable.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/api/BusinessProcessBeanTest.test.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/api/BusinessProcessBeanTest.testProcessWithoutWaitState.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/bpmn/SignalEventTests.catchAlertSignalBoundaryWithReceiveTask.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/bpmn/SignalEventTests.throwAlertSignalWithDelegate.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/impl/context/BusinessProcessContextTest.testChangeProcessScopedBeanProperty.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/impl/context/BusinessProcessContextTest.testConversationalBeanStoreFlush.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/impl/context/BusinessProcessContextTest.testResolution.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/impl/context/ContextScopingTest.testFallbackToRequestContext.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/impl/context/ThreadContextAssociationTest.testBusinessProcessScopedWithJobExecutor.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/impl/el/ElTest.testInvalidExpression.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/impl/el/ElTest.testSetBeanProperty.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/impl/event/EventNotificationTest.process1.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/impl/event/EventNotificationTest.process2.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/impl/event/MultiInstanceServiceTaskEvent.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/impl/event/MultiInstanceTaskCompleteEventTest.process1.bpmn20.xml.bpmn")
				.addAsResource("org/activiti/cdi/test/impl/event/TaskEventNotificationTest.process3.bpmn20.xml")
				.addAsResource("org/activiti/cdi/test/impl/util/beansWithAlternative.xml")

				
				.addAsResource("activiti.cfg.xml").addAsResource("log4j.properties")
				.addAsResource("org/apache/xerces/impl/xpath/regex/message.properties")
				.addAsResource("org/apache/xerces/impl/msg/XMLSchemaMessages.properties")
				.addAsResource("org/apache/xerces/impl/msg/XMLMessages.properties")
				.addAsResource("org/activiti/db/mapping/mappings.xml")
				.addAsResource("org/activiti/db/create/activiti.h2.create.engine.sql")
				.addAsResource("org/activiti/db/create/activiti.h2.create.history.sql")
				.addAsResource("org/activiti/db/create/activiti.h2.create.identity.sql")
				.addAsManifestResource("META-INF/services/org.activiti.cdi.spi.ProcessEngineLookup",
						"services/org.activiti.cdi.spi.ProcessEngineLookup")
				.addAsManifestResource("META-INF/beans.xml", "beans.xml");
		return javaArchive;
	}

	@Rule
	public ActivitiRule activitiRule;

	{
		try {
			activitiRule = new ActivitiRule(getBeanInstance(ProcessEngine.class));
		} catch (ActivitiException ex) {
			// IGNORE. Arquillian starts a rule twice, before client side and
			// after server side. We need only the server side. See
			// https://issues.jboss.org/browse/ARQ-286
		}
	}

	protected BeanManager beanManager;

	protected ProcessEngine processEngine;
	protected FormService formService;
	protected HistoryService historyService;
	protected IdentityService identityService;
	protected ManagementService managementService;
	protected RepositoryService repositoryService;
	protected RuntimeService runtimeService;
	protected TaskService taskService;
	protected ProcessEngineConfigurationImpl processEngineConfiguration;

	@Before
	public void setUp() throws Exception {

		beanManager = ProgrammaticBeanLookup.lookup(BeanManager.class);
		processEngine = ProgrammaticBeanLookup.lookup(ProcessEngine.class);
		processEngineConfiguration = ((ProcessEngineImpl) ProcessEngineLookupForTestsuite.processEngine)
				.getProcessEngineConfiguration();
		activitiRule.setProcessEngineConfiguration(processEngineConfiguration);
		formService = processEngine.getFormService();
		historyService = processEngine.getHistoryService();
		identityService = processEngine.getIdentityService();
		managementService = processEngine.getManagementService();
		repositoryService = processEngine.getRepositoryService();
		runtimeService = processEngine.getRuntimeService();
		taskService = processEngine.getTaskService();
	}

	protected void endConversationAndBeginNew(String processInstanceId) {
		getBeanInstance(BusinessProcess.class).associateExecutionById(processInstanceId);
	}

	protected <T> T getBeanInstance(Class<T> clazz) {
		return ProgrammaticBeanLookup.lookup(clazz);
	}

	protected Object getBeanInstance(String name) {
		return ProgrammaticBeanLookup.lookup(name);
	}

	//////////////////////// copied from AbstractActivitiTestcase

	public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
		JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
		jobExecutor.start();

		try {
			Timer timer = new Timer();
			InteruptTask task = new InteruptTask(Thread.currentThread());
			timer.schedule(task, maxMillisToWait);
			boolean areJobsAvailable = true;
			try {
				while (areJobsAvailable && !task.isTimeLimitExceeded()) {
					Thread.sleep(intervalMillis);
					areJobsAvailable = areJobsAvailable();
				}
			} catch (InterruptedException e) {
			} finally {
				timer.cancel();
			}
			if (areJobsAvailable) {
				throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
			}

		} finally {
			jobExecutor.shutdown();
		}
	}

	public void waitForJobExecutorOnCondition(long maxMillisToWait, long intervalMillis, Callable<Boolean> condition) {
		JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
		jobExecutor.start();

		try {
			Timer timer = new Timer();
			InteruptTask task = new InteruptTask(Thread.currentThread());
			timer.schedule(task, maxMillisToWait);
			boolean conditionIsViolated = true;
			try {
				while (conditionIsViolated) {
					Thread.sleep(intervalMillis);
					conditionIsViolated = !condition.call();
				}
			} catch (InterruptedException e) {
			} catch (Exception e) {
				throw new ActivitiException("Exception while waiting on condition: " + e.getMessage(), e);
			} finally {
				timer.cancel();
			}
			if (conditionIsViolated) {
				throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
			}

		} finally {
			jobExecutor.shutdown();
		}
	}

	public boolean areJobsAvailable() {
		return !managementService.createJobQuery().executable().list().isEmpty();
	}

	private static class InteruptTask extends TimerTask {
		protected boolean timeLimitExceeded = false;
		protected Thread thread;

		public InteruptTask(Thread thread) {
			this.thread = thread;
		}

		public boolean isTimeLimitExceeded() {
			return timeLimitExceeded;
		}

		public void run() {
			timeLimitExceeded = true;
			thread.interrupt();
		}
	}
}
