package com.carlncarl.isi.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morfologik.stemming.PolishStemmer;
import morfologik.stemming.WordData;

public class Executor {

	private static final String RESULT_DONT_UNDERSTAND = "Nie rozumiem.";
	private static final String RESULT_UNDERSTAND = "Rozumiem.";
	private static final String INCORRECT_INPUT = "Wprowadzony tekst nie mo¿e byæ pusty.";
	private static final String RESULT_ALREADY_KNOWN = "Wprowadzana wiedza jest ju¿ posiadana.";
	public static final String ANSWER_DO_NOT_KNOW = "Nie wiem";
	public static final String ANSWER_YES = "Tak";

	private UI frame;
	private LinkedHashMap<String, Action> rules;
	private ArrayList<Fact> facts;
	private HashMap<String, String> initialWords;
	private boolean debugMode;
	ArrayList<String> myQuest = new ArrayList<>();

	public Executor(UI ui) {
		this.frame = ui;
		facts = new ArrayList<Fact>();
		rules = new LinkedHashMap<>();
		initialWords = new HashMap<String, String>();
		addRules();
	}

	private String queryInterpretation(String inputUser) {
		if (inputUser.equals("")) {
			return INCORRECT_INPUT;
		}
		String inputTemp = inputUser;
		inputTemp = inputTemp.trim().toLowerCase();
		if (inputTemp.endsWith("?") || inputTemp.endsWith(".")) {
			inputTemp = inputTemp.substring(0, inputTemp.length() - 1);
		}

		Iterator<String> iter = rules.keySet().iterator();
		while (iter.hasNext()) {
			String patternString = iter.next();
			Pattern p = Pattern.compile(patternString);
			Matcher m = p.matcher(inputTemp);
			if (m.matches()) {
				String[] groups = new String[m.groupCount()];
				for (int i = 1; i <= m.groupCount(); i++) {
					groups[i - 1] = m.group(i);
				}
				String result = "";
				Action action = rules.get(patternString);
				action.setGroups(groups);

				switch (action.getMethod()) {
				case Action.ALL_PATHS:
					action.setQuestion(inputUser);
					// result = generateComplexAnswer(action, groups);
					result = getObjects(action, groups);

					break;
				case Action.ONE_PATH:
					action.setQuestion(inputUser);
					result = generateSimpleAnswer(action, groups);
					break;
				case Action.ADD_FACT:
					result = addFact(groups, action.getArg());
					break;
				default:
					break;
				}
				return result;
			}
		}
		return RESULT_DONT_UNDERSTAND;
	}

	private String addFact(String[] groups, String phrases) {
		String[] splittedPhrases = phrases.split("\\|");

		String[] phraseIn = new String[3];
		char[] spl = splittedPhrases[0].toCharArray();
		for (int i = 0; i < spl.length; i++) {
			phraseIn[i] = Character.toString(spl[i]);
		}

		String first = groups[Integer.parseInt(phraseIn[0])];
		String second = groups[Integer.parseInt(phraseIn[2])];

		// groups[Integer.parseInt(phraseIn[0])] = getStems(groups[Integer
		// .parseInt(phraseIn[0])]);
		// groups[Integer.parseInt(phraseIn[2])] = getStems(groups[Integer
		// .parseInt(phraseIn[2])]);

		initialWords.put(groups[Integer.parseInt(phraseIn[0])], first);
		initialWords.put(groups[Integer.parseInt(phraseIn[2])], second);

		String result = null;
		String type = getType(groups[1]);

		for (String phrase : splittedPhrases) {
			spl = phrase.toCharArray();
			String[] phraseP = new String[3];
			for (int i = 0; i < spl.length; i++) {
				phraseP[i] = Character.toString(spl[i]);
			}
			String typeA = null;
			String typeB = null;
			String rel = String.valueOf(phraseP[1]);

			char relC = rel.charAt(0);
			if (relC >= 'a' && relC <= 'z') {
				typeA = type;
			} else if (relC >= 'A' && relC <= 'Z') {
				typeB = type;
			}

			ObjectInfo a = new ObjectInfo(groups[Integer.parseInt(phraseP[0])],
					typeA);
			ObjectInfo b = new ObjectInfo(groups[Integer.parseInt(phraseP[2])],
					typeB);

			Fact fact = new Fact(a, rel, b);

			if (!facts.contains(fact)) {
				facts.add(fact);
				debug("Add Fact: " + fact.toString());
				result = RESULT_UNDERSTAND;
			} else {
				result = RESULT_ALREADY_KNOWN;
			}
		}

		return result;
	}

