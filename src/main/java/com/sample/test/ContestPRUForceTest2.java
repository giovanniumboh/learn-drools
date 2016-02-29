package com.sample.test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import com.giovanni.contest_test.Insured;
import com.giovanni.contest_test.Policy;

/**
 * This is a sample class to launch a rule.
 */
public class ContestPRUForceTest2 {
	static KieContainer kContainer = null;
	static KieSession kSession = null;
	static KieServices ks = null;
	static StatelessKieSession statelessSession = null;

	public static void testLoad3() throws IOException {
		// url ini menuju ke maven repository file jar rule yang kita tuju,
		// untuk pathnya bisa dilihat di business central menu Authoring ->
		// Artifact Repository
		String url = "http://localhost:8080//business-central/maven2/com/giovanni/contest-test/1.0/contest-test-1.0.jar";
		ks = KieServices.Factory.get();

		KieRepository kr = ks.getRepository();
		UrlResource urlResource = (UrlResource) ks.getResources().newUrlResource(url);

		// username dan password login ke business central
		urlResource.setUsername("emerio");
		urlResource.setPassword("emerio@123");
		// urlResource.setPassword("P@ssw0rd");
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

			LoadFactTestScenario2();

			kSession.fireAllRules();

			QueryResults results2 = kSession.getQueryResults("getObjectsOfContestDetail");
			System.out.println();
			for (QueryResultsRow row : results2) {
				ContestDetail contestDetail = (ContestDetail) row.get("$result");
				System.out.println("Contest detail object :   " + "agentCode: " + contestDetail.getAgentNumber()
						+ "     " + "policyNo: " + contestDetail.getPolicyNo() + "     " + "contestCode: "
						+ contestDetail.getContestCode() + "     " + "api: " + contestDetail.getApi());
			}

			QueryResults results3 = kSession.getQueryResults("getObjectsOfPolicy");
			System.out.println();
			for (QueryResultsRow row : results3) {
				Policy policy = (Policy) row.get("$result");
				System.out.println("Policy object :   " + "policyNo: " + policy.getPolicyNo());
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			kSession.dispose();
		}
	}

