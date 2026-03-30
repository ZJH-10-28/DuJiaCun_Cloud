package dujiacun.orderservice.entity.dto;

import lombok.Data;

import java.util.Date;
@Data
public class OrderResponseDto {
    private String userName;
    private String passWord;
    private Date lastLoginDate;

    public OrderResponseDto(String userName, String passWord, Date lastLoginDate) {
        this.userName = userName;
        this.passWord = passWord;
        this.lastLoginDate = lastLoginDate;
    }
}