	private String getType(String group) {
		String type = ObjectInfo.MALE;
		if (group.contains("³a ")) {
			type = ObjectInfo.FEMALE;
		}
		return type;
	}

	public void addFact(String[] row) {
		ObjectInfo a;
		ObjectInfo b = new ObjectInfo(row[2], null);
		String rel = row[1];

		String[] spl = row[0].split("|");
		if (spl.length > 1) {
			a = new ObjectInfo(spl[0], spl[1]);
		} else {
			a = new ObjectInfo(row[0], null);
		}

		Fact fact = new Fact(a, rel, b);

		if (!facts.contains(fact)) {
			facts.add(fact);
		}

		// dodanie odwrtornej zale¿noœci
		fact = new Fact(b, rel.toUpperCase(), a);
		if (!facts.contains(fact)) {
			facts.add(fact);
		}

	}

	// Methods of role USERINTERFACE
	public void setDebugMode(boolean selected) {
		debugMode = selected;
	}

	public String receiveQuery(String userInput) {
		return queryInterpretation(userInput);
	}

	public String generateComplexAnswer(Action action, String[] groups) {
		ObjectInfo inputObject = null;
		ObjectInfo start;

		start = new ObjectInfo(groups[groups.length - 1], null);

		ObjectInfo first = start;

		LinkedList<Fact> used = new LinkedList<Fact>();
		LinkedList<ObjectInfo> matches = new LinkedList<ObjectInfo>();
		LinkedList<ObjectInfo> q = new LinkedList<ObjectInfo>();

		while (true) {
			int i = 0;
			for (Fact fact : facts) {
				if (used.contains(fact)
						|| !fact.getA().equals(start)
						|| !fact.getRel()
								.equals(action.getArg().charAt(i) + "")) {
					continue;
				}

				if (!fact.getB().equals(first)
						&& !matches.contains(fact.getB())) {
					if (inputObject == null) {
						inputObject = fact.getA();
					}
					matches.add(fact.getB());
					used.add(fact);
					q.add(fact.getB());
				}
			}

			if (q.size() == 0) {
				break;
			}
			start = q.removeLast();

		}
		if (inputObject == null) {
			inputObject = start;
		}

		return prepareAnswer(action, inputObject, matches);
	}

	public String getObjects(Action action, String[] groups) {

		LinkedList<ObjectInfo> matches = new LinkedList<ObjectInfo>();
		LinkedList<ObjectInfo> nextStep = new LinkedList<>();

		ObjectInfo start = new ObjectInfo(groups[groups.length - 1], null);
		nextStep.add(start);
		ObjectInfo inputObject = null;

		for (int i = 0; i < action.getArg().length(); i++) {
			String rel = action.getArg().charAt(i) + "";
			LinkedList<ObjectInfo> values = new LinkedList<>();
			for (ObjectInfo objectInfo : nextStep) {
				inputObject = getObjectsMatches(rel, objectInfo, inputObject,
						values);
			}

			if (i + 1 == action.getArg().length()) {
				matches.addAll(values);
			} else {
				nextStep.addAll(values);
			}
		}

		return prepareAnswer(action, inputObject, matches);

	}

	public ObjectInfo getObjectsMatches(String rel, ObjectInfo start,
			ObjectInfo inputObject, LinkedList<ObjectInfo> matches) {

		ObjectInfo first = start;

		LinkedList<Fact> used = new LinkedList<Fact>();
		LinkedList<ObjectInfo> q = new LinkedList<ObjectInfo>();

		while (true) {
			int i = 0;
			for (Fact fact : facts) {
				if (used.contains(fact) || !fact.getA().equals(start)
						|| !fact.getRel().equals(rel)) {
					continue;
				}

				if (!fact.getB().equals(first)
						&& !matches.contains(fact.getB())) {
					if (inputObject == null) {
						inputObject = fact.getA();
					}
					matches.add(fact.getB());
					used.add(fact);
					q.add(fact.getB());
				}
			}

			if (q.size() == 0) {
				break;
			}
			start = q.removeLast();

		}
		if (inputObject == null) {
			inputObject = start;
		}

		return inputObject;
	}

