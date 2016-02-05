package com.sample.test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

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

import com.giovanni.claim_scoring_test.Claim;
import com.giovanni.claim_scoring_test.ClaimParameter;
import com.giovanni.claim_scoring_test.Parameter;

/**
 * This is a sample class to launch a rule.
 */
public class ClaimScoringTest {

	static KieContainer kContainer = null;
	static KieSession kSession = null;
	static KieServices ks = null;
	static StatelessKieSession statelessSession = null;

	public static void testLoad3() throws IOException {
		String url = "http://localhost:8080//business-central/maven2/com/giovanni/claim-scoring-test/1.0/claim-scoring-test-1.0.jar";
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

			// LoadFactTestScenario1();
			// kSession.fireAllRules();
			LoadFactTestScenario2();
			kSession.fireAllRules();

			QueryResults results2 = kSession.getQueryResults("getObjectsOfClaim");
			System.out.println();
			for (QueryResultsRow row : results2) {
				Claim claim = (Claim) row.get("$result");
				System.out.println(
						"Claim object :   " + "Contract Number: " + claim.getContractNumber() + "     " + "Claim Type: "
								+ claim.getClaimType() + "     " + "Total Scoring: " + claim.getTotalScoring());
			}

			QueryResults results3 = kSession.getQueryResults("getObjectsOfClaimParameter");
			System.out.println();
			for (QueryResultsRow row : results3) {
				ClaimParameter claimParameter = (ClaimParameter) row.get("$result");
				System.out.println("Claim Parameter object :   " + "Contract Number: "
						+ claimParameter.getContractNumber() + "     " + "Param Code: " + claimParameter.getParamCode()
						+ "     " + "Total: " + claimParameter.getScore());
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			kSession.dispose();
		}
	}

	public static void LoadFactTestScenario1() {

		// load policy data from database
		for (int i = 1; i <= 2; i++) {
			Claim claim = new Claim();
			claim.setContractNumber("19027293" + (i));
			if (i == 1) {
				claim.setClaimType("HB");
				claim.setTotalScoring(0);
				claim.setClaimAmount(BigDecimal.valueOf(400000));
			} else if (i > 1 && i <= 2) {
				claim.setClaimType("CC");
				claim.setTotalScoring(0);
				claim.setClaimAmount(BigDecimal.valueOf(50000000));
			}
			kSession.insert(claim);
		}

		// search agent data in each policy
		Parameter param1 = new Parameter();
		param1.setParamCode("P01");
		param1.setParamName("Black Hospital Flag");
		param1.setScore(0);
		kSession.insert(param1);

		Parameter param2 = new Parameter();
		param2.setParamCode("P02");
		param2.setParamName("Black Agent Flag");
		param2.setScore(0);
		kSession.insert(param2);

		Parameter param3 = new Parameter();
		param3.setParamCode("P18");
		param3.setParamName("Total Claim Amount");
		param3.setScore(0);
		kSession.insert(param3);

		// PARAMETER CONTEST 1 START
		ClaimParameter claimParam1 = new ClaimParameter();
		claimParam1.setContractNumber("190272931");
		claimParam1.setParamCode("P01");
		claimParam1.setValue("Y");
		kSession.insert(claimParam1);

		ClaimParameter claimParam2 = new ClaimParameter();
		claimParam2.setContractNumber("190272931");
		claimParam2.setParamCode("P02");
		claimParam2.setValue("Y");
		kSession.insert(claimParam2);

		ClaimParameter claimParam3 = new ClaimParameter();
		claimParam3.setContractNumber("190272932");
		claimParam3.setParamCode("P01");
		claimParam3.setValue("Y");
		kSession.insert(claimParam3);

		ClaimParameter claimParam4 = new ClaimParameter();
		claimParam4.setContractNumber("190272932");
		claimParam4.setParamCode("P18");
		claimParam4.setValue("Y");
		kSession.insert(claimParam4);

		kSession.getAgenda().getAgendaGroup("param").setFocus();
	}

	public static void LoadFactTestScenario2() {

		// load policy data from database
		for (int i = 1; i <= 2; i++) {
			Claim claim = new Claim();
			claim.setContractNumber("19027293" + (i));
			if (i == 1) {
				claim.setClaimType("HB");
				claim.setTotalScoring(175);
				claim.setClaimAmount(BigDecimal.valueOf(400000));
			} else if (i > 1 && i <= 2) {
				claim.setClaimType("CC");
				claim.setTotalScoring(75);
				claim.setClaimAmount(BigDecimal.valueOf(10000000));
			}
			kSession.insert(claim);
		}

		// search agent data in each policy
		Parameter param1 = new Parameter();
		param1.setParamCode("P01");
		param1.setParamName("Black Hospital Flag");
		param1.setScore(0);
		kSession.insert(param1);

		Parameter param2 = new Parameter();
		param2.setParamCode("P02");
		param2.setParamName("Black Agent Flag");
		param2.setScore(0);
		kSession.insert(param2);

		Parameter param3 = new Parameter();
		param3.setParamCode("P18");
		param3.setParamName("Total Claim Amount");
		param3.setScore(5);
		kSession.insert(param3);

		// PARAMETER CONTEST 1 START
		ClaimParameter claimParam1 = new ClaimParameter();
		claimParam1.setContractNumber("190272931");
		claimParam1.setParamCode("P01");
		claimParam1.setValue("Y");
		kSession.insert(claimParam1);

		ClaimParameter claimParam2 = new ClaimParameter();
		claimParam2.setContractNumber("190272931");
		claimParam2.setParamCode("P02");
		claimParam2.setValue("Y");
		kSession.insert(claimParam2);

		ClaimParameter claimParam3 = new ClaimParameter();
		claimParam3.setContractNumber("190272932");
		claimParam3.setParamCode("P01");
		claimParam3.setValue("Y");
		kSession.insert(claimParam3);

		ClaimParameter claimParam4 = new ClaimParameter();
		claimParam4.setContractNumber("190272932");
		claimParam4.setParamCode("P18");
		claimParam4.setValue("Y");
		kSession.insert(claimParam4);

		kSession.getAgenda().getAgendaGroup("rating").setFocus();
	}

	public static StatelessKieSession getStatelessSession() {
		return kContainer.newStatelessKieSession();
	}

	public static KieSession getStatefulSession() {
		return kContainer.newKieSession();
		// return kContainer.newKieSession("ksession-rules");
	}

}
