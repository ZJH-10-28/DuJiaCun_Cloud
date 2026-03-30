package dujiacun.orderservice.entity;

import lombok.Data;

import java.util.Date;

@Data
public class OrderEntity {
    private String userName;
    private String passWord;
    private Date lastLoginDate;

    public OrderEntity(String userName, String passWord, Date lastLoginDate) {
        this.userName = userName;
        this.passWord = passWord;
        this.lastLoginDate = lastLoginDate;
    }
}