	public String prepareAnswer(Action action, ObjectInfo start,
			LinkedList<ObjectInfo> matches) {
		String linkWord = null;

		String userForm = start.getValue();

		if (action.getArg().equals("p") || action.getArg().equals("P")) {
			linkWord = " posiada: ";
		} else if (action.getArg().equals("r")) {
			linkWord = " jest równowa¿ny ";
		} else if (action.getArg().equals("N")) {
			linkWord = " jest ";
		} else if (action.getArg().equals("w")) {

			if (start.getType() != null
					&& start.getType().equals(ObjectInfo.FEMALE)) {
				linkWord = " zagra³a w ";
			} else {
				linkWord = " zagra³ w ";
			}

		}

		String answer = "";
		if (matches.size() == 0) {

			String questionAsAnswer = action.getQuestion().substring(0, 2)
					.toLowerCase()
					+ action.getQuestion().substring(1);
			answer = ANSWER_DO_NOT_KNOW + " "
					+ questionAsAnswer.replace("?", ".");

		} else {

			String[] aGroups = action.getGroups();
			if (aGroups.length == 2) {

				answer = aGroups[aGroups.length - 1];
				answer += " " + aGroups[aGroups.length - 2];

			} else if (action.getArg().equals("w")
					|| action.getArg().equals("r")
					|| action.getArg().equals("T")
					|| action.getArg().equals("rt")
					|| action.getArg().equals("wR")
					|| action.getArg().equals("wt")
					|| action.getArg().equals("rW")) {
				answer = aGroups[aGroups.length - 1];
				answer += " " + aGroups[aGroups.length - 2];

				String startQuestion = aGroups[aGroups.length - 3];

				String[] spl = startQuestion.split(" ");
				ArrayList<String> toAns = new ArrayList<String>();

				for (int i = 0; i < spl.length; i++) {
					String word = spl[i];
					String stem = getStems(word);
					if (stem.equals("jaki")) {
						continue;
					}
					toAns.add(word);
				}
				for (String word : toAns) {
					answer += " " + word;
				}
			} else if (action.getArg().equals("t")
					|| action.getArg().equals("R")
					|| action.getArg().equals("W")
					|| action.getArg().equals("TR")
					|| action.getArg().equals("TW")) {

				answer = aGroups[aGroups.length - 2];
				answer += " " + aGroups[aGroups.length - 1];
				answer += " " + aGroups[aGroups.length - 3];

				if (aGroups.length > 3) {
					String startQuestion = aGroups[aGroups.length - 4];

					String[] spl = startQuestion.split(" ");
					ArrayList<String> toAns = new ArrayList<String>();

					for (int i = 0; i < spl.length; i++) {
						String word = spl[i];
						String stem = getStems(word);
						if (stem.equals("jaki")) {
							continue;
						}
						toAns.add(word);
					}
					for (String word : toAns) {
						answer += " " + word;
					}
				}
			}

			if (matches.size() == 1) {
				answer += " " + matches.getFirst();
			} else {
				// answer = userForm
				// + ((action.getArg().equals("P") ? " posiadaj¹ "
				// : linkWord));
				String ma = " ";
				for (ObjectInfo match : matches) {
					if (ma.length() > 1) {
						ma += ", ";
					}
					ma += match;
				}
				answer += ma;
			}
		}
		return answer;
	}

	public String generateSimpleAnswer(Action action, String[] groups) {
		String[] splRules = action.getArg().split("\\|");
		String ruleP = splRules[0];
		groups[Integer.parseInt(ruleP.substring(0, 1))] = getStems(groups[Integer
				.parseInt(ruleP.substring(0, 1))]);
		groups[Integer.parseInt(ruleP.substring(ruleP.length() - 1))] = getStems(groups[Integer
				.parseInt(ruleP.substring(ruleP.length() - 1))]);
		ArrayList<String> ans = new ArrayList<String>();
		for (String rule : splRules) {
			String pattern = rule.substring(1, rule.length() - 1);
			ObjectInfo start = new ObjectInfo(groups[Integer.parseInt(rule
					.substring(0, 1))], null);
			ObjectInfo stop = new ObjectInfo(groups[Integer.parseInt(ruleP
					.substring(ruleP.length() - 1))], null);
			ans = new ArrayList<String>();

			path(pattern, start, stop, new LinkedList<Fact>(), ans, "", " ");
			if (ans.size() > 0)
				break;
		}
		String answer;
		if (ans.size() > 0) {
			answer = ANSWER_YES;
		} else {
			answer = ANSWER_DO_NOT_KNOW;
		}
		return answer;

	}

