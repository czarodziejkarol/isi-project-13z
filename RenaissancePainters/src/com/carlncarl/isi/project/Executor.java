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
	public static final String ANSWER_DO_NOT_KNOW = "Nie wiadomo";
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

				switch (action.getMethod()) {
				case Action.ALL_PATHS:
					action.setQuestion(inputUser);
					LinkedList<String> matches = generateComplexAnswer(action,
							groups);
					result = prepareAnswer(action, groups, matches);
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

		groups[Integer.parseInt(phraseIn[0])] = getStems(groups[Integer
				.parseInt(phraseIn[0])]);
		groups[Integer.parseInt(phraseIn[2])] = getStems(groups[Integer
				.parseInt(phraseIn[2])]);

		initialWords.put(groups[Integer.parseInt(phraseIn[0])], first);
		initialWords.put(groups[Integer.parseInt(phraseIn[2])], second);

		String result = null;

		for (String phrase : splittedPhrases) {
			spl = phrase.toCharArray();
			String[] phraseP = new String[3];
			for (int i = 0; i < spl.length; i++) {
				phraseP[i] = Character.toString(spl[i]);
			}

			Fact fact = new Fact(groups[Integer.parseInt(phraseP[0])],
					String.valueOf(phraseP[1]),
					groups[Integer.parseInt(phraseP[2])]);
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

	// Methods of role USERINTERFACE
	public void setDebugMode(boolean selected) {
		debugMode = selected;
	}

	public String receiveQuery(String userInput) {
		return queryInterpretation(userInput);
	}



	public LinkedList<String> generateComplexAnswer(Action action,
			String[] groups) {

		String start;

		if (action.getArg().equals("p") || action.getArg().equals("r")) {
			groups[1] = getStems(groups[1]);
			start = groups[1];
		} else {
			groups[0] = getStems(groups[0]);
			start = groups[0];
		}

		String first = start;

		LinkedList<Fact> used = new LinkedList<Fact>();
		LinkedList<String> matches = new LinkedList<String>();
		LinkedList<String> q = new LinkedList<String>();
		while (true) {
			for (Fact fact : facts) {
				if (used.contains(fact) || !fact.getA().equals(start)
						|| !fact.getRel().equals(action.getArg())) {
					continue;
				}

				if (!fact.getB().equals(first)
						&& !matches.contains(initialWords.get(fact.getB()))) {
					matches.add(initialWords.get(fact.getB()));
					used.add(fact);
					q.add(fact.getB());
				}
			}

			if (q.size() == 0) {
				break;
			}
			start = q.removeLast();
		}

		return matches;
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
			String pattern = rule.substring(1, rule.length() - 2);
			String start = groups[Integer.parseInt(rule.substring(0, 1))];
			String stop = groups[Integer.parseInt(ruleP.substring(ruleP
					.length() - 1))];
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
			LinkedList<String> answerList) {
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
		}
		String answer;
		if (answerList.size() == 0) {
			answer = ANSWER_DO_NOT_KNOW;
		} else if (answerList.size() == 1) {
			answer = userForm + linkWord + answerList.getFirst();
		} else {
			answer = userForm
					+ ((action.getArg().equals("P") ? " posiadaj¹ " : linkWord));
			String ma = "";
			for (String match : answerList) {
				if (ma.length() > 0) {
					ma += ", ";
				}
				ma += match;
			}
			answer += ma;
		}
		return answer;
	}

	private void path(String pattern, String start, String end,
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

		rules.put("co (posiada|ma) (.*)", new Action(Action.ALL_PATHS, "p"));

		rules.put("co (odpowiada|jest równowa¿n[y|a|e]) (.*)", new Action(
				Action.ALL_PATHS, "r"));

		rules.put("co jest (.*)", new Action(Action.ALL_PATHS, "N"));

		rules.put("jakie obiekty posiadaj¹ (.*)", new Action(Action.ALL_PATHS,
				"P"));

		rules.put("kto posiada (.*)", new Action(Action.ALL_PATHS, "P"));

		rules.put("czy ka¿d[y|a|e] (.*) (to|jest) (.*)", new Action(
				Action.ONE_PATH, "0r*j*2"));

		rules.put("czy (.*) (to|jest) (.*)", new Action(Action.ONE_PATH,
				"0r*nj*2|0r*j*2"));

		rules.put("czy ka¿d[y|a|e] (.*) (posiada|ma) (.*)", new Action(
				Action.ONE_PATH, "0r*p*j*2"));

		rules.put("czy któr[y|a|e]kolwiek (.*) (posiada|ma) (.*)", new Action(
				Action.ONE_PATH, "0J*Nr*pj*2"));

		rules.put("czy (.*) (posiada|ma) (.*)", new Action(Action.ONE_PATH,
				"0r*nj*pj*2|0J*Nr*pj*2|0r*p*j*2"));

		rules.put("(ka¿d[y|a|e]|dowoln[y|a|e]) (.*) (to|jest podzbiorem) (.*)",
				new Action(Action.ADD_FACT, "1j3|3J1"));

		rules.put("(ka¿d[y|a|e]|dowoln[y|a|e]) (.*) (jest) (.*)", new Action(
				Action.ADD_FACT, "1j3|3J1"));

		rules.put("(.*) (odpowiada|jest równowa¿n[y|a|e]) (.*)", new Action(
				Action.ADD_FACT, "0r2|2r0"));

		rules.put("(.*) (to|jest|nale¿y do|to cz³onek|jest cz³onkiem) (.*)",
				new Action(Action.ADD_FACT, "0n2|2N0"));

		rules.put(
				"(ka¿d|dowoln[y|a|e]) (.*) (posiada|ma) (jak[i|¹]œ|dowoln[y|¹|e]) (.*)",
				new Action(Action.ADD_FACT, "1p4|4P1"));

		rules.put("(ka¿d[y|a|e]|dowoln[y|a|e]) (.*) (posiada|ma) (.*)",
				new Action(Action.ADD_FACT, "1p3|3P1"));

		rules.put("(.*) (posiada|ma) (.*)", new Action(Action.ADD_FACT,
				"0p2|2P0"));

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
