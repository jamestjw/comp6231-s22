package com.pouriarm.remotingrmiserver;

import com.pouriarm.remotingrmi.core.Dictionary;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.rmi.RmiServiceExporter;

@SpringBootApplication
public class RemotingRmiServerApplication {

    // TODO: Create dictionaryService Bean that returns DictionaryImpl object.
    // Folow Spring boot documentation to learn how you can do it.
    @Bean
    Dictionary buildDictionary() {
        return new DictionaryImpl();
    }
    //

    /**
     *  Expose a service via RMI. Remote obect URL is:
     *          rmi://<HOST>:<PORT>/<SERVICE_NAME>
     *          6231 is the default port
     *  You must use RmiServiceExporter to compelte this part
     */
    @Bean 
    RmiServiceExporter exporter(Dictionary implementation) {
       //TODO: Complete this part and return the correct object!
        // Think About the return object. Should we return Dictionary object?
        Class<Dictionary> serviceInterface = Dictionary.class;
        RmiServiceExporter exporter = new RmiServiceExporter();
        exporter.setServiceInterface(serviceInterface);
        exporter.setService(implementation);
        exporter.setServiceName(serviceInterface.getSimpleName());
        exporter.setRegistryPort(6231); 
        return exporter;
    }

    public static void main(String[] args) {
        SpringApplication.run(RemotingRmiServerApplication.class, args);
    }
}