	public String prepareAnswer(Action action, String[] groups,
			LinkedList<ObjectInfo> matches) {
		String linkWord = null;

		String userForm;

		if (action.getArg().equals("p") || action.getArg().equals("r")) {
			userForm = groups[1];
		} else {
			userForm = groups[0];
		}

		if (action.getArg().equals("p") || action.getArg().equals("P")) {
			linkWord = " posiada: ";
		} else if (action.getArg().equals("r")) {
			linkWord = " jest równowa¿ny ";
		} else if (action.getArg().equals("N")) {
			linkWord = " jest: ";
		} else if (action.getArg().equals("w")) {
			linkWord = " zagra³: ";
		}
		String answer;
		if (matches.size() == 0) {
			answer = ANSWER_DO_NOT_KNOW;
		} else if (matches.size() == 1) {
			answer = userForm + linkWord + matches.getFirst();
		} else {
			answer = userForm
					+ ((action.getArg().equals("P") ? " posiadaj¹ " : linkWord));
			String ma = "";
			for (ObjectInfo match : matches) {
				if (ma.length() > 0) {
					ma += ", ";
				}
				ma += match;
			}
			answer += ma;
		}
		return answer;
	}

	private void path(String pattern, ObjectInfo start, ObjectInfo end,
			LinkedList<Fact> before, ArrayList<String> ans, String sofar,
			String indent) {
		debug(indent + " (" + start + ") -> (" + end + ")");

		LinkedList<Fact> used = new LinkedList<Fact>();

		used.addAll(before);
		if (indent.length() > 20) {
			return;
		}
		for (Fact fact : facts) {
			if (used.contains(fact) || !fact.getA().equals(start)) {
				continue;
			}
			int sts = okSoFar(pattern, sofar + fact.getRel());
			if (sts == 0) {
				continue;
			}
			used.add(fact);

			if (fact.getB().equals(end)) {
				if (sts == 2) {
					ans.add(sofar + fact.getRel());
				}
			} else {
				path(pattern, fact.getB(), end, used, ans,
						sofar + fact.getRel(), indent + "  ");
			}

		}

	}

	private int okSoFar(String a, String b) {
		int ans = 2;

		while (a.length() > 0) {
			if (Pattern.matches("^" + a + "$", b)) {
				return ans;
			}
			if (a.endsWith("*"))
				a = a.substring(0, a.length() - 2);
			else
				a = a.substring(0, a.length() - 1);
			// ans = 1;

		}
		return 0;
	}

