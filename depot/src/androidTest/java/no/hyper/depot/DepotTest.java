package no.hyper.depot;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;
import android.test.InstrumentationTestCase;
import android.util.Log;


public class DepotTest extends InstrumentationTestCase{

    private Context context;

    private static final String TEST_STRING = "Such String";
    private static final String TEST_STRING_NL = "Such String\nOn two lines\n";
    private static final String STRING_FILENAME = "teststring.txt";

    @Override
    protected void setUp() throws Exception {
        context = getInstrumentation().getContext();
    }

    @Override
    protected void tearDown() throws Exception {

        super.tearDown();
    }

    public void testSaveAndRetrieveStringAsObject() {

        Object stringObject = new String(TEST_STRING);

        Depot.with(context).store(STRING_FILENAME, stringObject);

        assertTrue(Depot.with(context).contains(STRING_FILENAME));
        String result = (String) Depot.with(context).getObject(STRING_FILENAME);
        assertEquals(TEST_STRING, result);

    }

    public void testSaveAndRetrievePlainString() {
        Depot.with(context).store(STRING_FILENAME, TEST_STRING);
        assertTrue(Depot.with(context).contains(STRING_FILENAME));
        String result = Depot.with(context).getString(STRING_FILENAME);
        assertEquals(TEST_STRING.trim(), result.trim()); //The String reading implementation always adds \n
    }

    public void testSaveAndRetrieveStringWithNewline() {
        Depot.with(context).store(STRING_FILENAME, TEST_STRING_NL);

        assertTrue(Depot.with(context).contains(STRING_FILENAME));
        String result = Depot.with(context).getString(STRING_FILENAME);
        assertEquals(TEST_STRING_NL.trim(), result.trim());

        //Also test the general object saving method
        Object stringObject = new String(TEST_STRING_NL);
        Depot.with(context).store(STRING_FILENAME, stringObject);
        assertTrue(Depot.with(context).contains(STRING_FILENAME));
        result = (String) Depot.with(context).getObject(STRING_FILENAME);
        assertEquals(TEST_STRING_NL, result);
    }


    public void testSaveAndRetrievePrimitives() {
        boolean bool = true;
        int i = Integer.MAX_VALUE - 302;
        long l = Long.MAX_VALUE - 302;
        float f = Float.MAX_VALUE - 2352;

        Depot depot = Depot.with(context);

        depot.store("TestBoolean", bool);
        depot.store("TestInt", i);
        depot.store("TestLong", l);
        depot.store("TestFloat", f);

        assertTrue(depot.contains("TestBoolean"));
        assertTrue(depot.contains("TestInt"));
        assertTrue(depot.contains("TestLong"));
        assertTrue(depot.contains("TestFloat"));

        assertEquals(bool, depot.getBoolean("TestBoolean"));
        assertEquals(i, depot.getInt("TestInt"));
        assertEquals(l, depot.getLong("TestLong"));
        assertEquals(f, depot.getFloat("TestFloat"));
    }


    public void testPurge() {
        Depot.with(context).store("purgedString", "I am an ephemeral String");
        Depot.with(context).store("purgedInt", 42);

        assertTrue(Depot.with(context).contains("purgedString"));
        assertTrue(Depot.with(context).contains("purgedInt"));

        Depot.with(context).purge();

        assertFalse(Depot.with(context).contains("purgedString"));
        assertFalse(Depot.with(context).contains("purgedInt"));

    }


    public void testDelete() {
        Depot.with(context).store("deleteString", "I am Jack's dead file");
        assertTrue(Depot.with(context).contains("deleteString"));
        Depot.with(context).delete("deleteString");
        assertFalse(Depot.with(context).contains("deleteString"));

        Depot.with(context).store("deleteInt", 42);
        assertTrue(Depot.with(context).contains("deleteInt"));
        Depot.with(context).delete("deleteInt");
        assertFalse(Depot.with(context).contains("deleteInt"));
    }

}
