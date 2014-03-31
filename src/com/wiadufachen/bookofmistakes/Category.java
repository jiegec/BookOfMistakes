package com.wiadufachen.bookofmistakes;

/**
 * Created by win7 on 2014-03-27.
 */
public class Category {
    private Integer id;
    private String name;

    Category(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
