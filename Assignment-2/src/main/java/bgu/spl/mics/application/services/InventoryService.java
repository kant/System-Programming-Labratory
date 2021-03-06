package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CheckAvailability;
import bgu.spl.mics.application.messages.TakingBookEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import bgu.spl.mics.application.passiveObjects.OrderResult;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

import java.util.concurrent.CountDownLatch;

/**
 * InventoryService is in charge of the book inventory and stock.
 * Holds a reference to the {@link Inventory} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class InventoryService extends MicroService{

	private Inventory inv;
	private CountDownLatch countDownLatch;

	public InventoryService(String name,CountDownLatch countDownLatch) {
		super(name);
		inv = Inventory.getInstance();
		this.countDownLatch = countDownLatch;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, br->{
			//System.out.println("terminating: " + getName());
			terminate();
			//Thread.currentThread().interrupt();
		});
		subscribeEvent(CheckAvailability.class, ev->{
//			try {
//				Thread.sleep(5);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			complete(ev, inv.checkAvailabiltyAndGetPrice(ev.getBookTitle())); //if not exist return -1

		});
		subscribeEvent(TakingBookEvent.class, ev->{
			//System.out.println(getName()+": receiving TakingBookEvent from " + ev.getSenderName());
			if (inv.checkAvailabiltyAndGetPrice(ev.getBookTitle()) == -1 || inv.take(ev.getBookTitle()) == OrderResult.NOT_IN_STOCK)
				complete(ev, false);
			complete(ev,true);
		});
		countDownLatch.countDown();
	}

}
