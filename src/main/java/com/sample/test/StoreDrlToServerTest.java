package com.sample.test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.drools.core.io.impl.UrlResource;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;

import com.giovanni.contest_test.Agent;
import com.giovanni.contest_test.ContestMaster;
import com.giovanni.contest_test.ContestParameter;
import com.giovanni.contest_test.Policy;

/**
 * This is a sample class to launch a rule.
 */
public class StoreDrlToServerTest {

	static KieContainer kieContainer = null;
	static KieSession kieSession = null;
	static KieServices kieServices = null;
	static StatelessKieSession statelessSession = null;

	public static void testLoad1() throws IOException {
		kieServices = KieServices.Factory.get();
		// sampai di sini kieModule masih default
		KieRepository kieRepository = kieServices.getRepository();

		String url = "http://localhost:8080/business-central/maven2/com/giovanni/contest-test/1.0/contest-test-1.0.jar";
		UrlResource urlResource = (UrlResource) kieServices.getResources().newUrlResource(url);

		// username dan password login ke business central
		urlResource.setUsername("emerio");
		urlResource.setPassword("emerio@123");
		urlResource.setBasicAuthentication("enabled");

		InputStream is = urlResource.getInputStream();
		KieModule kModule = kieRepository.addKieModule(kieServices.getResources().newInputStreamResource(is));

		// generate dynamic rule to kBase
		KieFileSystem kfs = kieServices.newKieFileSystem();

		String rule = "package com.giovanni.contest_test; \n" + "rule \"contest_generated\" \n" + "when \n" + "then \n"
				+ "System.out.println(\"rule generated fired\"); \n" + "end";

		kfs.write("src/main/resources/rules/generatedRule.drl", rule);
		System.out.println("isi rule yang akan digenerate : ");
		System.out.println(rule);

		KieBuilder kb = kieServices.newKieBuilder(kfs).buildAll();
		// ReleaseId releaseId =
		// kieServices.newReleaseId(kModule.getReleaseId().getGroupId(),
		// kModule.getReleaseId().getArtifactId(),
		// kModule.getReleaseId().getVersion());

		kieRepository.addKieModule(kb.getKieModule());
		// System.out.println(kieRepository.getKieModule(kb.getKieModule().getReleaseId()));

		kieContainer = kieServices.newKieContainer(kModule.getReleaseId());
		kieContainer = kieServices.newKieContainer(kb.getKieModule().getReleaseId());
		kieSession = getStatefulSession();
		statelessSession = getStatelessSession();

		System.out.println();
		// System.out.println("kieModule from server = " + kModule.toString());
		System.out.println("kieModule from java = " + kb.getKieModule().toString());
		System.out.println();

		// kb.buildAll(); // kieModule is automatically deployed to
		// KieRepository if successfully built.
		if (kb.getResults().hasMessages(Level.ERROR)) {
			throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
		} else {
			System.out.println("rule successfully created \n");
		}

		System.out.println("Connected to rule server...");
		System.out.println();
		// cek jumlah rule yang ada di rule engine jar
		cekRule();
	}

	public static void cekRule() {
		KieBaseConfiguration kieBaseConf = kieServices.newKieBaseConfiguration();
		kieBaseConf.setOption(EventProcessingOption.STREAM);
		KieBase kBase = kieContainer.newKieBase(kieBaseConf);
		System.out.println("Isi rule yang ada di engine : ");
		int i = 0;
		for (KiePackage a : kBase.getKiePackages()) {
			for (Rule r : a.getRules()) {
				System.out.println(i + 1 + ". KiePackage - Rule = " + a.getName() + " - " + r.getName());
				i++;
			}
		}
		System.out.println();
	}

