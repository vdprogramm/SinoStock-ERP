package com.sinostock.erp.domain;
import jakarta.persistence.*;
@Entity @Table(name="suppliers")
public class Supplier {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=160) private String name;
 @Column(length=80) private String phone;
 @Column(length=160) private String email;
 @Column(length=255) private String address;
 protected Supplier(){}
 public Supplier(String name,String phone,String email,String address){this.name=name;this.phone=phone;this.email=email;this.address=address;}
 public Long getId(){return id;} public String getName(){return name;} public String getPhone(){return phone;} public String getEmail(){return email;} public String getAddress(){return address;}
}
