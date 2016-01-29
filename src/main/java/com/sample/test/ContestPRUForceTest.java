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
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;

import com.giovanni.contest_test.Agent;
import com.giovanni.contest_test.ContestDetail;
import com.giovanni.contest_test.ContestMaster;
import com.giovanni.contest_test.ContestParameter;
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

		System.out.println("Connected to rule engine...");
		System.out.println();
		// cek jumlah rule yang ada di rule engine jar
		cekRule();
	}

	public static void cekRule() {
		KieBaseConfiguration kieBaseConf = ks.newKieBaseConfiguration();
		kieBaseConf.setOption(EventProcessingOption.STREAM);
		KieBase kBase = kContainer.newKieBase(kieBaseConf);
		System.out.println("Isi rule yang ada engine : ");
		int i = 0;
		for (KiePackage a : kBase.getKiePackages()) {
			for (Rule r : a.getRules()) {
				System.out.println(i + 1 + ". KiePackage - Rule = " + a.getName() + " - " + r.getName());
				i++;
			}
		}
		System.out.println();
	}

	public static final void main(String[] args) {
		try {
			// load up the knowledge base #3
			testLoad3();

			LoadFactTestScenarioInsertNewPolicyContestFsc();

			kSession.fireAllRules();

			QueryResults results2 = kSession.getQueryResults("getObjectsOfContestDetail");
			System.out.println();
			for (QueryResultsRow row : results2) {
				ContestDetail contestDetail = (ContestDetail) row.get("$result");
				System.out.println("Contest detail object :   " + "agentCode: " + contestDetail.getAgentNumber()
						+ "     " + "policyNo: " + contestDetail.getPolicyNo() + "     " + "contestCode: "
						+ contestDetail.getContestCode() + "     " + "api: " + contestDetail.getApi());
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			kSession.dispose();
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
			} else if (i > 1 && i <= 3) {
				policy.setAgentNumber("AG01");
				policy.setProductCode("SILVER");
				policy.setInstallmentPremium(new BigDecimal(1000000));
			} else {
				policy.setAgentNumber("AG02");
				policy.setProductCode("GOLD");
				policy.setInstallmentPremium(new BigDecimal(1500000));
			}
			kSession.insert(policy);
		}

		// search agent data in each policy
		Agent agent1 = new Agent();
		agent1.setAgentNumber("AG01");
		agent1.setAgentName("Gio");
		agent1.setAgentType("LEADER");
		kSession.insert(agent1);

		Agent agent2 = new Agent();
		agent2.setAgentNumber("AG02");
		agent2.setAgentName("Shadrach");
		agent2.setAgentType("AGENT");
		kSession.insert(agent2);

		// load all contest master data and contest parameter from database
		// CONTEST 1
		ContestMaster contestFsc = new ContestMaster();
		contestFsc.setContestCode("C0001");
		contestFsc.setContestName("Contest FSC Partnership Distribution Awards Night Event");
		contestFsc.setChannel("PD");
		contestFsc.setStartDate(convertStringToDate("01-01-2016"));
		contestFsc.setEndDate(convertStringToDate("01-12-2016"));
		contestFsc.setReviewingFlag("1");
		contestFsc.setReviewingEndDate(convertStringToDate("01-01-2017"));
		kSession.insert(contestFsc);

		String master = "";
		if (contestFsc.getChannel() != null) {
			master = "channel == " + "\"" + contestFsc.getChannel() + "\", ";
			// System.out.println(master);
		}
		if (contestFsc.getReviewingFlag() != null) {
			master = master + "reviewingFlag == " + "\"" + contestFsc.getReviewingFlag() + "\" ";
			// System.out.println(master);
		}

		// PARAMETER CONTEST 1 START
		ContestParameter contestParamFsc1 = new ContestParameter();
		contestParamFsc1.setContestCode(contestFsc.getContestCode());
		contestParamFsc1.setParamCode("PRM01");
		contestParamFsc1.setOperator("==");
		contestParamFsc1.setValue("AGENT");
		kSession.insert(contestParamFsc1);

		ContestParameter contestParamFsc2 = new ContestParameter();
		contestParamFsc2.setContestCode(contestFsc.getContestCode());
		contestParamFsc2.setParamCode("PRM02");
		contestParamFsc2.setOperator("==");
		contestParamFsc2.setValue("GOLD");
		kSession.insert(contestParamFsc2);
		// PARAMETER CONTEST 1 END

		// CONTEST 2
		ContestMaster contestFsc2 = new ContestMaster();
		contestFsc2.setContestCode("C0002");
		contestFsc2.setContestName("Contest FSC Premier Club Gold");
		contestFsc2.setChannel("PD");
		contestFsc2.setStartDate(convertStringToDate("01-01-2016"));
		contestFsc2.setEndDate(convertStringToDate("01-06-2016"));
		contestFsc2.setReviewingFlag("1");
		contestFsc2.setReviewingEndDate(convertStringToDate("01-01-2017"));
		kSession.insert(contestFsc2);

		// PARAMETER CONTEST 2 START
		ContestParameter contestParamFsc21 = new ContestParameter();
		contestParamFsc21.setContestCode(contestFsc2.getContestCode());
		contestParamFsc21.setParamCode("PRM01");
		contestParamFsc21.setOperator("==");
		contestParamFsc21.setValue("LEADER");
		kSession.insert(contestParamFsc21);

		ContestParameter contestParamFsc22 = new ContestParameter();
		contestParamFsc22.setContestCode(contestFsc2.getContestCode());
		contestParamFsc22.setParamCode("PRM02");
		contestParamFsc22.setOperator("==");
		contestParamFsc22.setValue("SILVER");
		kSession.insert(contestParamFsc22);
		// PARAMETER CONTEST 2 END

		// kSession.getAgenda().getAgendaGroup("contest_pd_fsc").setFocus();
		kSession.getAgenda().getAgendaGroup("contest_desicion_table").setFocus();
		// kSession.getAgenda().getAgendaGroup("contest_desicion_table_2").setFocus();
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
		return kContainer.newStatelessKieSession();
	}

	public static KieSession getStatefulSession() {
		return kContainer.newKieSession();
		// return kContainer.newKieSession("ksession-rules");
	}

}
