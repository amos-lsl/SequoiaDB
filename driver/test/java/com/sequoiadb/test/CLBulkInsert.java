package com.sequoiadb.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sequoiadb.base.CollectionSpace;
import com.sequoiadb.base.DBCollection;
import com.sequoiadb.base.DBCursor;
import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.base.SequoiadbConstants;
import com.sequoiadb.exception.BaseException;


public class CLBulkInsert {
	
	private static Sequoiadb sdb ;
	private static CollectionSpace cs ;
	private static DBCollection cl ;
	private static DBCollection cl1 ;
	private static DBCursor cursor ;
	
	@BeforeClass
	public static void setConnBeforeClass() throws Exception{
		// sdb
		sdb = new Sequoiadb(Constants.COOR_NODE_CONN,"","");
		// cs
		if(sdb.isCollectionSpaceExist(Constants.TEST_CS_NAME_1)){
			sdb.dropCollectionSpace(Constants.TEST_CS_NAME_1);
			cs = sdb.createCollectionSpace(Constants.TEST_CS_NAME_1);
		}
		else
			cs = sdb.createCollectionSpace(Constants.TEST_CS_NAME_1);
		// cl
		BSONObject conf = new BasicBSONObject();
		conf.put("ReplSize", 0);
		cl = cs.createCollection(Constants.TEST_CL_NAME_1, conf);
		System.out.println("OK");
	}
	
	@AfterClass
	public static void DropConnAfterClass() throws Exception {
		sdb.dropCollectionSpace(Constants.TEST_CS_NAME_1);
		sdb.disconnect();
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	    cl.truncate();
	}
	
	@Test
	public void bulkInsert(){
		List<BSONObject>list = ConstantsInsert.createRecordList(100000);
		cl.bulkInsert(list, DBCollection.FLG_INSERT_CONTONDUP);
		cursor = cl.query();
		int i =0 ;
	    while(cursor.hasNext()){
//	    	System.out.println(cursor.getNext().toString());
	    	cursor.getNext();
	    	i++;
	    }
	    assertEquals(100000,i);
	}
	
	@Test
	public void bulkInsert2(){
		int i =0 ;
		BSONObject record=null;
		// build list
		List<BSONObject>list = ConstantsInsert.createRecordList(2);
		
		// case 1:
		// check isOIDEnsured()
		cl.ensureOID(false);
		assertFalse(cl.isOIDEnsured());
		
		// bulk insert
		cl.bulkInsert(list, DBCollection.FLG_INSERT_CONTONDUP);
		cursor = cl.query();
		// check
		i = 0;
	    while(cursor.hasNext()){
	    	cursor.getNext();
	    	i++;
	    }
	    assertEquals(2,i);
	    for ( i = 0; i < list.size(); i++ )
	    {
	    	record=list.get(i);
	    	if ( null != record.get(SequoiadbConstants.OID) )
	    	{
	    		assertTrue("Record should not contain oid add by bulk insert", false);
	    	}
	    }
	    
	    // case 2:
		// build another record
		cl.ensureOID(true);
		
		// check isOIDEnsured()
		assertTrue(cl.isOIDEnsured());
		
		// bulk insert
		cl.bulkInsert(list, DBCollection.FLG_INSERT_CONTONDUP);
		cursor = cl.query();
		// check
		i = 0;
	    while(cursor.hasNext()){
	    	cursor.getNext();
	    	i++;
	    }
	    assertEquals(4,i);
	    for ( i = 0; i < list.size(); i++ )
	    {
	    	record=list.get(i);
	    	if ( null == record.get(SequoiadbConstants.OID) )
	    	{
	    		assertTrue("Record has no oid add by bulk insert", false);
	    	}
	    }
	    
	    // case 3:
		BSONObject obj = new BasicBSONObject() ;
		BSONObject obj1 = new BasicBSONObject() ;
		BSONObject obj2 = new BasicBSONObject() ;
		ObjectId oid=ObjectId.get();
		obj.put(SequoiadbConstants.OID, oid);
		obj.put("a", 1);
		obj1.put(SequoiadbConstants.OID, "2");
		obj1.put("b", 2);
		obj2.put(SequoiadbConstants.OID, 3);
		obj2.put("c", 3);
		list=ConstantsInsert.createRecordList(1);
		list.add(obj);
		list.add(obj1);
		list.add(obj2);
		// bulk insert
		cl.bulkInsert(list, DBCollection.FLG_INSERT_CONTONDUP);
		cursor = cl.query();
		// check
		i = 0;
	    while(cursor.hasNext()){
	    	cursor.getNext();
	    	i++;
	    }
	    assertEquals(8,i);
	    for ( i = 0; i < list.size(); i++ )
	    {
	    	record=list.get(i);
	    	if ( null == record.get(SequoiadbConstants.OID) )
	    	{
	    		assertTrue("Record has no oid add by bulk insert", false);
	    	}
	    	if ( null != record.get("a"))
	    	{
	    		ObjectId id = (ObjectId)record.get(SequoiadbConstants.OID);
	    		if ( false == id.toString().endsWith(oid.toString()) )
	    		{
	    			assertTrue("Record's oid should not be changed by bulk insert", false);
	    		}
	    	}
	    	if ( null != record.get("b"))
	    	{
	    		String id = (String)record.get(SequoiadbConstants.OID);
	    		if ( false == id.toString().equals("2") )
	    		{
	    			assertTrue("Record's oid should not be changed by bulk insert", false);
	    		}
	    	}
	    	if ( null != record.get("c"))
	    	{
	    		int id = (Integer)record.get(SequoiadbConstants.OID);
	    		if (3 != id )
	    		{
	    			assertTrue("Record's oid should not be changed by bulk insert", false);
	    		}
	    	}
	    }
	    assertEquals(4, i);
	    
	    // case 4:
	    try
	    {
	    	cl.bulkInsert(list, DBCollection.FLG_INSERT_CONTONDUP);
	    }
	    catch(BaseException e)
	    {
	    	assertTrue(false);
	    }
	    try
	    {
	    	cl.bulkInsert(list, 0);
	    	// should not go on
	    	assertTrue(false);
	    }
	    catch(BaseException e)
	    {
	    }
	}
	
	@Test
	public void bulkInsert_cl_with_conpress(){
		cl1 = cs.createCollection(Constants.TEST_CL_NAME_2,
		        new BasicBSONObject("Compressed",true).append("ReplSize", 0));
		List<BSONObject>list = ConstantsInsert.createRecordList(100000);
		cl1.bulkInsert(list, DBCollection.FLG_INSERT_CONTONDUP);
		cursor = cl1.query();
		int i =0 ;
	    while(cursor.hasNext()){
//	    	System.out.println(cursor.getNext().toString());
	    	cursor.getNext();
	    	i++;
	    }
	    assertEquals(100000,i);
	}
}
