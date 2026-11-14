package com.knapsack.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "name")
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        if (categoryName == null && name != null) {
            categoryName = name;
        } else if (name == null && categoryName != null) {
            name = categoryName;
        }
    }

    public Category() {}

    public Category(String categoryName, String description, String icon, String imageUrl) {
        this.categoryName = categoryName;
        this.name = categoryName;
        this.description = description;
        this.icon = icon;
        this.imageUrl = imageUrl;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName != null ? categoryName : name;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
        this.name = categoryName;
    }

    public String getName() {
        return name != null ? name : categoryName;
    }

    public void setName(String name) {
        this.name = name;
        this.categoryName = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
