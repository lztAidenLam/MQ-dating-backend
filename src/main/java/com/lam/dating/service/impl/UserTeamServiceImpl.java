package com.lam.dating.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lam.dating.model.entity.UserTeam;
import com.lam.dating.service.UserTeamService;
import com.lam.dating.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author AidenLam
* @description 针对表【dt_user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-04-17 23:19:08
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




