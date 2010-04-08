package com.eharmony.configuration.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.eharmony.configuration.ConfigProperties;

public class TriggerReloadServlet extends HttpServlet {
    /**
     * generated.
     */
    private static final long serialVersionUID = 0L;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        out.println("\n\n*** Now Getting the same things from Spring \n");
        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(this.getServletConfig().getServletContext());
        ConfigProperties fromSpring = (ConfigProperties) ctx.getBean("configProperties");
        // calling getProeprty after reload interval should trigger a refresh
        out.println("Calling ConfigProperties.getProperty(\"user.home\") to trigger refresh");
        out.println("user.home=" + fromSpring.getProperty("user.home"));
        out.println("Now sleeping for 10 seconds for refreshing to finish up");
        out.flush();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        out.println("\n\n*** Hopefully now you should see the new property value :-) \n");        
        fromSpring.list(out);
    }
}
