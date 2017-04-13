package ru.temaslikov.searchEngine.search;

import com.fathzer.soft.javaluator.*;
import ru.temaslikov.searchEngine.Resources;
import ru.temaslikov.searchEngine.TokenInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Created by Артём on 19.03.2017.
 */
public class ExpressionEvaluator extends AbstractEvaluator<Set<Integer>> {
    /** The negate unary operator. */
    public final static Operator NEGATE = new Operator("!", 1, Operator.Associativity.RIGHT, 3);
    /** The logical AND operator. */
    private static final Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 2);
    /** The logical OR operator. */
    public final static Operator OR = new Operator("||", 2, Operator.Associativity.LEFT, 1);

    private static final Parameters PARAMETERS;

    private Map<String, TokenInfo> idTokenMap;
    private Set<Integer> allTokens;

    static {
        // Create the evaluator's parameters
        PARAMETERS = new Parameters();
        // Add the supported operators
        PARAMETERS.add(AND);
        PARAMETERS.add(OR);
        PARAMETERS.add(NEGATE);
        PARAMETERS.addExpressionBracket(BracketPair.PARENTHESES);
    }

    public ExpressionEvaluator(Map<String, TokenInfo> idTokenMap, Set<Integer> allTokens) {
        super(PARAMETERS);
        this.idTokenMap = idTokenMap;
        this.allTokens = allTokens;
    }

    @Override
    protected Set<Integer> toValue(String literal, Object evaluationContext) {
        return findWord(literal);
    }

    @Override
    protected Set<Integer> evaluate(Operator operator,
                               Iterator<Set<Integer>> operands, Object evaluationContext) {
        if (operator == NEGATE) {
            return not(allTokens, operands.next());
        } else if (operator == OR) {
            Set<Integer> o1 = operands.next();
            Set<Integer> o2 = operands.next();
            return or(o1, o2);
        } else if (operator == AND) {
            Set<Integer> o1 = operands.next();
            Set<Integer> o2 = operands.next();
            return and(o1, o2);
        } else {
            return super.evaluate(operator, operands, evaluationContext);
        }
    }

    private Set<Integer> not(Set<Integer> sAll, Set<Integer> s2) {
        Set<Integer> result = new TreeSet<>();
        result.addAll(sAll);
        result.removeAll(s2);
        return result;
    }

    private Set<Integer> and(Set<Integer> s1, Set<Integer> s2) {
        s1.retainAll(s2);
        return s1;
    }

    private Set<Integer> or(Set<Integer> s1, Set<Integer> s2) {
        s1.addAll(s2);
        return s1;
    }

    private Set<Integer> findWord(String word) {

        word = word.toLowerCase();
        Set<Integer> docIdSet = new HashSet<>();
        if (!idTokenMap.containsKey(word)) {
            System.out.println("Exception: Can't find word : " + word);
            return  docIdSet;
        }

        long shift = idTokenMap.get(word).getShift();
        try {
            RandomAccessFile file = new RandomAccessFile(new File(Resources.indexResultPath + "\\index_result.dat"), "r");

            file.seek(shift);

            // tokenId
            file.readInt();
            Integer docIdSize = file.readInt();

            for (int i = 0; i < docIdSize; i++) {
                docIdSet.add(file.readInt());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return docIdSet;
    }

}
