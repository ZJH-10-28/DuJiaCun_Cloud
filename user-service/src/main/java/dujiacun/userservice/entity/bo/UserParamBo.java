package dujiacun.userservice.entity.bo;

import lombok.Data;

import java.util.Date;
@Data
public class UserParamBo {
    private Long userId;
    private String userName;
    private String passWord;
    private Integer isAdmin;
    private Date lastLoginDate;

}