	public static void LoadFactTestScenario1() {

		// load dummy data polis
		// 1 polis platinum AG01
		// 2 polis silver AG01
		// 2 polis gold AG02
		for (int i = 1; i <= 5; i++) {
			Policy policy = new Policy();
			policy.setPolicyNo("PLC0" + (i));
			policy.setPolicyIssuedDate(new Date());
			if (i == 1) {
				policy.setAgentNumber("AG01");
				policy.setProductCode("PLATINUM");
				policy.setInstallmentPremium(new BigDecimal(200000));
				policy.setOwnerNumber("CLI01");
			} else if (i == 2) {
				policy.setAgentNumber("AG01");
				policy.setProductCode("SILVER");
				policy.setInstallmentPremium(new BigDecimal(1000000));
				policy.setOwnerNumber("CLI01");
			} else if (i == 3) {
				policy.setAgentNumber("AG01");
				policy.setProductCode("SILVER");
				policy.setInstallmentPremium(new BigDecimal(1000000));
				policy.setOwnerNumber("CLI01");
			} else {
				policy.setAgentNumber("AG02");
				policy.setProductCode("GOLD");
				policy.setInstallmentPremium(new BigDecimal(1500000));
				policy.setOwnerNumber("CLI02");
			}
			kSession.insert(policy);
		}

		// search agent data in each policy
		Agent agent1 = new Agent();
		agent1.setAgentNumber("AG01");
		agent1.setAgentName("Gio");
		agent1.setAgentType("AG");
		agent1.setClientNumber("CLI01");
		kSession.insert(agent1);

		Agent agent2 = new Agent();
		agent2.setAgentNumber("AG02");
		agent2.setAgentName("Shadrach");
		agent2.setAgentType("AM");
		agent2.setClientNumber("CLI01");
		kSession.insert(agent2);

		// load all contest master data and contest parameter from database
		// CONTEST 1
		ContestMaster contestFsc = new ContestMaster();
		contestFsc.setContestCode("C0001");
		contestFsc.setContestName("Contest Production Club Producer / Agent");
		contestFsc.setChannel("AGENCY");
		contestFsc.setStartDate(convertStringToDate("01-01-2016"));
		contestFsc.setEndDate(convertStringToDate("01-12-2016"));
		contestFsc.setReviewingFlag("1");
		contestFsc.setReviewingEndDate(convertStringToDate("31-03-2017"));
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
		contestParamFsc1.setValue("SILVER");
		kSession.insert(contestParamFsc1);

		ContestParameter contestParamFsc2 = new ContestParameter();
		contestParamFsc2.setContestCode(contestFsc.getContestCode());
		contestParamFsc2.setParamCode("PRM01");
		contestParamFsc2.setOperator("==");
		contestParamFsc2.setValue("GOLD");
		kSession.insert(contestParamFsc2);

		ContestParameter contestParamFsc3 = new ContestParameter();
		contestParamFsc3.setContestCode(contestFsc.getContestCode());
		contestParamFsc3.setParamCode("PRM02");
		contestParamFsc3.setOperator("==");
		contestParamFsc3.setValue("AM");
		kSession.insert(contestParamFsc3);

		// ContestParameter contestParamFsc31 = new ContestParameter();
		// contestParamFsc31.setContestCode(contestFsc.getContestCode());
		// contestParamFsc31.setParamCode("PRM02");
		// contestParamFsc31.setOperator("==");
		// contestParamFsc31.setValue("AG");
		// kSession.insert(contestParamFsc31);

		ContestParameter contestParamFsc4 = new ContestParameter();
		contestParamFsc4.setContestCode(contestFsc.getContestCode());
		contestParamFsc4.setParamCode("PRM04");
		contestParamFsc4.setOperator("==");
		contestParamFsc4.setAttribute6(true);
		kSession.insert(contestParamFsc4);
		// PARAMETER CONTEST 1 END

		// CONTEST 2

		ContestMaster contestFsc2 = new ContestMaster();
		contestFsc2.setContestCode("C0002");
		contestFsc2.setContestName("Contest FSC Premier Club Gold");
		contestFsc2.setChannel("AGENCY");
		contestFsc2.setStartDate(convertStringToDate("01-01-2016"));
		contestFsc2.setEndDate(convertStringToDate("01-06-2016"));
		contestFsc2.setReviewingFlag("1");
		contestFsc2.setReviewingEndDate(convertStringToDate("31-03-2017"));
		kSession.insert(contestFsc2);

		ContestParameter cp1 = new ContestParameter();
		cp1.setContestCode(contestFsc2.getContestCode());
		cp1.setParamCode("PRM01");
		cp1.setOperator("==");
		cp1.setValue("SILVER");
		kSession.insert(cp1);

		ContestParameter cp112 = new ContestParameter();
		cp112.setContestCode(contestFsc2.getContestCode());
		cp112.setParamCode("PRM01");
		cp112.setOperator("==");
		cp112.setValue("GOLD");
		kSession.insert(cp112);

		ContestParameter cp2 = new ContestParameter();
		cp2.setContestCode(contestFsc2.getContestCode());
		cp2.setParamCode("PRM01");
		cp2.setOperator("==");
		cp2.setValue("PLATINUM");
		kSession.insert(cp2);

		// PARAMETER CONTEST 2 START
		ContestParameter cp3 = new ContestParameter();
		cp3.setContestCode(contestFsc2.getContestCode());
		cp3.setParamCode("PRM02");
		cp3.setOperator("==");
		cp3.setValue("AG");
		kSession.insert(cp3);

		ContestParameter cp4 = new ContestParameter();
		cp4.setContestCode(contestFsc2.getContestCode());
		cp4.setParamCode("PRM02");
		cp4.setOperator("==");
		cp4.setValue("UM");
		kSession.insert(cp4);

		ContestParameter cp5 = new ContestParameter();
		cp5.setContestCode(contestFsc2.getContestCode());
		cp5.setParamCode("PRM03");
		cp5.setOperator("==");
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, 0, 4, 00, 00, 00);
		cp5.setAttribute4(calendar.getTime());
		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(2016, 1, 28, 23, 59, 59);
		cp5.setAttribute5(calendar2.getTime());
		kSession.insert(cp5);

		ContestParameter cp6 = new ContestParameter();
		cp6.setContestCode(contestFsc2.getContestCode());
		cp6.setParamCode("PRM04");
		cp6.setOperator("==");
		cp6.setAttribute6(true);
		kSession.insert(cp6);

		kSession.insert(new Insured());

		// PARAMETER CONTEST 2 END

		ContestParameter a = new ContestParameter();
		a.setContestCode("ANDOR1");
		a.setParamCode("PRM01");
		a.setOperator("TERTANGGUNGG");
		kSession.insert(a);

		ContestParameter b = new ContestParameter();
		b.setContestCode("ANDOR1");
		b.setParamCode("PRM02");
		b.setValue("PEMEGANG_POLISS");
		kSession.insert(b);

		ContestParameter c = new ContestParameter();
		c.setContestCode("ANDOR1");
		c.setParamCode("PRM03");
		c.setOperator("TERTANGGUNGG");
		c.setValue("PEMEGANG_POLISS");
		kSession.insert(c);

