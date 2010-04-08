package com.eharmony.configuration.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.eharmony.configuration.ConfigManager;
import com.eharmony.configuration.ConfigProperties;
import com.eharmony.configuration.Configuration;
import com.eharmony.configuration.SingletonConfigManagerImpl;

public class ConfigTestServlet extends HttpServlet {
    /**
     * generated.
     */
    private static final long serialVersionUID = 0L;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.getWriter().println("*** Getting ConfigProperties from SingletonConfigManager \n\n");
        ConfigManager manager = SingletonConfigManagerImpl.getInstance();
        Configuration configProperties = manager.buildConfigProperties();
        configProperties.list(out);

        ConfigProperties config = (ConfigProperties) configProperties;

        out.println("\nReloader class name: "  + config.getReloader().getClass().getName());
        out.println("at address: " + config.getReloader());
        
        out.println("\n\n*** Listing Root Configuration now ***");
        ((ConfigProperties)configProperties).getRootProperties().list(out);
        
        out.println("\n\n*** Configured ConfigSources ***");
        out.println(config.getConfigSources());
        
        out.println("\n\n*** Configured ConfigScopes ***");
        out.println(config.getConfigScopes());
        
        out.flush();
        
        out.println("\n\n*** Now Getting the same things from Spring \n");
        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(this.getServletConfig().getServletContext());
        ConfigProperties fromSpring = (ConfigProperties) ctx.getBean("configProperties");
        fromSpring.list(out);

        out.println("\nReloader class name: "  + fromSpring.getReloader().getClass().getName());
        out.println("at address: " + fromSpring.getReloader());
        
        out.println("\n\n*** Listing Root Configuration now ***");
        fromSpring.getRootProperties().list(out);
 
        out.println("\n\n*** Configured ConfigSources ***");
        out.println(fromSpring.getConfigSources());
        
        out.println("\n\n*** Configured ConfigScopes ***");
        out.println(fromSpring.getConfigScopes());

        out.println("\n\n*** Now let's trying to reload the damn thing ***");
        System.setProperty("sample.property.from.system.property", "sample 5 from system properties, after refresh");
        fromSpring.getReloader().reload(fromSpring, false);
        out.println("sample.property.from.system.property=" + fromSpring.getProperty("sample.property.from.system.property"));
        out.println("\n\n*** After refresh  ***");
        fromSpring.list(out);

        out.println("\n\n*** Now retrieve property using getProperty(key, new Locale(\"\", \"\")");
        out.println("sampleProperty.no.override should have value of 'sample 4 from global'");
        out.println("retrieved value = "
                + config.getProperty("sampleProperty.no.override", new Locale("", "")));


    }
}
