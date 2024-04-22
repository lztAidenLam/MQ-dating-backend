package com.lam.dating.service;

import com.lam.dating.model.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lam.dating.model.entity.User;

/**
* @author AidenLam
* @description 针对表【dt_team(队伍)】的数据库操作Service
* @createDate 2024-04-17 23:28:35
*/

public interface TeamService extends IService<Team> {

    /**
     * 增加队伍
     * @param team 需增加的队伍对象
     * @param loginUser 登录用户
     * @return
     */
    Long addTeam(Team team, User loginUser);
}