	private void addRules() {
		/*
		 * rules.put("co (posiada|ma) (.*)", new Action(Action.ALL_PATHS, "p"));
		 * 
		 * rules.put("co (odpowiada|jest równowa¿n[y|a|e]) (.*)", new Action(
		 * Action.ALL_PATHS, "r"));
		 * 
		 * rules.put("co jest (.*)", new Action(Action.ALL_PATHS, "N"));
		 * 
		 * rules.put("jakie obiekty posiadaj¹ (.*)", new
		 * Action(Action.ALL_PATHS, "P"));
		 * 
		 * rules.put("kto posiada (.*)", new Action(Action.ALL_PATHS, "P"));
		 * 
		 * rules.put("czy ka¿d[y|a|e] (.*) (to|jest) (.*)", new Action(
		 * Action.ONE_PATH, "0r*j*2"));
		 * 
		 * rules.put("czy (.*) (to|jest) (.*)", new Action(Action.ONE_PATH,
		 * "0r*nj*2|0r*j*2"));
		 * 
		 * rules.put("czy ka¿d[y|a|e] (.*) (posiada|ma) (.*)", new Action(
		 * Action.ONE_PATH, "0r*p*j*2"));
		 * 
		 * rules.put("czy któr[y|a|e]kolwiek (.*) (posiada|ma) (.*)", new
		 * Action( Action.ONE_PATH, "0J*Nr*pj*2"));
		 * 
		 * rules.put("czy (.*) (posiada|ma) (.*)", new Action(Action.ONE_PATH,
		 * "0r*nj*pj*2|0J*Nr*pj*2|0r*p*j*2"));
		 * 
		 * rules.put("(ka¿d[y|a|e]|dowoln[y|a|e]) (.*) (to|jest podzbiorem) (.*)"
		 * , new Action(Action.ADD_FACT, "1j3|3J1"));
		 * 
		 * rules.put("(ka¿d[y|a|e]|dowoln[y|a|e]) (.*) (jest) (.*)", new Action(
		 * Action.ADD_FACT, "1j3|3J1"));
		 * 
		 * rules.put("(.*) (odpowiada|jest równowa¿n[y|a|e]) (.*)", new Action(
		 * Action.ADD_FACT, "0r2|2r0"));
		 * 
		 * rules.put("(.*) (to|jest|nale¿y do|to cz³onek|jest cz³onkiem) (.*)",
		 * new Action(Action.ADD_FACT, "0n2|2N0"));
		 * 
		 * rules.put(
		 * "(ka¿d|dowoln[y|a|e]) (.*) (posiada|ma) (jak[i|¹]œ|dowoln[y|¹|e]) (.*)"
		 * , new Action(Action.ADD_FACT, "1p4|4P1"));
		 * 
		 * rules.put("(ka¿d[y|a|e]|dowoln[y|a|e]) (.*) (posiada|ma) (.*)", new
		 * Action(Action.ADD_FACT, "1p3|3P1"));
		 * 
		 * rules.put("(.*) (posiada|ma) (.*)", new Action(Action.ADD_FACT,
		 * "0p2|2P0"));
		 */

		rules.put("(.*) (wyst¹pi[³|³a] w filmie|zagra[³|³a] w filmie) (.*)",
				new Action(Action.ADD_FACT, "0w2|2W0"));

		rules.put("w filmie (.*) (wyst¹pi[³|³a]|zagra[³|³a]) (.*)", new Action(
				Action.ADD_FACT, "0W2|2w0"));

		rules.put("film (.*) (to) (.*)", new Action(Action.ADD_FACT, "0t2|2T0"));

		rules.put("(re¿yserem filmu) (.*) (jest) (.*)", new Action(
				Action.ADD_FACT, "3r1|1R3"));

		rules.put("kto (wyst¹pi³) (w filmie) (.*)", new Action(
				Action.ALL_PATHS, "W"));

		rules.put("jakie filmy (s¹) (.*)", new Action(Action.ALL_PATHS, "T"));

		rules.put("(w jakich filmach) (zagra[³|³a]|wyst¹pi[³|³a]) (.*)",
				new Action(Action.ALL_PATHS, "w"));

		rules.put("kto (jest) (re¿yserem filmu) (.*)", new Action(
				Action.ALL_PATHS, "R"));

		rules.put("(jakie filmy) (wyre¿yserowa[³|³a]|nakrêci[³|³a]) (.*)",
				new Action(Action.ALL_PATHS, "r"));

		rules.put("(jakiego gatunku) (jest) (film) (.*)", new Action(
				Action.ALL_PATHS, "t"));

		rules.put("(w jakich gatunkach filmowych) (zagra[³|³a]|wyst¹pi[³|³a]) (.*)",
				new Action(Action.ALL_PATHS, "wt"));

		rules.put("czy (.*) (wspó³pracowa[³|³a]) (.*)", new Action(
				Action.ONE_PATH, "0wR2|2rW0|0rW2|2Rw0"));

		rules.put("(z jakimi re¿yserami) (wspó³pracowa[³|³a]) (.*)",
				new Action(Action.ALL_PATHS, "wR"));

		rules.put("(z jakimi aktorami) (wspó³pracowa³[³|³a]) (.*)", new Action(
				Action.ALL_PATHS, "rW"));

		rules.put("(jacy aktorzy) (wyst¹pili) (w filmach typu) (.*)",
				new Action(Action.ALL_PATHS, "TW"));

		rules.put("(jakie gatunki filmów) (wyre¿yserowa³[³|³a]) (.*)", new Action(
				Action.ALL_PATHS, "rt"));

		rules.put("(jacy re¿yserowie) (tworzyli) (filmy typu) (.*)", new Action(
				Action.ALL_PATHS, "TR"));
	}

	private String getStems(String phrase) {
		String result = "";
		String[] words = phrase.split(" ");
		PolishStemmer ps = new PolishStemmer();
		for (String word : words) {
			@SuppressWarnings("unchecked")
			List<WordData> wordList = ps.lookup(word);
			String exWord;
			if (wordList.size() > 0) {
				exWord = wordList.get(0).getStem().toString();
			} else {
				exWord = word;
			}
			if (result.length() > 0) {
				result += " ";
			}
			result += exWord;
		}
		debug("Stemming: " + phrase + " -> " + result);
		return result;
	}

	public void debug(String string) {
		if (debugMode) {
			frame.showDebugInfo(string);
		}
	}

	public void addNewFacts(ArrayList<Fact> newFacts) {
		for (Fact newFact : newFacts) {
			if (!facts.contains(newFact)) {
				facts.add(newFact);
				debug("Add Fact: " + newFact.toString());
			}
		}
	}

	public ArrayList<Fact> getFacts() {
		return facts;
	}

	public void addQuestion(String q) {
		synchronized (myQuest) {
			myQuest.add(q);
		}
	}

	public HashMap<String, String> getInitialWords() {
		return initialWords;
	}

	public void setInitialWords(HashMap<String, String> initialWords) {
		this.initialWords = initialWords;
	}

	public void addInitialWords(HashMap<String, String> initialWords) {
		this.initialWords.putAll(initialWords);
	}
}
