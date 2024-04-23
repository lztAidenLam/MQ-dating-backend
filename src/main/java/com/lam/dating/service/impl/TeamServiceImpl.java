package com.lam.dating.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lam.dating.common.ErrorCode;
import com.lam.dating.common.TeamStatusEnum;
import com.lam.dating.exception.BusinessException;
import com.lam.dating.mapper.TeamMapper;
import com.lam.dating.model.dto.TeamJoinRequest;
import com.lam.dating.model.dto.TeamQuery;
import com.lam.dating.model.dto.TeamUpdateRequest;
import com.lam.dating.model.entity.Team;
import com.lam.dating.model.entity.User;
import com.lam.dating.model.entity.UserTeam;
import com.lam.dating.model.vo.TeamUserVO;
import com.lam.dating.model.vo.UserVO;
import com.lam.dating.service.TeamService;
import com.lam.dating.service.UserService;
import com.lam.dating.service.UserTeamService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Resource
    private UserService userService;

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

    private  LambdaQueryWrapper<Team> getQueryWrapper(TeamQuery teamQuery, boolean isAdmin) {
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        if (teamQuery != null) {
            Long teamId = teamQuery.getId();
            if (teamId != null && teamId > 0) {
                queryWrapper.eq(Team::getId, teamId);
            }
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isEmpty(idList)) {
                queryWrapper.in(Team::getId, idList);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like(Team::getName, searchText).or().like(Team::getDescription, searchText));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like(Team::getName, name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like(Team::getDescription, description);
            }
            // 查询最大人数相等的
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq(Team::getMaxNum, maxNum);
            }
            Long userId = teamQuery.getUserId();
            // 根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq(Team::getUserId, userId);
            }
            // 根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq(Team::getStatus, statusEnum.getValue());
        }
        // 不显示过期队伍
        queryWrapper.and(qw -> qw.gt(Team::getExpireTime, new Date()).or().isNull(Team::getExpireTime));
        return queryWrapper;
    }

    @Override
    public List<TeamUserVO> selectList(TeamQuery teamQuery, boolean isAdmin) {
        LambdaQueryWrapper<Team> queryWrapper = getQueryWrapper(teamQuery, isAdmin);
        List<Team> teamList = this.list(queryWrapper);

        // 查询不到数据，返回空集合
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }

        ArrayList<TeamUserVO> teamUserList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserList.add(teamUserVO);
        }
        return teamUserList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest team, User loginUser) {
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long teamId = team.getId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id错误");
        }
        Team oldTeam = getTeamById(teamId);
        TeamUpdateRequest oldTeamUpdateRequest = new TeamUpdateRequest();
        BeanUtils.copyProperties(oldTeam, oldTeamUpdateRequest);
        // 如果传入参数和原有队伍所有对应属性相同，直接返回true
        if (team.equals(oldTeamUpdateRequest)) {
            return true;
        }
        if (!oldTeam.getUserId().equals(loginUser.getId()) || !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(team.getStatus());
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(team, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {

        Long teamId = teamJoinRequest.getTeamId();
        Long userId = loginUser.getId();
        // 用户加入队伍数量
        LambdaQueryWrapper<UserTeam> userTeamQueryWrapper = new LambdaQueryWrapper<UserTeam>();
        userTeamQueryWrapper.eq(UserTeam::getUserId, userId);
        long count = userTeamService.count(userTeamQueryWrapper);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多只能加5个队伍");
        }

        // 队伍是否存在
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无此队伍");
        }
        // 不可加入已加入的队伍
        userTeamQueryWrapper.eq(UserTeam::getTeamId, teamId);
        UserTeam userTeam = userTeamService.getOne(userTeamQueryWrapper);
        if (userTeam != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已加入此队伍");
        }
        // 队伍已满人
        userTeamQueryWrapper = new LambdaQueryWrapper<>();
        userTeamQueryWrapper.eq(UserTeam::getTeamId, teamId);
        long teamUsersCount = userTeamService.count(userTeamQueryWrapper);
        if (teamUsersCount >= 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满人");
        }
        // 不可加入已过期队伍
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已过期");
        }
        // 不可加入私有队伍
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(team.getStatus());
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不可加入此队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }

        // 新增 用户-队伍 关联信息
        UserTeam joinUserTeam = new UserTeam();
        joinUserTeam.setTeamId(teamId);
        joinUserTeam.setUserId(userId);
        joinUserTeam.setJoinTime(new Date());
        boolean result = userTeamService.save(joinUserTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入队伍失败");
        }
        return true;
    }

    @Override
    public Page<TeamUserVO> selectListPage(TeamQuery teamQuery, boolean isAdmin) {
        LambdaQueryWrapper<Team> queryWrapper = getQueryWrapper(teamQuery, isAdmin);
        Page<Team> page = new Page<>(teamQuery.getPagNum(), teamQuery.getPageSize());
        // 查询并分页
        Page<Team> teamPage = this.page(page, queryWrapper);
        // 提取分页数据并转换成vo封装
        List<TeamUserVO> teamUserVOList = teamPage.getRecords().stream()
                .map(team -> BeanUtil.toBean(team, TeamUserVO.class))
                .collect(Collectors.toList());
        Page<TeamUserVO> pageVO = new Page<>(teamQuery.getPagNum(), teamQuery.getPageSize());
        pageVO.setRecords(teamUserVOList);
        return pageVO;
    }

    @Override
    public Boolean quitTeam(long teamId, User loginUser) {
        Long userId = loginUser.getId();
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team team = getTeamById(teamId);
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getUserId, userId);
        queryWrapper.eq(UserTeam::getTeamId, teamId);
        UserTeam userTeam = userTeamService.getOne(queryWrapper);
        if (userTeam == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "此用户不在当前队伍中");
        }
        /*
        如果队伍只剩一个人 -> 解散该队伍
        如果队伍还有其他人
            -- 如果是队伍管理员 -> 将队伍管理员转让给下一个队伍成员（按进队先后顺序排）
            -- 如果是普通成员 -> 直接退出
         */

        long count = this.countTeamUserByTeamId(teamId);
        // 解散队伍
        if (count == 1) {
            userTeamService.remove(queryWrapper);
            this.removeById(teamId);
            return true;
        }
        if (team.getUserId().equals(userId)) {
            // 是管理员，转让管理员权限到下一位队伍成员
            LambdaQueryWrapper<UserTeam> userTeamQueryWrapper = new LambdaQueryWrapper<>();
            userTeamQueryWrapper.eq(UserTeam::getTeamId, teamId);
            userTeamQueryWrapper.last("order by id asc limit 2");
            List<UserTeam> teamList = userTeamService.list(userTeamQueryWrapper);
            if (CollectionUtils.isEmpty(teamList) || teamList.size() <= 1) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            UserTeam nextUserTeam = teamList.get(1);
            Long nextTeamLeaderId = nextUserTeam.getUserId();
            // 更新当前队伍管理员
            Team updateTeam = new Team();
            updateTeam.setUserId(nextTeamLeaderId);
            updateTeam.setId(teamId);
            boolean result = this.updateById(updateTeam);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍管理员失败");
            }
        }

        // 退出队伍
        return userTeamService.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTeam(long teamId, User loginUser) {
        // 校验队伍是否存在
        Team team = getTeamById(teamId);
        // 校验你是不是队伍的队长
        if (team.getUserId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }
        // 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }

    @NotNull
    private Team getTeamById(long teamId) {
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        return team;
    }

    /**
     * 获取某队伍当前人数
     *
     * @param teamId 队伍id
     * @return 队伍人数
     */
    private long countTeamUserByTeamId(long teamId) {
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId, teamId);
        return userTeamService.count(queryWrapper);
    }
}




