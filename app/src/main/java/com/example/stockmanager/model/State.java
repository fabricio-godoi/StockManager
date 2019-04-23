package com.example.stockmanager.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class State {

    transient private static final String TAG = State.class.getSimpleName();

    private String name;

    private List<Product> products = new ArrayList<>();

    public State(String name){
        this.name = name;
    }


    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }

    public List<Product> getProducts(){ return products; }

    public void addProduct(Product product){
        products.add(product);
    }

    /**
     * Return the amount of product value
     * @return
     */
    public Double getProductsValue(){
        Double total = 0.0;
        if(products != null && products.size() > 0){
            for(Product p : products){
                try {
                    total += p.getValue();
                } catch (Exception e){
                    Log.e(TAG, "Product with missing value", e);
                }
            }
        }
        return total;
    }

}
