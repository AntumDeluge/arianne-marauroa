package marauroa.common.game.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

import org.junit.Test;


public class TestRPObject {
	
	@Test
	public void testRPObject() {
		RPObject obj=new RPObject();
		
		obj.put("a",1);
		obj.put("b","1");
		obj.put("c",2.0);
		obj.put("d","string of text");
		
		obj.addSlot("lhand");
		obj.addSlot("rhand");
		
		obj.addEvent("chat", "Hi there!");
		
		assertTrue(obj.has("a"));
		assertEquals(1, obj.getInt("a"));
		assertTrue(obj.has("b"));
		assertEquals("1", obj.get("b"));
		assertTrue(obj.has("c"));
		assertEquals(2.0, obj.getDouble("c"));
		assertFalse(obj.has("e"));
		
		assertTrue(obj.hasSlot("lhand"));
		assertTrue(obj.hasSlot("rhand"));
		
		for(Iterator<RPEvent> it=obj.eventsIterator(); it.hasNext();) {
			RPEvent event=it.next();
			assertEquals("chat", event.getName());
		}
	}
	
	@Test
	public void testRPSlots() {
		RPObject obj=new RPObject();
		
		obj.put("a",1);
		obj.put("b","1");
		obj.put("c",2.0);
		obj.put("d","string of text");
		
		obj.addSlot("lhand");
		obj.addSlot("rhand");
		
		RPSlot lhand=obj.getSlot("lhand");
		assertNotNull(lhand);
		
		RPObject coin=new RPObject();
		coin.put("euro", 100);
		coin.put("value", 100);

		lhand.add(coin);
		
		assertEquals(coin, obj.getSlot("lhand").getFirst());
	}

	@Test
	public void testRPEvents() {
		RPObject obj=new RPObject();
		
		obj.put("a",1);
		obj.put("b","1");
		obj.put("c",2.0);
		obj.put("d","string of text");
		
		obj.addSlot("lhand");
		obj.addSlot("rhand");
		
		obj.addEvent("test", "work!");
		obj.addEvent("test", "it MUST work!");
		obj.addEvent("test", "OMG! It works!");
		
		RPSlot lhand=obj.getSlot("lhand");
		assertNotNull(lhand);
		
		RPObject coin=new RPObject();
		coin.setID(new RPObject.ID(1,""));
		coin.put("euro", 100);
		coin.put("value", 100);

		lhand.add(coin);

		obj.clearVisible();
		
		System.out.println(obj);
		
		assertTrue(obj.isEmpty());
		}
}