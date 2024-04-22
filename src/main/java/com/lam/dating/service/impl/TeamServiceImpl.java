package com.lam.dating.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lam.dating.common.ErrorCode;
import com.lam.dating.common.TeamStatusEnum;
import com.lam.dating.exception.BusinessException;
import com.lam.dating.model.entity.Team;
import com.lam.dating.model.entity.User;
import com.lam.dating.model.entity.UserTeam;
import com.lam.dating.service.TeamService;
import com.lam.dating.mapper.TeamMapper;
import com.lam.dating.service.UserTeamService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

/**
 * @author AidenLam
 * @description 针对表【dt_team(队伍)】的数据库操作Service实现
 * @createDate 2024-04-17 23:28:35
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

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
        // 队伍人数
        Integer maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum <= 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }
        // 队伍标题
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        // 队伍描述
        String description = team.getDescription();
        if (StringUtils.isBlank(description) || description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不满足要求");
        }
        // 队伍状态
        Integer teamStatus = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamStatus);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        // 如果状态为加密，要有密码 ，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        // 超时时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间不小于现在");
        }
        // 校验当前用户最多只能建5个队伍
        Long userId = loginUser.getId();
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Team::getUserId, userId);
        long teamCount = this.count(queryWrapper);
        if (teamCount > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多只能建5个队伍");
        }

        /* 插入队伍信息到队伍表 */
        team.setId(null);
        team.setUserId(userId);
        boolean teamResult = this.save(team);
        Long teamId = team.getId();
        if (!teamResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍插入失败");
        }

        /* 插入用户 - 队伍关系 到关系表 */
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        boolean userTeamResult = userTeamService.save(userTeam);
        if (!userTeamResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入关系表失败");
        }
        return teamId;
    }
}




