package dujiacun.userservice.entity.bo;

import lombok.Data;

import java.util.Date;
@Data
public class UserInfoBo {
    private Long userId;
    private String userName;
    private String passWord;
    private Date lastLoginDate;

    public UserInfoBo(){};

    public UserInfoBo(Long userId, String userName,String passWord,Date lastLoginDate) {
        this.userId = userId;
        this.userName = userName;
        this.passWord = passWord;
        this.lastLoginDate = lastLoginDate;
    }
}
