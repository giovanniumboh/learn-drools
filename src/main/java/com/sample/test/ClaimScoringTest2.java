package com.sample.test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public class ClaimScoringTest2 {

	static KieContainer kContainer = null;
	static KieSession kSession = null;
	static KieServices ks = null;
	static StatelessKieSession statelessSession = null;

	static Set<String> listClaimParamCode = new HashSet<String>();
	static List<Parameter> listParameter = new ArrayList<Parameter>();

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
		// cekRule();
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
		List<Claim> listClaimUpdated = new ArrayList<Claim>();
		try {
			// load up the knowledge base #3
			testLoad3();

			LoadFactParam(new ArrayList<Claim>());
			kSession.fireAllRules();

			System.out.println("HASIL PERTAMA");
			QueryResults results1 = kSession.getQueryResults("getObjectsOfClaim");
			for (QueryResultsRow row : results1) {
				Claim claim = (Claim) row.get("$resultClaim");
				System.out.println(
						"Claim object :   " + "Contract Number: " + claim.getContractNumber() + "     " + "Claim Type: "
								+ claim.getClaimType() + "     " + "Total Scoring: " + claim.getTotalScoring());
				for (ClaimParameter claimParameter : claim.getListClaimParam()) {
					System.out.println(
							"Claim Parameter object :   " + "Contract Number: " + claimParameter.getContractNumber()
									+ "     " + "Param Code: " + claimParameter.getParamCode() + "     " + "Value: "
									+ claimParameter.getValue() + "     " + "Score: " + claimParameter.getScore());
				}
				System.out.println();
				listClaimUpdated.add(claim);
			}

			QueryResults results4 = kSession.getQueryResults("getObjectsOfParameter");
			for (QueryResultsRow row : results4) {
				Parameter parameter = (Parameter) row.get("$resultParameter");
				listParameter.add(parameter);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			kSession.dispose();
		}

		try {
			// load up the knowledge base #3
			testLoad3();

			LoadFactRating(listClaimUpdated);
			kSession.fireAllRules();

			System.out.println("HASIL KEDUA");
			QueryResults results3 = kSession.getQueryResults("getObjectsOfClaim");
			for (QueryResultsRow row : results3) {
				Claim claim = (Claim) row.get("$resultClaim");
				System.out.println(
						"Claim object :   " + "Contract Number: " + claim.getContractNumber() + "     " + "Claim Type: "
								+ claim.getClaimType() + "     " + "Total Scoring: " + claim.getTotalScoring());
				for (ClaimParameter claimParameter : claim.getListClaimParam()) {
					System.out.println("Claim Parameter object :   " + "Contract Number: "
							+ claimParameter.getContractNumber() + "     " + "Param Code: "
							+ claimParameter.getParamCode() + "     " + "Value: " + claimParameter.getValue() + "     "
							+ "     " + "Score: " + claimParameter.getScore());
				}
				System.out.println();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			kSession.dispose();
		}
	}

	public static void LoadFactParam(List<Claim> claimData) {
		for (int i = 1; i <= 3; i++) {
			Claim claim = new Claim();
			claim.setContractNumber("19027293" + (i));
			if (i == 1) {
				claim.setClaimType("HB");
				claim.setTotalScoring(0);
				claim.setClaimAmount(BigDecimal.valueOf(400000));

				ClaimParameter claimParam1 = new ClaimParameter();
				claimParam1.setContractNumber(claim.getContractNumber());
				claimParam1.setParamCode("P01");
				claimParam1.setValue("Y");
				claim.getListClaimParam().add(claimParam1);
				listClaimParamCode.add(claimParam1.getParamCode());
				kSession.insert(claimParam1);

				ClaimParameter claimParam2 = new ClaimParameter();
				claimParam2.setContractNumber(claim.getContractNumber());
				claimParam2.setParamCode("P02");
				claimParam2.setValue("Y");
				claim.getListClaimParam().add(claimParam2);
				listClaimParamCode.add(claimParam2.getParamCode());
				kSession.insert(claimParam2);

				ClaimParameter claimParam3 = new ClaimParameter();
				claimParam3.setContractNumber(claim.getContractNumber());
				claimParam3.setParamCode("P03");
				claimParam3.setValue("Y");
				claim.getListClaimParam().add(claimParam3);
				listClaimParamCode.add(claimParam3.getParamCode());
				kSession.insert(claimParam3);

				ClaimParameter claimParam4 = new ClaimParameter();
				claimParam4.setContractNumber(claim.getContractNumber());
				claimParam4.setParamCode("P04");
				claimParam4.setValue("Y");
				claim.getListClaimParam().add(claimParam4);
				listClaimParamCode.add(claimParam4.getParamCode());
				kSession.insert(claimParam4);

				ClaimParameter claimParam5 = new ClaimParameter();
				claimParam5.setContractNumber(claim.getContractNumber());
				claimParam5.setParamCode("P18");
				claimParam5.setValue("Y");
				claim.getListClaimParam().add(claimParam5);
				listClaimParamCode.add(claimParam5.getParamCode());
				kSession.insert(claimParam5);
			} else if (i > 1 && i <= 2) {
				claim.setClaimType("CC");
				claim.setTotalScoring(0);
				claim.setClaimAmount(BigDecimal.valueOf(50000000));

				ClaimParameter claimParam1 = new ClaimParameter();
				claimParam1.setContractNumber(claim.getContractNumber());
				claimParam1.setParamCode("P01");
				claimParam1.setValue("N");
				claim.getListClaimParam().add(claimParam1);
				listClaimParamCode.add(claimParam1.getParamCode());
				kSession.insert(claimParam1);

				ClaimParameter claimParam2 = new ClaimParameter();
				claimParam2.setContractNumber(claim.getContractNumber());
				claimParam2.setParamCode("P02");
				claimParam2.setValue("Y");
				claim.getListClaimParam().add(claimParam2);
				listClaimParamCode.add(claimParam2.getParamCode());
				kSession.insert(claimParam2);

				ClaimParameter claimParam3 = new ClaimParameter();
				claimParam3.setContractNumber(claim.getContractNumber());
				claimParam3.setParamCode("P03");
				claimParam3.setValue("N");
				claim.getListClaimParam().add(claimParam3);
				listClaimParamCode.add(claimParam3.getParamCode());
				kSession.insert(claimParam3);

				ClaimParameter claimParam4 = new ClaimParameter();
				claimParam4.setContractNumber(claim.getContractNumber());
				claimParam4.setParamCode("P04");
				claimParam4.setValue("Y");
				claim.getListClaimParam().add(claimParam4);
				listClaimParamCode.add(claimParam4.getParamCode());
				kSession.insert(claimParam4);

				ClaimParameter claimParam5 = new ClaimParameter();
				claimParam5.setContractNumber(claim.getContractNumber());
				claimParam5.setParamCode("P18");
				claimParam5.setValue("Y");
				claim.getListClaimParam().add(claimParam5);
				listClaimParamCode.add(claimParam5.getParamCode());
				kSession.insert(claimParam5);
			} else {
				claim.setClaimType("Death Claim");
				claim.setTotalScoring(0);
				claim.setClaimAmount(BigDecimal.valueOf(200000000));

				ClaimParameter claimParam1 = new ClaimParameter();
				claimParam1.setContractNumber(claim.getContractNumber());
				claimParam1.setParamCode("P01");
				claimParam1.setValue("N");
				claim.getListClaimParam().add(claimParam1);
				listClaimParamCode.add(claimParam1.getParamCode());
				kSession.insert(claimParam1);

				ClaimParameter claimParam2 = new ClaimParameter();
				claimParam2.setContractNumber(claim.getContractNumber());
				claimParam2.setParamCode("P02");
				claimParam2.setValue("Y");
				claim.getListClaimParam().add(claimParam2);
				listClaimParamCode.add(claimParam2.getParamCode());
				kSession.insert(claimParam2);

				ClaimParameter claimParam3 = new ClaimParameter();
				claimParam3.setContractNumber(claim.getContractNumber());
				claimParam3.setParamCode("P03");
				claimParam3.setValue("Y");
				claim.getListClaimParam().add(claimParam3);
				listClaimParamCode.add(claimParam3.getParamCode());
				kSession.insert(claimParam3);

				ClaimParameter claimParam4 = new ClaimParameter();
				claimParam4.setContractNumber(claim.getContractNumber());
				claimParam4.setParamCode("P18");
				claimParam4.setValue("Y");
				claim.getListClaimParam().add(claimParam4);
				listClaimParamCode.add(claimParam4.getParamCode());
				kSession.insert(claimParam4);
			}
			kSession.insert(claim);
		}

		for (String cp : listClaimParamCode) {
			Parameter parameter = new Parameter();
			parameter.setParamCode(cp);
			parameter.setScore(0);
			kSession.insert(parameter);
		}

		kSession.getAgenda().getAgendaGroup("param").setFocus();
	}

	public static void LoadFactRating(List<Claim> claimData) {
		for (Claim c : claimData) {
			kSession.insert(c);
			for (ClaimParameter cp : c.getListClaimParam()) {
				cp.setProcessed(false);
				kSession.insert(cp);
			}
		}

		for (Parameter cp : listParameter) {
			Parameter parameter = new Parameter();
			parameter.setParamCode(cp.getParamCode());
			parameter.setScore(cp.getScore());
			kSession.insert(parameter);
		}

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
