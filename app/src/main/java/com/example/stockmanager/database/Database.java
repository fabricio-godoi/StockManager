package com.example.stockmanager.database;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.stockmanager.model.Product;
import com.example.stockmanager.model.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * This class should not be instantiated, it must be called always by getInstance()
 */
public class Database {

    private static final String TAG = Database.class.getSimpleName();

    private static Database INSTANCE;
    private static Context CONTEXT;



    private Map<String, State> states = new HashMap<>();
    private Map<String, Product> products = new HashMap<>();

    private Map<String, String> get;

    /**
     * Get the default instance of the Database that are loaded by the application
     * @return
     */
    public static Database getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = new Database();
            CONTEXT = context;

        }
        return INSTANCE;
    }

    /**
     * Save all information from ram in the flash in a csv format
     */
    public void save(){
        Collection<State> sv = states.values();
        String db = "STATE_NAME, RRODUCT_NAME, PRODUCT_VALUE\n";
        for(State state : sv){
            for(Product product : state.getProducts()){
                db += state.getName()+","+product.getName()+","+product.getValue()+"\n";
            }
        }

        SharedPreferences settings = CONTEXT.getSharedPreferences("sotckdb", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("db");
        editor.putString("db", db);
        editor.commit();

    }

    /**
     * Get data from the flash
     */
    public void load(){
        states = new HashMap<>();
        products = new HashMap<>();

        SharedPreferences settings = CONTEXT.getSharedPreferences("sotckdb", MODE_PRIVATE);
        String db = settings.getString("db", "");
        String[] dbLines = db.split("\n");
        // Check if has data
        int id = 1;
        if(dbLines == null || dbLines.equals("")) return;
        for(String line : dbLines){
            if(line == null || line.equals("")) continue;
            try {
                String[] values = line.split(",");
                String stateName = values[0];
                String productName = values[1];
                Double productValue = Double.parseDouble(values[2]);

                State state;
                Product product;
                if (!states.containsKey(stateName)) {
                    state = new State(stateName);
                    states.put(stateName, state);
                } else {
                    state = states.get(stateName);
                }
                product = new Product(productName, productValue);
                products.put(String.valueOf(id++), product);

                state.addProduct(product);
                product.addState(state);
            } catch (Exception e) {
                Log.e(TAG, "Line malformed", e);
            }

        }
    }

    /**
     * Create a new product in the database
     * @param name
     * @param value
     * @param stateName
     * @return null if the product already exists
     */
    public Product createProduct(String name, Double value, String stateName){
        try {
            // Format names and variables
            name = name.trim().toLowerCase();
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            stateName = stateName.trim().toLowerCase();
            stateName = stateName.substring(0, 1).toUpperCase() + stateName.substring(1);


            // Add a new product with a new id
            String lastID = "0";
            if(products.keySet().size() > 0){
                lastID = (String)products.keySet().toArray()[products.keySet().size()-1];
            }
            Product product = new Product(name, value);
            products.put(String.valueOf(Integer.parseInt(lastID)+1), product);

            // Check if the state is already registered
            State dbState = states.get(stateName);
            if (dbState == null) {
                dbState = new State(stateName);
                states.put(stateName, dbState);
            }

            dbState.addProduct(product);
            product.addState(dbState);

            save();
            return product;
        } catch (Exception e) {
            Log.e(TAG, "Could not create product", e);
            return null;
        }
    }


    public List<State> getStateList() {
        List<State> list = new ArrayList<>();
        list.addAll(states.values());
        return list;
    }

    public List<Product> getProductList() {
        List<Product> list = new ArrayList<>();
        list.addAll(products.values());
        return list;
    }


    /**
     * Wipe all information in the database
     */
    public void wipe(){
        products.clear();
        states.clear();
        SharedPreferences settings = CONTEXT.getSharedPreferences("sotckdb", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("db");
        editor.commit();

    }

}
