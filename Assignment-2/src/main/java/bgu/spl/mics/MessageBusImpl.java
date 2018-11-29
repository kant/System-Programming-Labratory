package bgu.spl.mics;

import bgu.spl.mics.application.messages.RequestVehicleEvent;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	/**
	 * singleton
	 */
	private static class SingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}
	private MessageBusImpl() {
		// initialization code..
	}
	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * private variables
	 */

	private static final Map<MicroService, MessageQueue> servicesMessageQueue = new ConcurrentHashMap<>();
	private static final Map<Class<? extends Message>, CopyOnWriteArrayList<MicroService>> messageSubscribes = new ConcurrentHashMap<>();
    private static final Map<Event<?>, Object> eventResults = new ConcurrentHashMap<>();

	/**
	 *
	 * @param type The type to subscribe to,
	 * @param m    The subscribing micro-service.
	 * @param <T>
	 */
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if (!messageSubscribes.containsKey(type)){
			messageSubscribes.put(type,new CopyOnWriteArrayList<>());
		}
		messageSubscribes.get(type).add(m);
	}

	/**
	 *
	 * @param type 	The type to subscribe to.
	 * @param m    	The subscribing micro-service.
	 */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if (!messageSubscribes.containsKey(type)){
			messageSubscribes.put(type,new CopyOnWriteArrayList<>());
		}
		messageSubscribes.get(type).add(m);
	}

	/**
	 *
	 * @param e      The completed event.
	 * @param result The resolved result of the completed event.
	 * @param <T>
	 */
	@Override
	public <T> void complete(Event<T> e, T result) {
		if (e == null){
			System.out.println("forcing to terminate all MessageQueue");
			servicesMessageQueue.values().stream()
					.forEach(m -> m.onTerminated());
			return;
		}

		MicroService ms = RoundRobin.getMicroService(e);
		if (ms != null){
			if (!((Future<T>) eventResults.get(e)).isDone())
				((Future<T>) eventResults.get(e)).resolve(result);
			servicesMessageQueue.get(ms).onCompleted();
		}
	}

	/**
	 *
	 * @param b 	The message to added to the queues.
	 */
	@Override
	public void sendBroadcast(Broadcast b) {
		Objects.requireNonNull(b, "brodcast");
		messageSubscribes.keySet().stream()
				.filter(type -> type.isAssignableFrom(b.getClass()))
				.flatMap(type -> messageSubscribes.get(type).stream())
				.forEach(sub -> servicesMessageQueue.get(sub).addToQueue(b));
	}

	/**
	 *
	 * @param e     	The event to add to the queue.
	 * @param <T>
	 * @return
	 */
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Objects.requireNonNull(e, "events");
		Future<T> future = new Future<>();
		eventResults.put(e,future);
		if (!messageSubscribes.containsKey(e.getClass()))
			return null;
		int size = messageSubscribes.get(e.getClass()).size();
		if (size == 0) return null;
		MicroService ms = messageSubscribes.get(e.getClass()).get(RoundRobin.getIndex(e,size).get());
		if (ms == null) return null;
		servicesMessageQueue.get(ms).addToQueue(e);
		return future;
	}

	/**
	 *
	 * @param m the micro-service to create a queue for.
	 */
	@Override
	public void register(MicroService m) {
		Objects.requireNonNull(m,"Micro service");
		if (!servicesMessageQueue.containsKey(m))
			servicesMessageQueue.put(m,new MessageQueue());
	}

	/**
	 *
	 * @param m the micro-service to unregister.
	 */
	@Override
	public void unregister(MicroService m) {
		Objects.requireNonNull(m,"Micro service");
		messageSubscribes.values().stream()
				.filter(f -> f.contains(m))
				.forEach(s -> s.remove(m));
		servicesMessageQueue.remove(m);
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		//if (servicesMessageQueue.containsKey(m)){
			return servicesMessageQueue.get(m).pollMessage();
		//}
		//return null;
	}

	private class MessageQueue {

		private class Data{
			final Queue<Message> queue = new ConcurrentLinkedQueue<>();
			final AtomicBoolean isCompleted = new AtomicBoolean(true);
			final AtomicBoolean terminated = new AtomicBoolean(false);
		}

		private final Data data;

		MessageQueue() {
			data = new Data();
		}

		MessageQueue(Data data) {
			this.data = data;
		}

		synchronized void addToQueue(Message message){
			data.queue.add(message);
			this.notifyAll();
		}

		synchronized Message pollMessage(){
			while (!data.terminated.get() && data.queue.isEmpty() || !data.isCompleted.get()) {
				try {
					this.wait();
				} catch (InterruptedException e) {

                }
			}
			if (data.queue.isEmpty() || data.terminated.get())
				return null;
			Message message= data.queue.poll();
			if (!(message instanceof Broadcast))
				data.isCompleted.compareAndSet(true,false);
			this.notifyAll(); //notify service for receiving Message
			return message;
		}

		synchronized void onCompleted(){
			data.isCompleted.compareAndSet(false,true);
			this.notifyAll();
		}

		synchronized void onTerminated(){
			data.terminated.compareAndSet(false,true);
			this.notifyAll();
		}
	}

	private static class RoundRobin {

		private static final Map<Class<? extends Message> , AtomicInteger> eventIdxMap = new ConcurrentHashMap<>();

		static MicroService getMicroService(Event<?> event){
			if (eventIdxMap.get(event.getClass()) == null)
				return null;
			return messageSubscribes.get(event.getClass()).//return linkedList of micro-services
					get(eventIdxMap.get(event.getClass()).get()); //return the relevant services
		}

		static AtomicInteger getIndex(Event<?> e, int size) {
			if (!eventIdxMap.containsKey(e.getClass())) {
				eventIdxMap.put(e.getClass(), new AtomicInteger(0));
				return new AtomicInteger(0);
			}
			eventIdxMap.get(e.getClass()).getAndIncrement();
			return new AtomicInteger(eventIdxMap.get(e.getClass()).get() % size);
		}
	}
}
