package dujiacun.orderservice.entity.dto;

import lombok.Data;

import java.util.Date;
@Data
public class OrderRequestDto {
    private String userName;
    private String passWord;
    private Date lastLoginDate;

    public OrderRequestDto(String userName, String passWord, Date lastLoginDate) {
        this.userName = userName;
        this.passWord = passWord;
        this.lastLoginDate = lastLoginDate;
    }
}
