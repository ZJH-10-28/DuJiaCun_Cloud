package dujiacun.orderservice.entity.bo;

import lombok.Data;

import java.util.Date;
@Data
public class OrderParamBo {
    private String userName;
    private String passWord;
    private Date lastLoginDate;

    public OrderParamBo(String userName, String passWord, Date lastLoginDate) {
        this.userName = userName;
        this.passWord = passWord;
        this.lastLoginDate = lastLoginDate;
    }
}
