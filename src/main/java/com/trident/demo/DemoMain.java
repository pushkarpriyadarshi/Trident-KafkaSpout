package com.trident.demo;


import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import storm.kafka.*;

import storm.trident.*;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.builtin.Count;
import storm.trident.testing.MemoryMapState;
import storm.trident.tuple.TridentTuple;
import storm.kafka.trident.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

class Enrich extends BaseFunction {

    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
    	System.out.println("in"+ tuple.getString(0));
    	//Extract stream
    	String log = tuple.getString(0);
    	//create JSONObject from the string
    	JSONObject logJson = (JSONObject)JSONValue.parse(log);
    	System.out.println("clientId="+(String)logJson.get("clientId"));
    	/*collector.emit(new Values((String)logJson.get("clientId"), 
    							  (String)logJson.get("memory"),
    							  (String)logJson.get("timemills")));*/
    	
    }
    
}


class PrintFunc extends BaseFunction {

	
    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
    	System.out.println("id="+tuple.getString(0)+"memory="+tuple.getString(1)+"time="+tuple.getString(2));
    	//collector.emit(new Values(tuple.getString(1)));
    }
    
}


public class DemoMain {
	
	final static String TOPIC_NAME = "memory";
	public LocalCluster _cluster = new LocalCluster();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DemoMain demo = new DemoMain();
		BrokerHosts brokerHosts = new ZkHosts("localhost:2181");
        
		
		/*SpoutConfig kafkaConfig = new SpoutConfig(brokerHosts, TOPIC_NAME, "", "storm-demo");
        kafkaConfig.forceStartOffsetTime(-2);
        kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        KafkaSpout spout = new KafkaSpout(kafkaConfig);*/
        
        TridentKafkaConfig tridentKafkaConfig = new TridentKafkaConfig(brokerHosts, TOPIC_NAME);
        
        //Used inside kafka spout for encoding information
        tridentKafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        
        TransactionalTridentKafkaSpout transactionalSpout = new TransactionalTridentKafkaSpout(tridentKafkaConfig);
        
        TridentTopology topology = new TridentTopology(); 
        
        topology.newStream("kafkaspout", transactionalSpout)
               .each(new Fields("str"), new Enrich(), new Fields("client", "memory", "time"));
               //.each(new Fields("client", "memory", "time"), new PrintFunc(), new Fields("deviceMemory"));
        
        Config conf = new Config();

        demo._cluster.submitTopology("trident-kafka-exp", conf, topology.build());


	}

}
