package com.sample.util;

import java.io.Serializable;

import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.runtime.manager.cdi.qualifier.Singleton;

//@Singleton
public class BRMSUtil implements Serializable {

	private static final long serialVersionUID = 1562882558996412866L;

	private static KieContainer kContainer = null;

	public static void main(String[] args) {
		KieServices kServices = KieServices.Factory.get();

		ReleaseId releaseId = kServices.newReleaseId("com.giovanni", "contest-test", "1.0");

		kContainer = kServices.newKieContainer(releaseId);

		KieScanner kScanner = kServices.newKieScanner(kContainer);

		// Start the KieScanner polling the maven repository every 10 seconds
		System.out.println("Starting KieScanner...");
		System.out.println();
		kScanner.start(10000L);
		System.out.println("Started KieScanner sucessfully...");
		System.out.println();
	}

	public BRMSUtil() {

		KieServices kServices = KieServices.Factory.get();

		ReleaseId releaseId = kServices.newReleaseId("com.giovanni", "contest-test", "1.0");

		kContainer = kServices.newKieContainer(releaseId);

		KieScanner kScanner = kServices.newKieScanner(kContainer);

		// Start the KieScanner polling the maven repository every 10 seconds
		System.out.println("Starting KieScanner...");
		System.out.println();
		kScanner.start(10000L);
		System.out.println("Started KieScanner sucessfully...");
		System.out.println();
	}

	public StatelessKieSession getStatelessSession() {
		return kContainer.newStatelessKieSession();
	}

	public KieSession getStatefulSession() {
		return kContainer.newKieSession();
	}

}
