package com.sample.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.core.io.impl.UrlResource;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.KieScanner;
import org.kie.api.command.Command;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;

import com.giovanni.contest_test.Agent;
import com.giovanni.contest_test.Contest;
import com.giovanni.contest_test.ContestDetail;
import com.giovanni.contest_test.Policy;

/**
 * This is a sample class to launch a rule.
 */
public class KieApiTest {

	static KieContainer kContainer = null;
	static KieSession kSession = null;
	static KieServices ks = null;
	static StatelessKieSession statelessSession = null;

	public static void testLoad1() {
		ks = KieServices.Factory.get();
		ReleaseIdImpl releaseId = new ReleaseIdImpl("com.giovanni", "contest-test", "1.0");
		System.out.println("isi releaseId pom path = " + releaseId.getPomXmlPath());
		System.out.println("isi releaseId pom properties path = " + releaseId.getPomPropertiesPath());

		KieRepository kr = ks.getRepository();

		System.out.println("isi repository = " + kr);
		System.out.println("kr.getKieModule() = " + kr.getKieModule(releaseId));

		kr.addKieModule(kr.getKieModule(releaseId));

		// kContainer =
		// ks.newKieContainer(kr.getKieModule(releaseId).getReleaseId());

		cekRule();
	}

	public static void testLoad2() {
		String url = "http://localhost:8080/business-central/maven2/com/giovanni/contest-test/1.0/contest-test-1.0.jar";
		ReleaseIdImpl releaseId = new ReleaseIdImpl("com.giovanni", "contest-test", "1.0");
		ks = KieServices.Factory.get();
		ks.getResources().newUrlResource(url);
		kContainer = ks.newKieContainer(releaseId);

		// check every 5 seconds if there is a new version at the URL
		KieScanner kieScanner = ks.newKieScanner(kContainer);
		kieScanner.start(5000L);
		// alternatively:
		// kieScanner.scanNow();

		cekRule();

		Scanner scanner = new Scanner(System.in);
		while (true) {
			// runRule(kieContainer);
			System.out.println("Press enter in order to run the test again....");
			scanner.nextLine();
		}

	}

	public static void testLoad3() throws IOException {
		String url = "http://localhost:8080/business-central/maven2/com/giovanni/contest-test/1.0/contest-test-1.0.jar";
		ks = KieServices.Factory.get();
		KieRepository kr = ks.getRepository();
		UrlResource urlResource = (UrlResource) ks.getResources().newUrlResource(url);
		urlResource.setUsername("emerio");
		urlResource.setPassword("emerio@123");
		urlResource.setBasicAuthentication("enabled");
		InputStream is = urlResource.getInputStream();
		KieModule kModule = kr.addKieModule(ks.getResources().newInputStreamResource(is));
		kContainer = ks.newKieContainer(kModule.getReleaseId());
		kSession = getStatefulSession();
		statelessSession = getStatelessSession();

		// KieScanner kieScanner = ks.newKieScanner(kContainer);
		// System.out.println("kieScanner " + kieScanner);
		// kieScanner.start(5000L);

		// cek jumlah rule yang ada di rule engine jar
		cekRule();
	}

	public static void cekRule() {
		KieBaseConfiguration kieBaseConf = ks.newKieBaseConfiguration();
		kieBaseConf.setOption(EventProcessingOption.STREAM);
		KieBase kBase = kContainer.newKieBase(kieBaseConf);
		System.out.println("Isi rule yang ada : ");
		int i = 0;
		for (KiePackage a : kBase.getKiePackages()) {
			for (Rule r : a.getRules()) {
				System.out.println(i + 1 + ". KiePackage - Rule = " + a.getName() + "-" + r.getName());
				i++;
			}
		}
	}

	public static final void main(String[] args) {
		try {
			// load up the knowledge base #1
			// testLoad1();

			// load up the knowledge base #2
			// testLoad2();

			// load up the knowledge base #3
			testLoad3();

			Agent agent = new Agent();
			agent.setAgentCode("AG01");
			agent.setAgentName("Giovanni");
			statelessSession.execute(agent);
			// kSession.insert(agent);

			Contest contest1 = new Contest();
			contest1.setContestCode("C0001");
			contest1.setContestName("Contest Silver");
			contest1.setNeedMonitor(false);
			statelessSession.execute(contest1);
			// kSession.insert(contest1);

			Contest contest2 = new Contest();
			contest2.setContestCode("C0002");
			contest2.setContestName("Contest Gold");
			contest2.setNeedMonitor(false);
			statelessSession.execute(contest2);
			// kSession.insert(contest2);
			// kSession.fireAllRules();

			Contest contest3 = new Contest();
			contest3.setContestCode("C0003");
			contest3.setContestName("Contest Platinum");
			contest3.setNeedMonitor(true);
			statelessSession.execute(contest3);
			// kSession.insert(contest3);

			Policy policy1 = new Policy();
			policy1.setAgentCode("AG01");
			policy1.setPolicyNo("PLC01");
			policy1.setPolicyType("Silver");
			// statelessSession.execute(policy1);
			// kSession.insert(policy1);
			// kSession.fireAllRules();

			Policy policy2 = new Policy();
			policy2.setAgentCode("AG01");
			policy2.setPolicyNo("PLC02");
			policy2.setPolicyType("Gold");
			// statelessSession.execute(policy2);
			// kSession.insert(policy2);

			Policy policy3 = new Policy();
			policy3.setAgentCode("AG01");
			policy3.setPolicyNo("PLC03");
			policy3.setPolicyType("Platinum");
			// statelessSession.execute(policy3);
			// kSession.insert(policy3);

			statelessSession.execute(Arrays.asList(new Object[] { policy1, policy2, policy3 }));
			System.out.println("isi fact ada " + kSession.getFactCount());

			// kSession.getAgenda().getAgendaGroup("contest").setFocus();
			// kSession.fireAllRules();

			// List cmds = new ArrayList();
			// cmds.add(CommandFactory.newSetGlobal("listContestDetail", new
			// ArrayList<ContestDetail>()));

			// statlessKs.setGlobal("listContestDetail", new
			// ArrayList<ContestDetail>());

			// ExecutionResults results =
			// kSession.execute(CommandFactory.newBatchExecution(commands));
			// results.getValue("listContestDetail"); // returns the ArrayList

		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			// kSession.dispose();
		}
	}

	public static StatelessKieSession getStatelessSession() {
		return kContainer.newStatelessKieSession();
	}

	public static KieSession getStatefulSession() {
		return kContainer.newKieSession();
		// return kContainer.newKieSession("ksession-rules");
	}

}
