package com.example.myapplication;

public class Product {
    private String name;
    private double price;
    private int imageResource; // You can also use a URL if images are fetched from the web

    public Product(String name, double price, int imageResource) {
        this.name = name;
        this.price = price;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getImageResource() {
        return imageResource;
    }
}

