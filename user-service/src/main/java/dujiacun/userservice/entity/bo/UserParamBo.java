package dujiacun.userservice.entity.bo;

import lombok.Data;

import java.util.Date;
@Data
public class UserParamBo {
    private String userName;
    private String passWord;
    private Date lastLoginDate;

    public UserParamBo(String userName,String passWord,Date lastLoginDate) {
        this.userName = userName;
        this.passWord = passWord;
        this.lastLoginDate = lastLoginDate;
    }
}
