package ru.temaslikov.searchEngine.search;

import ru.temaslikov.searchEngine.Resources;
import ru.temaslikov.searchEngine.TokenInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Артём on 14.03.2017.
 */
public class SearchService {

    private Map<Integer, String> titleMap;
    private Map<Integer, TreeSet<Integer>> tokensMap;
    private Map<String, TokenInfo> idTokenMap;
    private Set<Integer> allTokens;
    private ExpressionEvaluator expressionEvaluator;


    public SearchService () {
        titleMap = new TreeMap<>();
        tokensMap = new TreeMap<>();
        idTokenMap = new TreeMap<>();
        allTokens = new TreeSet<>();
        expressionEvaluator = new ExpressionEvaluator(idTokenMap, allTokens);

    }

    public void readIndexes() {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(Resources.titlePath))) {
            for (Path entry: stream) {
                if (!entry.toFile().isDirectory()) {

                    Files.lines(entry, StandardCharsets.UTF_8).forEach( (line) -> {
                        String[] lines = line.split(" ", 2);
                        titleMap.put(Integer.parseInt(lines[0]), lines[1]);
                        allTokens.add(Integer.parseInt(lines[0]));
                    });
                }
            }
            //System.out.println(allTokens);
        } catch (IOException | DirectoryIteratorException x) {
            System.err.println(x);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(Resources.idTokenPath))) {
            for (Path entry: stream) {
                if (!entry.toFile().isDirectory()) {

                    Files.lines(entry, StandardCharsets.UTF_8).forEach( (line) -> {
                        String[] lines = line.split(" ");
                        idTokenMap.put(lines[0], new TokenInfo(Integer.parseInt(lines[1]), Long.parseLong(lines[2])));
                    });
                }
            }
        } catch (IOException | DirectoryIteratorException x) {
            System.err.println(x);
        }
    }

    public Set<Integer> findExpression(String expression) {

        return expressionEvaluator.evaluate(expression.replaceAll(" +", "&&"));
    }

    public Map<String, TokenInfo> getIdTokenMap() {
        return idTokenMap;
    }

    public Map<Integer, String> getTitleMap() {
        return titleMap;
    }


    public Set<Integer> getAllTokens() {
        return allTokens;
    }
}
