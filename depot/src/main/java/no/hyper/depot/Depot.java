package no.hyper.depot;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * Created by espenalmdahl on 18/06/15.
 * Depot is a convenience class for storing Strings, Serializables and primitives on disk in
 * a key/value format.
 *
 * It uses SharedPreferences for primitives, while Strings and serialized objects are stored
 * to files.
 */
public class Depot {

    private static Depot singleton;
    private Context context;
    private static final String TAG = "==> DEPOT <==";


    private Depot(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized Depot with(Context context) {
        if (singleton == null) {
            singleton = new Depot(context);
        }
        return singleton;
    }



    /**
     *
     * Store primitives in SharedPreferences. Feels like overkill to create a separate file for each.
     *
     */
    public void store(String name, boolean flag) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
        prefs.putBoolean(name, flag);
        prefs.commit();
    }

    public void store(String name, int i) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
        prefs.putInt(name, i);
        prefs.commit();
    }

    public void store(String name, float f) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
        prefs.putFloat(name, f);
        prefs.commit();
    }

    public void store(String name, long l) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
        prefs.putLong(name, l);
        prefs.commit();
    }


    /**
     * Store a binary object. Remember to implement the Serializable interface in all
     * referenced objects
     *
     * @param name
     * @param serializableObject
     */
    public void store(String name, Object serializableObject) {
        if ( serializableObject instanceof Serializable) {

            try {
                String tmpName = name + ".tmp";
                String tmpPath = context.getFilesDir() + "/" + tmpName;
                BufferedOutputStream bos = new BufferedOutputStream(context.openFileOutput(tmpName, Context.MODE_PRIVATE));
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(serializableObject);
                oos.close();
                File from = new File(tmpPath);
                File to = new File(context.getFilesDir() + "/" + name);
                from.renameTo(to);
                Log.i(TAG, serializableObject.getClass().getSimpleName() + " object stored to file: " + context.getFilesDir() + "/" + name);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e(TAG, "Object " + serializableObject.getClass().getSimpleName() + " does not implement Serializable.");
        }
    }


    /**
     * Store a string and keep it readable for humans
     * @param name
     * @param content String to store
     */
    public void store(String name, String content) {
        try {
            String tmpName = name + ".tmp";
            String tmpPath = context.getFilesDir() + "/" + tmpName;
            File f = new File(tmpPath);
            FileWriter writer = new FileWriter(f);
            writer.write(content);
            writer.close();
            f.renameTo(new File(context.getFilesDir() + "/" + name));
            Log.i(TAG, "String saved to file: " + context.getFilesDir() + "/" + name);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getString(String name) {
        try {
            File f = new File(context.getFilesDir(), name);
            FileReader reader = new FileReader(f);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = "";
            StringBuilder builder = new StringBuilder();
            while ( (line = bufferedReader.readLine()) != null ) {
                builder.append(line);
                builder.append("\n");
            }
            bufferedReader.close();
            return builder.toString();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Object getObject(String name) {

        if (this.contains(name)) {
            try {
                InputStream inputStream = context.openFileInput(name);
                if ( inputStream != null ) {

                    ObjectInputStream ois = new ObjectInputStream(inputStream);
                    Object object = ois.readObject();

                    ois.close();
                    return object;
                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e(TAG, "File " + name + " does not exist");
        }
        return null;
    }


    public int getInt(String name) {
        SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        return prefs.getInt(name, Integer.MIN_VALUE);
    }

    public long getLong(String name) {
        SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        return prefs.getLong(name, Long.MIN_VALUE);
    }

    public boolean getBoolean(String name) {
        SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        return prefs.getBoolean(name, false);
    }

    public float getFloat(String name) {
        SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        return prefs.getFloat(name, Float.MIN_VALUE);
    }


    public boolean contains(String name) {
        SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        if ( prefs.contains(name) ) return true;
        else {
            File file = new File(context.getFilesDir() + "/" + name);
            return file.exists();
        }
    }


    public void purge() {
        SharedPreferences.Editor prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
        prefs.clear();
        prefs.commit();
        File dir = context.getFilesDir();
        String[] files = dir.list();
        for (String file : files) {
            new File(dir, file).delete();
        }
    }


    public void delete(String name) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
        prefs.remove(name);
        prefs.commit();
        File dir = context.getFilesDir();
        String[] files = dir.list();
        for (String file : files) {
            if ( file.equals(name)) {
                new File(dir, name).delete();
                break;
            }
        }
    }
}
