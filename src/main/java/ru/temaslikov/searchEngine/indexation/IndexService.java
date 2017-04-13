package ru.temaslikov.searchEngine.indexation;

import ru.temaslikov.searchEngine.Resources;
import ru.temaslikov.searchEngine.Statics;
import ru.temaslikov.searchEngine.TokenInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Created by Артём on 11.03.2017.
 */

public class IndexService {

    private Map<Integer, String> titleMap;
    private Map<Integer, TreeSet<Integer>> tokensMap;
    private Map<String, TokenInfo> idTokenMap;
    private Map<Integer, String> tokenIdMap;
    // docId - frequency if doc (tf)
    private final Map<Integer, Map<Integer, Integer>> tokensMapTfIdf;

    private Tokenizaton tokenizaton;
    private Integer countPartsIndexFile;

    private Long shiftNumber;

    public IndexService () {
        titleMap = new TreeMap<>();
        tokensMap = new TreeMap<>();
        idTokenMap = new TreeMap<>();
        tokenIdMap = new TreeMap<>();
        tokensMapTfIdf = new TreeMap<>(new TreeMap<>());
        tokenizaton = new Tokenizaton(titleMap, tokensMap, idTokenMap, tokenIdMap, tokensMapTfIdf);
        countPartsIndexFile = 0;
        shiftNumber = 0L;
    }