	// public static void testLoad2() throws IOException {
	// KieServices ks = KieServices.Factory.get();
	//
	// KieFileSystem kfs = ks.newKieFileSystem();
	//
	//
	// Resource ex1Res =
	// ks.getResources().newFileSystemResource(getFile("named-kiesession"));
	//
	// Resource ex2Res =
	// ks.getResources().newFileSystemResource(getFile("kiebase-inclusion"));
	//
	//
	// ReleaseId rid = ks.newReleaseId("org.drools", "kiemodulemodel-example",
	// "6.0.0-SNAPSHOT");
	//
	// kfs.generateAndWritePomXML(rid);
	//
	//
	// KieModuleModel kModuleModel = ks.newKieModuleModel();
	//
	// kModuleModel.newKieBaseModel("kiemodulemodel")
	//
	// .addInclude("kiebase1")
	//
	// .addInclude("kiebase2")
	//
	// .newKieSessionModel("ksession6");
	//
	//
	// kfs.writeKModuleXML(kModuleModel.toXML());
	//
	// kfs.write("src/main/resources/kiemodulemodel/HAL6.drl", getRule());
	//
	//
	// KieBuilder kb = ks.newKieBuilder(kfs);
	//
	// kb.setDependencies(ex1Res, ex2Res);
	//
	// kb.buildAll(); // kieModule is automatically deployed to KieRepository if
	// successfully built.
	//
	// if (kb.getResults().hasMessages(Level.ERROR)) {
	//
	// throw new RuntimeException("Build Errors:\n" +
	// kb.getResults().toString());
	//
	// }
	//
	//
	// KieContainer kContainer = ks.newKieContainer(rid);
	//
	//
	// KieSession kSession = kContainer.newKieSession("ksession6");
	//
	// kSession.setGlobal("out", out);
	//
	//
	// Object msg1 = createMessage(kContainer, "Dave", "Hello, HAL. Do you read
	// me, HAL?");
	//
	// kSession.insert(msg1);
	//
	// kSession.fireAllRules();
	//
	//
	// Object msg2 = createMessage(kContainer, "Dave", "Open the pod bay doors,
	// HAL.");
	//
	// kSession.insert(msg2);
	//
	// kSession.fireAllRules();
	//
	//
	// Object msg3 = createMessage(kContainer, "Dave", "What's the problem?");
	//
	// kSession.insert(msg3);
	//
	// kSession.fireAllRules();
	// }

	public static final void main(String[] args) {
		try {
			// load up the knowledge base #3
			testLoad1();

			// LoadFactTestScenarioInsertNewPolicyContestFsc();

			kieSession.fireAllRules();

			// QueryResults results2 =
			// kieSession.getQueryResults("getObjectsOfContestDetail");
			// System.out.println();
			// for (QueryResultsRow row : results2) {
			// ContestDetail contestDetail = (ContestDetail) row.get("$result");
			// System.out.println("Contest detail object : " + "agentCode: " +
			// contestDetail.getAgentCode() + " "
			// + "policyNo: " + contestDetail.getPolicyNo() + " " +
			// "contestCode: "
			// + contestDetail.getContestCode() + " " + "api: " +
			// contestDetail.getApi());
			// }
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			kieSession.dispose();
		}
	}

