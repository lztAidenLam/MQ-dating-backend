package com.lam.dating.service;

import com.lam.dating.model.dto.TeamQuery;
import com.lam.dating.model.dto.TeamUpdateRequest;
import com.lam.dating.model.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lam.dating.model.entity.User;
import com.lam.dating.model.vo.TeamUserVO;

import java.util.List;

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
     * @return 新增的队伍id
     */
    Long addTeam(Team team, User loginUser);

    /**
     * 根据传入条件查询所有符合条件的队伍
     * @param teamQuery 传入查询队伍参数
     * @param admin 是否为管理员
     * @return 符合条件的队伍列表
     */
    List<TeamUserVO> selectList(TeamQuery teamQuery, boolean admin);

    /**
     * 更新队伍
     * @param team 传入更新队伍参数
     * @param loginUser 登录用户
     * @return 更新结果
     */
    boolean updateTeam(TeamUpdateRequest team, User loginUser);
}
