package dujiacun.userservice.entity.bo;

import lombok.Data;

import java.util.Date;
@Data
public class UserInfoBo {
    private String userName;
    private String passWord;
    private Date lastLoginDate;

    public UserInfoBo(String userName,String passWord,Date lastLoginDate) {
        this.userName = userName;
        this.passWord = passWord;
        this.lastLoginDate = lastLoginDate;
    }
}