    public void mergeIndexes() {

        Map<BufferedReader, String> bFileReaders = new HashMap<>();

        // кладём файлы в массив
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(Resources.indexPartsPath))) {
            for (Path entry: stream) {
                if (!entry.toFile().isDirectory()) {
                    BufferedReader br = new BufferedReader(new FileReader(entry.toFile()));
                    bFileReaders.put(br, null);
                }
            }
        } catch (IOException | DirectoryIteratorException x) {
            System.err.println(x);
        }

        String line;

        // заполнили первоначальными данными
        for (Map.Entry<BufferedReader, String> entry : bFileReaders.entrySet()) {
            try {

                line = entry.getKey().readLine();
                if (line == null) {
                    bFileReaders.remove(entry.getKey());
                }
                else {
                    entry.setValue(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileWriter writerTest = new FileWriter(Resources.indexResultPathTest + "\\index_result.txt", true);
            DataOutputStream writerBin = new DataOutputStream(new FileOutputStream(Resources.indexResultPath + "\\index_result.dat"));
            int nullSizeReaders = 0;
            while (nullSizeReaders != bFileReaders.size()) {
                nullSizeReaders = writeMinToken(bFileReaders, writerBin, writerTest);
            }
            writerTest.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        writeToHDDIdToken();

    }

    private int writeMinToken (Map<BufferedReader, String> bFileReaders, DataOutputStream writerBin, FileWriter writerTest) throws IOException {
        Integer minIdToken = Integer.MAX_VALUE;
        Integer nullSizeReaders = 0;

        // находим min token из всей выборки
        for (Map.Entry<BufferedReader, String> entry : bFileReaders.entrySet()) {

            if (entry.getValue() == null) {
                nullSizeReaders++;
                continue;
            }

            String lines[] = entry.getValue().split(" ");


            if (minIdToken >= Integer.parseInt(lines[0])) {
                minIdToken = Integer.parseInt(lines[0]);
            }
        }

        if (nullSizeReaders == bFileReaders.size()) {
            return nullSizeReaders;
        }

        // смещение
        idTokenMap.get(tokenIdMap.get(minIdToken)).setShift(shiftNumber);
        /*
        for (Map.Entry<String, TokenInfo> entry : idTokenMap.entrySet()) {
            if (Objects.equals(entry.getValue().getIdToken(), minIdToken)) {
                entry.getValue().setShift(shiftNumber);
            }
        }*/

        // пишем множество документов из этого мин токена
        Set<Integer> idDocSet = new TreeSet<>();

        for (Map.Entry<BufferedReader, String> entry : bFileReaders.entrySet()) {

            if (entry.getValue() != null) {

                String lines[] = entry.getValue().split(" ");
                if (Objects.equals(minIdToken, Integer.parseInt(lines[0]))) {

                    for (int i = 1; i < lines.length; i++) {
                        idDocSet.add(Integer.parseInt(lines[i]));
                    }
                    entry.setValue(entry.getKey().readLine());

                }

            }
        }

        writerTest.write(minIdToken + " " + idDocSet.size());
        writerBin.writeInt(minIdToken);
        writerBin.writeInt(idDocSet.size());
        shiftNumber += 4 * (2 + idDocSet.size());

        for (Integer idDoc: idDocSet) {
            writerTest.write(" " + idDoc);
            writerBin.writeInt(idDoc);
        }
        writerTest.write("\n");
        return nullSizeReaders;
    }

    public void getTokens() {

        parseTokens(Paths.get(Resources.dumpHtmlPath));

        if (!Statics.tfIfd) {
            writeToHDDIndex();
        }
        else {
            writeToHDDIndexTfIdf();
        }
        writeToHDDTitle();


        System.out.println("Total tokens: " + idTokenMap.size());
        System.out.println("Amount tokens in all documents " + Statics.amountTokens);
        System.out.println("assignment of documents in tokens " + (float) Statics.amountTokens / idTokenMap.size());

        // writeToHDDIdToken();
        // idTokenMap.clear();
    }


    private void parseTokens(Path path) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry: stream) {
                if (entry.toFile().isDirectory()) {
                    parseTokens(entry);
                }
                else {

                    Files.lines(entry, StandardCharsets.UTF_8).forEach( (line) -> {
                        tokenizaton.parse(line);

                        if (tokensMap.size() > Resources.maxSizeTokensMap) {
                            if (!Statics.tfIfd) {
                                writeToHDDIndex();
                            }
                            else {
                                writeToHDDIndexTfIdf();
                            }
                            writeToHDDTitle();
                        }

                    });
                }

            }
        } catch (IOException | DirectoryIteratorException x) {
            System.err.println(x);
        }
    }

    private void writeToHDDTitle() {

        FileWriter writer;

        try {
            if (!Statics.tfIfd) {
                writer = new FileWriter(Resources.titlePath + "\\title.txt", true);
            } else {
                writer = new FileWriter(Resources.titlePathTfIfd + "\\title.txt", true);
            }
            for (Map.Entry<Integer, String> entry : titleMap.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
                //System.out.println(entry.getKey() + " " + entry.getValue());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        titleMap.clear();

    }

    private void writeToHDDIdToken() {

        FileWriter writer;

        try {
            if (!Statics.tfIfd) {
                writer = new FileWriter(Resources.idTokenPath + "\\id_token.txt", true);
            } else {
                writer = new FileWriter(Resources.idTokenPathTfIfd + "\\id_token.txt", true);
            }
            for (Map.Entry<String, TokenInfo> entry : idTokenMap.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue().getIdToken() + " " + entry.getValue().getShift() + "\n");
                //System.out.println(entry.getKey() + " " + entry.getValue());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        titleMap.clear();

    }


    private void writeToHDDIndex() {

        FileWriter writer;

        try {
            writer = new FileWriter(Resources.indexPartsPath + "\\partIndexFile_" + countPartsIndexFile + ".txt", true);
            for (Map.Entry<Integer, TreeSet<Integer>> entry : tokensMap.entrySet()) {
                writer.write(entry.getKey().toString());
                for (Integer docIdToken : entry.getValue()) {
                    // записываем id токенов здесь
                    writer.write(" " + docIdToken);
                }
                writer.write("\n");
            }
            countPartsIndexFile++;
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tokensMap.clear();
    }

    private void writeToHDDIndexTfIdf() {

        FileWriter writer;

        try {
            writer = new FileWriter(Resources.indexPartsPathTfIfd + "\\partIndexFile_" + countPartsIndexFile + ".txt", true);
            for (Map.Entry<Integer, Map<Integer, Integer>> entry : tokensMapTfIdf.entrySet()) {
                writer.write(entry.getKey().toString());
                for (Map.Entry<Integer, Integer> subEntry : entry.getValue().entrySet()) {
                    // записываем id токенов и tf здесь
                    writer.write(" " + subEntry.getKey());
                    writer.write(" " + subEntry.getValue());
                }
                writer.write("\n");
            }
            countPartsIndexFile++;
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tokensMap.clear();
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
            System.out.println(entry.getKey() + " : " +
                    entry.getValue()
            );
        }
        System.out.println("}");
    }

    public void clear() {
        titleMap.clear();
        tokensMap.clear();
        idTokenMap.clear();
        tokenIdMap.clear();
    }
}
