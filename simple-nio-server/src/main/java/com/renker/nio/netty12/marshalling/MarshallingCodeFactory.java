package com.renker.nio.netty12.marshalling;

import java.io.IOException;


import org.jboss.marshalling.Marshaller;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.Unmarshaller;

public class MarshallingCodeFactory {
	protected static Marshaller buildMarshaller() throws IOException{
		MarshallerFactory factroy = Marshalling.getProvidedMarshallerFactory("serial");
		
		MarshallingConfiguration configuration = new MarshallingConfiguration();
		configuration.setVersion(5);
		
		Marshaller marshaller = factroy.createMarshaller(configuration);
		
		return marshaller;
	}
	
	protected static Unmarshaller buildUnMarshalling() throws IOException{
		MarshallerFactory factroy = Marshalling.getProvidedMarshallerFactory("serial");
		
		MarshallingConfiguration configuration = new MarshallingConfiguration();
		configuration.setVersion(5);
		
		Unmarshaller unmarshaller = factroy.createUnmarshaller(configuration);
		
		return unmarshaller;
	}
}
