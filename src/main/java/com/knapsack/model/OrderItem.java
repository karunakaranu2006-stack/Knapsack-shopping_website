package com.knapsack.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Integer orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price")
    private Double price;

    @Column(name = "price_at_purchase")
    private Double priceAtPurchase;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        if (price == null && priceAtPurchase != null) {
            price = priceAtPurchase;
        } else if (priceAtPurchase == null && price != null) {
            priceAtPurchase = price;
        }
    }

    public OrderItem() {}

    public OrderItem(Order order, Product product, Integer quantity, Double price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.priceAtPurchase = price;
    }

    public Integer getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Integer orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price != null ? price : priceAtPurchase;
    }

    public void setPrice(Double price) {
        this.price = price;
        this.priceAtPurchase = price;
    }

    public Double getPriceAtPurchase() {
        return priceAtPurchase != null ? priceAtPurchase : price;
    }

    public void setPriceAtPurchase(Double priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
        this.price = priceAtPurchase;
    }

    public Double getSubtotal() {
        Double effective = getPrice();
        return (effective != null ? effective : 0.0) * quantity;
    }
}