		// kSession.getAgenda().getAgendaGroup("coba").setFocus();
		kSession.getAgenda().getAgendaGroup("contest").setFocus();
		// kSession.getAgenda().getAgendaGroup("contest_pd_fsc").setFocus();
		// kSession.getAgenda().getAgendaGroup("contest_desicion_table").setFocus();
	}

	public static void LoadFactTestScenario2() {

		// load dummy data polis
		for (int i = 1; i <= 5; i++) {
			Policy policy = new Policy();
			policy.setPolicyNo("PLC0" + (i));
			policy.setPolicyIssuedDate(new Date());
			if (i == 1) {
				policy.setAgentNumber("AG01");
				policy.setOwnerNumber("OWN01");
			} else if (i == 2) {
				policy.setAgentNumber("AG01");
				policy.setOwnerNumber("OWN01");
			} else if (i == 3) {
				policy.setAgentNumber("AG01");
				policy.setOwnerNumber("OWN01");
			} else {
				policy.setAgentNumber("AG02");
				policy.setOwnerNumber("OWN02");
			}
			kSession.insert(policy);
		}

		Agent agent1 = new Agent();
		agent1.setAgentNumber("AG01");
		agent1.setAgentName("Gio");
		agent1.setAgentType("AG");
		agent1.setClientNumber("OWN01");
		kSession.insert(agent1);

		Agent agent2 = new Agent();
		agent2.setAgentNumber("AG02");
		agent2.setAgentName("Shadrach");
		agent2.setAgentType("AM");
		agent2.setClientNumber("OWN02");
		kSession.insert(agent2);

		for (int i = 0; i < 5; i++) {
			if (i % 2 == 0) {
				Insured insured1 = new Insured();
				insured1.setPolicyNumber("PLC0" + (i + 1));
				insured1.setLifeNumber("01");
				insured1.setLifeClientNumber("OWN01");
				kSession.insert(insured1);
			}
		}
		// Insured insured1 = new Insured();
		// insured1.setPolicyNumber("PLC01");
		// insured1.setLifeNumber("01");
		// insured1.setLifeClientNumber("OWN01");
		// kSession.insert(insured1);
		//
		// Insured insured2 = new Insured();
		// insured2.setPolicyNumber("PLC03");
		// insured2.setLifeNumber("01");
		// insured2.setLifeClientNumber("OWN01");
		// kSession.insert(insured2);

		// PARAMETER CONTEST 2 END

		ContestMaster cm1 = new ContestMaster();
		cm1.setContestCode("C0001");
		cm1.setChannel("AGENCY");
		cm1.setReviewingFlag("1");
		kSession.insert(cm1);

		ContestMaster cm2 = new ContestMaster();
		cm2.setContestCode("C0002");
		cm2.setChannel("AGENCY");
		cm2.setReviewingFlag("1");
		kSession.insert(cm2);

		for (int i = 0; i < 5; i++) {
			ContestDetail cd1 = new ContestDetail();
			if (i <= 3) {
				cd1.setContestCode(cm1.getContestCode());
			} else {
				cd1.setContestCode(cm2.getContestCode());
			}
			cd1.setPolicyNo("PLC0" + (i + 1));
			kSession.insert(cd1);
		}

		ContestParameter cp1a = new ContestParameter();
		cp1a.setContestCode(cm1.getContestCode());
		cp1a.setParamCode("PRM05");
		cp1a.setAttribute6(true);
		kSession.insert(cp1a);

		ContestParameter cp1b = new ContestParameter();
		cp1b.setContestCode(cm1.getContestCode());
		cp1b.setParamCode("PRM06");
		cp1b.setAttribute6(true);
		kSession.insert(cp1b);

		ContestParameter cp2a = new ContestParameter();
		cp2a.setContestCode(cm2.getContestCode());
		cp2a.setParamCode("PRM05");
		cp2a.setAttribute6(true);
		kSession.insert(cp2a);

		ContestParameter cp2b = new ContestParameter();
		cp2b.setContestCode(cm2.getContestCode());
		cp2b.setParamCode("PRM06");
		cp2b.setAttribute6(true);
		kSession.insert(cp2b);

		kSession.getAgenda().getAgendaGroup("contest_exclude").setFocus();
	}

	public static void LoadFactTestExclude() {

		// load dummy data polis
		// 1 polis platinum AG01
		// 2 polis silver AG01
		// 2 polis gold AG02
		for (int i = 1; i <= 5; i++) {
			Policy policy = new Policy();
			policy.setPolicyNo("PLC0" + (i));
			policy.setPolicyIssuedDate(new Date());
			if (i == 1) {
				policy.setAgentNumber("AG01");
				policy.setOwnerNumber("TERTANGGUNG");
			} else if (i == 2) {
				policy.setAgentNumber("AG01");
				policy.setProductCode("PEMEGANG POLIS");
			} else if (i == 3) {
				policy.setAgentNumber("AG01");
				policy.setOwnerNumber("TERTANGGUNG");
				policy.setProductCode("PEMEGANG POLIS");
			} else {
				policy.setAgentNumber("AG02");
				policy.setProductCode("BUKAN TERTANGGUNG ");
			}
			kSession.insert(policy);
		}

		kSession.insert(new Insured());

		// PARAMETER CONTEST 2 END

		ContestParameter a = new ContestParameter();
		a.setContestCode("ANDOR1");
		a.setParamCode("PRM01");
		a.setOperator("TERTANGGUNG");
		kSession.insert(a);

		ContestParameter b = new ContestParameter();
		b.setContestCode("ANDOR1");
		b.setParamCode("PRM02");
		b.setValue("PEMEGANG POLIS");
		kSession.insert(b);

		ContestParameter c = new ContestParameter();
		c.setContestCode("ANDOR1");
		c.setParamCode("PRM03");
		c.setOperator("TERTANGGUNG");
		c.setValue("PEMEGANG POLIS");
		kSession.insert(c);

		kSession.getAgenda().getAgendaGroup("coba4").setFocus();
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
