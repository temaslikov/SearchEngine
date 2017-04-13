package ru.temaslikov.searchEngine.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.temaslikov.searchEngine.search.SearchService;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static java.lang.System.currentTimeMillis;

/**
 * Created by Артём on 26.03.2017.
 */

@Controller
public class searchEngineController {

    private SearchService searchService;
    private long start, duration;
    private Set<Integer> result;

    public searchEngineController() {
        searchService = new SearchService();
        result = new TreeSet<>();
        start = currentTimeMillis();
        searchService.readIndexes();
        duration = currentTimeMillis() - start;
        System.out.println("INFO: duration of get indexes to searchService: " + duration / 1000 + " seconds");

    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView start() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("startSearch");
        return modelAndView;
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ModelAndView search(@RequestParam(value="expression") String expression) throws UnsupportedEncodingException {
        ModelAndView modelAndView = new ModelAndView();
        result = searchService.findExpression(expression);

        modelAndView.addObject("expressionViewJSP", expression);
        modelAndView.addObject("titleMapViewJSP", searchService.getTitleMap());
        modelAndView.addObject("resultViewJSP", searchService.findExpression(expression));
        modelAndView.setViewName("mainSearch");
        return modelAndView;
    }
}
