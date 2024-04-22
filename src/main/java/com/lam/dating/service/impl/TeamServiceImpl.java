package com.lam.dating.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lam.dating.model.entity.Team;
import com.lam.dating.model.entity.User;
import com.lam.dating.service.TeamService;
import com.lam.dating.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author AidenLam
* @description 针对表【dt_team(队伍)】的数据库操作Service实现
* @createDate 2024-04-17 23:28:35
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Override
    public Long addTeam(Team team, User loginUser) {
        /*
        校验信息：
        队伍人数 > 1 且 <= 20
        队伍标题 <= 20
        描述 <= 512
        status 是否公开 （int） 不传默认为 0 （公开）
        如果 status 是加密状态，一定要有密码，且密码 <= 32
        超时时间 > 当前时间
        校验用户最多创建 5 个队伍
         */
        return null;
    }
}




