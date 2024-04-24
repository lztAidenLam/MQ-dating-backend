package com.lam.dating.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lam.dating.common.BaseResponse;
import com.lam.dating.common.ErrorCode;
import com.lam.dating.common.ResultUtils;
import com.lam.dating.exception.BusinessException;
import com.lam.dating.model.dto.TeamAddRequest;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author AidenLam
 * @date 2024/4/12
 */
@RestController
@RequestMapping("/team")
@Slf4j
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;


    /**
     * 增加队伍
     *
     * @param teamAddRequest 传入队伍对象
     * @param request        请求
     * @return 新增队伍id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        Long id = teamService.addTeam(team, loginUser);

        return ResultUtils.success(team.getId());
    }




    /**
     * 更新队伍信息
     *
     * @param team    传入队伍对象
     * @param request 请求
     * @return 更新结果
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest team, HttpServletRequest request) {
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean updateResult = teamService.updateTeam(team, loginUser);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 查询队伍
     *
     * @param teamId 查询的队伍id
     * @return 查询到的队伍对象
     */
    @GetMapping("/getOne")
    public BaseResponse<Team> getById(long teamId) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有该队伍");
        }
        return ResultUtils.success(team);
    }

    /**
     * 查询所有符合条件的队伍
     *
     * @param teamQuery 查询条件
     * @param request   请求
     * @return 查询到的队伍集合
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> teamsList(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.selectList(teamQuery, isAdmin);
        return ResultUtils.success(teamList);
    }

    /**
     * 查询所有符合条件的队伍，并分页
     *
     * @param teamQuery 查询条件
     * @param request   请求
     * @return 查询到的队伍集合并分页
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<TeamUserVO>> teamsListPage(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        Page<TeamUserVO> pageVO = teamService.selectListPage(teamQuery, isAdmin);
        return ResultUtils.success(pageVO);
    }

    /**\
     * 用户加入队伍
     *
     * @param teamJoinRequest 要加入的队伍信息
     * @param request         请求
     * @return 加入结果
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 退出队伍
     * @param teamId 队伍 id
     * @param request 请求
     * @return 退出结果
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody long teamId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Boolean result = teamService.quitTeam(teamId, loginUser);
        return ResultUtils.success(result);
    }


    /**
     * 删除队伍
     *
     * @param teamId 队伍id
     * @param request 请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody long teamId, HttpServletRequest request) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Boolean deleteResult = teamService.deleteTeam(teamId, loginUser);
        if (!deleteResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 查询当前用户所有加入的队伍
     * @param request 请求
     * @return 队伍信息vo集合
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(HttpServletRequest request) {

        return ResultUtils.success(teamService.selectMyJoinTeams(request));
    }

    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(HttpServletRequest request) {

        return ResultUtils.success(teamService.selectMyJCreateTeams(request));
    }

}
