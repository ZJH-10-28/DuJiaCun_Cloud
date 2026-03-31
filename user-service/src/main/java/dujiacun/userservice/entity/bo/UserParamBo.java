package dujiacun.userservice.entity.bo;

import lombok.Data;

import java.util.Date;
@Data
public class UserParamBo {
    private Long userId;
    private String userName;
    private String passWord;
    private Date lastLoginDate;

    public UserParamBo(){};

    public UserParamBo(Long userId, String userName,String passWord,Date lastLoginDate) {
        this.userId = userId;
        this.userName = userName;
        this.passWord = passWord;
        this.lastLoginDate = lastLoginDate;
    }
}
