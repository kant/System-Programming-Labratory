package bgu.spl.mics.example.services;

import java.util.concurrent.TimeUnit;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Utils;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;

import javax.rmi.CORBA.Util;

public class ExampleMessageSenderService extends MicroService {

    private boolean broadcast;

    public ExampleMessageSenderService(String name, String[] args) {
        super(name);

        if (args.length != 1 || !args[0].matches("broadcast|event")) {
            throw new IllegalArgumentException("expecting a single argument: broadcast/event");
        }

        this.broadcast = args[0].equals("broadcast");
    }

    @Override
    protected void initialize() {
        System.out.println("Sender " + getName() + " started");
        long start = System.currentTimeMillis();
        if (broadcast) {
            sendBroadcast(new ExampleBroadcast(getName()));
            System.out.println("Sender " + getName() + " publish an event and terminate");
            terminate();
        } else {
            Utils.Start = System.currentTimeMillis();
            Future<String> futureObject = (Future<String>)sendEvent(new ExampleEvent(getName()));
            if (futureObject != null) {
            	String resolved = futureObject.get(100, TimeUnit.DAYS);
            	if (resolved != null) {
            		System.out.println("Completed processing the event, its result is \"" + resolved + "\" - success");
            		System.out.println("process took: " + (System.currentTimeMillis() - start));
            	}
            	else {
                	System.out.println(getName()+  " Time has elapsed, no services has resolved the event - terminating");
                }
            }
            else {
            	System.out.println("No Micro-Service has registered to handle ExampleEvent events! The event cannot be processed");
            }
        }
    }

}
