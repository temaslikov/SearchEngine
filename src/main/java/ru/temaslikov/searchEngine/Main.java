package ru.temaslikov.searchEngine;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import ru.temaslikov.searchEngine.indexation.IndexService;
import ru.temaslikov.searchEngine.search.SearchService;

import java.nio.file.Paths;

import static java.lang.System.currentTimeMillis;

/**
 * Created by Артём on 11.03.2017.
 */


public class Main {

    public static void main(String[] args) throws Exception {

        //runIndexService(Statics.tfIfd);

        runServer();
        // runTest();
    }


    static void runIndexService(boolean tfIdf) {
        IndexService indexService = new IndexService();
        long start, duration;
        start = currentTimeMillis();

        indexService.getTokens();

        duration = currentTimeMillis() - start;
        System.out.println("duration of get and write tokens: " + duration / 1000 / 60 + " minutes");

        /*
        start = currentTimeMillis();
        indexService.mergeIndexes();
        duration = currentTimeMillis() - start;
        System.out.println("duration of merge indexes: " + duration / 1000 / 60 + " minutes");

        indexService.clear();
        */
    }

    private static void runServer() throws Exception {
        Server server = new Server(8080);

        HandlerCollection handlers = new HandlerCollection();

        WebAppContext webapp = new WebAppContext();
        webapp.setResourceBase(Paths.get("src", "main", "webapp").toString());
        webapp.setDescriptor(Paths.get("target", "web.xml").toString());
        webapp.setContextPath("/");

        webapp.setAttribute("org.eclipse.jetty.containerInitializers", new ContainerInitializer(new JettyJasperInitializer(), null));
        webapp.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());

        handlers.addHandler(webapp);

        Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
        classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");

        server.setHandler(handlers);

        server.start();
        System.out.println("Server started");
        java.util.logging.Logger.getGlobal().info("Server started");
        server.join();
    }

    static void runTest() {
        long start, duration;

        SearchService searchService = new SearchService();

        start = currentTimeMillis();
        searchService.readIndexes();
        //System.out.println(searchService.getAllTokens());
        duration = currentTimeMillis() - start;
        System.out.println("duration of get indexes to searchService: " + duration / 1000 + " seconds");

        System.out.println(searchService.findExpression("мост сакура"));
        System.out.println(searchService.findExpression("(мост сакура) !парка"));
        System.out.println(searchService.findExpression("((мост сакура) !парка)||(мост сакура)"));

    }

}