	public static void LoadFactTestScenarioInsertNewPolicyContestFsc() {

		// load policy data from database
		for (int i = 1; i <= 10; i++) {
			Policy policy = new Policy();
			policy.setPolicyNo("PLC0" + (i));
			policy.setBillingChannel("PD");
			if (i == 1) {
				policy.setAgentNumber("AG01");
				policy.setProductCode("PLATINUM");
				policy.setInstallmentPremium(new BigDecimal(200000));
			} else if (i > 1 && i <= 5) {
				policy.setAgentNumber("AG01");
				policy.setProductCode("SILVER");
				policy.setInstallmentPremium(new BigDecimal(1000000));
			} else {
				policy.setAgentNumber("AG02");
				policy.setProductCode("GOLD");
				policy.setInstallmentPremium(new BigDecimal(1500000));
			}
			kieSession.insert(policy);
		}

		// search agent data in each policy
		Agent agent1 = new Agent();
		agent1.setAgentNumber("AG01");
		agent1.setAgentName("Gio");
		agent1.setAgentType("LEADER");
		kieSession.insert(agent1);

		Agent agent2 = new Agent();
		agent2.setAgentNumber("AG02");
		agent2.setAgentName("Shadrach");
		agent2.setAgentType("AGENT");
		kieSession.insert(agent2);

		// load all contest master data and contest parameter from database
		// CONTEST 1
		ContestMaster contestFsc = new ContestMaster();
		contestFsc.setContestCode("C0001");
		contestFsc.setContestName("Contest FSC Partnership Distribution Awards Night Event");
		contestFsc.setChannel("PD");
		contestFsc.setStartDate(new Date());
		contestFsc.setEndDate(new Date());
		contestFsc.setReviewingFlag("1");
		contestFsc.setReviewingEndDate(new Date());
		kieSession.insert(contestFsc);

		// PARAMETER CONTEST 1 START
		ContestParameter contestParamFsc1 = new ContestParameter();
		contestParamFsc1.setContestCode(contestFsc.getContestCode());
		contestParamFsc1.setParamCode("PRM01");
		contestParamFsc1.setOperator("==");
		contestParamFsc1.setValue("AGENT");
		kieSession.insert(contestParamFsc1);

		ContestParameter contestParamFsc2 = new ContestParameter();
		contestParamFsc2.setContestCode(contestFsc.getContestCode());
		contestParamFsc2.setParamCode("PRM02");
		contestParamFsc2.setOperator("==");
		contestParamFsc2.setValue("GOLD");
		kieSession.insert(contestParamFsc2);
		// PARAMETER CONTEST 1 END

		// CONTEST 2
		ContestMaster contestFsc2 = new ContestMaster();
		contestFsc2.setContestCode("C0002");
		contestFsc2.setContestName("Contest FSC Premier Club Gold");
		contestFsc2.setChannel("PD");
		contestFsc2.setStartDate(new Date());
		contestFsc2.setEndDate(new Date());
		contestFsc2.setReviewingFlag("1");
		contestFsc2.setReviewingEndDate(new Date());
		kieSession.insert(contestFsc2);

		// PARAMETER CONTEST 2 START
		ContestParameter contestParamFsc21 = new ContestParameter();
		contestParamFsc21.setContestCode(contestFsc2.getContestCode());
		contestParamFsc21.setParamCode("PRM01");
		contestParamFsc21.setOperator("==");
		contestParamFsc21.setValue("LEADER");
		kieSession.insert(contestParamFsc21);

		ContestParameter contestParamFsc22 = new ContestParameter();
		contestParamFsc22.setContestCode(contestFsc2.getContestCode());
		contestParamFsc22.setParamCode("PRM02");
		contestParamFsc22.setOperator("==");
		contestParamFsc22.setValue("SILVER");
		kieSession.insert(contestParamFsc22);
		// PARAMETER CONTEST 2 END

		kieSession.getAgenda().getAgendaGroup("contest_pd_fsc").setFocus();
	}

	String ruleTemplate = "" + "package com.giovanni.contest_test; \n" + "rule \"{contestName}\" \n"
			+ "date-effective \"{dateEffective}\" \n" + "date-expires \"{dateExpires}\" \n"
			+ "ruleflow-group \"{ruleFlowGroup}\" \n" + "when \n" + "{condition} \n" + "then \n" + "{body} \n"
			// + "ContestDetail cd = new ContestDetail(); \n"
			// + "cd.setContestCode($contestMaster.getContestCode()); \n"
			// + "cd.setPolicyNo($policy.getPolicyNo()); \n"
			// + "cd.setAgentCode($policy.getAgentNumber()); \n"
			// + "cd.setApi($policy.getInstallmentPremium()); \n"
			// + " insertLogical(cd); \n"
			+ "end";

	private String generateRule(String template) {
		String result = template;
		return result;
	}

	private static Date convertStringToDate(String dateString) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		Date date = null;
		try {
			date = formatter.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static StatelessKieSession getStatelessSession() {
		return kieContainer.newStatelessKieSession();
	}

	public static KieSession getStatefulSession() {
		return kieContainer.newKieSession();
		// return kContainer.newKieSession("ksession-rules");
	}

}
