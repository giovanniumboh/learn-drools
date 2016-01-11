package com.sample.test;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;

import com.sample.model.Person;

/**
 * This is a sample class to launch a rule.
 */
public class DroolsTest {

	static KieContainer kContainer = null;

	public static StatelessKieSession getStatelessSession() {
		return kContainer.newStatelessKieSession();
	}

	public static KieSession getStatefulSession() {
		// return kContainer.newKieSession();
		return kContainer.newKieSession("ksession-rules");
	}

	public static final void main(String[] args) {
		KieSession kSession = null;
		try {
			// load up the knowledge base
			KieServices ks = KieServices.Factory.get();

			// ReleaseId releaseId = ks.newReleaseId("com.giovanni",
			// "contest-test", "1.0");
			// kContainer = ks.newKieContainer(releaseId);

			kContainer = ks.getKieClasspathContainer();

			// KieScanner kScanner = ks.newKieScanner(kContainer);

			kSession = getStatefulSession();

			Person p = new Person();
			p.setWage(12);
			p.setFirstName("Tom");
			p.setLastName("Summers");
			p.setHourlyRate(10);

			Person p1 = new Person();
			p1.setWage(12);
			p1.setFirstName("Jerry");
			p1.setLastName("Summers");
			p1.setHourlyRate(10);

			kSession.insert(p);
			kSession.insert(p1);
			kSession.fireAllRules();

			System.out.println();
			System.out.println("name " + "Tom Summers" + " change to " + p.getFirstName() + " " + p.getLastName());

			// kScanner.start(1000L);

			// KieCommands kCommand = ks.getCommands();
			// kCommand.newInsert(p, "tom", true, null);
			// kCommand.newInsert(p1, "jerry", true, null);
			//
			// List<Command> listCommand = new ArrayList<Command>();
			// listCommand.add(kCommand.newInsert(p, "tom", true, null));
			// listCommand.add(kCommand.newInsert(p1, "jerry", true, null));
			//
			// ExecutionResults er =
			// kSession.execute(kCommand.newBatchExecution(listCommand));
			// kSession.fireAllRules();

			// kCommand.newFireAllRules();

			// System.out.println();
			// System.out.println(((Person) er.getValue("tom")).getFirstName());
			// System.out.println("isi fact ada " + kSession.getFactCount());

		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			kSession.dispose();
		}
	}

}
