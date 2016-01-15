package com.sample.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.core.io.impl.UrlResource;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.KieScanner;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;

import com.giovanni.contest_test.Agent;
import com.giovanni.contest_test.Contest;
import com.giovanni.contest_test.ContestDetail;
import com.giovanni.contest_test.Policy;

/**
 * This is a sample class to launch a rule.
 */
public class ContestPRUForceTest {

	static KieContainer kContainer = null;
	static KieSession kSession = null;
	static KieServices ks = null;
	static StatelessKieSession statelessSession = null;

	public static void testLoad3() throws IOException {
		// url ini menuju ke maven repository file jar rule yang kita tuju,
		// untuk pathnya bisa dilihat di business central menu Authoring ->
		// Artifact Repository
		String url = "http://localhost:8080/business-central/maven2/com/giovanni/contest-test/1.0/contest-test-1.0.jar";
		ks = KieServices.Factory.get();
		KieRepository kr = ks.getRepository();
		UrlResource urlResource = (UrlResource) ks.getResources().newUrlResource(url);

		// username dan password login ke business central
		urlResource.setUsername("emerio");
		urlResource.setPassword("emerio@123");
		urlResource.setBasicAuthentication("enabled");

		InputStream is = urlResource.getInputStream();
		KieModule kModule = kr.addKieModule(ks.getResources().newInputStreamResource(is));
		kContainer = ks.newKieContainer(kModule.getReleaseId());
		kSession = getStatefulSession();
		statelessSession = getStatelessSession();

		// cek jumlah rule yang ada di rule engine jar
		System.out.println("Connected to rule engine...");
		System.out.println();
		cekRule();
	}

	public static void cekRule() {
		KieBaseConfiguration kieBaseConf = ks.newKieBaseConfiguration();
		kieBaseConf.setOption(EventProcessingOption.STREAM);
		// KieBase kBase = kContainer.newKieBase(kieBaseConf);
		// System.out.println("Isi rule yang ada : ");
		// int i = 0;
		// for (KiePackage a : kBase.getKiePackages()) {
		// for (Rule r : a.getRules()) {
		// System.out.println(i + 1 + ". KiePackage - Rule = " +
		// a.getName() + "-" + r.getName());
		// i++;
		// }
		// }
	}

	public static final void main(String[] args) {
		try {
			// load up the knowledge base #3
			// testLoad3();

			LoadFactTestScenarioContestFsc();

			kSession.fireAllRules();

			QueryResults results2 = kSession.getQueryResults("getObjectsOfContestDetail");
			System.out.println();
			for (QueryResultsRow row : results2) {
				ContestDetail contestDetail = (ContestDetail) row.get("$result");
				System.out.println("Contest detail object : " + contestDetail.getAgentCode() + "\t"
						+ contestDetail.getPolicyNo() + "\t" + contestDetail.getContestCode());
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			// kSession.dispose();
		}
	}

	public static void LoadFactTestScenarioContestFsc() {
		Agent agent = new Agent();
		agent.setAgentCode("AG01");
		agent.setAgentName("Gio");
		// statelessSession.execute(agent);
		kSession.insert(agent);

		Contest contest1 = new Contest();
		contest1.setContestCode("C0001");
		contest1.setContestName("Contest Silver");
		contest1.setContestType("Silver");
		contest1.setNeedMonitor(false);
		// statelessSession.execute(contest1);
		kSession.insert(contest1);

		Contest contest2 = new Contest();
		contest2.setContestCode("C0002");
		contest2.setContestName("Contest Gold");
		contest2.setContestType("Gold");
		contest2.setNeedMonitor(false);
		// statelessSession.execute(contest2);
		kSession.insert(contest2);

		Contest contest3 = new Contest();
		contest3.setContestCode("C0003");
		contest3.setContestName("Contest Platinum");
		contest3.setNeedMonitor(true);
		contest3.setContestType("Platinum");
		// statelessSession.execute(contest3);
		kSession.insert(contest3);

		Policy policy1 = new Policy();
		policy1.setAgentCode("AG01");
		policy1.setPolicyNo("PLC01");
		policy1.setPolicyType("Silver");
		// statelessSession.execute(policy1);
		kSession.insert(policy1);

		Policy policy2 = new Policy();
		policy2.setAgentCode("AG01");
		policy2.setPolicyNo("PLC01");
		policy2.setPolicyType("Gold");
		// statelessSession.execute(policy2);
		kSession.insert(policy2);

		Policy policy3 = new Policy();
		policy3.setAgentCode("AG01");
		policy3.setPolicyNo("PLC03");
		policy3.setPolicyType("Platinum");
		// statelessSession.execute(policy3);
		kSession.insert(policy3);

		kSession.getAgenda().getAgendaGroup("contest").setFocus();
		// statelessSession.execute(Arrays.asList(new Object[] { policy1,
		// policy2, policy3 }));
		// System.out.println("isi fact ada " + kSession.getFactCount());
	}

	public static StatelessKieSession getStatelessSession() {
		return kContainer.newStatelessKieSession();
	}

	public static KieSession getStatefulSession() {
		return kContainer.newKieSession();
		// return kContainer.newKieSession("ksession-rules");
	}

}
