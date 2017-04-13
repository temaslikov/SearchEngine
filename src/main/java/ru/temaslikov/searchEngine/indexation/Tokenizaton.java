package ru.temaslikov.searchEngine.indexation;

import ru.temaslikov.searchEngine.Statics;
import ru.temaslikov.searchEngine.TokenInfo;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Артём on 11.03.2017.
 */

public class Tokenizaton {
    private final Map<Integer, String> titleMap;
    private final Map<Integer, TreeSet<Integer>> tokensMap;
    private final Map<Integer, Map<Integer, Integer>> tokensMapTfIdf;
    private final Map<String, TokenInfo> idTokenMap;
    private Map<Integer, String> tokenIdMap;
    private Integer docId;
    private Integer tokenId;
    private Pattern titlePattern;
    private Pattern delimiterPattern;

    Tokenizaton(Map<Integer, String> titleMap, Map<Integer, TreeSet<Integer>> tokensMap,
                Map<String, TokenInfo> idTokenMap, Map<Integer, String> tokenIdMap,
                Map<Integer, Map<Integer, Integer>> tokensMapTfIdf) {
        this.titleMap = titleMap;
        this.tokensMap = tokensMap;
        this.idTokenMap = idTokenMap;
        this.tokenIdMap = tokenIdMap;
        this.tokensMapTfIdf = tokensMapTfIdf;
        docId = -1;
        tokenId = 0;
        titlePattern = Pattern.compile("^<doc id=\"([0-9]+)\".+title=\"(.+)\">$");

        String delimiter = "\\.+|,+|\\s+|;+|:+|!+|\\?|—+|\"+|»+|«+|&lt;.*?&gt;|<.*?>";
        delimiter += "|\\)+|\\(+|\\{+|\\}+|\\+|/+|№+|“+|„+|\\[+|\\]+";
        delimiter += "|…+";
        delimiter += "|-";
        delimiter += "| +";// не пробел
        delimiterPattern = Pattern.compile(delimiter);
    }

    void parse(String line){
        Matcher titleMatcher = titlePattern.matcher(line);
        Matcher delimiterMatcher = delimiterPattern.matcher(line);

        if (titleMatcher.matches()) {
            titleMap.put(Integer.parseInt(titleMatcher.group(1)), titleMatcher.group(2));
            docId = Integer.parseInt(titleMatcher.group(1));
        }
        else {
            String tokensLine = delimiterMatcher.replaceAll(" ");
            String[] tokens = tokensLine.split("\\s+");
            // printTokensInLine(tokens);
            for (String token : tokens) {
                if (!Objects.equals(token, "")) {

                    token = modify(token);
                    // есть docId и token
                    if (!idTokenMap.containsKey(token)) {
                        idTokenMap.put(token, new TokenInfo(tokenId, null));
                        tokenIdMap.put(tokenId, token);
                        tokenId++;
                    }

                    if (!Statics.tfIfd) {
                        putToken(idTokenMap.get(token).getIdToken());
                    }
                    else {
                        putTokenTfIdf(idTokenMap.get(token).getIdToken());
                    }
                    Statics.amountTokens += 1;
                }
            }
        }

    }

    private String modify (String token) {
        token = token.toLowerCase();
        return token;
    }

    private void putToken(Integer token_Id) {
        if (!tokensMap.containsKey(token_Id)) {
            TreeSet<Integer> treeSet = new TreeSet<>();
            treeSet.add(docId);
            tokensMap.put(token_Id, treeSet);
        }
        else {
            tokensMap.get(token_Id).add(docId);
        }
    }

    private void putTokenTfIdf(Integer token_Id) {
        if (!tokensMapTfIdf.containsKey(token_Id)) {
            TreeMap<Integer, Integer> treeMap = new TreeMap<>();
            treeMap.put(docId, 1);
            tokensMapTfIdf.put(token_Id, treeMap);
        }
        else {
            if (!tokensMapTfIdf.get(token_Id).containsKey(docId)) {
                tokensMapTfIdf.get(token_Id).put(docId, 1);
            }
            else {
                int frequency = tokensMapTfIdf.get(token_Id).get(docId) + 1;
                tokensMapTfIdf.get(token_Id).put(docId, frequency);
            }
            // tokensMapTfIdf.get(token_Id).add(docId).add(t);
        }
    }

    public void printTitleMap(){
        System.out.println("titleMap { ");
        for (Map.Entry entry : titleMap.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
        System.out.println("}");
    }

    public void printTokensMap(){
        System.out.println("tokensMap { ");
        for (Map.Entry entry : tokensMap.entrySet()) {
            System.out.println(entry.getKey() + " : {" +
                    entry.getValue()
                    + " }"
            );
        }
        System.out.println("}");
    }

    public void printTokensInLine(String[] tokens){
        System.out.println("tokens { ");
        for (String token : tokens) {
            System.out.print(token + " ");
        }
        System.out.println("}");
    }
}
