package com.sample.test;

import java.util.HashMap;
import java.util.Map;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;

import com.sample.model.Room;
import com.sample.model.Sprinkler;

/**
 * This is a sample class to launch a rule.
 */
public class RoomTest {

	static KieContainer kContainer = null;

	public static StatelessKieSession getStatelessSession() {
		return kContainer.newStatelessKieSession();
	}

	public static KieSession getStatefulSession() {
		return kContainer.newKieSession("kSession-rules");
	}

	public static final void main(String[] args) {
		KieSession kSession = null;
		try {
			// load up the knowledge base
			KieServices ks = KieServices.Factory.get();
			kContainer = ks.getKieClasspathContainer();
			kSession = getStatefulSession();

			String[] names = new String[] { "kitchen", "bedroom", "office", "livingroom" };

			Map<String, Room> name2room = new HashMap<String, Room>();

			for (String name : names) {
				Room room = new Room(name);
				name2room.put(name, room);
				kSession.insert(room);

				Sprinkler sprinkler = new Sprinkler(room);
				kSession.insert(sprinkler);

			}

			kSession.fireAllRules();

		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			kSession.dispose();
		}
	}

}
