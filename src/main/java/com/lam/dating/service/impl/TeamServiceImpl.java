package com.lam.dating.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lam.dating.model.entity.Team;
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

}




