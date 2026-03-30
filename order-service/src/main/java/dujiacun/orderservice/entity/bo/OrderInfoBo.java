package dujiacun.orderservice.entity.bo;

import lombok.Data;

import java.util.Date;
@Data
public class OrderInfoBo {
    private String userName;
    private String passWord;
    private Date lastLoginDate;

    public OrderInfoBo(String userName, String passWord, Date lastLoginDate) {
        this.userName = userName;
        this.passWord = passWord;
        this.lastLoginDate = lastLoginDate;
    }
}
